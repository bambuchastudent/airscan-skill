package dev.airscan.api.v1;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import dev.airscan.adapter.archive.LocalGzipObservationArchive;
import dev.airscan.adapter.auth.LocalTokenWorkerIdentityVerifier;
import dev.airscan.adapter.firestore.FirestoreWorkerProtocolStore;
import dev.airscan.application.worker.WorkerIdentityVerifier;
import dev.airscan.application.worker.WorkerProtocolService;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** Explicit emulator-only POC wiring. No ADC fallback or service-account discovery. */
@ApplicationScoped
public class PocRuntime {
  private volatile WorkerProtocolService service;
  private volatile WorkerIdentityVerifier verifier;
  private Firestore firestore;
  private FirestoreWorkerProtocolStore store;

  public synchronized WorkerProtocolService service() {
    if (service == null) {
      String host = System.getenv("FIRESTORE_EMULATOR_HOST");
      String project = System.getenv("AIRSCAN_TEST_PROJECT_ID");
      String archive = System.getenv("AIRSCAN_POC_ARCHIVE_DIR");
      String credentialsFile = System.getenv("AIRSCAN_POC_WORKERS_FILE");
      if (host == null
          || host.isBlank()
          || project == null
          || project.isBlank()
          || archive == null
          || archive.isBlank()
          || credentialsFile == null
          || credentialsFile.isBlank()) {
        throw new WebApplicationException("Local POC environment not configured", 503);
      }
      String workers;
      try {
        workers = Files.readString(Path.of(credentialsFile)).trim();
      } catch (IOException unreadable) {
        throw new WebApplicationException("Missing local POC worker credentials", 503);
      }
      Map<String, String> secrets = new HashMap<>();
      for (String entry : workers.split(";")) {
        String[] pair = entry.split("=", 2);
        if (pair.length != 2
            || pair[0].isBlank()
            || pair[1].isBlank()
            || secrets.putIfAbsent(pair[0], pair[1]) != null) {
          throw new WebApplicationException("Invalid POC worker configuration", 503);
        }
      }
      verifier = new LocalTokenWorkerIdentityVerifier(secrets);
      firestore =
          FirestoreOptions.newBuilder()
              .setProjectId(project)
              .setEmulatorHost(host)
              .build()
              .getService();
      store = new FirestoreWorkerProtocolStore(firestore);
      service =
          new WorkerProtocolService(
              store, new LocalGzipObservationArchive(Path.of(archive)), Clock.systemUTC());
    }
    return service;
  }

  public WorkerIdentityVerifier verifier() {
    service();
    return verifier;
  }

  public FirestoreWorkerProtocolStore storeForEmulatorTest() {
    service();
    return store;
  }

  @PreDestroy
  void close() throws Exception {
    if (firestore != null) {
      firestore.close();
    }
  }
}
