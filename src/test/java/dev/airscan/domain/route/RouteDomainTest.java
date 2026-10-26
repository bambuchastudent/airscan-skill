package dev.airscan.domain.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.airscan.application.port.ProviderOutcome;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RouteDomainTest {
  private static final Source AENA =
      new Source("aena-vlc", URI.create("https://www.aena.es/en/valencia/"));

  @Test
  void canonicalAirportPairIsCarrierAndScanIndependent() {
    var origin = new Airport(" vlc ");
    var destination = new Airport(" ams ");
    var route = new DirectRoute(origin, destination);
    var firstScan = new Scan("scan-1", AENA, Instant.parse("2026-10-25T12:00:00Z"));
    var secondScan = new Scan("scan-2", AENA, Instant.parse("2026-10-25T14:00:00Z"));
    var evidence1 = new RouteEvidence(route, Set.of(new Carrier("Transavia")), firstScan);
    var evidence2 =
        new RouteEvidence(route, Set.of(new Carrier("KLM"), new Carrier("Vueling")), secondScan);
    assertEquals("DIRECT:VLC:AMS", evidence1.route().canonicalKey());
    assertEquals(evidence1.route().canonicalKey(), evidence2.route().canonicalKey());
    assertEquals(2, evidence2.carriers().size());
    assertEquals("scan-2", evidence2.scan().scanId());
    assertEquals("aena-vlc", evidence2.scan().source().sourceId());
    assertNotEquals(
        route.canonicalKey(), new DirectRoute(origin, new Airport("BIO")).canonicalKey());
  }

  @Test
  void invalidIdentityAndSelfRouteFail() {
    assertThrows(IllegalArgumentException.class, () -> new Airport("VLC1"));
    assertThrows(IllegalArgumentException.class, () -> new Airport("1X!"));
    assertThrows(IllegalArgumentException.class, () -> new Airport(""));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DirectRoute(new Airport("VLC"), new Airport("vlc")));
  }

  @Test
  void dateEvidenceIsDistinctAndRetainsScanTime() {
    var scan = new Scan("scan-3", AENA, Instant.parse("2026-10-25T15:00:00Z"));
    var route = new DirectRoute(new Airport("VLC"), new Airport("LIS"));
    var date =
        new DatedAvailabilityObservation(
            route, Carrier.UNKNOWN, LocalDate.parse("2026-12-14"), scan);
    assertEquals(LocalDate.parse("2026-12-14"), date.flightDate());
    assertEquals(scan.observedAt(), date.scan().observedAt());
    assertEquals(Carrier.UNKNOWN, date.carrier());
  }

  @Test
  void failureCannotMasqueradeAsCompleteEmptyScan() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProviderOutcome<>(
                ProviderOutcome.Status.SOURCE_TRANSPORT_FAILURE,
                ProviderOutcome.Coverage.COMPLETE,
                Set.<String>of().stream().toList(),
                "timeout"));
    var partial =
        new ProviderOutcome<>(
            ProviderOutcome.Status.SUCCESS_NO_OBSERVATIONS,
            ProviderOutcome.Coverage.PARTIAL,
            java.util.List.<String>of(),
            "partial coverage");
    assertTrue(partial.observations().isEmpty());
  }
}
