# Iteration summary — initial AirScan specification

## Goal

Turn an empty repository into a reviewable project definition for a local-first low-cost route/fare crawler and establish agent-development rules before implementation begins.

## Starting context / assumptions

- Repository was empty.
- Default branch is `develop`.
- Desired production implementation is Java, not Python.
- Initial origins: VLC, CDT, ALC, MAD, BCN.
- Agent tooling should combine OpenSpec, Graphify, Serena, Alloy, TLA+/TLC, and Dafny without requiring every formal tool for every feature.

## Changes made

Prepared repository artifacts for:
- project/product description;
- mandatory agent workflow;
- formal invariant catalogue;
- OpenSpec project configuration;
- bootstrap tooling change;
- route-topology change;
- durable scanning/retention change;
- project-local memory, append-only work log, iteration summaries, and external review reports.

## Explicitly not changed

- No Java application code exists yet.
- No Quarkus version has been pinned yet.
- No database schema has been implemented yet.
- No crawler/provider has been implemented.
- No live source/API endpoint has been adopted.
- No formal model has been executed yet.

## Verification evidence

- GitHub repository metadata inspected.
- Repository confirmed empty before README initialization.
- Default branch confirmed as `develop`.
- README initialization commit confirmed by GitHub API.

OpenSpec/Graphify/Serena/formal tool commands have not yet run and must not be reported as verified.

## Formal verification

- Alloy: not run.
- TLA+/TLC: not run.
- Dafny: not run.

## Decisions

- GLM is the implementation orchestrator.
- Worker agents receive narrow implementation tasks.
- External ChatGPT acts as architect, planner, and independent reviewer.
- OpenSpec changes should be applied in dependency order.
- Formal methods are selective: Alloy for structural invariants, TLA+ for concurrency, Dafny for pure deterministic core logic.
- Every meaningful iteration must leave deterministic evidence and a handoff summary.

## Risks / unresolved questions

- Tool version compatibility must be confirmed locally.
- Source/provider access must be researched before implementation.
- Formal-tool integration must demonstrate useful checks rather than become ceremonial overhead.

## Relevant artifacts

- `README.md`
- `AGENTS.md`
- `docs/TOOLING.md`
- `formal/INVARIANTS.md`
- `openspec/config.yaml`
- `openspec/changes/bootstrap-agentic-foundation/`
- `openspec/changes/add-route-topology/`
- `openspec/changes/add-durable-scan-retention/`

## Next recommended action

Apply only `bootstrap-agentic-foundation`, collect exact tool/build evidence, and submit its diff plus worklog/iteration report for independent review before beginning `add-route-topology`.
