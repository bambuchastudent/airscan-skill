package dev.airscan.application.port;

import java.util.List;
import java.util.Objects;

/** Typed scan outcome: failed/partial/unknown is never negative route evidence. */
public record ProviderOutcome<T>(
    Status status, Coverage coverage, List<T> observations, String diagnostic) {
  public enum Status {
    SUCCESS_WITH_OBSERVATIONS,
    SUCCESS_NO_OBSERVATIONS,
    SOURCE_TRANSPORT_FAILURE,
    SOURCE_REJECTED_OR_RATE_LIMITED,
    PARSER_FAILURE
  }

  public enum Coverage {
    COMPLETE,
    PARTIAL,
    UNKNOWN
  }

  public ProviderOutcome {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(coverage, "coverage");
    observations = List.copyOf(Objects.requireNonNull(observations, "observations"));
    diagnostic = diagnostic == null ? "" : diagnostic;
    if (status == Status.SUCCESS_WITH_OBSERVATIONS && observations.isEmpty()) {
      throw new IllegalArgumentException("Successful positive outcome requires observations");
    }
    if (status == Status.SUCCESS_NO_OBSERVATIONS && !observations.isEmpty()) {
      throw new IllegalArgumentException("Empty-success outcome has observations");
    }
    if (status != Status.SUCCESS_WITH_OBSERVATIONS
        && status != Status.SUCCESS_NO_OBSERVATIONS
        && (!observations.isEmpty() || coverage == Coverage.COMPLETE)) {
      throw new IllegalArgumentException("Failed scan cannot report positive data or full coverage");
    }
  }
}
