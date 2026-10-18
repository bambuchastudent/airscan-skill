package dev.airscan.api.v1;

import dev.airscan.application.CapabilitiesService;
import dev.airscan.application.CapabilitiesSnapshot;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/** Public, read-only v1 contract; independent of MCP and storage. */
@Path("/api/v1/capabilities")
@Tag(name = "System")
public class CapabilitiesResource {
  @Inject CapabilitiesService capabilities;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Discover supported system-level application operations")
  public CapabilitiesSnapshot current() {
    return capabilities.current();
  }
}
