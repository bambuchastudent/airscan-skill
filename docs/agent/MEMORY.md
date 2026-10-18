# AirScan Project Memory

This file is the curated durable memory for agents working on this repository. Keep it concise and factual. Update it when durable project state changes; use WORKLOG.md for chronology.

## Current state

- Repository is in specification/bootstrap phase.
- Default branch: `develop`.
- Production implementation language: Java, not Python.
- Planned runtime baseline: Java 25 LTS, Maven, stable Quarkus 3.x selected/pinned during bootstrap.
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

1. `bootstrap-agentic-foundation`
2. `add-distributed-control-plane`
3. `add-route-topology`
4. `add-durable-scan-retention`

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
- Exact worker authentication mechanism (simple project token vs Google-native identity/OIDC) must be decided in add-distributed-control-plane.
- Actual Firestore/Cloud Run/Storage region/cost must be verified at deployment; preferred first region is Madrid (`europe-southwest1`) where supported.
- First authoritative topology adapter must be selected only after source-access research.
- First airline fare adapter must not be selected until topology/bootstrap foundations are proven.
- Concrete route-inactivity thresholds remain a future configuration/design decision.

## Next step

Apply and verify `bootstrap-agentic-foundation` only. Its structure must preserve control-plane and crawler-worker runtime roles but must not provision production cloud resources. After external review, the next intended change is `add-distributed-control-plane`.


## Bootstrap implementation update (2026-10-18)

GitHub Actions on ubuntu-24.04 is the canonical verification environment. Run 39113553968 verified Java 25, Maven 3.10.0 and strict OpenSpec validation. Maven `verify` was skipped because the project did not yet have a POM. A subsequent series of commits added a Quarkus 3.27.2 POM, CDI bootstrap marker, REST/OpenAPI dependency, bootstrap status endpoint and tests. Those later commits have **no observed build/test evidence yet**. No production capability or worker protocol is implemented. Graphify, Serena, Alloy, TLC, and Dafny are pending. Continue bootstrap only; review GitHub Actions before reporting PASS.

## 2026-10-18 remediation status

Independent audit identified false-green Quarkus background runtime exceptions on Java 25. Quarkus upgraded from 3.27.2 to supported 3.40.1 LTS; Surefire receives --add-opens java.base/java.lang=ALL-UNNAMED. Real REST/OpenAPI HTTP tests and a separate no-REST worker Maven entrypoint were added. Maven 3.9.11 shell launcher and CI exception detection added. **None of these changes has yet been verified by a completed new Actions run.** Earlier CI 39113553968 verifies Java environment/OpenSpec only. Formatting, Graphify, Serena, Alloy, TLC, Dafny remain NOT VERIFIED. Bootstrap status NEEDS_CHANGES; no downstream change started.

Bootstrap formatting update: Spotless 2.43.0 with google-java-format 1.25.2 and `bash mvnw spotless:check` was added to GitHub CI; still NOT VERIFIED. Current execution environment has Java 21 and no Maven, not the canonical Java 25 runner. The standard Maven Wrapper remains outstanding; the existing `mvnw` is a custom checksum-verified shell launcher. No new CI PASS has been observed.

Follow-up 2026-10-18: Java sources/tests were adjusted toward Spotless Google format; GitHub CI now attempts independent worker main execution and a tracked-runtime-file/credential filename check. These changes have NOT been verified on a Java 25 CI run. Local runtime is Java 21.0.12.1, Maven absent, external Maven Central DNS unavailable. No current Actions run ID or log was retrievable. Bootstrap remains NEEDS_CHANGES.

2026-10-18 priority remediation: Spotless 2.46.1 / google-java-format 1.28.0, Quarkus quarkus-junit, Surefire @{argLine}, LogManager property, Maven launcher version sourced from properties, executable Git mode 100755. All CI results remain NOT VERIFIED, and launcher is not yet upstream Maven Wrapper. Bootstrap NEEDS_CHANGES.

## Current verified bootstrap snapshot — 2026-10-18 (supersedes older unverified notes above)

- PR #1 branch `ci/bootstrap-audit-gates` remains unmerged; only active change is `bootstrap-agentic-foundation`. Do not start product features.
- Java 25 / Quarkus 3.40.1 / Maven Wrapper 3.9.11, OpenSpec, Spotless, Checkstyle, worker tests, Alloy, TLC, Graphify Java AST and Kotlin shared cache, Dafny verification/build/Java execution passed CI [39124094544](https://github.com/bambuchastudent/airscan-skill/actions/runs/39124094544) at `d2bc2b9c8293ca616f2a0f9a12f50b17d14bff56`.
- Graphify uses `graphifyy==0.1.14` via `scripts/graphify-shared-cache.main.kts`; unchanged inputs reuse the cache, changed inputs rebuild the whole AST graph. A shared `--cache-dir` is necessary for agents in distinct worktrees.
- Serena MCP Java semantic lookup is NOT verified: latest completed CI [39124470305](https://github.com/bambuchastudent/airscan-skill/actions/runs/39124470305) failed on unsupported CLI `--headless`; fix `8a6a272` is pending CI confirmation.
- Canonical plan and completion checkboxes: `openspec/changes/bootstrap-agentic-foundation/tasks.md`. Chronological evidence: `docs/agent/WORKLOG.md`. This file is curated current state; historical NOT VERIFIED statements above refer to older commits, not this snapshot.
- Gate: NEEDS_CHANGES pending Serena, complete final SHA CI, iteration evidence and independent review.

## Latest verified snapshot (2026-10-18; supersedes prior Serena pending state)

Eight of eight CI jobs passed at `733714ab675ac744bf5ec1fb46c76dffb0bbd44f`, run 39124935556, including Serena MCP Java symbol/reference lookup. PR #1 is not merged. OpenSpec tasks are canonical. Final commit CI and independent audit remain outstanding. Serena dependency is unpinned upstream Git main; Graphify cache is not per-file incremental. Status NEEDS_CHANGES pending review.
