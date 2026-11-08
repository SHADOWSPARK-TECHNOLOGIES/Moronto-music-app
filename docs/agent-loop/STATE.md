# Mornica — Agent State (persistent working memory)

Maintained by agents across sessions. **Update after every verified change.** If you cannot see
proof (branch, SHA, passing command output), mark it stale — a note is not evidence.

## Active loop
Nigeria-first streaming/offline plan: 1 = access policy (done) · 2 = native Media3 online playback
(done) · 3 = offline downloads (DONE, incl. offline-PLAYBACK device proof via FLAG_BLOCK_ON_CACHE,
merged to main) · 4 = player + Downloads UI (DONE as code+unit; rendered pixels UNVERIFIED) ·
5 = airplane-mode & background testing · 6 = independent QA + release candidate (no publish without
owner approval).
Authoritative spec: `docs/agent-loop/MORNICA_NIGERIA_FIRST_STREAMING_OFFLINE_SPEC.md`.

## Last verified state (2026-11-07)
- `main` = `7c6ade8` — PRs #11, #12, #13 MERGED (gate+playback, offline-proof, lint config).
- Loop 4 work lives on branch `agent/mornica-loop4-catalog-ui-20261107` (DRAFT PR to main), based on
  the post-merge consolidation branch `agent/mornica-gate-consolidation-20261107`.
- Working tree: clean. Consolidation worktree
  `/home/moronto/.hermes/cache/scratch/mornica-wt-consolidate` (authoritative; sdk.dir set).
- Repo: SHADOWSPARK-TECHNOLOGIES/Moronto-music-app (remote origin). Owner's checkout is
  `/home/moronto/StudioProjects/Moronto-music-app` (old antigravity branch; DO NOT disturb).

## Verification ledger (as of loop 4, 2026-11-07)
- [x] unit-verified: `:app:testDebugUnitTest` 55/55 — gate 7, playback 7, catalog-download 8,
      download-store 6, acceptance 12, accessibility 7, download-control 5 (new), catalog-section 3 (new,
      Robolectric Compose interaction/semantics).
- [x] build-verified: `:app:assembleDebug` produces debug APK (20.4 MB, applicationId intact)
- [x] lint-verified: `:app:lintDebug` green (UnsafeOptInUsageError disabled with rationale)
- [x] device-verified: loop-2 streaming playback 1/1; loop-3 download COMPLETED 1/1 + offline
      READINESS bytes 1/1 + offline PLAYBACK via FLAG_BLOCK_ON_CACHE 1/1 (API 37 x86_64 emulator).
- [ ] unverified now: audible real-speaker (human); real licensed catalogue (present is demo fixtures);
      loop-4 rendered PIXELS on a device (headless emulator ⇒ only Compose semantics/interaction
      verified via Robolectric); independent QA + release candidate (loop 6).

## Loop 4 interaction audit (2026-11-08, branch agent/mornica-loop4-as-inspect-20261107)
`./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` = BUILD SUCCESSFUL:
**56/56 unit tests** (added `LicensedCatalogSectionTest.streamOnlyTrackHidesDownloadControl`),
**0 lint errors** (35 warnings: GradleDependency/UseKtx/NewerVersion/UnusedResources/MonochromeIcon/
OldTargetApi/ObsoleteSdkInt), debug APK 20.4 MB.
Device (API 36/37 x86_64 emulator, `Medium_Phone`, 1080x2400@420 = ~411dp, headless, ADB-driven):
- [x] device-verified bottom nav: Explore Videos (external-YouTube cards, no embed), Creator Hub,
      My Music — all 3 reachable; evidence screenshots under `screenshots/audit-20261108/`.
- [x] device-verified library tabs ×5: Tracks/Queues/Albums/Artists/Folders switch content
      (Queue · Afrobeats Offline Queue / Album · Mornica Creator Sessions / Artist · Teniola Blaze /
      Folder · Mornica Demos).
- [x] device-verified Stream: banner "Streaming licensed demo…"; MediaSession logged PLAYING(3)
      progressing 0:00→0:21+ on the live SoundHelix source (audio focus + ExoPlayer Init).
