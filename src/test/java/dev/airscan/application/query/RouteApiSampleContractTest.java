package dev.airscan.application.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Offline JSON examples constrain intended future REST shapes; they do not start REST endpoints.
 */
class RouteApiSampleContractTest {
  private final ObjectMapper mapper = new ObjectMapper();

  private JsonNode sample(String name) throws IOException {
    try (var stream = getClass().getResourceAsStream("/route-query/" + name)) {
      if (stream == null) {
        throw new IOException("Missing contract sample " + name);
      }
      return mapper.readTree(stream);
    }
  }

  @Test
  void topologyCatalogueHasNoDateOrFareRequirement() throws IOException {
    var catalog = sample("topology-example.json");
    assertEquals("VLC", catalog.path("origin").asText());
    assertEquals("PARTIAL", catalog.path("sourceCoverage").asText());
    JsonNode route = catalog.path("routes").get(0);
    assertEquals("DIRECT:VLC:AMS", route.path("routeId").asText());
    assertEquals(2, route.path("carriers").size());
    assertEquals("aena-vlc", route.path("evidence").get(0).path("sourceId").asText());
    assertFalse(route.has("matchingAvailabilityDates"));
    assertFalse(route.has("fares"));
  }

  @Test
  void dateEnrichmentKeepsUncoveredRoutesAndPositiveEvidenceOnly() throws IOException {
    var response = sample("date-enrichment-example.json");
    var query =
        RouteSearchQuery.parse(
            response.path("origin").asText(),
            response.path("from").asText(),
            response.path("to").asText());
    assertEquals(2, response.path("routes").size());
    assertEquals("UNCOVERED", response.path("routes").get(0).path("availabilityStatus").asText());
    assertTrue(response.path("routes").get(0).path("matchingAvailabilityDates").isEmpty());
    var confirmed = response.path("routes").get(1);
    assertEquals("CONFIRMED", confirmed.path("availabilityStatus").asText());
    assertTrue(
        query.includes(
            LocalDate.parse(confirmed.path("matchingAvailabilityDates").get(0).asText())));
    assertEquals(
        "example-date-source", confirmed.path("dateEvidence").get(0).path("sourceId").asText());
    assertFalse(confirmed.has("fares"));
  }
}
