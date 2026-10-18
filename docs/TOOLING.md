# Development Tooling

AirScan is intentionally optimized for agent-assisted development with deterministic and formal guardrails.

## 1. OpenSpec — planning and behavior contracts

OpenSpec owns the lifecycle of product changes.

Use the current spec-driven workflow:

`proposal -> specs + design -> tasks -> apply -> archive`

The repository uses `openspec/config.yaml` for project context and rules.

Expected change shape:

```text
openspec/changes/<change-name>/
  .openspec.yaml
  proposal.md
  design.md
  tasks.md
  specs/<capability>/spec.md
```

A pure tooling/docs/refactor change may set `skip_specs: true`.

Before implementation:
- inspect pending changes;
- validate the selected change;
- read its proposal, spec delta, design, and tasks;
- do not mix unrelated changes into the same apply session.

Use OpenSpec's installed skills/commands rather than copying old legacy instructions. The repository intentionally does not use legacy `openspec/project.md` or `openspec/AGENTS.md`.

## 2. Project-local memory and evidence

Agent work must remain reviewable without chat history.

Maintain:
- `docs/agent/MEMORY.md` — concise current state and durable decisions;
- `docs/agent/WORKLOG.md` — append-only changes and verification evidence;
- `docs/agent/iterations/` — per-iteration handoff summaries;
- `docs/reviews/` — self-contained external review packages.

A task is not complete merely because code exists. Its evidence and handoff record are part of completion.

Do not store credentials, personal data, raw chat transcripts, or unverified speculation in project memory.

## 3. Graphify — repository knowledge graph

Purpose: narrow architecture discovery and change impact before editing.

Expected use:
- build/refresh the local code graph;
- locate affected concepts and symbols;
- inspect dependency paths;
- refresh after structural changes.

Graphify is development tooling only. It must not become a production dependency.

Because CLI details may change, the worker must confirm the installed version and `--help` output instead of assuming command syntax.

Record meaningful impact findings in the iteration summary/review report when they affect review scope.

## 4. Serena — semantic IDE/MCP

Purpose: give coding agents semantic project navigation and targeted editing.

Preferred operations:
- locate symbols;
- locate references;
- inspect symbol context;
- refactor or edit at symbol granularity.

Use Serena over broad repository reads where possible. Use exact text search for non-semantic assets such as migrations, YAML, SQL, fixtures, and literal URLs.

The bootstrap change must verify Serena can activate the repository and understand the chosen Java/Maven layout.

Record relevant semantic/reference findings in review evidence when used to justify blast radius.

## 5. Alloy — bounded domain invariant checking

Use Alloy for compact structural models, not framework simulation.

Initial targets:
- route identity;
- airport/route relationships;
- evidence/provenance relationships;
- route lifecycle consistency;
- snapshot relationships;
- retention safety properties.

Keep scopes explicit and record them with model-check results. A successful run means no counterexample was found within the checked scope.

## 6. TLA+ / TLC — concurrency/state-machine checking

Use TLA+/PlusCal where action ordering matters.

Initial target: durable scanning and snapshot publication.

Model:
- queued jobs;
- leases;
- running work;
- retry wait;
- success/failure;
- lease expiry/recovery;
- snapshot publication.

Check safety properties first, then useful liveness properties with bounded/finite model configurations.

Do not use TLA+ for ordinary entity CRUD.

## 7. Dafny — verified deterministic core

Use Dafny selectively for pure logic with stable contracts.

Candidate functions:
- canonical route identity;
- merge of route evidence;
- route lifecycle transition;
- time-window membership;
- candidate distance filtering;
- top-N cheapest selection;
- deterministic candidate pruning.

The normal verification command family is `dafny verify`. Java generation should use the supported Java translation/build workflow discovered from the installed Dafny version. Generated Java is treated as build output or generated source and is never manually edited.

Avoid unverified assumptions. If an axiom/extern is necessary, document it against an invariant ID and audit it explicitly.

## 8. Runtime stack

### Backend

- Java 25 LTS
- Maven
- current stable Quarkus 3.x pinned by the bootstrap change
- Java HTTP client or another explicitly justified client
- jsoup for HTML parsing when useful
- Jackson for JSON
- Flyway or an equivalent explicit migration tool selected during bootstrap
- JUnit 5
- AssertJ

Do not choose Quarkus beta/preview releases for the baseline.

Prefer explicit SQL/repository boundaries over a large ORM unless a design document justifies the ORM.

### Persistence

AirScan has two persistence tiers plus worker-local state.

**Central operational/materialized state:** Cloud Firestore is the preferred Google-first implementation. It stores authoritative route/job/snapshot state and query-friendly materializations. Designs must minimize hot documents/index fanout and use transactions only where invariants require them.

**Historical/archive state:** Cloud Storage is preferred for compressed immutable scan/fare batches and sanitized retained source artifacts. Do not spend Firestore document writes on large raw history when an append-only object segment is sufficient.

**Worker-local state:** SQLite is used for cache, spool/outbox, offline recovery, and local development. It is not the authoritative distributed database.

Worker SQLite design must address WAL/busy-timeout behavior, short transactions, crash-safe outbox semantics, migrations, and rebuildability of derived cache. Central designs must address Firestore contention, idempotent ingestion, query/index cost, archive lifecycle, and emulator-based deterministic tests where practical.

### Frontend

- TypeScript
- Vite
- MapLibre GL JS
- Firebase Hosting as the preferred static host/CDN

