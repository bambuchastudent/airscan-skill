package dev.airscan.application.port;

/** Immutable normalized-batch archive contract, independent of GCS and worker SQLite. */
public interface ObservationArchive {
  ArchiveReference archive(String sourceId, String batchId, byte[] sanitizedPayload);
}
