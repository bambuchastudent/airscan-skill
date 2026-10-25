package dev.airscan.application.port;

import dev.airscan.domain.route.Airport;
import dev.airscan.domain.route.DatedAvailabilityObservation;
import dev.airscan.domain.route.Scan;
import java.time.LocalDate;

/** Supplies positive calendar-day observations; unrelated to fares or ranking. */
public interface DatedAvailabilityProvider {
  ProviderOutcome<DatedAvailabilityObservation> observe(
      Airport origin, LocalDate fromInclusive, LocalDate toInclusive, Scan scan);
}
