# AirScan milestone plan

## Current accepted baseline

Local distributed-control-plane POC: **COMPLETE**.

Accepted develop baseline before this planning update:

`e8d2916240275ea98aef808228fe830c29fb49a4`

The accepted local pipeline already proves authenticated worker leasing, SQLite outbox recovery, idempotent result submission, Firestore-emulator authority, immutable local archive behavior, and REST query semantics.

## Milestones

### M0 — Local distributed-control-plane POC

Status: **COMPLETE**

Purpose: prove the distributed execution and retry/idempotency architecture locally.

### M1 — VLC Route/Date Discovery POC

Status: **NEXT**

User outcome:

> Run AirScan locally and ask for real direct destinations from Valencia (VLC) that have positive availability evidence inside a requested date interval.

Target iterations:

- **RT-1:** contracts + source research + sanitized fixtures.
- **RT-2:** one real VLC provider vertical slice + clean-store E2E + local query.
- **Review gate:** independent PASS / VLC ROUTE-DATE POC COMPLETE.

No fare or cloud deployment is required for M1.

### M2 — Deployed distributed-control-plane POC

Status: **DEFERRED UNTIL M1 PASS**

Resume `add-distributed-control-plane` tasks 4.1–4.5:

- Cloud Run;
- production Firestore configuration;
- GCS archive adapter;
- OIDC/ingress;
- Firebase Hosting;
- region/cost/budget evidence;
- deployed synthetic E2E.

### M3 — Broader route topology

After M2:

- default origins VLC/CDT/ALC/MAD/BCN;
- additional reviewed topology sources as needed;
- geographic candidate generation;
- full `add-route-topology` closeout.

### M4 — Durable scan/retention and fare work

Continue the existing durability, snapshot, retention, fare-observation, ranking and later ground-routing roadmap after the route graph is useful and deployable.

## M1 acceptance gate

M1 is complete only when all of the following are evidenced:

- local reproducible startup instructions;
- exactly one reviewed real source path;
- real VLC live smoke;
- deterministic sanitized fixture tests;
- topology and dated availability modeled separately;
- existing authenticated worker/control-plane protocol used for ingestion;
- stable route identity and idempotent repeated submission;
- local query accepts VLC + inclusive date range;
- each returned destination has a matching observed date plus provenance/freshness;
- provider failure is not negative evidence;
- no fares are invented;
- strict OpenSpec + Maven + Graphify + Serena + applicable invariant checks pass;
- independent external review returns PASS.

After M1 PASS, do not automatically broaden providers/origins. Resume M2 first.
