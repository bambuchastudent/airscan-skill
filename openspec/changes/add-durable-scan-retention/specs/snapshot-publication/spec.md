# Spec Delta

## Purpose

Provide consumers with an atomic current view of successfully refreshed data so dashboards and route queries never treat partially completed or older scans as the latest published state.

## ADDED Requirements

### Requirement: Partial scans are not current

The system SHALL promote a snapshot to current only after its required scan work satisfies the configured successful-completion policy.

#### Scenario: Required job is still running

- **WHEN** a snapshot has unfinished required work
- **THEN** consumers continue to receive the previously published current snapshot

#### Scenario: Required job fails

- **WHEN** a snapshot does not satisfy the configured successful-completion policy
- **THEN** the incomplete snapshot remains available for diagnostics but is not promoted as current

### Requirement: Publication is monotonic

The system SHALL prevent an older snapshot from replacing a newer current snapshot for the same logical scope.

#### Scenario: Older work completes late

- **WHEN** an older snapshot finishes after a newer snapshot has already been published
- **THEN** the older snapshot is not promoted over the newer current snapshot

### Requirement: Published snapshot identity is queryable

The system SHALL expose which completed snapshot backs a current route/ranking query and when that snapshot was published.

#### Scenario: Consumer inspects current data

- **WHEN** a current-data query is returned
- **THEN** the response can identify the backing snapshot and its publication/freshness time
