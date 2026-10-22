package dev.airscan.application.port;

/** Provenance and content identity for an immutable compressed synthetic batch. */
public record ArchiveReference(
    String sourceId,
    String batchId,
    String sha256,
    String objectKey,
    String compression,
    long originalBytes,
    long compressedBytes) {}
