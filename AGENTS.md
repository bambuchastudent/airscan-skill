# AirScan Agent Instructions

These instructions apply to every AI coding agent working in this repository.

## Mission

Build AirScan as a distributed, local-friendly, deterministic travel graph and fare-observation system. LLMs may assist development and optional analysis, but the runtime route graph, scheduler, persistence, ranking, provenance, and correctness checks must not depend on an LLM.

Initial origins: `VLC`, `CDT`, `ALC`, `MAD`, `BCN`.

Primary low-cost scope: Ryanair, Vueling, easyJet, Wizz Air, Volotea. The provider model must remain extensible.

## Project roles

### GLM orchestrator

Owns execution planning inside the repository, task decomposition, delegation to workers, implementation coordination, and collection of deterministic verification evidence.

### Worker agents

Implement narrowly scoped tasks with explicit acceptance criteria. Workers must not silently expand scope.

### External architect / planner / reviewer

The external ChatGPT role for this project is architect, planner, and independent reviewer of modifications.

This role:
- reviews architecture and OpenSpec plans;
- challenges assumptions and methodology;
- reviews diffs, test/model-check evidence, and worker reports;
- separates measured facts from assumptions;
- recommends the next smallest justified change;
- may request rework when evidence does not support a conclusion.

The external reviewer is not the default production-code executor.

## Mandatory workflow

1. Read `README.md`, `docs/TOOLING.md`, `docs/agent/MEMORY.md`, `formal/INVARIANTS.md`, and relevant OpenSpec artifacts.
2. Inspect current OpenSpec state before planning implementation.
3. For new behavior, create/update an OpenSpec change before production code.
4. Before modifying existing code, update/query Graphify when available to identify dependencies and impact.
5. Use Serena when available as the primary semantic navigation/editing interface: symbols, references, targeted refactors.
6. Check whether the change creates or modifies an invariant in `formal/INVARIANTS.md`.
7. Delegate narrow tasks when worker agents are available. Give each worker exact scope and deterministic acceptance criteria.
8. Verify with commands/tests/model checkers. Do not accept "should pass" or an LLM judgment as evidence.
9. Review the actual diff and command results before marking an OpenSpec task complete.
10. Update the project evidence trail described below.
11. Keep changes small enough to review and revert.

## Mandatory project memory and evidence trail

Every implementation/review iteration must leave enough local context for another agent or human reviewer to continue without reading the original chat.

### Durable project memory

Maintain:

`docs/agent/MEMORY.md`

This is a compact, curated current-state memory for the repository. Update it when durable project facts change.

It should contain:
- current architecture decisions;
- active OpenSpec changes and dependency order;
- important source/API findings;
- verified tool versions;
- known constraints;
- unresolved risks;
- current next step.

Do not use it as a chronological log. Do not put credentials, personal data, raw chat transcripts, or speculative claims in it.

### Append-only work log

Maintain:

`docs/agent/WORKLOG.md`

Append an entry for every meaningful change/review iteration. Each entry must record:
- UTC timestamp;
- actor/agent role;
- OpenSpec change/task IDs;
- commit or commit range when available;
- files/components changed;
- intent;
- deterministic verification commands/checks actually run;
- result of each check;
- formal-model results and bounds/configuration where applicable;
- known failures or skipped checks with reason;
- next step.

Do not rewrite older entries except to correct an explicit factual mistake; corrections must be annotated.

### Iteration summaries

Create one short handoff summary per meaningful iteration under:

`docs/agent/iterations/YYYY-MM-DDTHHMMZ-<slug>.md`

Each summary must contain:
- goal;
- starting assumptions/context;
- what changed;
- what did not change;
- evidence;
- decisions made;
- unresolved risks/questions;
- next recommended action;
- links/paths to relevant OpenSpec artifacts and review reports.

These summaries are optimized for agent-to-agent handoff.

### Review reports

For every completed OpenSpec change, and whenever an external review is requested, create or update a report under:

