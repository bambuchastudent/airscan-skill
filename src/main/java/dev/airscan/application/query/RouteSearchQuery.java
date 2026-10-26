package dev.airscan.application.query;

import dev.airscan.domain.route.Airport;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Validation-only contract; no REST endpoint or persistence is implemented in RT-1. */
public record RouteSearchQuery(Airport origin, LocalDate from, LocalDate to) {
  private static final long MAX_DAYS_INCLUSIVE = 366;

  public RouteSearchQuery {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("from must not exceed to");
    }
    if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_DAYS_INCLUSIVE) {
      throw new IllegalArgumentException("Date interval is too large");
    }
  }

  public static RouteSearchQuery parse(String origin, String from, String to) {
    try {
      return new RouteSearchQuery(new Airport(origin), LocalDate.parse(from), LocalDate.parse(to));
    } catch (DateTimeParseException | NullPointerException error) {
      throw new IllegalArgumentException("Malformed or missing date", error);
    }
  }

  public boolean includes(LocalDate day) {
    return day != null && !day.isBefore(from) && !day.isAfter(to);
  }
}
