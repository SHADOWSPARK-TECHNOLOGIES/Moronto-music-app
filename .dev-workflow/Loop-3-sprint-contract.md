# Sprint Contract — Loop 3: Authorized offline download for catalog tracks

> **This is a contract, not implementation.** Hermes proposes this, a reviewer/owner approves it,
> THEN coding starts. Success is judged against the frozen criteria below, not ad-hoc judgment.
> Source spec: `docs/agent-loop/MORNICA_NIGERIA_FIRST_STREAMING_OFFLINE_SPEC.md`

## What will be built
- `CatalogDownloadManager.kt` — Media3 `DownloadManager` (DownloadService/DownloadIndex/
  CacheDataSource) that consults `LicensedCatalogRights` BEFORE enqueueing; only ALLOW
  (SAVE_OFFLINE) decisions are enqueued.
- `CatalogDownloadService.kt` — foreground `DownloadService` subclass with a download progress
  notification (Wi-Fi-only option, pause/resume/cancel/retry where Media3 exposes it).
- `DownloadStore.kt` — Room persistence for download states + the rights snapshot the track was
  granted under, so offline playback re-checks rights (refuse if rights expired/revoked).

## Success criteria
- `SAVE_OFFLINE` is DENIED for: `rightsConfirmed=false`, expired, out-of-territory,
  unapproved-host, and streaming-only (`offlineAllowed=false`) tracks.
- An authorized track downloads, completes, and plays back with the network disabled
  (PLAY_DOWNLOADED succeeds) — and a stream-only/expired track is NEVER present in the download index.
- A download survives process death and resumes/validates correctly on restart.
- No YouTube / googlevideo URI is ever enqueued or played offline (defense-in-depth maintained).
- No Play Console or signing change; no push/merge without owner approval.

## Verification steps
1. Unit: gate returns `OFFLINE_NOT_AUTHORIZED` for each denial fixture (rights, expiry, territory,
   host, stream-only).
2. Unit: `CatalogDownloadManager` enqueues ONLY ALLOW decisions; stream-only never enqueued.
3. Instrumented: download a demo (offline-allowed) track; verify it completes; enable
   airplane-mode; verify `PLAY_DOWNLOADED` succeeds offline.
4. Instrumented: verify a stream-only track never appears in the download queue.
5. `./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest :app:assembleDebug`.

## Dependencies
- Media3 download modules (`media3-exoplayer` DownloadManager/DownloadService/DownloadIndex, plus
  CacheDataSource) — may require adding catalog aliases in `gradle/libs.versions.toml`.
- Room for `DownloadStore` — new dependency + schema.
- The existing `LicensedCatalogRights` gate (must remain the single authorization authority).
- Airplane-mode toggling on the emulator via `adb shell cmd connectivity airplane-mode enable`.

## Risks
- Media3 `DownloadManager` needs a real cache dir + network; emulator storage limits.
- Wi-Fi-only enforcement on emulator is notional (no cell network) — verify via config path.
- Rights-recheck on offline playback adds download lifecycle complexity; keep `rightsConfirmed`
  fail-closed (a missing backend is a denial, not a pass).
- Room schema migration from nothing is trivial (new DB), but do not reuse the device media DB.

## Out of scope
- Player/Downloads UI (Loop 4), airplane-mode/background testing (Loop 5), QA + release (Loop 6).
