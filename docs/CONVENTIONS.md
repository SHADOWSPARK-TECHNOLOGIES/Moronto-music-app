# Mornica — Code & Process Conventions

## Architecture
- Kotlin + Jetpack Compose + Material 3, package `com.shadowspark.morontomornica` (Play identity, keep).
- ONE native Media3 engine (ExoPlayer in `LocalAudioPlaybackService` / `MediaSessionService`).
  Local device tracks and licensed streamed tracks share it — never a second player.
- Fail-closed rights gate (`LicensedCatalogRights`) is the boundary; call `evaluate()` before any
  player operation. It is pure (no I/O, no side effects).
- YouTube is external discovery only: clearly-labeled "Watch on YouTube" links; no embed, no
  extraction, no downloads.

## Identifiers & state
- MediaItem mediaId: local = plain track id; catalog = `catalog:<id>` (prefix const
  `PlaybackRouter.CATALOG_MEDIA_ID_PREFIX`).
- `isLocalPlaying` is driven by the controller callback, never set optimistically.
- Territory: default `"NG"`; inject `territoryProvider` for tests.

## Content / licensing
- Demo fixtures are visibly labeled `DEMO`, `licenseReference = "DEMO-*"`, and come only from an
  allowlisted host in `LicensedCatalogConfig`. Never hardcode a host elsewhere.
- `rightsConfirmed` true ONLY from a trusted backend/model with a real agreement. Never fabricate.
- Production streaming/download hosts must be added to the allowlist WITH a rights note (AGENTS.md mandate).

## Tests
- Unit tests (Robolectric) cover the gate and router logic — pure, no device required.
- Instrumented test(s) prove real playback on a device/emulator (see STATE.md for the AVD + boot flags).
- Every PR carries a Verification Ledger (unit-verified / device-verified / unverified) — see
  `.github/PULL_REQUEST_TEMPLATE.md`.

## Process
- Contract-first: a Sprint Contract in `.dev-workflow/` is approved before coding each Loop.
- Evidence to `docs/EXECUTION_LOG.md`; decisions to `docs/decisions.md`; agent loop state to `docs/agent-loop/`.
