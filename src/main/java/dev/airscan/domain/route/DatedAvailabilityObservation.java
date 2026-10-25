package dev.airscan.domain.route;

import java.time.LocalDate;
import java.util.Objects;

/** Positive flight-day evidence only; absence never establishes unavailable flights. */
public record DatedAvailabilityObservation(
    DirectRoute route, Carrier carrier, LocalDate flightDate, Scan scan) {
  public DatedAvailabilityObservation {
    Objects.requireNonNull(route, "route");
    Objects.requireNonNull(carrier, "carrier");
    Objects.requireNonNull(flightDate, "flightDate");
    Objects.requireNonNull(scan, "scan");
  }
}
