package dev.airscan.domain.route;

import java.net.URI;

/** Published provenance, not authorization to crawl a web page. */
public record Source(String sourceId, URI sourceUri) {
  public Source {
    if (sourceId == null || !sourceId.matches("[a-z0-9][a-z0-9._-]{1,63}")) {
      throw new IllegalArgumentException("Invalid source identity");
    }
    if (sourceUri == null || !"https".equalsIgnoreCase(sourceUri.getScheme())) {
      throw new IllegalArgumentException("Source URI must be HTTPS");
    }
  }
}
