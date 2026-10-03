# Tasks

## 1. Runtime and build baseline

- [ ] 1.1 Verify the local JDK and current stable Quarkus 3.x release, pin reproducible Java 25/Maven/Quarkus versions, and verify a clean Maven build exits successfully.
- [ ] 1.2 Create the minimal Maven/Quarkus project structure justified by the design and verify the application test suite starts from a clean checkout.
- [ ] 1.3 Establish formatting/static-analysis and dependency-version conventions, documenting commands in docs/TOOLING.md and verifying each command exits successfully.

## 2. Agent/spec tooling

- [ ] 2.1 Initialize/update OpenSpec for the repository using the current supported skills workflow and verify strict validation succeeds for the pending changes.
- [ ] 2.2 Configure and test Graphify against the repository, record the tested version/setup in docs/TOOLING.md, and verify it can locate at least one Java symbol after bootstrap code exists.
- [ ] 2.3 Configure and test Serena project activation, record the tested version/setup in docs/TOOLING.md, and verify semantic symbol/reference lookup works on the bootstrap Java code.

## 3. Formal toolchain

- [ ] 3.1 Add an Alloy toolchain smoke model/check and verify the configured checker finds no counterexample in the declared test scope.
- [ ] 3.2 Add a finite TLA+/TLC smoke specification/config and verify TLC completes successfully.
- [ ] 3.3 Add a minimal Dafny verified function plus Java translation/build integration, verify Dafny succeeds, and verify generated Java is not manually maintained.
- [ ] 3.4 Update formal/INVARIANTS.md only as needed for toolchain conventions and verify all referenced files/check commands exist.

## 4. Repository safety, evidence, and CI

- [ ] 4.1 Add gitignore/local-config conventions for SQLite runtime files, generated output, secrets, caches, and agent-tool caches; verify no secret/example runtime artifact is tracked.
- [ ] 4.2 Verify the project-local evidence workflow: MEMORY.md, append-only WORKLOG.md, per-iteration summaries, and review-report location/template; document exact completion rules in AGENTS.md.
- [ ] 4.3 Add minimal CI that runs the deterministic bootstrap gates available in the CI environment and verify the workflow configuration parses and the local equivalents pass.
- [ ] 4.4 Run the complete documented bootstrap verification set plus strict OpenSpec validation, append exact results to WORKLOG.md, update MEMORY.md with verified versions/decisions, and create a bootstrap implementation iteration summary.
- [ ] 4.5 Create a self-contained review report for the bootstrap implementation commit range; do not start add-route-topology until the review has no blocking NEEDS_CHANGES findings.
