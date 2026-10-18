package dev.airscan.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.airscan.application.port.CentralOperationalStore;
import dev.airscan.application.port.SyntheticControlRecord;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CentralRecordServiceTest {
  @Test
  void applicationOwnsIdentityAndDelegatesToPortWithoutCloudCredentials() {
    Map<String, SyntheticControlRecord> records = new HashMap<>();
    CentralOperationalStore fake =
        new CentralOperationalStore() {
          @Override
          public void create(SyntheticControlRecord record) {
            if (records.putIfAbsent(record.recordId(), record) != null) {
              throw new IllegalStateException("Duplicate central identity");
            }
          }

          @Override
          public Optional<SyntheticControlRecord> find(String id) {
            return Optional.ofNullable(records.get(id));
          }
        };
    var application = new CentralRecordService(fake);
    var first = application.createSynthetic("synthetic");
    var second = application.createSynthetic("synthetic");
    assertNotEquals(first.recordId(), second.recordId());
    assertEquals(first, application.find(first.recordId()).orElseThrow());
    assertFalse(application.find("missing").isPresent());
    assertThrows(IllegalArgumentException.class, () -> application.createSynthetic(""));
  }
}
