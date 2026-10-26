# RT-1 manual one-shot VLC topology import — evidence handoff

**Requested review:** candidate one-shot importer design/implementation inside existing PR #8. **NOT** unconditional RT-1 acceptance.

- Base: `develop@301d979f66b765651303337876fe8998a2b2ca99`
- Reviewed code/docs candidate: `e2b1ed9f6265dbbf16241c24052526a415e8d76f`
- Exact code CI: https://github.com/bambuchastudent/airscan-skill/actions/runs/40330470134, **9/9 PASS** (19 root, 10 worker, 5 emulator IT)
- New architecture decision: https://github.com/bambuchastudent/airscan-skill/pull/8#issuecomment-6400275771
- OpenSpec change: `add-route-topology`, tasks **0.1/0.2/0.5 accepted**, 0.3/0.4 pending actual Aena captured DOM evidence.

## Scope and technical claims

Manual Java CLI `dev.airscan.worker.aena.AenaCatalogImportCli` requires `--input`, `--output`, `--captured-at`; no HTTP request, background schedule, live crawler, route-batch protocol or central DB adapter. Browser snippet `scripts/export-aena-dom.js` manually downloads rendered `<main>`. Java jsoup parser handles structurally recognized destination tables/heading lists and source-listed carriers, rightmost IATA in `(MIL) (TSF)`, duplicates/rejections and advertised count discrepancy. Snapshot contains sourceId, URL, capturedAt, schema version, input SHA-256 and diagnosis. Immutable per-capture JSON with atomic `latest.json` replacement for COMPLETE only; failed/partial runs preserve prior accepted latest.

Tests cover a synthetic HTML structural fixture, corruption, duplicates, count mismatch, unknown carriers, list cards, actual CLI `main` invocation and preservation of previous complete snapshot. No fares, cloud, additional origins, persistent RT-2 ingestion or RT-3 date adapter.

## Independent review findings to resolve

1. **Blocking genuine source DOM evidence (0.3/0.4):** present fixture is explicitly reconstructed from page-visible labels, **not** a browser capture of original Aena DOM. The actual page may use a different hierarchy and dynamic entries. A manual browser export must be applied to CLI, reconciled with its live advertised count, and a minimal sanitized true DOM fixture included for regression (subject to Aena rights). Without that evidence source coverage remains partial/unknown and `latest.json` must not be accepted.
2. **Legal:** Aena's website legal notice still limits reproduction/redistribution; personal/local human-triggered use replaces need for unattended crawler permissions, not any assertion of universal rights.
3. **Graphify / Serena:** their CI jobs verify existing POC smoke only, not source parser selector correctness. Formal Alloy/TLC/Dafny bootstrap checks are not proofs of these DOM mappings.

**Verdict on current evidence:** `NEEDS_REAL_AENA_DOM_EVIDENCE` for full RT-1; code implementation is ready for independent examination, not final merge. Do not mark 0.3/0.4 complete or begin RT-2/RT-3.
