# Proposal

## Why

AirScan starts as an empty repository but will be developed primarily by coding agents. Before product features are implemented, the repository needs a reproducible Java baseline, agent navigation workflow, specification workflow, formal-verification entry points, and deterministic quality gates so later workers do not invent incompatible conventions.

## What Changes

- Bootstrap the Java/Maven/Quarkus repository structure without implementing travel-provider features.
- Pin supported tool/runtime versions after verifying current stable releases.
- Initialize OpenSpec agent workflow for the orchestrator and workers.
- Verify Graphify and Serena can inspect the repository.
- Establish formal-tool directories and executable checks for Alloy, TLA+/TLC, and Dafny.
- Establish deterministic build/test/validation commands and minimal CI.
- Define generated-source policy for Dafny-to-Java output.
- Establish local configuration/secrets and fixture-sanitization conventions.

## Capabilities

### New Capabilities

None. This change establishes development tooling and repository structure only.

### Modified Capabilities

None.

## Impact

This change affects repository structure, build configuration, developer/agent tooling, CI, and formal-verification scaffolding. It intentionally does not implement route discovery, crawling, fare search, or dashboard behavior.
