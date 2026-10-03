# AirScan Review Reports

Review reports are self-contained evidence packages for human or independent-agent review.

Create reports under:

`docs/reviews/<openspec-change>/YYYY-MM-DDTHHMMZ-review.md`

Template:

```markdown
# Review — <change>

## Review status

READY | READY_WITH_FOLLOWUPS | NEEDS_CHANGES

## Scope
- OpenSpec change:
- commit range:
- components:

## Applicable specs and invariants

## Implementation summary

## Spec conformance findings

## Deterministic verification evidence
- command/check:
- observed result:

## Formal verification evidence
### Alloy
### TLA+/TLC
### Dafny

## Graphify / Serena impact review

## External source/API risks

## Regression / compatibility concerns

## Findings
- severity:
- evidence:
- required action:

## Unresolved questions

## Exact follow-ups
```

A report must distinguish:
- measured/observed facts;
- source-backed facts;
- assumptions;
- reviewer interpretation.

READY must never be based only on an implementation-agent summary.
