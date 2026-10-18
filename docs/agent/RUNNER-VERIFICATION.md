# GitHub runner verification policy

The canonical deterministic bootstrap environment is GitHub Actions on `ubuntu-24.04`. Local execution is optional; no developer Mac installation is required to claim CI verification.

## Evidence rules

- A workflow definition or successful installation is not evidence that a product gate passed.
- Record the commit SHA, workflow run URL, exact command, outcome, and version output in `docs/agent/WORKLOG.md` after observing a completed run.
- Missing project inputs are `NOT VERIFIED`, not PASS.
- Graphify and Serena are development-agent tools. Their CI smoke checks must demonstrate actual Java symbol/reference navigation before being called PASS.
- Formal gates (Alloy, TLC, Dafny) must execute bounded checks and retain logs; a smoke check is not proof of AirScan domain invariants.
- Do not mark bootstrap tasks complete or declare READY_FOR_EXTERNAL_REVIEW until all required gates have observed evidence.

## Current migration status

GitHub Actions includes Java 25 and OpenSpec entry points. Maven only executes after a root `pom.xml` exists. Alloy, TLC, Dafny, Graphify, and Serena gates remain pending. Their absence is explicitly not a successful check.
