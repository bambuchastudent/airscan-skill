package dev.airscan.application;

import dev.airscan.application.port.CentralOperationalStore;
import dev.airscan.application.port.SyntheticControlRecord;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The application, not a crawler or storage adapter, creates authoritative record identities. */
public final class CentralRecordService {
  private final CentralOperationalStore store;

  public CentralRecordService(CentralOperationalStore store) {
    this.store = Objects.requireNonNull(store);
  }

  public SyntheticControlRecord createSynthetic(String label) {
    if (label == null || label.isBlank() || label.length() > 64) {
      throw new IllegalArgumentException("Synthetic label must be 1 to 64 characters");
    }
    var record = new SyntheticControlRecord(UUID.randomUUID().toString(), label);
    store.create(record);
    return record;
  }

  public Optional<SyntheticControlRecord> find(String recordId) {
    return store.find(recordId);
  }
}
