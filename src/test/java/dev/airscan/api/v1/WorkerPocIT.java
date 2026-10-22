package dev.airscan.api.v1;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.airscan.application.worker.WorkerProtocolStore;
import dev.airscan.worker.WorkerPocClient;
import dev.airscan.worker.outbox.OutboxEnvelope;
import dev.airscan.worker.outbox.SqliteOutbox;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Real HTTP -> application -> Firestore emulator and worker SQLite restart. */
@QuarkusTest
class WorkerPocIT {
  @Inject PocRuntime runtime;
  @TempDir Path temp;

  private static String bearer(String id) {
    String fixture = System.getenv("AIRSCAN_POC_WORKERS_FILE");
    assertNotNull(fixture, "POC identity fixture path must be supplied");
    String config;
    try {
      config = Files.readString(Path.of(fixture)).trim();
    } catch (IOException failure) {
      throw new IllegalStateException("POC token fixture missing", failure);
    }
    for (String candidate : config.split(";")) {
      String[] pair = candidate.split("=", 2);
      if (pair[0].equals(id)) {
        return "Bearer " + pair[1];
      }
    }
    throw new IllegalStateException("POC identity fixture missing");
  }

  private static WorkerProtocolStore.Lease lease(String job, String worker) {
    return given()
        .contentType(ContentType.JSON)
        .header("Authorization", bearer(worker))
        .body(new WorkerProtocolResource.LeaseRequest(job))
        .post("/api/v1/worker/leases")
        .then()
        .statusCode(200)
        .extract()
        .as(WorkerProtocolStore.Lease.class);
  }

  private static WorkerProtocolStore.Accepted submit(
      String job, String lease, String batch, String worker, String payload) {
    return given()
        .contentType(ContentType.JSON)
        .header("Authorization", bearer(worker))
        .body(new WorkerProtocolResource.ResultRequest(job, lease, batch, payload))
        .post("/api/v1/worker/results")
        .then()
        .statusCode(200)
        .extract()
        .as(WorkerProtocolStore.Accepted.class);
  }

  @Test
  void lostAcknowledgementReopenRetryAndQueryProvesOneLogicalAcceptance() throws Exception {
    String job = runtime.service().seedSyntheticJob("poc-lost-ack");
    var client =
        new WorkerPocClient(
            URI.create("http://localhost:" + io.restassured.RestAssured.port),
            bearer("worker-a").substring(7));
    var lease = client.lease(job);
    String leaseId = lease.path("leaseId").asText();
    assertEquals("worker-a", lease.path("workerId").asText());
    Path database = temp.resolve("outbox.sqlite");
    String batch = UUID.randomUUID().toString();
    String payload = "{\"kind\":\"synthetic\",\"value\":7}";
    var envelope = new OutboxEnvelope(batch, payload, Instant.parse("2026-10-23T00:00:00Z"));
    try (var workerProcessA = new SqliteOutbox(database)) {
      workerProcessA.enqueue(envelope);
      assertEquals(envelope, workerProcessA.pending().getFirst());
    }

    var first = client.submit(job, leaseId, envelope);
    // Deliberately discard the response. No outbox acceptance acknowledgement.
    try (var workerProcessB = new SqliteOutbox(database)) {
      assertEquals(envelope, workerProcessB.pending().getFirst());
      var restartedClient =
          new WorkerPocClient(
              URI.create("http://localhost:" + io.restassured.RestAssured.port),
              bearer("worker-a").substring(7));
      var repeated = restartedClient.submit(job, leaseId, workerProcessB.pending().getFirst());
      assertEquals(first, repeated);
      var queried = restartedClient.queryAccepted(job);
      assertEquals(first, queried);
      assertEquals(job, queried.path("jobId").asText());
      assertEquals(batch, queried.path("batchId").asText());
      assertEquals(64, queried.path("archive").path("sha256").asText().length());
      assertEquals("gzip", queried.path("archive").path("compression").asText());
      assertEquals("synthetic-poc", queried.path("archive").path("sourceId").asText());
      assertEquals(batch, queried.path("archive").path("batchId").asText());
      Path object =
          Path.of(System.getenv("AIRSCAN_POC_ARCHIVE_DIR"))
              .resolve(queried.path("archive").path("objectKey").asText());
      assertTrue(Files.isRegularFile(object));
      workerProcessB.markAccepted(batch);
    }
    try (var workerProcessC = new SqliteOutbox(database)) {
      assertTrue(workerProcessC.pending().isEmpty());
    }
  }

