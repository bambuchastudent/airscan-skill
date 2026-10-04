# Spec Delta

## Purpose

Provide one authoritative hosted AirScan control plane while allowing crawler workers to execute on arbitrary machines or supported cloud runtimes.

## ADDED Requirements

### Requirement: Central authority

The system SHALL treat the control plane as authoritative for operational route/job/snapshot state and publication decisions.

#### Scenario: Worker discovers data

- **WHEN** a crawler worker discovers normalized observations
- **THEN** the worker submits them through the worker protocol rather than directly mutating the authoritative database

### Requirement: Portable workers

The system SHALL allow a crawler worker to operate independently of where the control plane is hosted, provided it has supported runtime dependencies and outbound HTTPS connectivity.

#### Scenario: Local worker leases work

- **WHEN** a worker running on a local developer machine requests eligible work
- **THEN** it can use the same versioned worker protocol as a cloud-hosted worker

### Requirement: Idempotent submission

The system SHALL make worker result submission idempotent for the same logical job/lease/batch identity.

#### Scenario: Submission response is lost

- **WHEN** a worker retries a previously accepted result batch after losing the original HTTP response
- **THEN** the control plane does not create duplicate logical observations from the retry

### Requirement: Offline-safe worker outbox

The system SHALL permit a worker to persist unsent normalized batches locally and retry them after temporary control-plane/network unavailability.

#### Scenario: Control plane is temporarily unreachable

- **WHEN** a worker finishes source work but cannot reach the control plane
- **THEN** the result can remain in a durable local outbox and later be submitted without losing its original scan/job identity

### Requirement: REST/OpenAPI is the primary machine contract

The system SHALL expose versioned REST/OpenAPI application/query contracts independently of MCP.

#### Scenario: MCP is unavailable

- **WHEN** an automation/client cannot use MCP
- **THEN** supported query/control behavior remains accessible through REST/OpenAPI where authorized

### Requirement: MCP reuses application services

The MCP adapter SHALL call the same application/query services used by other interfaces and SHALL NOT implement independent route/fare business rules.

#### Scenario: Same query through REST and MCP

- **WHEN** REST and MCP request the same supported query against the same current snapshot
- **THEN** both interfaces derive their answer from the same application semantics

### Requirement: Browser does not depend on Firestore schema

The static web application SHALL consume a stable read/query API rather than reading authoritative Firestore collections as its business contract.

#### Scenario: Firestore collection layout changes

- **WHEN** an internal central-storage schema changes without changing the read API contract
- **THEN** the web application does not require a matching domain-logic rewrite solely because of the database layout

### Requirement: Hot and archive data are separated

The system SHALL keep operational/materialized state separate from bulky immutable scan/fare archives.

#### Scenario: Completed scan produces a large batch

- **WHEN** a completed scan produces history that is not required as hot query state
- **THEN** the system may store the immutable compressed batch in archive storage while retaining query/provenance pointers and required materializations in the operational store

### Requirement: Hosted deployment exposes freshness

The hosted query interfaces SHALL expose enough snapshot/source freshness information for consumers to distinguish current from stale results.

#### Scenario: No worker has refreshed a source recently

- **WHEN** a query returns data backed by an old successful snapshot
- **THEN** the result exposes the relevant observation/snapshot freshness instead of presenting the data as newly checked
