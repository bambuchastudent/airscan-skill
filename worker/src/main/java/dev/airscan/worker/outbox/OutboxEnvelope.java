package dev.airscan.worker.outbox;

import java.time.Instant;

/** Durable retry identity and sanitized synthetic payload; no HTTP lease protocol. */
public record OutboxEnvelope(String batchId, String payload, Instant createdAt) {}
