package dev.airscan.adapter.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import dev.airscan.application.port.ArchiveReference;
import dev.airscan.application.worker.WorkerProtocolStore;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/** Single-document Firestore transactions enforce synthetic lease and ingestion uniqueness. */
public final class FirestoreWorkerProtocolStore implements WorkerProtocolStore {
  private final Firestore firestore;

  public FirestoreWorkerProtocolStore(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  private DocumentReference document(String jobId) {
    return firestore.collection("poc_worker_jobs").document(jobId);
  }

  @Override
  public void seed(String jobId, String label, Instant now) {
    await(
        document(jobId)
            .create(
                Map.of(
                    "jobId",
                    jobId,
                    "label",
                    label,
                    "createdAt",
                    now.toString(),
                    "state",
                    "READY")));
  }

  @Override
  public Optional<Lease> acquire(String jobId, String workerId, Instant now, Duration lifetime) {
    return atomic(
        tx -> {
          DocumentReference ref = document(jobId);
          DocumentSnapshot record = tx.get(ref).get();
          if (!record.exists() || "ACCEPTED".equals(record.getString("state"))) {
            return Optional.<Lease>empty();
          }
          if ("LEASED".equals(record.getString("state"))
              && Instant.parse(record.getString("expiresAt")).isAfter(now)) {
            return Optional.<Lease>empty();
          }
          Lease lease =
              new Lease(jobId, UUID.randomUUID().toString(), workerId, now.plus(lifetime));
          tx.update(
              ref,
              Map.of(
                  "state",
                  "LEASED",
                  "leaseId",
                  lease.leaseId(),
                  "workerId",
                  workerId,
                  "expiresAt",
                  lease.expiresAt().toString()));
          return Optional.of(lease);
        });
  }

  @Override
  public Lease heartbeat(
      String jobId, String leaseId, String workerId, Instant now, Duration lifetime) {
    return atomic(
        tx -> {
          DocumentReference ref = document(jobId);
          DocumentSnapshot record = tx.get(ref).get();
          requireLease(record, leaseId, workerId, now);
          Lease renewed = new Lease(jobId, leaseId, workerId, now.plus(lifetime));
          tx.update(ref, "expiresAt", renewed.expiresAt().toString());
          return renewed;
        });
  }

  @Override
  public void requireActive(String jobId, String leaseId, String workerId, Instant now) {
    requireLease(await(document(jobId).get()), leaseId, workerId, now);
  }

  @Override
  public Accepted accept(
      String jobId,
      String leaseId,
      String batchId,
      String workerId,
      String hash,
      ArchiveReference archive,
      Instant now) {
    return atomic(
        tx -> {
          DocumentReference ref = document(jobId);
          DocumentSnapshot record = tx.get(ref).get();
          if (record.exists() && "ACCEPTED".equals(record.getString("state"))) {
            Accepted existing = acceptedRecord(record);
            if (existing.leaseId().equals(leaseId)
                && existing.batchId().equals(batchId)
                && existing.workerId().equals(workerId)
                && existing.normalizedPayloadHash().equals(hash)) {
              return existing;
            }
            throw new IllegalStateException("Accepted identity collision");
          }
          requireLease(record, leaseId, workerId, now);
          var updated = new HashMap<String, Object>();
          updated.put("state", "ACCEPTED");
          updated.put("batchId", batchId);
          updated.put("hash", hash);
          updated.put("acceptedAt", now.toString());
          updated.put("archiveSourceId", archive.sourceId());
          updated.put("archiveBatchId", archive.batchId());
          updated.put("archiveKey", archive.objectKey());
          updated.put("archiveSha", archive.sha256());
          updated.put("archiveCompression", archive.compression());
          updated.put("archiveOriginalBytes", archive.originalBytes());
          updated.put("archiveCompressedBytes", archive.compressedBytes());
          tx.update(ref, updated);
          return new Accepted(jobId, leaseId, batchId, workerId, hash, now, archive);
        });
  }

  @Override
  public void fail(
      String jobId, String leaseId, String workerId, String classification, Instant now) {
    atomic(
        tx -> {
          DocumentReference ref = document(jobId);
          DocumentSnapshot record = tx.get(ref).get();
          requireLease(record, leaseId, workerId, now);
          tx.update(
              ref,
              Map.of(
                  "state",
                  "READY",
                  "lastFailureClass",
                  classification,
                  "lastFailureAt",
                  now.toString()));
          return true;
        });
  }

  @Override
  public Optional<Accepted> accepted(String jobId) {
    DocumentSnapshot record = await(document(jobId).get());
    return record.exists() && "ACCEPTED".equals(record.getString("state"))
        ? Optional.of(acceptedRecord(record))
        : Optional.empty();
  }

  private static Accepted acceptedRecord(DocumentSnapshot record) {
    ArchiveReference archive =
        new ArchiveReference(
            record.getString("archiveSourceId"),
            record.getString("archiveBatchId"),
            record.getString("archiveSha"),
            record.getString("archiveKey"),
            record.getString("archiveCompression"),
            record.getLong("archiveOriginalBytes"),
            record.getLong("archiveCompressedBytes"));
    return new Accepted(
        record.getId(),
        record.getString("leaseId"),
        record.getString("batchId"),
        record.getString("workerId"),
        record.getString("hash"),
        Instant.parse(record.getString("acceptedAt")),
        archive);
  }

  private static void requireLease(
      DocumentSnapshot record, String leaseId, String workerId, Instant now) {
    if (!record.exists()
        || !"LEASED".equals(record.getString("state"))
        || !Objects.equals(leaseId, record.getString("leaseId"))
        || !Instant.parse(record.getString("expiresAt")).isAfter(now)) {
      throw new IllegalStateException("Lease not active");
    }
    if (!Objects.equals(workerId, record.getString("workerId"))) {
      throw new SecurityException("Lease belongs to another worker");
    }
  }

  private <T> T atomic(Transaction.Function<T> operation) {
    return await(firestore.runTransaction(operation));
  }

  private static <T> T await(ApiFuture<T> future) {
    try {
      return future.get();
    } catch (InterruptedException interrupted) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted during central transaction", interrupted);
    } catch (ExecutionException failure) {
      Throwable cause = failure.getCause();
      if (cause instanceof RuntimeException runtime) {
        throw runtime;
      }
      throw new IllegalStateException("Central transaction failed", cause);
    }
  }
}
