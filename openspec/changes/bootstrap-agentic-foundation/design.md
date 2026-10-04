# Design

## Context

See proposal.md. AirScan is greenfield and agent-driven, so the first implementation risk is inconsistent tooling and architecture rather than product behavior.

## Goals / Non-Goals

Goals:

- a reproducible Java 25/Maven baseline;
- a stable Quarkus 3.x release pinned only after verification;
- explicit runtime boundaries for a hosted control plane and portable crawler worker without over-modularizing the empty project;
- stable interface direction: REST/OpenAPI primary, CLI/JSON for local automation, MCP as an adapter;
- working OpenSpec, Graphify, and Serena workflows;
- runnable formal-method toolchain entry points;
- deterministic CI evidence.

Non-goals:

- airline/airport scraping;
- price providers;
- production Firestore/Cloud Storage provisioning;
- production crawler/job protocol implementation;
- dashboard implementation;
- full formal models.

## Decisions

### Java and framework

Use Java 25 LTS and Maven. Select the current stable Quarkus 3.x release during apply after checking official release metadata. Do not use beta/preview Quarkus releases.

### Initial repository shape

Start with the minimum number of Maven modules justified by real boundaries. Do not pre-create one module per future capability.

The bootstrap architecture must preserve two real runtime roles:

- **control plane** — hosted API/query/MCP application, later targeted at Cloud Run;
- **crawler worker** — portable execution process that can run locally or as a cloud job.

Shared domain/application code may live in a common module if that reduces duplication. Bootstrap does not provision Google Cloud resources.

### Interface and cloud boundary

REST/OpenAPI is the primary machine-facing contract. CLI/JSON is the primary local/script interface. MCP will be a thin adapter over the same application services after stable query behavior exists.

Preferred hosted architecture:

- Firebase Hosting for static UI;
- Cloud Run for REST/OpenAPI control plane and MCP;
- Firestore for authoritative operational/materialized state;
- Cloud Storage for append-only compressed history;
- SQLite on workers for local cache/spool/outbox;
- Cloud Scheduler and Cloud Run Jobs only where later changes justify them.

Bootstrap only establishes code/configuration boundaries that do not make this deployment difficult. It must not require live cloud credentials to build or test.

### Agent navigation

Graphify is used for repository impact discovery; Serena is used for semantic code navigation and targeted edits. Both are development-only tools.

The apply phase must record tested versions/setup instructions in docs/TOOLING.md.

### Formal tools

Create executable, minimal smoke checks:

- Alloy: one small model/check proving the tool invocation works;
- TLA+/TLC: one finite model/check proving TLC execution works;
- Dafny: one verified pure function and Java translation/build smoke test.

These are toolchain checks, not product proofs.

### Dafny generated code

Dafny sources are authoritative. Generated Java lives in a clearly marked generated-source/output location and is never manually edited. CI verifies Dafny before consuming generated output.

### Secrets and fixtures

Local secrets stay outside git. Recorded HTTP fixtures must be sanitized before commit. No provider credentials are required by this bootstrap change.

## Risks / Trade-offs

- [Tool installation differences across machines] -> document exact verified versions and checks; fail clearly when optional development tools are unavailable.
- [Premature multi-module complexity] -> create only modules needed for the control-plane/worker/shared boundaries.
- [Cloud lock-in leaks into domain] -> keep Firestore/Cloud Run/Firebase behind deployment/storage adapters and test application logic without cloud credentials.
- [Free-tier assumptions become architecture] -> treat quotas as cost constraints to measure, not correctness assumptions; archive bulk history outside Firestore.
- [Formal tools become ceremonial] -> each formal tool gets a minimal executable gate and later product changes must reference invariant IDs when they use it.
- [Agent-specific setup churn] -> prefer vendor-neutral OpenSpec skills where possible and keep root AGENTS.md authoritative.

## Migration Plan

Greenfield repository; no migration is required.
