# Local AirScan distributed-control-plane POC

**Mode:** deterministic Java 25 + Quarkus HTTP + Firestore Emulator +
worker Java HTTP client + real SQLite outbox + immutable local gzip archive.
No Google credentials and no production cloud calls are needed.

## Prerequisites

Java 25, executable repo Maven Wrapper, Google Cloud CLI
**550.0.0** with `cloud-firestore-emulator` installed.
Use separate shell sessions for the emulator and the test harness.

Terminal A:

```bash
export FIRESTORE_EMULATOR_HOST=127.0.0.1:8085
gcloud emulators firestore start --database-mode=firestore-native \
  --host-port="$FIRESTORE_EMULATOR_HOST"
```

Terminal B (from repository root, checked out to the POC PR branch):

```bash
export FIRESTORE_EMULATOR_HOST=127.0.0.1:8085
export AIRSCAN_TEST_PROJECT_ID=airscan-emulator-local
export AIRSCAN_POC_ARCHIVE_DIR="$(mktemp -d)"
umask 077
export AIRSCAN_POC_WORKERS_FILE="$(mktemp)"
printf 'worker-a=%s;worker-b=%s' \
  "$(openssl rand -hex 24)" "$(openssl rand -hex 24)" \
  > "$AIRSCAN_POC_WORKERS_FILE"
./mvnw -B -ntp spotless:check
./mvnw -B -ntp checkstyle:check
./mvnw -B -ntp clean verify
./mvnw -B -ntp -f worker/pom.xml clean verify
./mvnw -B -ntp -Pfirestore-emulator verify
```

**Expected result:** 11 regular root tests, 2 independent worker tests,
5 Failsafe emulator IT (four `WorkerPocIT` + one previous Firestore IT);
zero failures/errors/skips. Tests seed unique UUID jobs in the emulator,
so rerunning doesn't require emulator data cleanup. SQLite/outbox files
are kept under temporary JUnit directories and no DBs are committed.

On completion, close the emulator in terminal A and delete the two
temporary paths `$AIRSCAN_POC_WORKERS_FILE` and
`$AIRSCAN_POC_ARCHIVE_DIR` (after the tests and while still in the same
shell). Do not paste/echo the randomly generated bearer tokens.

## What the POC proves

1. Server seeds one synthetic job; worker authenticates via a local
   bearer token and receives a server-generated lease ID.
2. Independent plain-Java `WorkerPocClient` posts over loopback HTTP.
   A normalized JSON result with stable `batchId` is persisted in
   worker `SqliteOutbox` before HTTP submission.
3. Control plane checks principal/lease, stores an immutable gzip archive,
   and transactionally commits one accepted result in Firestore.
4. Simulated lost HTTP acknowledgment leaves SQLite PENDING.
   A new SQLite instance and worker HTTP client resend the same batch.
   The same accepted response is returned and the central state remains
   a single logical ingestion.
5. The worker marks SQLite ACCEPTED, reopens the DB and sees no pending
   item. Narrow REST accepted-result query proves archive hash,
   source and batch identity, and acceptance timestamp.
6. Other tests race worker A/B leases, race identical result submissions,
   check wrong-principal heartbeats and classified failure/expiry recovery.

## Limits

The POC has **no production auth/ingress** and intentionally no general job
seed endpoint. The local accepted synthetic query is unauthenticated and
must not be publicly exposed. A managed production scheduler, OIDC, Cloud
Run, GCS, route/fares or dashboard are outside this milestone. Existing
bootstrap formal gates are not a proof of protocol concurrency.
