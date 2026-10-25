package dev.airscan.domain.route;

import java.util.Objects;

/** An airport-pair identity; multiple operating carriers may support one route. */
public record DirectRoute(Airport origin, Airport destination) {
  public DirectRoute {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(destination, "destination");
    if (origin.equals(destination)) {
      throw new IllegalArgumentException("A direct route cannot be a self route");
    }
  }

  public String canonicalKey() {
    return "DIRECT:" + origin.iata() + ":" + destination.iata();
  }
}
