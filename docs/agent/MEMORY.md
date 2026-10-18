# AirScan Project Memory

This file is the curated durable memory for agents working on this repository. Keep it concise and factual. Update it when durable project state changes; use WORKLOG.md for chronology.

## Current state

- Bootstrap is complete and merged; the repository is entering implementation of `add-distributed-control-plane`.
- Default branch: `develop`.
- Production implementation language: Java, not Python.
- Verified runtime baseline: Java 25 Temurin 25.0.4.1, Maven Wrapper 3.9.11, Quarkus 3.40.1.
- Preferred hosted control plane: Google Cloud Run.
- Preferred central operational/materialized store: Cloud Firestore.
- Preferred immutable history/archive: Cloud Storage compressed scan/fare batches.
- Worker-local persistence: SQLite cache/spool/outbox with offline recovery; it is not authoritative.
- Initial map UI direction: TypeScript + Vite + MapLibre GL JS on Firebase Hosting.
- Primary machine contract: REST/OpenAPI; MCP is a thin adapter over the same application services.
- Crawler workers may run locally, in Cloud Run Jobs, CI runners, or other hosts and communicate over authenticated HTTPS.
- Ground-routing direction: Transitous/MOTIS behind a replaceable provider boundary.
- Runtime must remain useful without an LLM.

## Initial origin airports

- VLC — Valencia
- CDT — Castellón
- ALC — Alicante
- MAD — Madrid
- BCN — Barcelona

The origin set must remain configurable.

## Initial low-cost provider focus

Phase 1:
- Ryanair
- Vueling
- easyJet
- Wizz Air
- Volotea

Provider architecture must remain extensible.

## Core architecture decisions

- Route topology discovery, schedule availability, fare observation, and ground routing are separate concerns/adapters.
- Route existence must not depend on current fare visibility.
- Fare observations are append-only historical evidence.
- Normalized facts retain provenance and scan identity.
- Failed/transient scans do not count as negative route evidence.
- Partial scans never become current published snapshots.
- Straight-line distance generates candidates; actual ground connectivity is separate evidence.
- Background scanning is durable/persisted rather than relying only on in-memory timers.
- Distributed workers never directly publish authoritative state or mutate Firestore; they submit idempotent result batches through the control plane.
- Firestore holds hot operational/materialized state; bulky append-only history belongs in compressed archive objects.
- Firebase/web clients consume the stable query API instead of using the Firestore schema as the domain contract.

## Agent-development model

- OpenSpec: behavior/change planning.
- Graphify: repository knowledge and blast-radius analysis.
- Serena: semantic IDE/MCP navigation and targeted edits.
- Alloy: bounded structural/domain invariant checks.
- TLA+/TLC: concurrency/state-machine checks.
- Dafny: selected pure deterministic verified core compiled/integrated with Java.
- GLM is the implementation orchestrator.
- Worker agents implement narrow tasks.
- External ChatGPT role is architect, planner, and independent reviewer of modifications.

## Required evidence trail

- `docs/agent/WORKLOG.md` — append-only verification/change log.
- `docs/agent/iterations/` — one handoff summary per meaningful iteration.
- `docs/reviews/` — self-contained review reports for completed changes/external review.

OpenSpec tasks are not complete without deterministic evidence and iteration documentation. OpenSpec changes are not ready to archive without a review report.

## Active OpenSpec changes and intended order

Completed and archived: `bootstrap-agentic-foundation`.

Active dependency order:

1. `add-distributed-control-plane`
2. `add-route-topology`
3. `add-durable-scan-retention`

Current implementation target: `add-distributed-control-plane` only. Do not mass-implement airline adapters before the first provider architecture and scan semantics are validated.

## Current invariant areas

See `formal/INVARIANTS.md`.

Initial invariant families:
- canonical route identity;
- provenance;
- append-only fare evidence;
- retention based only on completed evidence;
- unique active scan lease;
- crash recovery;
- atomic/monotonic snapshot publication;
- deterministic top-N ranking.

## Completed bootstrap baseline (2026-10-18)

- Final implementation HEAD: `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd`; implementation CI `39126546579`: **8/8 PASS**.
- Documentation/evidence closeout HEAD: `351c6835e003a3e5863d3632d77dd92566a25b45`; closeout CI `39127216895`: **8/8 PASS**.
- Independent final verdict: **PASS / READY**.
- PR #1 merged to `develop` as `a2e527c153f403ca47aabcec70a9460f75499909`.
- Verified baseline: Java 25 Temurin 25.0.4.1, Maven Wrapper 3.9.11, Quarkus 3.40.1, REST/OpenAPI, independent worker boundary, Spotless, Checkstyle, OpenSpec 1.14.0, Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0, Serena pinned at `d9e4c796fae0207ba6848d1df4b331122a5567b5`, Graphify `0.9.92`.
- Canonical review: `docs/reviews/bootstrap-agentic-foundation/2026-10-18T-final-review.md`.
- Nonblocking hardening remains: immutable GitHub Actions SHA pins, checksums for downloaded formal-tool distributions, Serena LSP cache persistence, finer Graphify incremental updates, and branch protection.

## Future design decisions (not bootstrap blockers)

- Worker authentication choice (OIDC vs project token) belongs to `add-distributed-control-plane`.
- Firestore/Cloud Run/Storage deployment region/cost requires deployment-time validation.
- First topology and fare adapters and route inactivity thresholds belong to future changes.

## Historical bootstrap remediation notes

Earlier CI failures, unverified tool statuses, Serena CLI incompatibility and mutable upstream dependency were resolved before CI 39126546579. For chronological evidence consult `docs/agent/WORKLOG.md`. This curated memory intentionally omits superseded state.
