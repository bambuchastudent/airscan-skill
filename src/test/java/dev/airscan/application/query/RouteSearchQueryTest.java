package dev.airscan.application.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RouteSearchQueryTest {
  @Test
  void inclusiveCalendarDateBoundariesHaveNoTimezone() {
    var query = RouteSearchQuery.parse(" vlc ", "2026-12-10", "2026-12-20");
    assertTrue(query.includes(LocalDate.parse("2026-12-10")));
    assertTrue(query.includes(LocalDate.parse("2026-12-20")));
    assertFalse(query.includes(LocalDate.parse("2026-12-09")));
    assertFalse(query.includes(LocalDate.parse("2026-12-21")));
  }

  @Test
  void invalidInputIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> RouteSearchQuery.parse(null, "2026-12-10", "2026-12-20"));
    assertThrows(IllegalArgumentException.class, () -> RouteSearchQuery.parse("VLC", null, "2026-12-20"));
    assertThrows(IllegalArgumentException.class, () -> RouteSearchQuery.parse("V1C", "2026-12-10", "2026-12-20"));
    assertThrows(IllegalArgumentException.class, () -> RouteSearchQuery.parse("VLC", "2026-02-30", "2026-12-20"));
    assertThrows(IllegalArgumentException.class, () -> RouteSearchQuery.parse("VLC", "2026-12-21", "2026-12-20"));
    assertThrows(IllegalArgumentException.class, () -> RouteSearchQuery.parse("VLC", "2026-01-01", "2027-12-31"));
  }
}
