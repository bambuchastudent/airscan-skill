package dev.airscan.domain.route;

import java.util.Locale;

/** Carrier as explicitly named by a source; unknown must not be guessed. */
public record Carrier(String name) {
  public static final Carrier UNKNOWN = new Carrier("UNKNOWN");

  public Carrier {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Carrier name is required");
    }
    name = name.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
  }
}
