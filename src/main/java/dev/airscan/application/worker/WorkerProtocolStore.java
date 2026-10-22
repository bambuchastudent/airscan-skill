package dev.airscan.application.worker;

import dev.airscan.application.port.ArchiveReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** Central authoritative protocol state; no Firestore SDK types escape this port. */
public interface WorkerProtocolStore {
  record Lease(String jobId, String leaseId, String workerId, Instant expiresAt) {}

  record Accepted(
      String jobId,
      String leaseId,
      String batchId,
      String workerId,
      String normalizedPayloadHash,
      Instant acceptedAt,
      ArchiveReference archive) {}

  void seed(String jobId, String label, Instant now);

  Optional<Lease> acquire(String jobId, String workerId, Instant now, Duration lifetime);

  Lease heartbeat(String jobId, String leaseId, String workerId, Instant now, Duration lifetime);

  void requireActive(String jobId, String leaseId, String workerId, Instant now);

  Accepted accept(
      String jobId,
      String leaseId,
      String batchId,
      String workerId,
      String hash,
      ArchiveReference archive,
      Instant now);

  void fail(String jobId, String leaseId, String workerId, String classification, Instant now);

  Optional<Accepted> accepted(String jobId);
}
