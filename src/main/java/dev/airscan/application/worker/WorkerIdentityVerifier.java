package dev.airscan.application.worker;

/** Pluggable authentication boundary; an OIDC implementation may replace local tokens. */
public interface WorkerIdentityVerifier {
  WorkerPrincipal verify(String authorization);
}
