# Agent Memory, Logs, and Handoffs

AirScan deliberately stores a small project-local evidence trail so agents and external reviewers do not depend on chat history.

## Files

### MEMORY.md

Curated current project state.

Update in place when a durable fact changes. Keep it compact and factual.

### WORKLOG.md

Append-only chronology of meaningful implementation/review iterations and the deterministic evidence actually produced.

### iterations/

Create one file per meaningful iteration:

`YYYY-MM-DDTHHMMZ-<slug>.md`

Use this shape:

```markdown
# Iteration summary — <title>

## Goal

## Starting context / assumptions

## Changes made

## Explicitly not changed

## Verification evidence
- command/check:
- result:

## Formal verification
- Alloy:
- TLC:
- Dafny:

## Decisions

## Risks / unresolved questions

## Relevant artifacts
- OpenSpec:
- commits:
- review report:

## Next recommended action
```

Never write "tests passed" without recording what was actually run.

## Review reports

See `docs/reviews/README.md`.
