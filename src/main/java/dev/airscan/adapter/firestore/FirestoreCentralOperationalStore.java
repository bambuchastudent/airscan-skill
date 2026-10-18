package dev.airscan.adapter.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import dev.airscan.application.port.CentralOperationalStore;
import dev.airscan.application.port.SyntheticControlRecord;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/** Firestore is authoritative only when invoked by control-plane application services. */
public final class FirestoreCentralOperationalStore implements CentralOperationalStore {
  private static final String COLLECTION = "synthetic_control_records";

  private final Firestore firestore;

  public FirestoreCentralOperationalStore(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public void create(SyntheticControlRecord record) {
    var doc = firestore.collection(COLLECTION).document(record.recordId());
    await(doc.create(Map.of("label", record.label())));
  }

  @Override
  public Optional<SyntheticControlRecord> find(String recordId) {
    var doc = await(firestore.collection(COLLECTION).document(recordId).get());
    if (!doc.exists()) {
      return Optional.empty();
    }
    return Optional.of(new SyntheticControlRecord(doc.getId(), doc.getString("label")));
  }

  private static <T> T await(ApiFuture<T> future) {
    try {
      return future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while accessing central store", e);
    } catch (ExecutionException e) {
      throw new IllegalStateException("Central store operation failed", e);
    }
  }
}
