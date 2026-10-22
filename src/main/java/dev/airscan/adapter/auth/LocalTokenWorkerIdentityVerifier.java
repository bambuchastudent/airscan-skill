package dev.airscan.adapter.auth;

import dev.airscan.application.worker.WorkerIdentityVerifier;
import dev.airscan.application.worker.WorkerPrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;

/** Local POC only; tokens are injected at runtime and never logged. */
public final class LocalTokenWorkerIdentityVerifier implements WorkerIdentityVerifier {
  private final Map<String, String> tokens;

  public LocalTokenWorkerIdentityVerifier(Map<String, String> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      throw new IllegalArgumentException("Worker identity configuration is required");
    }
    this.tokens = Map.copyOf(tokens);
    if (this.tokens.values().stream().anyMatch(v -> v == null || v.isBlank())) {
      throw new IllegalArgumentException("Empty token is not supported");
    }
  }

  @Override
  public WorkerPrincipal verify(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new SecurityException("Unauthorized worker");
    }
    byte[] supplied = authorization.substring(7).getBytes(StandardCharsets.UTF_8);
    for (var worker : tokens.entrySet()) {
      if (MessageDigest.isEqual(
          supplied, worker.getValue().getBytes(StandardCharsets.UTF_8))) {
        return new WorkerPrincipal(worker.getKey());
      }
    }
    throw new SecurityException("Unauthorized worker");
  }
}
