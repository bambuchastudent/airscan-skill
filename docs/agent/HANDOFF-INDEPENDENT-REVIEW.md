# AirScan bootstrap — independent reviewer handoff

## Ownership
Implementer prepares this handoff and evidence; a **different agent** performs the independent audit. The implementer has **not** issued an independent PASS verdict. Do not merge until the reviewer explicitly accepts the implementation.

## Repository and scope
- Repository: https://github.com/bambuchastudent/airscan-skill
- PR: https://github.com/bambuchastudent/airscan-skill/pull/1
- Source branch: `ci/bootstrap-audit-gates`; target: `develop`
- Baseline: `d424d400c846f574a729d430f492b932396cd444`
- Active OpenSpec change: `openspec/changes/bootstrap-agentic-foundation`
- **Review the latest branch HEAD at audit time**, not the SHA in this handoff. Record exact reviewed SHA and `git diff` against baseline.

## Evidence and documents
- Canonical task plan: `openspec/changes/bootstrap-agentic-foundation/tasks.md`
- Architecture/agent policy: `AGENTS.md`
- Toolchain: `docs/TOOLING.md`
- Invariants: `formal/INVARIANTS.md`
- Append-only evidence: `docs/agent/WORKLOG.md`
- Current state: `docs/agent/MEMORY.md`
- Iteration summary: `docs/agent/iterations/bootstrap-2026-10-18.md`
- Implementer pre-audit findings: `docs/agent/reviews/bootstrap-audit-2026-10-18.md` (NOT independent audit)
- Latest fully observed green CI before final documentation: https://github.com/bambuchastudent/airscan-skill/actions/runs/39124935556 (8/8 PASS at `733714ab675ac744bf5ec1fb46c76dffb0bbd44f`)
- Later CI after pinning Serena: https://github.com/bambuchastudent/airscan-skill/actions/runs/39125231439 (check final conclusion and whether it covers review HEAD)

## Independent review checklist
1. Verify actual HEAD, base commit, PR diff, workflow triggers and branch protection; do not trust descriptions alone.
2. Verify `./mvnw -B -ntp spotless:check`, `./mvnw -B -ntp checkstyle:check`, `./mvnw -B -ntp clean verify`, and `./mvnw -B -ntp -f worker/pom.xml clean verify`. Check actual test XML and runtime exceptions.
3. Run `openspec validate bootstrap-agentic-foundation --strict` and `openspec validate --changes --strict`. Compare checked tasks against implementation evidence.
4. Review all CI jobs and logs: java-and-contract, openspec, graphify-java, serena-java, java-symbol-smoke, alloy-bootstrap, tlc-bootstrap, dafny-bootstrap. Verify Serena uses actual MCP `find_symbol` and `find_referencing_symbols`, not only lexical grep.
5. Verify Serena upstream commit pin `d9e4c796fae0207ba6848d1df4b331122a5567b5` is resolvable and works in CI. Check version provenance and dependency safety.
6. Check Graphify cache correctness: cold build, warm hit, invalidation on changed source, concurrency and shared-directory safety. Do not claim per-file incremental updates without evidence.
7. Verify formal model scope and `formal/INVARIANTS.md` consistency. Bootstrap bounded smokes do not prove future AirScan product invariants.
8. Inspect `.gitignore`, tracked files, credentials, SQLite/target artifacts, cache persistence and GitHub Actions cache poisoning exposure.
9. Check `docs/agent/WORKLOG.md`, `docs/agent/MEMORY.md`, iteration summary and docs for stale contradictory current-state claims.
10. Confirm no prohibited product feature scope (adapters/crawler/search/dashboard/ground routing) was implemented.

## Reviewer output contract
Create a **separate independent review report** containing reviewed SHA, base SHA, exact commands/runs, findings with severity and file/line evidence, unresolved risks, OpenSpec task consistency, and final `PASS` or `NEEDS_CHANGES` verdict. Do not rewrite the implementer's pre-audit as your own review. If blockers exist, report them without silently fixing implementation. No merge before explicit independent PASS and green CI on the reviewed SHA.

## Known limits to test, not assume away
Graphify currently rebuilds the full AST graph on any changed source; no proven per-file incremental cache. Serena index persistence across ephemeral CI runners is not established. Tool download caching is configured but speedup depends on cache hits. Workflow cancel-in-progress is enabled. PR branch remains unmerged.
