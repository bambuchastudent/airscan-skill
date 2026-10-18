package dev.airscan.application;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/** Single application-level source of capability semantics for all adapters. */
@ApplicationScoped
public class CapabilitiesService {
  public CapabilitiesSnapshot current() {
    return new CapabilitiesSnapshot("v1", List.of("airscan.capabilities"));
  }
}
