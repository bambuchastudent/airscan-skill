# Tasks

## 0. VLC Route/Date Discovery POC milestone

### RT-1 — airport-wide contracts, source research, fixtures

- [x] 0.1 Define canonical airport, carrier, direct-route, source/scan and route-evidence contracts for all advertised direct routes from VLC. Canonical route identity must not depend on carrier or scan timestamp; test invariants.
- [x] 0.2 Define a topology provider and a **separate** date-availability observation provider, each with typed failures and provenance.
- [ ] 0.3 Validate the public **Aena VLC airport-wide destination catalog** and document a reproducible **user-initiated rendered-DOM save/export → local file** capture workflow, observation provenance, completeness/freshness limits and local-use/legal restrictions. No unattended HTTP fetch, crawler authorization letter or recurring schedule is required for RT-1. Do not substitute Ryanair.
- [ ] 0.4 Implement a manual one-shot Java file importer; verify it against a **minimal genuine source-native Aena rendered-DOM fixture** with offline tests, source candidate reconciliation, duplicates, malformed/missing IATA, multiple/unknown listed carriers, completeness diagnostics and atomic published-snapshot preservation. Do not commit full restricted HTML; a synthetic HTML mock alone is insufficient.
- [x] 0.5 Design local topology endpoint plus inclusive date-range query with explicit CONFIRMED versus UNKNOWN/UNCOVERED semantics. Research date sources without prematurely choosing a single-airline solution.

### RT-2 — real airport-wide VLC topology

- [ ] 0.6 Implement and execute one official airport-wide VLC topology source adapter (Aena if validated); do not hardcode current destination count.
- [ ] 0.7 Ingest VLC route/evidence observations through existing authenticated worker, SQLite outbox and central idempotent control-plane submission; no worker direct Firestore writes.
- [ ] 0.8 Implement local `VLC` known-direct-route catalog REST query with carrier/source/scan/freshness provenance.
- [ ] 0.9 Clean-store local/emulated E2E, retry dedup and live smoke; compare stored route set with captured official source snapshot and explain every omission.
- [ ] 0.10 Publish RT-2 evidence and request independent topology gate review before dated provider integration.

### RT-3 — date availability over the VLC catalog

- [ ] 0.11 Select/verify dated source path(s) with lawful low-volume access, coverage and freshness boundaries; no airline source can silently stand for the full airport.
- [ ] 0.12 Implement positive dated availability observations linked to canonical VLC routes and source/scan, using the accepted worker/control-plane path.
- [ ] 0.13 Implement inclusive date-range query with CONFIRMED dates and UNKNOWN/UNCOVERED, stale, failed-source states; no false global negatives.
- [ ] 0.14 Verify local E2E, offline fixture tests and timestamped live smoke; document covered/uncovered proportion of known VLC catalog and user-facing limitations.
- [ ] 0.15 Produce reproducible local instructions and evidence; independent **PASS / VLC ROUTE-DATE POC COMPLETE** required before cloud deployment.

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

After tasks 0.1–0.15 receive independent **PASS / VLC ROUTE-DATE POC COMPLETE**, pause this change and return to `add-distributed-control-plane` tasks 4.1–4.5 for the deployed POC. Do not automatically continue to topology expansion tasks 1.x/3.x/4.x.
