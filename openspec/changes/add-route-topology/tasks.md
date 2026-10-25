# Tasks

## 0. VLC Route/Date Discovery POC milestone

### RT-1 — contracts, source research, fixtures

- [ ] 0.1 Define the minimum airport, carrier, direct-route, source, scan, route-evidence, and dated-availability contracts needed by the VLC POC; align route identity/provenance with INV-ROUTE-001/002 and INV-EVIDENCE-001 and verify invalid endpoint/date cases.
- [ ] 0.2 Define separate source-agnostic topology and narrow date-availability provider contracts with explicit provenance and failure semantics; verify offline contract tests require no network.
- [ ] 0.3 Research and document exactly one first real source path capable of supporting VLC direct-route discovery and date availability, including access method, source ownership, rate/usage constraints, stability evidence, and why the selected interface is acceptable. Do not claim undocumented private endpoints are stable.
- [ ] 0.4 Capture sanitized fixtures for the selected source and implement deterministic parser/normalization tests; keep any live smoke explicitly separate.
- [ ] 0.5 Define the versioned local REST query contract for origin + inclusive date interval and verify it distinguishes route existence, positive dated availability, and scan/provider failure.

### RT-2 — real VLC vertical slice

- [ ] 0.6 Implement exactly one real provider path for VLC and execute it from the portable worker runtime; do not add a second provider in this milestone.
- [ ] 0.7 Persist normalized route + dated-availability evidence through the existing authenticated worker/control-plane submission path; verify workers never mutate Firestore directly and repeated accepted submissions are idempotent.
- [ ] 0.8 Implement the local VLC/date-range query and verify every returned destination has at least one supporting date inside the requested inclusive interval plus source/provenance/freshness.
- [ ] 0.9 Run a clean-store local/emulated E2E: real VLC source observation -> worker -> SQLite/outbox as applicable -> authenticated submit -> central persistence -> REST query; verify provider failure is not interpreted as a negative route result.
- [ ] 0.10 Run one timestamped live smoke against the selected real source, record sanitized evidence, and document source limitations without making the live network call a deterministic CI requirement.
- [ ] 0.11 Publish reproducible local-run instructions and an iteration handoff proving the VLC Route/Date Discovery POC acceptance criteria; stop for independent external review before cloud deployment resumes.

## 1. Domain and persistence after VLC POC

- [ ] 1.1 Complete the broader airport, carrier, route, source, scan, and route-evidence domain contracts aligned with INV-ROUTE-001/002 and INV-EVIDENCE-001; verify unit tests cover canonical identity and invalid endpoint cases.
- [ ] 1.2 Generalize storage boundaries and central topology persistence beyond the narrow VLC slice while preserving evidence history and idempotent ingestion.
- [ ] 1.3 Add configurable origins with VLC/CDT/ALC/MAD/BCN defaults and verify configuration override tests.

## 2. Formal identity/evidence checks

- [ ] 2.1 Add/update the Alloy route/evidence model for applicable invariants, document finite scopes, and verify the checker finds no counterexample in the committed configuration.
- [ ] 2.2 Decide from measured complexity whether canonical route identity merits Dafny; if yes implement/verify it and Java integration, otherwise document why normal Java/property tests are sufficient.

## 3. Topology provider expansion

- [ ] 3.1 Generalize the source-agnostic topology-provider contract from POC evidence without collapsing schedule availability or fares into topology.
- [ ] 3.2 Research additional authoritative topology sources for the configured origins only after the VLC POC and deployed POC are accepted.
- [ ] 3.3 Add further topology adapters only from explicit reviewed scope; every adapter requires sanitized fixtures/parser tests and separately classified live evidence.

## 4. Geographic candidate generation

- [ ] 4.1 Add a documented airport coordinate dataset/import path and verify all default origins have valid coordinates.
- [ ] 4.2 Implement deterministic geographic-distance candidate queries and verify known-distance fixtures plus the rule that distance alone never creates a ground-transport edge.

## 5. Integration and closeout

- [ ] 5.1 Run broader topology ingestion end-to-end through the worker/control-plane boundary into a clean emulated/local central store, verify repeated submissions attach to stable route identity, and verify provenance can be queried.
- [ ] 5.2 Run Maven tests, relevant Alloy/Dafny checks, strict OpenSpec validation, and Graphify/Serena impact review before marking the full change complete.

## Milestone ordering constraint

After tasks 0.1–0.11 receive independent **PASS / VLC ROUTE-DATE POC COMPLETE**, pause this change and return to `add-distributed-control-plane` tasks 4.1–4.5 for the deployed POC. Do not automatically continue to topology expansion tasks 1.x/3.x/4.x.
