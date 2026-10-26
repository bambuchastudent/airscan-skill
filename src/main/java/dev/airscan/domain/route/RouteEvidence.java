package dev.airscan.domain.route;

import java.util.Objects;
import java.util.Set;

/** A positive advertised-topology observation with source, scan and carriers. */
public record RouteEvidence(DirectRoute route, Set<Carrier> carriers, Scan scan) {
  public RouteEvidence {
    Objects.requireNonNull(route, "route");
    Objects.requireNonNull(scan, "scan");
    carriers =
        carriers == null || carriers.isEmpty() ? Set.of(Carrier.UNKNOWN) : Set.copyOf(carriers);
  }
}
