# Proposal

## Why

AirScan needs a durable local graph of direct routes before it can search fares efficiently. Route existence changes much less frequently than price, so topology must be discovered, normalized, retained, and queried independently from fare collection.

Before investing further in cloud deployment, the project also needs an early user-value milestone that proves the accepted local distributed-control-plane POC can discover useful real travel data. The first milestone is deliberately narrow: run AirScan locally and query real direct destinations from Valencia (VLC) that have source-confirmed flight availability within a caller-supplied date interval.

## What Changes

- Add a configurable airport catalog with the initial origins VLC, CDT, ALC, MAD, and BCN.
- Add canonical airport, carrier, direct-route, source, scan, and route-evidence concepts.
- Add a route-topology provider boundary separate from fare providers.
- Add a narrow schedule-availability observation boundary used by the VLC Route/Date Discovery POC; this does not introduce fares or general itinerary search.
- Prioritize the official Aena Valencia destination catalog as the first source candidate for the airport-wide VLC route graph; verify actual coverage and freshness, rather than selecting an airline as the route catalog.
- Research date-availability sources separately; they may differ by carrier and have limited coverage. Never equate absence of observed dates with absence of flights.
- Persist source provenance, first/last observation metadata, and evidence history.
- Expose a local full VLC topology catalog query and a separate dated-availability view/query for an inclusive interval, with explicit unknown/partial coverage.
- Add deterministic geographic-distance candidate generation for nearby airports/cities without treating distance as proof of ground connectivity.
- Expose deterministic queries needed by later fare and ground-routing changes.

## Capabilities

### New Capabilities

- `route-topology` — discover, normalize, retain, and query direct-route topology with provenance.
- `route-date-discovery-poc` — locally retrieve the airport-wide advertised direct-route catalog from VLC first, then annotate/filter by source-confirmed dates in a requested interval without falsely claiming exhaustive date coverage.

### Modified Capabilities

None.

## Milestone order

1. Local distributed-control-plane POC — COMPLETE.
2. VLC Route/Date Discovery POC — NEXT (airport-wide VLC topology first, date evidence second).
3. Deployed distributed-control-plane POC — resume `add-distributed-control-plane` tasks 4.1–4.5.
4. Broaden route topology to the full configured origin set and geographic candidate work.
5. Fare observation and later itinerary/ranking work.

This ordering is an intentional user-value exception to the earlier cloud-first dependency order. It does not make local state authoritative and does not weaken the accepted worker/control-plane boundaries.

## Impact

Introduces the first travel-domain model and persistence schema, an airport-wide VLC topology source and separately researched date-availability source paths, date-availability observations for the VLC POC, parser fixtures, and query APIs/services used by later scanning and fare changes. Cloud deployment remains deferred until this milestone is independently accepted.
