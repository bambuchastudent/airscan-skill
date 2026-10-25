package dev.airscan.domain.route;

import java.util.Locale;

/** Canonical IATA airport identity, independent of display labels. */
public record Airport(String iata) {
  public Airport {
    if (iata == null) {
      throw new IllegalArgumentException("Airport IATA is required");
    }
    iata = iata.strip().toUpperCase(Locale.ROOT);
    if (!iata.matches("[A-Z]{3}")) {
      throw new IllegalArgumentException("Invalid airport IATA: " + iata);
    }
  }
}
