# RT-1 VLC query contracts (design only — no REST implementation)

## Topology: `GET /api/v1/routes?origin=VLC`

Returns all canonical advertised direct routes **from the accepted airport-wide source snapshot**, independently of date evidence. Every route has a key `DIRECT:<ORIGIN>:<DESTINATION>`, multiple carrier labels (or UNKNOWN), and source/scan/time provenance. Response-level `sourceCoverage` is `COMPLETE | PARTIAL | UNKNOWN`; `scanStatus` distinguishes `SUCCESS_WITH_OBSERVATIONS`, `SUCCESS_NO_OBSERVATIONS`, `SOURCE_TRANSPORT_FAILURE`, `SOURCE_REJECTED_OR_RATE_LIMITED` and `PARSER_FAILURE`. Do not present an empty array as full catalog if latest scan failed. Unknown IATA, missing origin or unsupported origin: 400/422 as appropriate. RT-1 origin scope is VLC only.

Illustrative shape, **not live data or available endpoint**:

```json
{
  "origin": "VLC",
  "sourceCoverage": "PARTIAL",
  "scanStatus": "SUCCESS_WITH_OBSERVATIONS",
  "sourceSnapshot": {"sourceId": "aena-vlc", "scanId": "example-1", "observedAt": "2026-10-25T15:00:00Z"},
  "routes": [
    {"routeId": "DIRECT:VLC:AMS", "origin": "VLC", "destination": "AMS",
     "carriers": ["TRANSAVIA (TRA)", "EASYJET EUROPE (EJU)", "K.L.M. ROYAL DUTCH AIRLINES", "VUELING AIRLINES"],
     "evidence": [{"sourceId": "aena-vlc", "scanId": "example-1", "observedAt": "2026-10-25T15:00:00Z"}]}
  ],
  "omissions": [],
  "freshness": "SOURCE_OBSERVATION_TIMESTAMP"
}
```

The example is intentionally `PARTIAL` because it contains only a representative route; never call it a full Aena catalog.

## Dated enrichment: `GET /api/v1/routes/search?origin=VLC&from=YYYY-MM-DD&to=YYYY-MM-DD`

Return the **full topology catalog**, annotated by dates. RT-1 pure validator uses ISO calendar `LocalDate`, inclusive range and a temporary maximum of **366 inclusive days**; a source with a lower supported range must report its actual coverage rather than silently truncating. Invalid/missing IATA/date, reversed range and overlong range are errors. No timezone conversion for flight days. `CONFIRMED` only when an independently verified source has positive observations. `UNCOVERED` means no verified date source covers the route/date scope; `UNKNOWN` means no trustworthy positive date fact was observed, including incomplete/stale/failed scans. Neither means “no flights”.

```json
{
  "origin": "VLC", "from": "2026-12-10", "to": "2026-12-20",
  "topologySourceCoverage": "PARTIAL",
  "dateCoverage": {"scope": "CARRIER_PARTIAL", "status": "UNKNOWN", "uncoveredRouteCount": 1,
                   "failedSources": []},
  "routes": [
    {"routeId": "DIRECT:VLC:AMS", "availabilityStatus": "UNCOVERED",
     "matchingAvailabilityDates": [], "dateEvidence": []},
    {"routeId": "DIRECT:VLC:LIS", "availabilityStatus": "CONFIRMED",
     "matchingAvailabilityDates": ["2026-12-14"],
     "dateEvidence": [{"flightDate": "2026-12-14", "carrier": "UNKNOWN",
                       "sourceId": "example-date-source", "scanId": "example-date-scan",
                       "observedAt": "2026-10-25T15:00:00Z"}]}
  ]
}
```

Examples are fabricated contract examples, **not claimed airline dates**. No prices, fares, no-negative-evidence assumption or date-complete airport claim. Failed date providers surface a diagnostic and previous observed positive facts may remain with clear freshness. Never omit unconfirmed airport routes by default; an opt-in confirmed-only projection could be added later with explicit semantics.

## RT-2 ingestion protocol extension (not implemented)

Accepted `WorkerProtocolService.normalize()` currently hard-rejects any payload except exactly `{kind:"synthetic",value:<integer>}`, max 8192 chars. It archives with synthetic source ID; a real route batch cannot piggyback as a fake integer. RT-2 must introduce versioned `kind:"route-topology-batch"`, `schemaVersion:1`, `sourceId`, `scanId`, `origin`, `observedAt`, `coverage`, `reportedCandidates`, `omissions`, and canonical route/evidence entries. Validate allowlisted kind/schema and size/row bounds with explicit failure status; require server-side lease and source-scoped job permission, and source/scan/worker attribution. Treat unverified `COMPLETE` claims as insufficient to assert source exhaustiveness.

Preserve existing auth and scoped `WorkerPrincipal`, lease/heartbeat ownership, SQLite durable `batchId` and `PENDING -> ACCEPTED`, HTTP submit/retry, transactionally authoritative Firestore accepted receipt and immutable archive hashes. Keep the current synthetic envelope accepted under its **unchanged** validation contract; branch on explicit discriminators only. Worker must never import Firestore SDK or write directly to central state. Atomic archive-before-acceptance can leave safe orphaned content, as today.

**Explicit compatibility risk:** today's central acceptance is one result **per job**, not a general multi-row route projection. RT-2 must reconcile multi-route projection with batch acceptance/idempotency (consistent canonical route keys + source-scoped evidence IDs; never duplicate on lost ack), size bound, potential multiple batches, and partial extraction/published-snapshot gating. No direct worker->Firestore shortcut and no synthetic data coercion. If a new multijob protocol or atomicity change is required, stop for independent architecture review before implementing.

## RT-1 formal-method decision

`INV-ROUTE-001` self-route rejection and carrier-free `INV-ROUTE-002` key are small pure Java value-object logic covered by JUnit. `INV-EVIDENCE-001` requires non-null `Source` and `Scan` with every `RouteEvidence` / `DatedAvailabilityObservation`. An Alloy relational model is useful once persistence/multi-source merge structure stabilizes; it is not meaningful to misrepresent the **existing bootstrap Alloy** as covering routes. Dafny task 2.2 is deferred until larger identity/merge complexity is measured; this simple string concatenation does not justify generated Java today. No formal route model is claimed to have run.