- [x] device-verified persistent player: catalog playback now shows the mini player (my fix:
      bottom-bar shows when activeCatalogTrack != null) — "Demo Afrobeats Instrumental · 0:53 / 6:12".
- [x] device-verified Download: tap "Download for later" → banner "Download queued for offline" +
      CatalogDownloadService foreground service started + button → "Downloading…"/"Cancel".
- [x] device-verified Cancel: tap Cancel → banner "Download cancelled…" + CatalogDownloadService
      FGS stopped + button reverts to "Download for later".
- [x] device-verified streaming-only download control hidden: "Demo Streaming-Only Track" shows
      "Streaming only · offline not licensed" and NO download/cancel control (my fix).
- [x] device-verified Back: Creator Hub → My Music (app stays foreground); My Music home (sheet
      collapsed) → system default, foreground becomes launcher (app exits).
- [ ] OFFline REady runtime gap (DEVICE BLOCKED): a completed download does NOT flip the card to
      "Offline ready" / "downloaded · ready offline". Root cause: `MornicaRuntime.router` builds
      `PlaybackRouter` WITHOUT a `downloadStore` (ctor default null) and nothing observes
      DownloadManager completion to set `catalogDownloadStates[id]=COMPLETED`. So
      `offlinePlaybackAllowed` is always false at runtime and the UI stays "Downloading…" forever.
      The offline pipeline is proven by the Loop-3 instrumented tests, but the running app cannot
      surface "Offline ready". Needs: persist a real `DownloadStore` + observe
      `DownloadManager.Listener` completion → update router COMPLETED. NOT implemented (scope).


## Loop 4 implementation notes (important for the next worker)
- `LicensedCatalogSection` (ui/LicensedCatalogSection.kt) is mounted at the top of My Music. It renders
  as a plain **Column**, intentionally NOT a nested LazyColumn: a second scrollable breaks
  `MornicaAccessibilityTest` (its `performScrollToNode` assumes a single scrollable). If a real
  catalogue grows past ~10 rows, switch to a LazyColumn AND scope the accessibility test's scrollable.
- Wi-Fi-only: `router.setWifiOnlyDownloads(Boolean)` persists in state + calls
  `CatalogDownloadController.setWifiOnlyDownload`, which sets
  `DownloadManager.setRequirements(NETWORK_UNMETERED | NETWORK)`. Wired via `MornicaRuntime.wireCatalogDownloads`
  with `NETWORK` default (the Loop-3 note below says tighten to unmetered — it's now a user setting).
- Cancel: `router.cancelCatalogDownload(trackId)` → `CatalogDownloadController.cancelDownload(mediaId)`
  backed by the real `DownloadManager.removeDownload(...)`.
- Enablement: `MainActivity.onCreate` calls `MornicaRuntime.wireCatalogDownloads(appContext)` (idempotent),
  which attaches the gate-backed `CatalogDownloadManager` + a real download controller and sets
  `downloadServiceStarter` to start `CatalogDownloadService` via `buildAddDownloadIntent`.
- Demo catalogue is 2 tracks (afrobeat offlineAllowed + stream-only). `LicensedCatalogConfig.DEMO_TRUSTED_AUDIO_HOSTS`
  still `setOf("soundhelix.com")` — the only verified demo host (Google test bucket is 403).
- `@OptIn(UnstableApi::class)` has NO effect here (UnstableApi is a marker annotation, not @RequiresOptIn) —
  produces harmless compiler warnings; `UnsafeOptInUsageError` is disabled in app/build.gradle.kts.

## Loop 3 implementation notes (important for the next worker)
- Media3 1.5.x: download classes live in `androidx.media3.exoplayer.offline` (NOT `.download`).
- A standalone `DownloadManager` does NOT auto-run downloads — drive them through `CatalogDownloadService`
  (foreground `DownloadService`).
- `DownloadRequest` has no `setMediaId`; builder is `DownloadRequest.Builder(mediaId, Uri)`.
- Do NOT set `customCacheKey` on download requests — playback's CacheDataSource keys by URI, so both must
  share the URI key for offline reads.
- `DownloadStore` is a JSON store (no Room) — a deliberate, flagged deviation from the Loop-3 contract;
  switch to Room behind KSP only if a release candidate needs it.
