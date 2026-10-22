package dev.airscan.application.worker;

/** Server-recognized principal, never taken from an untrusted worker ID header. */
public record WorkerPrincipal(String workerId) {}
