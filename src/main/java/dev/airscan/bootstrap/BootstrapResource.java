package dev.airscan.bootstrap;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Bootstrap-only API: not a job, fare or crawler contract. */
@Path("/bootstrap/status")
public class BootstrapResource {
  @Inject BootstrapMarker marker;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String status() {
    return marker.status();
  }
}
