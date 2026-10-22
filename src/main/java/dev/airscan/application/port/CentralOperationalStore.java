package dev.airscan.application.port;

import java.util.Optional;

/** Control-plane-only operational persistence; contains no cloud API types. */
public interface CentralOperationalStore {
  void create(SyntheticControlRecord record);

  Optional<SyntheticControlRecord> find(String recordId);
}
