# Bootstrap pre-audit findings — 2026-10-18

## Scope and evidence
Repository: bambuchastudent/airscan-skill; PR #1; branch ci/bootstrap-audit-gates.
Observed CI: https://github.com/bambuchastudent/airscan-skill/actions/runs/39124935556
Verified SHA: 733714ab675ac744bf5ec1fb46c76dffb0bbd44f.
All eight jobs passed, including Serena semantic references, Graphify Java AST, OpenSpec, Java/Quarkus/worker, Alloy, TLC and Dafny generated Java execution.

## Findings
- **BLOCKER (reproducibility):** CI installs Serena from mutable upstream Git main without a commit SHA. A later upstream change can alter the verification tool while repository sources are unchanged. Pin to an immutable upstream commit or version with verified provenance.
- **REVIEW REQUIRED:** Latest documentation commits are not covered by the observed green CI SHA. Rerun and verify all gates on final review SHA.
- **NONBLOCKING:** Graphify cache invalidates/rebuilds full graph on any source change. Shared cache needs explicit --cache-dir; CI does not persist an LSP index across runners.
- **SCOPE LIMITATION:** Bootstrap formal models are bounded smoke checks; they do not prove future product invariants.
- **PROCESS:** No product capability implementation or downstream route-topology change is authorized in this review.

## Pre-audit status
IMPLEMENTATION HANDOFF IN PROGRESS. These are implementer-identified findings, not an independent review verdict. A separate reviewer must issue PASS or NEEDS_CHANGES after inspecting the final commit and evidence.

## Remediation after pre-audit

Commit `6fd0f23cdb8005f0a614b5d7e11b4bde3d7bdea2` pins Serena to upstream `d9e4c796fae0207ba6848d1df4b331122a5567b5`. The mutable-reference blocker is addressed in configuration but runtime compatibility and final-SHA green CI remain unverified. Verdict remains NEEDS_CHANGES pending CI and final evidence review.
