# Tasks

## 1. Runtime and build baseline

- [x] 1.1 Verify the local JDK and current stable Quarkus 3.x release, pin reproducible Java 25/Maven/Quarkus versions, and verify a clean Maven build exits successfully.
- [x] 1.2 Create the minimal Maven/Quarkus project structure justified by the design, preserving distinct control-plane and crawler-worker runtime roles without premature capability modules, and verify the application test suite starts from a clean checkout.
- [x] 1.3 Establish REST/OpenAPI as the primary machine interface direction and CLI/JSON as the local automation direction; keep MCP as a later thin adapter and verify bootstrap code does not duplicate business logic across interfaces.
- [x] 1.4 Establish formatting/static-analysis and dependency-version conventions, documenting commands in docs/TOOLING.md and verifying each command exits successfully.

## 2. Agent/spec tooling

- [x] 2.1 Initialize/update OpenSpec for the repository using the current supported skills workflow and verify strict validation succeeds for the pending changes.
- [x] 2.2 Configure and test Graphify against the repository, record the tested version/setup in docs/TOOLING.md, and verify it can locate at least one Java symbol after bootstrap code exists.
- [x] 2.3 Configure and test Serena project activation, record the tested version/setup in docs/TOOLING.md, and verify semantic symbol/reference lookup works on the bootstrap Java code.

## 3. Formal toolchain

- [x] 3.1 Add an Alloy toolchain smoke model/check and verify the configured checker finds no counterexample in the declared test scope.
- [x] 3.2 Add a finite TLA+/TLC smoke specification/config and verify TLC completes successfully.
- [x] 3.3 Add a minimal Dafny verified function plus Java translation/build integration, verify Dafny succeeds, and verify generated Java is not manually maintained.
- [x] 3.4 Update formal/INVARIANTS.md only as needed for toolchain conventions and verify all referenced files/check commands exist.

## 4. Repository safety, evidence, and CI

- [x] 4.1 Add gitignore/local-config conventions for worker SQLite runtime files, generated output, Google/Firebase local credentials/config, secrets, caches, and agent-tool caches; verify no secret/example runtime artifact is tracked.
- [x] 4.2 Verify the project-local evidence workflow: MEMORY.md, append-only WORKLOG.md, per-iteration summaries, and review-report location/template; document exact completion rules in AGENTS.md.
- [x] 4.3 Add minimal CI that runs the deterministic bootstrap gates available in the CI environment and verify the workflow configuration parses and the local equivalents pass.
- [x] 4.4 Run the complete documented bootstrap verification set plus strict OpenSpec validation, append exact results to WORKLOG.md, update MEMORY.md with verified versions/decisions, and create a bootstrap implementation iteration summary.
- [x] 4.5 Create a self-contained review report for the bootstrap implementation commit range; do not start add-route-topology until the review has no blocking NEEDS_CHANGES findings.

## Final implementation evidence — 2026-10-18

- **Tasks 1.1–4.5:** complete with deterministic implementation/evidence and independent review.
- Final implementation HEAD `ca8daf41ba7efe62b6287f19d9f4f2ca0bdfa0dd`; CI `39126546579`: **8/8 PASS**.
- Documentation/evidence closeout HEAD `351c6835e003a3e5863d3632d77dd92566a25b45`; CI `39127216895`: **8/8 PASS**.
- Independent final verdict: **PASS / READY**.
- PR #1 merged to `develop` as `a2e527c153f403ca47aabcec70a9460f75499909`.
- Canonical review: `docs/reviews/bootstrap-agentic-foundation/2026-10-18T-final-review.md`.
- Nonblocking future hardening: immutable action SHA pins, formal-tool distribution checksums, Serena LSP cache persistence, finer Graphify incremental updates, branch protection.
