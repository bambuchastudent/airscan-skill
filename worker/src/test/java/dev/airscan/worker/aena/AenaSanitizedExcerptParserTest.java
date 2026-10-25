package dev.airscan.worker.aena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AenaSanitizedExcerptParserTest {
  private static String fixture() throws Exception {
    try (InputStream input =
        AenaSanitizedExcerptParserTest.class
            .getResourceAsStream("/aena/vlc-sanitized-excerpt.txt")) {
      if (input == null) {
        throw new IllegalStateException("Missing source excerpt fixture");
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  void combinesCarriersAndDuplicateAirportPairsDeterministically() throws Exception {
    var parser = new AenaSanitizedExcerptParser();
    var result = parser.parse(fixture(), "VLC");
    assertEquals(6, result.sourceRows());
    assertEquals(5, result.routes().size());
    assertFalse(result.hasGaps());
    assertEquals(result, parser.parse(fixture(), "vlc"));
    var bilbao =
        result.routes().stream()
            .filter(route -> route.routeKey().equals("DIRECT:VLC:BIO"))
            .findFirst()
            .orElseThrow();
    assertEquals(3, bilbao.carrierNames().size());
    assertTrue(bilbao.carrierNames().contains("VOLOTEA"));
    var asturias =
        result.routes().stream()
            .filter(route -> route.routeKey().equals("DIRECT:VLC:OVD"))
            .findFirst()
            .orElseThrow();
    assertEquals(java.util.List.of("UNKNOWN"), asturias.carrierNames());
  }

  @Test
  void missingAndInvalidDestinationsAreReportedNeverSilentlyDiscarded() {
    var parser = new AenaSanitizedExcerptParser();
    String damaged =
        "# AENA_VLC_SANITIZED_EXCERPT_V1\n"
            + "Destination: INVALID AIRPORT (12)\n"
            + "Carriers: AIR NOSTRUM\n"
            + "Destination: VALENCIA (VLC)\n"
            + "Destination: LISBON (LIS)\n"
            + "Carriers: RYANAIR (RYR)\n"
            + "ChangedMarker: unexpected\n";
    var result = parser.parse(damaged, "VLC");
    assertEquals(3, result.sourceRows());
    assertEquals(1, result.routes().size());
    assertEquals("DIRECT:VLC:LIS", result.routes().get(0).routeKey());
    assertTrue(result.hasGaps());
    assertTrue(result.omissions().size() >= 3);
    assertThrows(
        IllegalArgumentException.class, () -> parser.parse("New layout", "VLC"));
  }
}
