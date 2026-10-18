package dev.airscan.application;

import java.util.List;

/** Versioned, domain-neutral capability discovery result. */
public record CapabilitiesSnapshot(String apiVersion, List<String> operations) {}
