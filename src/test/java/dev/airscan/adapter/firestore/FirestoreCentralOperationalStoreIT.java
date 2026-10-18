package dev.airscan.adapter.firestore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.cloud.firestore.FirestoreOptions;
import dev.airscan.application.CentralRecordService;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Only invoked with the explicit firestore-emulator Maven profile and CI emulator process. */
class FirestoreCentralOperationalStoreIT {
  @Test
  void recordSurvivesNewClientAndAdapterInstanceInEmulator() throws Exception {
    String host = System.getenv("FIRESTORE_EMULATOR_HOST");
    assertNotNull(host, "The mandatory emulator must be started explicitly");
    String project = System.getenv("AIRSCAN_TEST_PROJECT_ID");
    assertNotNull(project, "A synthetic project ID is required");
    var options = FirestoreOptions.newBuilder().setProjectId(project).setEmulatorHost(host).build();
    String uniqueLabel = "test-" + UUID.randomUUID();
    String id;
    try (var firstClient = options.getService()) {
      var application = new CentralRecordService(new FirestoreCentralOperationalStore(firstClient));
      var record = application.createSynthetic(uniqueLabel);
      id = record.recordId();
      assertEquals(record, application.find(id).orElseThrow());
      assertFalse(application.find(UUID.randomUUID().toString()).isPresent());
    }
    var restartedOptions =
        FirestoreOptions.newBuilder().setProjectId(project).setEmulatorHost(host).build();
    try (var restartedClient = restartedOptions.getService()) {
      var application =
          new CentralRecordService(new FirestoreCentralOperationalStore(restartedClient));
      assertEquals(uniqueLabel, application.find(id).orElseThrow().label());
      assertNotEquals("", application.find(id).orElseThrow().recordId());
    }
  }
}
