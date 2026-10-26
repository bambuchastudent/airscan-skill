package dev.airscan.domain.route;

import java.time.Instant;
import java.util.Objects;

/** Immutable scan provenance. Scan identity never contributes to the canonical route key. */
public record Scan(String scanId, Source source, Instant observedAt) {
  public Scan {
    if (scanId == null || scanId.isBlank() || scanId.length() > 128) {
      throw new IllegalArgumentException("Invalid scan ID");
    }
    Objects.requireNonNull(source, "source");
    Objects.requireNonNull(observedAt, "observedAt");
  }
}
