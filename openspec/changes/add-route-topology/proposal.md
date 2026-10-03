# Proposal

## Why

AirScan needs a durable local graph of direct routes before it can search fares efficiently. Route existence changes much less frequently than price, so topology must be discovered, normalized, retained, and queried independently from fare collection.

## What Changes

- Add a configurable airport catalog with the initial origins VLC, CDT, ALC, MAD, and BCN.
- Add canonical airport, carrier, direct-route, source, scan, and route-evidence concepts.
- Add a route-topology provider boundary separate from schedule and fare providers.
- Add at least one authoritative topology source implementation after source research.
- Persist source provenance, first/last observation metadata, and evidence history.
- Add deterministic geographic-distance candidate generation for nearby airports/cities without treating distance as proof of ground connectivity.
- Expose deterministic queries needed by later fare and ground-routing changes.

## Capabilities

### New Capabilities

- `route-topology` — discover, normalize, retain, and query direct-route topology with provenance.

### Modified Capabilities

None.

## Impact

Introduces the first travel-domain model and persistence schema, source adapters for topology discovery, database migrations, parser fixtures, and query APIs/services used by later scanning and fare changes.