Do not add React/Vue/etc. until UI complexity demonstrates a need.

Initial map views:
- origin: VLC/CDT/ALC/MAD/BCN;
- horizon: 7/90/180 days;
- top 10 cheapest observed direct destinations per origin;
- observation/snapshot freshness;
- route detail including airline, date, price, and ground-neighborhood information when known.

### Interfaces

Primary machine interface: versioned REST/OpenAPI.

Additional interfaces:
- CLI with JSON output for local operations/automation;
- MCP adapter over the same application services;
- authenticated worker HTTPS API for lease/heartbeat/result submission.

Do not duplicate business rules in MCP handlers.

### Google-first deployment

Preferred low-cost topology:

- Firebase Hosting — static UI;
- Cloud Run — REST/OpenAPI control plane and MCP;
- Firestore — authoritative operational/materialized data;
- Cloud Storage — compressed append-only archives;
- Cloud Scheduler — a small number of wake-up/plan triggers;
- Cloud Run Jobs — optional cloud crawler workers;
- local/other-host workers — the same HTTPS worker protocol.

Firebase Hosting may rewrite ordinary API calls to Cloud Run. Do not depend on Hosting rewrites for long-lived MCP transports; use the direct Cloud Run endpoint when transport duration/streaming requires it.

Cloud deployment may use billing-enabled pay-as-you-go services while remaining inside no-cost quotas at hobby scale where possible. Configure budget alerts and measure usage before increasing scan frequency.

### Ground routing

Preferred initial boundary: Transitous/MOTIS-compatible provider.

The architecture must permit:
1. remote/public Transitous-style routing during early development;
2. later replacement with locally hosted MOTIS and downloaded OSM/GTFS/NeTEx data.

Geodesic distance is not a substitute for actual public-transport connectivity.

## 9. External-source adapter policy

Separate provider responsibilities:
- `RouteTopologyProvider`
- `ScheduleProvider`
- `FareProvider`
- `GroundRoutingProvider`

Names may change during design; responsibilities must remain separate.

Each network adapter needs deterministic fixtures/contract tests so parser behavior can be tested offline.

Live tests must be explicitly separated from normal deterministic CI.

Never design around an undocumented endpoint without first recording evidence, stability risk, and fallback behavior.

## 10. CI / quality gates

The bootstrap change should establish reproducible commands for:
- Maven build/test;
- OpenSpec strict validation;
- Java formatting/static analysis chosen during bootstrap;
- Alloy model check;
- TLC model check;
- Dafny verification;
- frontend type/build check once the frontend exists.

Do not add expensive tools merely to satisfy a checklist. Each quality gate must protect a stated risk.

A review report must cite observed command results, not only CI intent.

## 11. Tool-version policy

Pin versions that affect reproducibility.

For rapidly changing agent tooling (OpenSpec, Graphify, Serena), record:
- version tested;
- how to verify availability;
- any required project initialization;
- update procedure.

Before changing a pinned version, create an explicit small change or dependency update with deterministic verification.

## Current verified bootstrap tooling — 2026-10-18

Java 25 Temurin, Maven Wrapper 3.9.11, Quarkus 3.40.1, REST/OpenAPI, separate worker, Spotless and Checkstyle, OpenSpec 1.14.0, Alloy 6.2.0, TLC 1.8.0, Dafny 4.11.0 including generated Java execution, and Serena MCP semantic lookup are VERIFIED in CI [39126546579](https://github.com/bambuchastudent/airscan-skill/actions/runs/39126546579) at `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd`.

Serena is pinned to upstream commit `d9e4c796fae0207ba6848d1df4b331122a5567b5`. Install with `python -m pip install 'git+https://github.com/oraios/serena.git@d9e4c796fae0207ba6848d1df4b331122a5567b5'`; verify with `kotlin scripts/serena-semantic-smoke.main.kts`.

Graphify **VERIFIED**: exact pin `graphifyy==0.9.92`, install `python -m pip install 'graphifyy==0.9.92'`. Graph command: `kotlin scripts/graphify-shared-cache.main.kts --root . --cache-dir /tmp/airscan-shared-graph --output graphify-evidence`. In CI [39126546579](https://github.com/bambuchastudent/airscan-skill/actions/runs/39126546579) at `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd`, Graphify found 27 nodes, 47 edges, symbols `BootstrapResource` and `BootstrapMarker`, and extracted the `calls` relationship `BootstrapResource.status() -> BootstrapMarker.status()` (source `BootstrapResource.java:L17`, AST, INFERRED 0.85). Cold cache `false`, warm cache `true`, changed Java input `false` (correct invalidation). Full graph rebuild on changed Java source is accepted; **per-file incremental update is not implemented**.

Final independent technical reviewer accepted implementation at `ca8daf41` and found only evidence/process gaps. Canonical review: `docs/reviews/bootstrap-agentic-foundation/2026-10-18T-final-review.md`. Evidence-closeout documentation is being committed; **CI on documentation-only HEAD and final metadata-only review remain pending**. No PR merge or downstream implementation until final PASS.

## Historical bootstrap remediation notes

Earlier bootstrap iterations encountered failed Serena CLI flags, unpinned Serena upstream, and runtime failures. Chronological evidence is preserved in `docs/agent/WORKLOG.md`; historical failures do not describe current state. Node 20 deprecation, mutable GitHub action major tags and missing formal-tool binary checksums are follow-up supply-chain hardening, not part of this remediation.
