# Bootstrap remediation iteration — 2026-10-18

## Goal
Remediate independent audit of baseline d424d400c846f574a729d430f492b932396cd444, without starting product changes.

## Audit findings addressed in code
- Upgrade Quarkus from 3.27.2 to supported 3.40.1 LTS (official release history: https://quarkus.io/releases/).
- Java 25 Surefire `--add-opens java.base/java.lang=ALL-UNNAMED` for JBoss Threads access (upstream issue https://github.com/quarkusio/quarkus/issues/47769).
- Replace direct resource invocation with HTTP REST Assured checks for status and JSON OpenAPI.
- Add independent `worker/` Maven artifact with plain Java main; no REST, cloud or crawler dependencies. The root project remains the control-plane bootstrap. Shared domain logic is not introduced or duplicated.
- Add pinned Maven 3.9.11 bootstrap launcher `mvnw`; its executable bit and Windows wrapper have not been independently validated.
- CI now invokes launcher, root and worker Maven clean verify, fails on observed background exception signatures, validates all OpenSpec changes.

## Verification matrix (observed only)
| Gate | Status | Evidence |
|---|---|---|
| Prior Java environment | VERIFIED | https://github.com/bambuchastudent/airscan-skill/actions/runs/39113553968 |
| Prior strict bootstrap OpenSpec | VERIFIED | same run, at earlier commit |
| Quarkus 3.40.1 runtime | NOT VERIFIED | new CI run not yet inspected |
| Maven clean verify | NOT VERIFIED | new CI run not yet inspected |
| REST HTTP | NOT VERIFIED | test added, not executed |
| OpenAPI HTTP | NOT VERIFIED | test added, not executed |
| Worker build | NOT VERIFIED | independent Maven artifact added, not executed |
| Maven wrapper | NOT VERIFIED | launcher added, no successful invocation evidence |
| OpenSpec all changes | NOT VERIFIED | CI command added, no output |
| Formatting/static | NOT VERIFIED | pending |
| Graphify | NOT VERIFIED | pending tool discovery/actual symbol output |
| Serena | NOT VERIFIED | pending activation/reference evidence |
| Alloy | NOT VERIFIED | pending smoke model/check |
| TLC | NOT VERIFIED | pending finite spec/check |
| Dafny | NOT VERIFIED | pending verified Java generation/build |
| Security | NOT VERIFIED | ignore patterns exist, tracked secret scan not performed |

## Decisions and risks
No distributed-control-plane behavior was implemented. This iteration is **not ready for external review** under the requested acceptance criteria. Root and worker are independent Maven artifacts rather than a reactor; a future bootstrap-only integration check must validate both. Maven launcher is a minimal version-pinned shell bootstrap, not the stock Apache Maven Wrapper implementation; requires follow-up for standard wrapper compliance. CI logs must be inspected for runtime exceptions and tests actually executed. No task checkboxes changed.

## Next action
Observe fresh Actions run, fix any build/test failure, then implement and verify formal/agent tooling and static checks before an implementation review can be READY_FOR_EXTERNAL_REVIEW.
