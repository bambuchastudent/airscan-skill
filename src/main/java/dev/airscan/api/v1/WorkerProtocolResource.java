package dev.airscan.api.v1;

import dev.airscan.application.worker.WorkerPrincipal;
import dev.airscan.application.worker.WorkerProtocolStore;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.function.Supplier;

/** Strictly synthetic, versioned worker protocol with no anonymous mutation path. */
@Path("/api/v1/worker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class WorkerProtocolResource {
  public record LeaseRequest(String jobId) {}

  public record HeartbeatRequest(String leaseId) {}

  public record ResultRequest(
      String jobId, String leaseId, String batchId, String normalizedPayload) {}

  public record FailureRequest(String jobId, String leaseId, String classification) {}

  public record FailureReceipt(String status) {}

  @Inject PocRuntime runtime;

  @POST
  @Path("/leases")
  public WorkerProtocolStore.Lease lease(
      @HeaderParam("Authorization") String authorization, LeaseRequest body) {
    WorkerPrincipal principal = authenticate(authorization);
    return respond(() -> runtime.service().lease(principal, body.jobId()));
  }

  @POST
  @Path("/leases/{jobId}/heartbeat")
  public WorkerProtocolStore.Lease heartbeat(
      @HeaderParam("Authorization") String authorization,
      @PathParam("jobId") String jobId,
      HeartbeatRequest body) {
    WorkerPrincipal principal = authenticate(authorization);
    return respond(() -> runtime.service().heartbeat(principal, jobId, body.leaseId()));
  }

  @POST
  @Path("/results")
  public WorkerProtocolStore.Accepted result(
      @HeaderParam("Authorization") String authorization, ResultRequest body) {
    WorkerPrincipal principal = authenticate(authorization);
    return respond(
        () ->
            runtime
                .service()
                .submit(
                    principal,
                    body.jobId(),
                    body.leaseId(),
                    body.batchId(),
                    body.normalizedPayload()));
  }

  @POST
  @Path("/failures")
  public FailureReceipt failure(
      @HeaderParam("Authorization") String authorization, FailureRequest body) {
    WorkerPrincipal principal = authenticate(authorization);
    return respond(
        () -> {
          runtime.service().fail(principal, body.jobId(), body.leaseId(), body.classification());
          return new FailureReceipt("RECOVERABLE");
        });
  }

  private WorkerPrincipal authenticate(String authorization) {
    try {
      return runtime.verifier().verify(authorization);
    } catch (SecurityException invalid) {
      throw new WebApplicationException("Unauthorized worker", Response.Status.UNAUTHORIZED);
    }
  }

  private static <T> T respond(Supplier<T> action) {
    try {
      return action.get();
    } catch (SecurityException security) {
      throw new WebApplicationException("Forbidden lease principal", Response.Status.FORBIDDEN);
    } catch (IllegalArgumentException invalid) {
      throw new WebApplicationException("Invalid synthetic worker request", 400);
    } catch (IllegalStateException conflict) {
      throw new WebApplicationException("Worker lease or batch conflict", 409);
    }
  }
}
