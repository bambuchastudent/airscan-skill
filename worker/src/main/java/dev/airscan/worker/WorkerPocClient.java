package dev.airscan.worker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.airscan.worker.outbox.OutboxEnvelope;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/** Portable plain-Java HTTP client: only HTTPS in production, localhost HTTP for emulator POC. */
public final class WorkerPocClient {
  private final HttpClient http = HttpClient.newHttpClient();
  private final ObjectMapper json = new ObjectMapper();
  private final URI base;
  private final String bearer;

  public WorkerPocClient(URI base, String bearer) {
    this.base = Objects.requireNonNull(base);
    if (bearer == null || bearer.isBlank()) {
      throw new IllegalArgumentException("No worker token");
    }
    if (!"https".equals(base.getScheme())
        && !("http".equals(base.getScheme())
            && ("localhost".equals(base.getHost()) || "127.0.0.1".equals(base.getHost())))) {
      throw new IllegalArgumentException("Plaintext worker traffic is local-only");
    }
    this.bearer = bearer;
  }

  public JsonNode lease(String jobId) {
    return request("POST", "/api/v1/worker/leases", Map.of("jobId", jobId));
  }

  public JsonNode heartbeat(String jobId, String leaseId) {
    return request(
        "POST", "/api/v1/worker/leases/" + jobId + "/heartbeat",
        Map.of("leaseId", leaseId));
  }

  public JsonNode submit(String jobId, String leaseId, OutboxEnvelope pending) {
    return request(
        "POST", "/api/v1/worker/results",
        Map.of(
            "jobId", jobId,
            "leaseId", leaseId,
            "batchId", pending.batchId(),
            "normalizedPayload", pending.payload()));
  }

  public JsonNode fail(String jobId, String leaseId, String classification) {
    return request(
        "POST", "/api/v1/worker/failures",
        Map.of("jobId", jobId, "leaseId", leaseId, "classification", classification));
  }

  public JsonNode queryAccepted(String jobId) {
    return request("GET", "/api/v1/synthetic/accepted/" + jobId, null);
  }

  private JsonNode request(String method, String path, Object body) {
    try {
      var builder =
          HttpRequest.newBuilder(base.resolve(path))
              .timeout(Duration.ofSeconds(15))
              .header("Authorization", "Bearer " + bearer);
      if ("POST".equals(method)) {
        builder.header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
      } else {
        builder.GET();
      }
      HttpResponse<String> response =
          http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new IllegalStateException("Worker protocol HTTP status " + response.statusCode());
      }
      return json.readTree(response.body());
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted worker request", interrupted);
    } catch (IOException failure) {
      throw new IllegalStateException("Worker protocol I/O failure", failure);
    }
  }
}
