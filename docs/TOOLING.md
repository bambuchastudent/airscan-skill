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

Initial local store: SQLite.

Bootstrap/design must address:
- WAL mode where appropriate;
- busy timeout;
- transaction boundaries;
- one-writer constraints;
- append-only fare observations;
- schema migrations;
- backup/rebuild strategy for derived data.

### Frontend

- TypeScript
- Vite
- MapLibre GL JS

Do not add React/Vue/etc. until UI complexity demonstrates a need.

Initial map views:
- origin: VLC/CDT/ALC/MAD/BCN;
- horizon: 7/90/180 days;
- top 10 cheapest observed direct destinations per origin;
- observation/snapshot freshness;
- route detail including airline, date, price, and ground-neighborhood information when known.

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
