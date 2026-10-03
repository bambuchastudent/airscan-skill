# Design

## Context

See proposal.md. AirScan is greenfield and agent-driven, so the first implementation risk is inconsistent tooling and architecture rather than product behavior.

## Goals / Non-Goals

Goals:

- a reproducible Java 25/Maven baseline;
- a stable Quarkus 3.x release pinned only after verification;
- explicit module boundaries that do not over-modularize the empty project;
- working OpenSpec, Graphify, and Serena workflows;
- runnable formal-method toolchain entry points;
- deterministic CI evidence.

Non-goals:

- airline/airport scraping;
- price providers;
- persistence domain schema beyond what bootstrap tooling requires;
- dashboard implementation;
- full formal models.

## Decisions

### Java and framework

Use Java 25 LTS and Maven. Select the current stable Quarkus 3.x release during apply after checking official release metadata. Do not use beta/preview Quarkus releases.

### Initial repository shape

Start with the minimum number of Maven modules justified by real boundaries. Do not pre-create one module per future capability. A reasonable initial split is a backend application plus a verified-core integration boundary only when Dafny output exists.

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
- [Premature multi-module complexity] -> create only modules needed for bootstrap boundaries.
- [Formal tools become ceremonial] -> each formal tool gets a minimal executable gate and later product changes must reference invariant IDs when they use it.
- [Agent-specific setup churn] -> prefer vendor-neutral OpenSpec skills where possible and keep root AGENTS.md authoritative.

## Migration Plan

Greenfield repository; no migration is required.
