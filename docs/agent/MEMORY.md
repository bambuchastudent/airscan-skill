# AirScan Project Memory

This file is the curated durable memory for agents working on this repository. Keep it concise and factual. Update it when durable project state changes; use WORKLOG.md for chronology.

## Current state

- Repository is in specification/bootstrap phase.
- Default branch: `develop`.
- Production implementation language: Java, not Python.
- Planned runtime baseline: Java 25 LTS, Maven, stable Quarkus 3.x selected/pinned during bootstrap.
- Initial local persistence: SQLite with explicit migrations and deliberate write-concurrency/WAL design.
- Initial map UI direction: TypeScript + Vite + MapLibre GL JS.
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

1. `bootstrap-agentic-foundation`
2. `add-route-topology`
3. `add-durable-scan-retention`

Do not mass-implement airline adapters before the first provider architecture and scan semantics are validated.

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

## Current unresolved items

- Exact stable Quarkus 3.x version must be verified/pinned during bootstrap.
- Exact tested versions/setup of OpenSpec, Graphify, Serena, Alloy, TLA+/TLC, and Dafny must be recorded by bootstrap.
- First authoritative topology adapter must be selected only after source-access research.
- First airline fare adapter must not be selected until topology/bootstrap foundations are proven.
- Concrete route-inactivity thresholds remain a future configuration/design decision.

## Next step

Apply and verify `bootstrap-agentic-foundation` only. Do not start route/fare provider implementation until its quality gates and agent tooling are reproducible.
