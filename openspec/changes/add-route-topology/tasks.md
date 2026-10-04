# Tasks

## 1. Domain and persistence

- [ ] 1.1 Define airport, carrier, route, source, scan, and route-evidence domain contracts aligned with INV-ROUTE-001/002 and INV-EVIDENCE-001; verify unit tests cover canonical identity and invalid endpoint cases.
- [ ] 1.2 Implement storage boundaries and the central topology persistence path using the distributed control-plane store, plus worker-local SQLite cache/spool only where needed; verify deterministic emulator/local integration tests preserve evidence history and idempotent ingestion.
- [ ] 1.3 Add configurable origins with VLC/CDT/ALC/MAD/BCN defaults and verify configuration override tests.

## 2. Formal identity/evidence checks

- [ ] 2.1 Add/update the Alloy route/evidence model for applicable invariants, document finite scopes, and verify the checker finds no counterexample in the committed configuration.
- [ ] 2.2 Decide from measured complexity whether canonical route identity merits Dafny; if yes implement/verify it and Java integration, otherwise document why normal Java/property tests are sufficient.

## 3. Topology provider boundary

- [ ] 3.1 Define a source-agnostic topology-provider contract with explicit provenance/failure semantics and verify contract tests can run without network access.
- [ ] 3.2 Research and document the first authoritative topology source for the configured origins, including access method, rate limits/constraints, and stability evidence; verify no undocumented endpoint is presented as stable.
- [ ] 3.3 Implement exactly one initial topology adapter with sanitized fixtures and verify fixture-based parser/normalization tests plus one explicitly separated live smoke check where appropriate.

## 4. Geographic candidate generation

- [ ] 4.1 Add a documented airport coordinate dataset/import path and verify all default origins have valid coordinates.
- [ ] 4.2 Implement deterministic geographic-distance candidate queries and verify known-distance fixtures plus the rule that distance alone never creates a ground-transport edge.

## 5. Integration

- [ ] 5.1 Run topology ingestion end-to-end through the worker/control-plane boundary into a clean emulated/local central store, verify repeated submissions attach to stable route identity, and verify provenance can be queried.
- [ ] 5.2 Run Maven tests, relevant Alloy/Dafny checks, strict OpenSpec validation, and Graphify/Serena impact review before marking the change complete.
