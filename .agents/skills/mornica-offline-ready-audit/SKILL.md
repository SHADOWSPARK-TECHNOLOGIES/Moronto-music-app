---
name: mornica-offline-ready-audit
description: Use when implementing/verifying Mornica offline downloads ("Offline ready"), the Media3 DownloadManager wiring, or running the on-device (emulator) navigation/interaction audit. Concretely covers the Offline-Ready wiring fix, the Media3 1.5 download API shape, and the ADB uiautomator touch-verification recipe. Synced from the Hermes skill software-development/mornica-emulator-playback-proof + the 2026-11-08 audit.
---

# Mornica — offline-ready wiring & on-device interaction audit

Project-scoped skill for the Moronto Mornica Android agent (built by Hermes during the
2026-11-08 loop-4 interaction audit, then handed to Antigravity).

## Trigger
Apply when: (a) running :app:connectedDebugAndroidTest / instrumented playback proofs, (b) touching
`LicensedCatalogRights`, `PlaybackRouter`, `CatalogDownloadManager/SemService`, `DownloadStore`,
`Media3LocalAudioController`, (c) wiring/verifying licensed-offline downloads ("Offline ready"),
(d) doing any on-device navigation/interaction audit on the emulator.

## Environment (this host)
- SDK: `/home/moronto/Android/Sdk` (platform android-36.1; system image android-37.0 google_apis_playstore
  x86_64). KVM present => hardware boot. AVDs live under XDG config: `ANDROID_AVD_HOME=/home/moronto/.config/.android/avd`.
- AVD names seen: `mornica_loop2`, `Medium_Phone`. adb at `/home/moronto/Android/Sdk/platform-tools/adb`.
- Boot headless: `ANDROID_AVD_HOME=… emulator -avd Medium_Phone -no-window -no-snapshot -no-boot-anim
  -gpu swiftshader_indirect -no-metrics`. Poll `adb shell getprop sys.boot_completed` == 1.
- Verification command (run before claiming done): `./gradlew --no-daemon :app:testDebugUnitTest
  :app:lintDebug :app:assembleDebug`.

## The two playback invariants (never regress)
1. ExoPlayer must be created AND released on the main thread (`runOnMainSync` in device tests).
2. `isLocalPlaying` is set ONLY by the controller callback `onPlayerIsPlayingChanged` — never
   optimistically in `playLocalTrack`/`playCatalogTrack`.

## Offline-Ready wiring (the 2026-11-08 fix — do not undo)
Root cause of the old gap: `MornicaRuntime.router` built `PlaybackRouter` WITHOUT a `downloadStore`
(ctor default null) AND nothing observed DownloadManager completion, so `catalogDownloadStates` never left
QUEUED and `offlinePlaybackAllowed(track)` was always false -> the card stayed "Downloading…/Cancel"
forever and "Offline ready" was unreachable at runtime.

Working fix (live-verified):
- `PlaybackRouter.downloadStore` is now `private var` (attachable). `attachDownloadStore(store)` rehydrates
  any records already in the store as COMPLETED (so "Offline ready" survives process restart).
- `PlaybackRouter.onDownloadStateChanged(trackId, state)`: on COMPLETED persists
  `DownloadRecord.fromTrack(track, territory, now)` via the store (making `offlinePlaybackAllowed` true),
  then updates `catalogDownloadStates`.
- `MornicaRuntime.wireCatalogDownloads(context)` now does `router.attachDownloadStore(buildDownloadStore(context))`
  and `real.addListener(DownloadManager.Listener { … onDownloadChanged(manager, download, exception) …
  STATE_COMPLETED -> COMPLETED, STATE_FAILED -> FAILED, else -> Unit })`, then reconciles persisted
  completed downloads from `real.downloadIndex` at startup so Offline-ready survives restart.

## Media3 1.5.x API SHAPE (javap-verified — older shapes DO NOT compile)
- Download classes live in `androidx.media3.exoplayer.offline` (NOT `.download`).
- `DownloadManager.Listener.onDownloadChanged` is THREE-arg:
  `onDownloadChanged(DownloadManager, Download, Exception?)`. A two-arg override fails with
  "'onDownloadChanged' overrides nothing".
- `DownloadManager.getDownloadIndex()` returns a top-level `DownloadIndex`
  (NOT `DownloadManager.DownloadIndex`). `index.getDownloads(int…)` returns a `DownloadCursor`:
  iterate with `moveToNext()`, read `getDownload()`, `close()` in finally. There is no `hasNext()/next()`.
  `index.getDownloads(...)` and `getDownload(id)` throw IOException -> wrap the reconcile in `runCatching`.