`docs/reviews/<change-name>/YYYY-MM-DDTHHMMZ-review.md`

A review report must be understandable without chat history and include:
- reviewed scope and commit range;
- relevant specs/invariants;
- summary of implementation;
- spec-conformance findings;
- deterministic verification evidence;
- formal verification evidence;
- regression/blast-radius notes from Graphify/Serena where applicable;
- source/API risk notes;
- unresolved concerns;
- review status: `READY`, `READY_WITH_FOLLOWUPS`, or `NEEDS_CHANGES`;
- exact follow-up actions.

Never mark a review READY solely from worker narrative. Base it on repository state and evidence.

### Completion rule

An OpenSpec task is not complete until:
- its deterministic verification has run;
- the work log entry exists;
- the current project memory is updated if durable facts changed;
- the iteration summary exists.

An OpenSpec change is not ready for archive until a review report exists.

## OpenSpec policy

OpenSpec is the source of truth for planned product behavior.

Use the current spec-driven flow:

`proposal -> specs + design -> tasks -> apply -> archive`

Rules:

- Specs describe observable behavior and constraints.
- Implementation details belong in `design.md` or `tasks.md`.
- Use one coherent capability per spec boundary.
- Pure tooling/docs/refactor changes may use `skip_specs: true`.
- Do not start mass implementation from a proposal alone.
- Apply pending changes in dependency order.
- Run OpenSpec validation before implementation and before archive.

Current intended order:

1. `bootstrap-agentic-foundation`
2. `add-distributed-control-plane`
3. `add-route-topology`
4. `add-durable-scan-retention`

## Repository navigation policy

### Graphify

Graphify is the repository knowledge/impact graph, not a runtime dependency.

Before an existing-code change:
- refresh the graph using the installed Graphify workflow;
- locate affected concepts/symbols;
- inspect dependency paths and likely blast radius.

After meaningful structural changes, refresh it again.

Do not repeatedly read the whole repository when the graph can narrow the search.

### Serena

Serena is the preferred semantic IDE/MCP layer.

Prefer:
- symbol lookup;
- reference lookup;
- semantic navigation;
- symbol-level edits/refactors.

Avoid whole-file rewrites when a targeted edit is possible. Exact text search is still appropriate for configuration, SQL migrations, fixtures, and literal values.

## Formal methods policy

Formal tools are selective safeguards, not ceremony.

### Alloy

Use Alloy for bounded structural/domain model checks, especially identity, relationships, lifecycle consistency, provenance, and retention structure.

Never describe a successful Alloy run as an unrestricted proof; Alloy analysis is bounded.

### TLA+ / TLC

Use TLA+/PlusCal for concurrency and state-machine behavior where interleavings matter, especially durable scan jobs, leases, retries, crash recovery, and atomic snapshot publication.

Do not model ordinary sequential CRUD in TLA+.

### Dafny

Use Dafny only for high-value pure deterministic logic where contracts produce useful guarantees, such as canonical identity, evidence merge, retention transitions, candidate pruning, time-window filtering, and top-N selection.

Generated Java from Dafny must not be edited manually. Verification must run before accepting generated artifacts.

### Traceability

Every important invariant has a stable ID in `formal/INVARIANTS.md`.

Map each invariant only to tools that add value:
- Alloy mapping, if applicable;
- TLA+ mapping, if applicable;
- Dafny mapping, if applicable;
- Java/test mapping.

Do not duplicate every invariant in every formal language.

## Runtime architecture constraints

Production application code must not be Python.

Baseline direction:
- Java 25 LTS;
- Maven;
- a current stable Quarkus 3.x version pinned during bootstrap;
- two runtime roles: hosted control plane and portable crawler worker;
- Google Cloud Run as the preferred hosted REST/OpenAPI control-plane and MCP target;
- Cloud Firestore as the preferred authoritative operational/materialized store;
- Cloud Storage as the preferred append-only compressed scan/fare archive;
- SQLite on crawler workers as local cache/spool/outbox, not as the distributed source of truth;
- Firebase Hosting for the static TypeScript + Vite + MapLibre GL JS map UI;
- Transitous/MOTIS behind a replaceable ground-routing provider boundary;
- JUnit 5 + AssertJ.