  @Test
  void authenticationWrongPrincipalHeartbeatAndClassifiedFailure() {
    String job = runtime.service().seedSyntheticJob("poc-auth");
    var request = new WorkerProtocolResource.LeaseRequest(job);
    given()
        .contentType(ContentType.JSON)
        .body(request)
        .post("/api/v1/worker/leases")
        .then()
        .statusCode(401);
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer incorrect")
        .body(request)
        .post("/api/v1/worker/leases")
        .then()
        .statusCode(401);
    var current = lease(job, "worker-a");
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", bearer("worker-b"))
        .body(new WorkerProtocolResource.HeartbeatRequest(current.leaseId()))
        .post("/api/v1/worker/leases/" + job + "/heartbeat")
        .then()
        .statusCode(403);
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", bearer("worker-b"))
        .body(
            new WorkerProtocolResource.ResultRequest(
                job, current.leaseId(), "batch-no", "{\"kind\":\"synthetic\",\"value\":1}"))
        .post("/api/v1/worker/results")
        .then()
        .statusCode(403);
    var refreshed =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", bearer("worker-a"))
            .body(new WorkerProtocolResource.HeartbeatRequest(current.leaseId()))
            .post("/api/v1/worker/leases/" + job + "/heartbeat")
            .then()
            .statusCode(200)
            .extract()
            .as(WorkerProtocolStore.Lease.class);
    assertEquals(current.leaseId(), refreshed.leaseId());
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", bearer("worker-a"))
        .body(new WorkerProtocolResource.FailureRequest(job, current.leaseId(), "SOURCE_TRANSPORT"))
        .post("/api/v1/worker/failures")
        .then()
        .statusCode(200);
    given().get("/api/v1/synthetic/accepted/" + job).then().statusCode(404);
    var recovered = lease(job, "worker-b");
    assertNotEquals(current.leaseId(), recovered.leaseId());
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", bearer("worker-b"))
        .body(
            new WorkerProtocolResource.FailureRequest(job, recovered.leaseId(), "WORKER_INTERNAL"))
        .post("/api/v1/worker/failures")
        .then()
        .statusCode(200);
    assertFalse(runtime.storeForEmulatorTest().accepted(job).isPresent());
  }

  @Test
  void emulatorConcurrentLeaseAndDuplicateSubmissionAreAuthoritativelyUnique() throws Exception {
    String job = runtime.service().seedSyntheticJob("poc-concurrent");
    var leaseGate = new CountDownLatch(1);
    var executor = Executors.newFixedThreadPool(2);
    try {
      Future<io.restassured.response.Response> a =
          executor.submit(
              () -> {
                leaseGate.await();
                return given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", bearer("worker-a"))
                    .body(new WorkerProtocolResource.LeaseRequest(job))
                    .post("/api/v1/worker/leases");
              });
      Future<io.restassured.response.Response> b =
          executor.submit(
              () -> {
                leaseGate.await();
                return given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", bearer("worker-b"))
                    .body(new WorkerProtocolResource.LeaseRequest(job))
                    .post("/api/v1/worker/leases");
              });
      leaseGate.countDown();
      var responses = java.util.List.of(a.get(), b.get());
      assertEquals(1, responses.stream().filter(x -> x.statusCode() == 200).count());
      assertEquals(1, responses.stream().filter(x -> x.statusCode() == 409).count());
      var winner =
          responses.getFirst().statusCode() == 200 ? responses.getFirst() : responses.get(1);
      var acquired = winner.as(WorkerProtocolStore.Lease.class);
      String batch = UUID.randomUUID().toString();
      String payload = "{\"kind\":\"synthetic\",\"value\":42}";
      var go = new CountDownLatch(1);
      Future<WorkerProtocolStore.Accepted> first =
          executor.submit(
              () -> {
                go.await();
                return submit(job, acquired.leaseId(), batch, acquired.workerId(), payload);
              });
      Future<WorkerProtocolStore.Accepted> second =
          executor.submit(
              () -> {
                go.await();
                return submit(job, acquired.leaseId(), batch, acquired.workerId(), payload);
              });
      go.countDown();
      var one = first.get();
      assertEquals(one, second.get());
      assertEquals(one, runtime.storeForEmulatorTest().accepted(job).orElseThrow());
      assertEquals(one, submit(job, acquired.leaseId(), batch, acquired.workerId(), payload));
      given()
          .contentType(ContentType.JSON)
          .header("Authorization", bearer(acquired.workerId()))
          .body(
              new WorkerProtocolResource.ResultRequest(
                  job, acquired.leaseId(), batch, "{\"kind\":\"synthetic\",\"value\":900}"))
          .post("/api/v1/worker/results")
          .then()
          .statusCode(409);
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void emulatorExpiryMakesLeaseRecoverableWithoutSleeping() {
    String job = runtime.service().seedSyntheticJob("poc-expiry");
    Instant start = Instant.parse("2026-10-23T12:00:00Z");
    Duration thirtySeconds = Duration.ofSeconds(30);
    var store = runtime.storeForEmulatorTest();
    var old = store.acquire(job, "worker-a", start, thirtySeconds).orElseThrow();
    assertTrue(store.acquire(job, "worker-b", start.plusSeconds(1), thirtySeconds).isEmpty());
    var current =
        store.acquire(job, "worker-b", start.plusSeconds(31), thirtySeconds).orElseThrow();
    assertNotEquals(old.leaseId(), current.leaseId());
    assertThrows(
        IllegalStateException.class,
        () ->
            store.heartbeat(job, old.leaseId(), "worker-a", start.plusSeconds(32), thirtySeconds));
    assertEquals(
        current.leaseId(),
        store
            .heartbeat(job, current.leaseId(), "worker-b", start.plusSeconds(32), thirtySeconds)
            .leaseId());
  }
}
