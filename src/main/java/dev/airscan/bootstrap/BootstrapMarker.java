package dev.airscan.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;

/** Minimal control-plane bootstrap marker; no product behavior. */
@ApplicationScoped
public class BootstrapMarker {
    public String status() {
        return "ready";
    }
}
