package dev.airscan.application.port;

/** Synthetic control-plane data only; no route, scan protocol or user payloads. */
public record SyntheticControlRecord(String recordId, String label) {}
