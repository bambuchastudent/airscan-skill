package dev.airscan.application.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.airscan.application.port.ObservationArchive;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Server-side worker protocol semantics, independent of HTTP and Firestore. */
public final class WorkerProtocolService {
  private static final Duration LEASE_LIFETIME = Duration.ofSeconds(30);
  private final WorkerProtocolStore store;
  private final ObservationArchive archive;
  private final Clock clock;
  private final ObjectMapper mapper = new ObjectMapper();

  public WorkerProtocolService(WorkerProtocolStore store, ObservationArchive archive, Clock clock) {
    this.store = Objects.requireNonNull(store);
    this.archive = Objects.requireNonNull(archive);
    this.clock = Objects.requireNonNull(clock);
  }

  /** Local fixture: only server code may seed synthetic work; there is no seed HTTP endpoint. */
  public String seedSyntheticJob(String label) {
    if (label == null || label.isBlank() || label.length() > 64) {
      throw new IllegalArgumentException("Invalid synthetic label");
    }
    String id = UUID.randomUUID().toString();
    store.seed(id, label, clock.instant());
    return id;
  }

  public WorkerProtocolStore.Lease lease(WorkerPrincipal worker, String jobId) {
    return store
        .acquire(jobId, worker.workerId(), clock.instant(), LEASE_LIFETIME)
        .orElseThrow(() -> new IllegalStateException("Job unavailable for lease"));
  }

  public WorkerProtocolStore.Lease heartbeat(WorkerPrincipal worker, String jobId, String leaseId) {
    return store.heartbeat(jobId, leaseId, worker.workerId(), clock.instant(), LEASE_LIFETIME);
  }

  public WorkerProtocolStore.Accepted submit(
      WorkerPrincipal worker,
      String jobId,
      String leaseId,
      String batchId,
      String normalizedPayload) {
    requireId(jobId);
    requireId(leaseId);
    requireId(batchId);
    byte[] content = normalize(normalizedPayload);
    String hash = sha256(content);
    // Authoritative idempotent replay is checked BEFORE lease expiry. A
    // previously committed response may have been lost and the lease expired.
    var previous = store.accepted(jobId);
    if (previous.isPresent()) {
      var accepted = previous.get();
      if (accepted.workerId().equals(worker.workerId())
          && accepted.leaseId().equals(leaseId)
          && accepted.batchId().equals(batchId)
          && accepted.normalizedPayloadHash().equals(hash)) {
        return accepted;
      }
      throw new IllegalStateException("Accepted batch identity collision");
    }
    // Avoid archiving malformed or currently unauthorized attempts. The final
    // transaction rechecks ownership, expiry and unique acceptance atomically.
    store.requireActive(jobId, leaseId, worker.workerId(), clock.instant());
    var reference = archive.archive("synthetic-poc", batchId, content);
    return store.accept(
        jobId, leaseId, batchId, worker.workerId(), hash, reference, clock.instant());
  }

  public void fail(WorkerPrincipal worker, String jobId, String leaseId, String classification) {
    if (!"SOURCE_TRANSPORT".equals(classification) && !"WORKER_INTERNAL".equals(classification)) {
      throw new IllegalArgumentException("Unknown failure classification");
    }
    store.fail(jobId, leaseId, worker.workerId(), classification, clock.instant());
  }

  public Optional<WorkerProtocolStore.Accepted> accepted(String jobId) {
    return store.accepted(jobId);
  }

  private byte[] normalize(String payload) {
    if (payload == null || payload.isBlank() || payload.length() > 8192) {
      throw new IllegalArgumentException("Invalid normalized synthetic payload");
    }
    try {
      var node = mapper.readTree(payload);
      if (!node.isObject()
          || node.size() != 2
          || !node.path("kind").asText().equals("synthetic")
          || !node.path("value").isIntegralNumber()) {
        throw new IllegalArgumentException("Only normalized synthetic observations are supported");
      }
      var canonical = mapper.createObjectNode();
      canonical.put("kind", "synthetic");
      canonical.put("value", node.get("value").asInt());
      return mapper.writeValueAsBytes(canonical);
    } catch (com.fasterxml.jackson.core.JsonProcessingException error) {
      throw new IllegalArgumentException("Malformed normalized synthetic payload", error);
    }
  }

  private static String sha256(byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("SHA-256 unavailable", error);
    }
  }

  private static void requireId(String value) {
    if (value == null || value.isBlank() || value.length() > 128) {
      throw new IllegalArgumentException("Invalid protocol identity");
    }
  }
}
