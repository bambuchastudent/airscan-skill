# Spec Delta

## Purpose

Run refresh work durably and retain discovered routes using completed scan evidence so transient failures or worker crashes cannot silently remove useful travel topology.

## ADDED Requirements

### Requirement: Durable scan jobs

The system SHALL persist scan work so eligible jobs can be resumed or retried after process interruption without relying on in-memory scheduler state.

#### Scenario: Worker stops during a scan

- **WHEN** a worker terminates before completing its leased scan job
- **THEN** the job becomes eligible for recovery according to the lease/retry policy rather than remaining permanently running

### Requirement: Single active ownership

The system SHALL prevent more than one non-expired active lease from owning the same logical scan job concurrently.

#### Scenario: Another worker requests leased work

- **WHEN** a logical job already has a valid active lease
- **THEN** the system does not grant a second active lease for that job

### Requirement: Failure is not negative route evidence

The system SHALL distinguish a successfully completed scan that did not observe a route from a scan that failed to obtain or interpret relevant source data.

#### Scenario: Topology request fails

- **WHEN** a relevant source request or parser fails
- **THEN** the failure does not increment route-miss evidence used for lifecycle deactivation

### Requirement: Route lifecycle uses completed evidence

The system SHALL transition route lifecycle only from configured counts of relevant successfully completed observations or misses.

#### Scenario: One successful scan misses an active route

- **WHEN** a completed relevant scan does not observe a previously active route
- **THEN** the system retains the route and records one completed miss without immediately deleting its evidence history

#### Scenario: Configured inactivity threshold is reached

- **WHEN** the configured number of relevant successful scans has consecutively missed a route
- **THEN** the system transitions the route according to the configured lifecycle policy while retaining historical evidence

### Requirement: Scan operations are observable

The system SHALL retain enough scan metadata to explain source freshness and failures.

#### Scenario: Source data is stale

- **WHEN** a consumer inspects freshness for a source or origin
- **THEN** the system can report the last successful scan time and relevant recent failure/retry information