- `DownloadRequest.Builder(mediaId, Uri)` (first ctor arg is the media id). NO `setMediaId`, NO
  `setCustomCacheKey` (playback's CacheDataSource keys by URI, so download must share the plain-URI key).
- A standalone DownloadManager does NOT auto-run downloads: drive them through the foreground
  `CatalogDownloadService`. `SimpleCache` requires a non-null `StandaloneDatabaseProvider`.
- `@OptIn(UnstableApi::class)` has NO effect (UnstableApi is a marker, not @RequiresOptIn) — harmless
  warnings; `lint { disable += "UnsafeOptInUsageError" }` is already in app/build.gradle.kts.

## Streaming-only tracks (make download controls visibly unavailable)
In `LicensedCatalogSection.kt` the track card branches on `offlineAllowed` FIRST:
`!offlineAllowed -> Text("Streaming only · offline not licensed")` — i.e. a stream-only track (demo
`catalog-demo-stream-only-02`) shows a muted label and NO download/cancel control (no dead tap the gate
would refuse). Do not revert to showing "Download for later" unconditionally.

## Persistent player
- The bottom-bar condition must gate on `(activeLocalTrack != null || activeCatalogTrack != null)` else
  catalog playback shows no mini player.
- `toggleLocalPlayPause()` on a LICENSED_CATALOG active track must keep `activeEngine =
  ActiveEngine.LICENSED_CATALOG` (not relabel to LOCAL_AUDIO) when resuming.
- BackHandler in `MornicaApp`: collapse the Now Playing sheet first, then return to My Music; it must be
  DISABLED on the My Music home so system Back still exits the app.

## On-device interaction audit (ADB, no GUI)
- Install once with perm grants: `adb install -r -g app/build/outputs/apk/debug/app-debug.apk`.
- Launch: `adb shell am start -n com.shadowspark.morontomornica/.MainActivity`; confirm
  `dumpsys activity activities | grep topResumedActivity`.
- uiautomator dump HANGS intermittently on this headless emulator: ALWAYS wrap in a CLI
  `timeout 40 adb shell uiautomator dump /sdcard/xx.xml` and retry the read up to 3x. Never chain many
  dump+action steps. For static controls (bottom nav y≈2232, library tabs y≈1461 at 1080x2400@420) parse
  one good dump for bounds and `adb shell input tap X Y`, then verify by re-dump.
- A TextButton's text node reports clickable=false; tap the clickable ancestor (or just the text center).
- Verify a tap by the DUMPED TEXT changing (e.g. tap "Download for later" -> "Downloading…"/"Cancel";
  a completed download -> "Offline ready" + "N downloaded · ready offline"). For a gate refusal, expect a
  denial banner AND no side-effect.
- Rendered-pixel proof requires a multimodal vision model on a screencap; DeepSeek-V4-Flash is NOT
  multimodal (rejects), so fall back to the uiautomator tree (text nodes positive-size, clickable bounds)
  and report as "structurally verified, not pixel-verified".
- Screen 1080x2400@420dpi ≈ 411dp wide, matches the 412dp screenshots. Override width via
  `adb shell wm size WxH` + `adb shell wm density D`; reset later.

## Known runtime facts (as of 2026-11-08)
- Demo catalogue = 2 tracks: `catalog-demo-afrobeat-01` (offlineAllowed=true) and
  `catalog-demo-stream-only-02` (offlineAllowed=false). Hosts = soundhelix.com (exact match incl. www).
- Demo tracks with a fully-cached URI complete their download instantly (shared SimpleCache) — that is
  correct; "Offline ready" renders once `offlinePlaybackAllowed` is true, which requires a persisted
  `catalog_downloads.json` record in `context.noBackupFilesDir`.
- Ensure the demo host stays allowlisted in `LicensedCatalogConfig` or the gate denies INVALID_SOURCE.

## Honest reporting
- A passing instrumented test proves the player reached a playing/offline state on that emulator — NOT
  audible sound on a physical speaker (needs a human listen).
- Units/lint/APK pass ≠ rendered pixels verified; label VERIFIED / INFERRED / UNKNOWN / BLOCKED with
  evidence (file/line/command). Do not self-assert success after two identical failures; stop and record.
- No push/merge/deploy/release/signing change without explicit owner approval. Never fabricate a license.
