package dev.airscan.api.v1;

import dev.airscan.application.worker.WorkerProtocolStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** Narrow synthetic read contract; no fares, routes, or Firestore schema exposure. */
@Path("/api/v1/synthetic/accepted")
@Produces(MediaType.APPLICATION_JSON)
public class SyntheticAcceptedResource {
  @Inject PocRuntime runtime;

  @GET
  @Path("/{jobId}")
  public WorkerProtocolStore.Accepted accepted(@PathParam("jobId") String jobId) {
    return runtime.service().accepted(jobId).orElseThrow(NotFoundException::new);
  }
}
