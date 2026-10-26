package dev.airscan.application.port;

import dev.airscan.domain.route.Airport;
import dev.airscan.domain.route.RouteEvidence;
import dev.airscan.domain.route.Scan;

/** Finds advertised direct destinations, without implying date availability. */
public interface RouteTopologyProvider {
  ProviderOutcome<RouteEvidence> discover(Airport origin, Scan scan);
}