Do not use preview/beta framework releases without an explicit OpenSpec design decision.

Prefer ports/adapters boundaries for external sources:
- route topology;
- schedule availability;
- fare observations;
- ground routing.

HTTP/HTML details must not leak into domain logic.

## Distributed runtime rules

- The control plane owns authoritative operational state, job/lease state, current snapshots, materialized query indexes, and publication decisions.
- Crawler workers may run on a developer machine, Cloud Run Jobs, CI runners, or other hosts reachable over HTTPS.
- Workers must not write Firestore or authoritative Cloud Storage objects directly unless a future reviewed design explicitly changes this rule.
- Workers receive work and submit results through a versioned authenticated worker protocol with idempotency keys.
- Workers may continue collecting into a local SQLite spool/outbox during temporary control-plane/network unavailability.
- REST/OpenAPI is the primary machine contract. MCP is a thin adapter over application services, not a second source of business logic.
- The static web app consumes the read/query API rather than depending on the Firestore schema directly.
- Full scan/fare history should prefer immutable compressed archive batches; Firestore should hold operational state and materialized/query-friendly summaries rather than every raw payload.
- Google Cloud is the preferred deployment, but domain/application code must not import cloud-specific concerns across architectural boundaries.
- Keep Cloud Run minimum instances at zero unless measurements justify otherwise.
- When billing is enabled, configure budget alerts and record expected cost drivers; alerts are monitoring, not a hard spending cap.

## Data rules

- Normalized facts retain provenance.
- Fare observations are append-only.
- Unknown values remain unknown; never invent metadata.
- A transient fetch/parser failure must not deactivate a route.
- Route existence and fare availability are separate concepts.
- Straight-line distance is only a candidate-generation signal; actual ground connectivity is separate.
- A partial scan must never become the current published snapshot.
- Older completed work must not replace a newer published snapshot.
- Public dashboards must expose staleness/freshness.
- Central writes from distributed workers must be idempotent and attributable to worker, lease/job, scan, and source.
- Raw browser/HTTP payloads must not be stored in Firestore merely for convenience; sanitize and archive only when retention is justified.

## Source-access rules

Prefer documented/public APIs and official airport/airline information.

When using public web pages:
- cache responsibly;
- use conditional requests when supported;
- keep per-host rate limits;
- use backoff for transient failures;
- retain parser fixtures for deterministic tests.

Do not bypass CAPTCHAs, authentication barriers, anti-bot protections, or access controls.

Browser automation may be proposed only when public information cannot be obtained through a simpler supported mechanism, and the design must document why.

## Deterministic verification

LLMs do not decide deterministic facts.

Use tools/commands for:
- build success;
- tests;
- migrations;
- formatting/static checks;
- exact file existence;
- parser fixture results;
- OpenSpec validation;
- Alloy checks;
- TLC checks;
- Dafny verification;
- git diff checks.

Report the exact evidence used.

## Agent delegation

The orchestrator owns implementation coordination and evidence collection.

Workers should receive narrow tasks such as:
- implement one adapter;
- add one migration;
- write one parser fixture suite;
- implement one verified function;
- model one state machine.

Do not delegate "implement the whole project".

Workers must not silently expand scope. If a source/API assumption is unverified, stop that task and report it as a risk rather than inventing an endpoint.

## Stop conditions

Pause implementation and surface the problem when:
- OpenSpec behavior is ambiguous;
- a source/API cannot be verified;
- a required invariant is unclear;
- formal verification exposes a counterexample;
- deterministic tests contradict the proposed design;
- a task would require bypassing access controls;
- a worker result lacks reproducible evidence.
