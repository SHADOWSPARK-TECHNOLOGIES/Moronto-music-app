# Mornica — Agent State (persistent working memory)

Maintained by agents across sessions. **Update after every verified change.** If you cannot see
proof (branch, SHA, passing command output), mark it stale — a note is not evidence.

## Active loop
Nigeria-first streaming/offline plan: 1 = access policy (done) · 2 = native Media3 online playback
(done) · 3 = offline downloads (DONE, incl. offline-PLAYBACK device proof via FLAG_BLOCK_ON_CACHE,
merged to main) · 4 = player + Downloads UI (RECOVERED & POLISHED: Mornica brand system, microcopy,
compact header, persistent mini player) · 5 = airplane-mode & background testing (DEVICE-VERIFIED 2026-11-08:
ready-offline track plays 18.2s+ at PLAYING(3) with RF unreachable + survives force-stop/offline relaunch;
physical hardware airplane radio / speaker audio UNVERIFIED) · 6 = independent QA DONE (PARTIAL: code PASS,
signed release **BLOCKED on signing creds**) + P0 closure (2026-11-08): "Ready offline" gated by REAL Media3
DownloadIndex + cache via OfflineReadinessVerifier; Play offline routes PLAY_DOWNLOADED fail-closed;
73/73 units + lint 0 + instrumented playback + on-device offline proof rerun; screenshots captured
(412dp/360dp/360dp@1.3×) but NOT independently pixel-reviewed. · N2 pending: `isLocalPlaying` direct-set
hardening (pre-existing, owner decision).
Authoritative spec: `docs/agent-loop/MORNICA_NIGERIA_FIRST_STREAMING_OFFLINE_SPEC.md`.

## Last verified state (2026-11-08)
- `main` = `7c6ade8` — PRs #11, #12, #13 MERGED.
- Branch `agent/antigravity-mornica-ui-polish-20261108` — UI polish + download persistence + P0 offline
  readiness closure; PRs #17 (this branch, DRAFT) targeting the Loop-4 chain head
  `agent/mornica-loop4-catalog-ui-20261107`. HEAD after P0 closure recorded in the completion report.
- WORKING TREE RECORD: UI polish recovered & refined; offline persistence
  invariant corrected (download store saves snapshot ONLY upon COMPLETED; interrupted/incomplete downloads
  never surface as completed on restart); **73/73 unit tests + 0 lint errors + debug APK build re-verified
  live** (SHA `dad1b417…` after P0 closure; earlier runs SHA `3f22b58f…`/`90105a…` — all build-run-specific,
  not source-drift claims).
- **Loop 5 airplane-mode offline playback DEVICE-VERIFIED** (2026-11-08, this session): with Wi-Fi + mobile
  data disabled (`svc wifi/data disable`; `ping` → Network is unreachable), the previously-downloaded
  afrobeat demo track renders `1 track saved · ready offline` + `Ready offline`, plays to 18.2s+ `PLAYING(3)`
  with zero network errors in logcat (bytes from the local SimpleCache), streaming-only track stays
  `Streaming only`, and `ready offline` survives force-stop + full-offline relaunch. Evidence in
  `docs/EXECUTION_LOG.md` 2026-11-08 Loop-5 entry + `screenshots/loop5-offline-ready.png`.

## Verification ledger (as of 2026-11-08)
- [x] unit-verified: `:app:testDebugUnitTest` 73/73 — gate 7, playback 7, catalog-download 8,
      download-store 6, acceptance 12, accessibility 7, download-control 11, catalog-section 4,
      offline-readiness 8 (P0 closure).
- [x] build-verified: `:app:assembleDebug` produces debug APK (SHA-256 dad1b417c225836bff81df08db39e402338f29f3deacb94eef7f129a527eed23, P0-closure build; run-specific)
- [x] lint-verified: `:app:lintDebug` green (0 errors)
- [x] device-verified: screencaps pulled at 360dp (`screenshots/mornica_360dp.png`), 412dp (`screenshots/mornica_412dp.png`), & default density (`screenshots/mornica_ui_polish_device.png`); uiautomator tree dumped on API 37 x86_64 emulator showing Mornica brand header ("Mornica" / "Your music, your way"), "Ready to play" catalog section, Wi-Fi downloads toggle, and bottom navigation.
- [ ] unverified now: physical speaker audible sound (needs human listen); real physical airplane-mode hardware toggle; real licensed catalogue backend (present is demo fixtures); independent QA + release candidate (loop 6).

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
