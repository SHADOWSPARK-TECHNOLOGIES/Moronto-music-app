# MORONTO MORNICA — Execution & Verification Log (2026-10-20)

## 1. Environment & Toolchain Verification (`VERIFIED`)
- **OS & Kernel**: Omarchy `3.5.1` (Arch Linux rolling, kernel `7.2.5-3-omarchy` `x86_64`)
- **Java Toolchain**: OpenJDK Temurin `21.0.12.1+1-LTS` (`/home/moronto/.local/share/mise/installs/java/21`)
- **Android SDK**: `/home/moronto/Android/Sdk` (`platforms/android-36.1`, `build-tools/36.1.0`, `platform-tools/adb`)
- **Gradle Wrapper**: `Gradle 9.3.1` (`AGP 9.1.1`, `Kotlin 2.2.10`, `AndroidX Media3 1.5.1`)
- **REA MCP & Skill**: `rea-agents@6.5.0` (`state: "aligned"`, skill version `42`, `207` tools)
- **Ponytail & ECC Skills**: `ponytail` installed and active; workspace ECC skills active in `.agents/skills/{design-system,make-interfaces-feel-better,android-clean-architecture,verification-loop}/SKILL.md` alongside `.agents/agents/moronto-{architect,builder,growth,qa}/agent.md`.

---

## 2. Architectural, P0 Repair & ECC Design Recovery Updates (`VERIFIED`)
- Updated `docs/PRODUCT_SPEC.md`, `docs/decisions.md` (`ADR-001`..`ADR-004`), and `docs/COMMERCIAL_VALIDATION.md`.
- Implemented `LocalAudioPlaybackService` (Media3 `MediaSessionService` + `ExoPlayer`), `LocalAudioController`, and `Media3LocalAudioController` (`domain/PlaybackRouter.kt`) for true background local audio playback, audio focus handling, headset-unplug auto-pause, seek synchronization, Shuffle, Repeat (`OFF`, `ALL`, `ONE`), and Skip Next/Previous (`ac8`..`ac10`).
- Replaced unverified YouTube video/channel IDs in `YouTubeDiscoveryRepository` with 7 verified official oEmbed video/channel IDs (`ac11`), added an honest empty state + official external YouTube search URL for rising/unmatched queries, and enforced `ProvenanceStatus.DEMO` for bundled synth previews (`res/raw/mornica_afrobeats_demo.wav`).
- Recovered the visual design of `com.aistudio.morontomusic.xkpqr` (`old-home.png`, `old-player.png`) in `MornicaTheme.kt` (`MornicaTokens`, concentric radii, tabular numerals `tnum`, optical `PlayArrow` offset) and `MornicaApp.kt` (`MorontoTopHeader`, 5-tab Musicolet-style library navigation, live search + cyan rescan button, track counter + `Title / Artist / Duration / Year` sort pills, `OfflineMiniPlayerBar`, and expandable `NowPlayingFullSheet`, verified in `ac12`).
- Wired environment/property-driven `signingConfigs.create("release")` in `app/build.gradle.kts` (`UPLOAD_KEYSTORE_PATH` defaulting to `~/.local/share/moronto-mornica/signing/upload.jks`, `UPLOAD_KEYSTORE_PASSWORD`, `UPLOAD_KEY_ALIAS`, `UPLOAD_KEY_PASSWORD`) with zero hardcoded secrets.
- Registered `addSession(session)` in `LocalAudioPlaybackService.onCreate()` (`domain/PlaybackRouter.kt`) and `ensurePlaybackServiceStarted()` in `MainActivity.onStart()` + `MornicaRuntime.serviceStarter` (`MainActivity.kt`), guaranteeing automatic Media3 `MediaNotificationManager` promotion to `isForeground=true` (`foregroundId=1001`, `types=0x00000002` / `mediaPlayback`) and resilient service restart across Android 16 (API 36) idle timeouts.
- Added `docs/PRIVACY_POLICY.md`, `docs/PLAY_STORE_DECLARATIONS.md`, and the in-app **Open Public Privacy Policy** button in `CreatorHubScreen` (`ui/MornicaApp.kt`, `model/MornicaModels.kt`).

---

## 3. Automated Build, Unit Test, Lint & Binary Verification (`VERIFIED`)

| Gate | Command / Tool | Result | Evidence |
| :--- | :--- | :--- | :--- |
| **Unit & Integration Tests (`AC-1`..`AC-12`)** | `./gradlew testDebugUnitTest` | `PASS (12/12, 0 failures)` | `MornicaAcceptanceTest` (`ac1`..`ac12` verified) |
| **Android API 36 Debug APK Build** | `./gradlew assembleDebug` | `PASS` | `app/build/outputs/apk/debug/app-debug.apk` (`sha256: b74309ec3affbb22d8a3d4ad6f4e0941fdcf9d6389ea691ace62cb77ae75565a`) |
| **Android Lint Check** | `./gradlew lintDebug` | `PASS` | `app/build/reports/lint-results-debug.html` (`0` errors) |
| **Release AAB Bundle (`bundleRelease` + `jarsigner`)** | `./gradlew --no-daemon bundleRelease` + `jarsigner -verify -verbose -certs` | `PASS (SIGNED & VERIFIED)` | `app/build/outputs/bundle/release/app-release.aab` & `app-release-signed.aab` (`sha256: defd9c3f8265f1e1fe37595e826668b1c8e356317743eb29458c1e6be3c3ed6e`); signed with `~/.local/share/moronto-mornica/signing/upload.jks` (`alias: moronto-upload`, `CN=Stephen Okoronkwo, OU=Shadowspark Technologies, O=Shadowspark Technologies, L=Owerri, ST=Imo, C=NG`, `3072-bit RSA`, Cert `SHA-256: 15:8C:A6:62:7F:81:A1:89:73:37:48:6C:E1:83:CB:D1:B4:A6:DC:34:0A:9C:3F:71:07:20:97:57:8B:20:C8:EE`) |
| **SDK & Manifest Badging** | `aapt2 dump badging app-debug.apk` | `PASS` | `compileSdkVersion='36'`, `targetSdkVersion='36'`, `minSdkVersion='26'`, `versionCode='1'`, `versionName='1.0.0-oct19'` |
| **Merged Release Manifest** | `processReleaseMainManifest` | `PASS` | `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `.domain.LocalAudioPlaybackService` (`foregroundServiceType="mediaPlayback"`) |
| **Live Connected Device Run (`Infinix_X6877`)** | `adb install -r` + `dumpsys activity services` + `dumpsys media_session` + `screenrecord` | `PASS (VERIFIED LIVE)` | Device `0826317000000025` (`Infinix_X6877`, Android 16 / API 36, `1208x2644`): `369` real device `MediaStore` tracks scanned; `LocalAudioPlaybackService` verified in background with `isForeground=true foregroundId=1001 types=0x00000002` (`FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`) and `MediaSession` `state=PLAYING(3)`, `active=true`, `controllers: 10`, `queue size=368`. Recorded Play Console demonstration video at `/home/moronto/src/moronto-design-compare/fgs-media-playback-demo.mp4` (`3.8 MB`, `sha256: bda83a8a600fb303c8d2cb8ca5e4bc3e45c70ef930a76dd0b717b88c7d42866d`). |



## 2026-10-25 — Scoped Compose accessibility patch

Baseline: cdbb490ef2a6c722f51926c51bf962fa8d7f2a98. Isolated branch: fix/compose-accessibility. Orchestrator coordinated separate production, test and deployment/toolchain scopes.

Implemented: selected library-tab role/state; native favorite/shuffle toggle state; named seek slider with formatted time preserving native range/actions; 48dp mini-player outer click target retaining inner pill appearance. Existing callbacks, router contracts, dependencies, permissions and destinations unchanged.

Regression tests were authored before production changes. A test bug was caught during review: selected-tab assertions must exclude bottom navigation tabs. Independent source review found no blocking correctness issue and requested coverage for row favorite nested click dispatch; that coverage is being added in the same patch.

Verification evidence:
- Focused wrapper command: ./gradlew :app:testDebugUnitTest --tests 'com.shadowspark.morontomornica.MornicaAccessibilityTest' --stacktrace — BLOCKED before compilation: wrapper download java.net.SocketException Network is unreachable.
- Recovered official Gradle 9.3.1 ZIP via curl. SHA-256 matched pinned wrapper checksum b266d5ff6b90eada6dc3b20cb090e3731302e553a27c5d3e4df1f0d76beaff06; ZIP integrity passed.
- Extracted Gradle --version — PASS, Gradle 9.3.1 / JDK 17.0.20.
- Extracted Gradle --offline --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug — BLOCKED at settings.gradle.kts:15: org.gradle.toolchains.foojay-resolver-convention:1.0.0 unavailable in cache. Android SDK also not found in inspected conventional locations.
- git diff --check — PASS at integration inspection; this proves whitespace/patch integrity only.

No behavioral red-green result, compilation, lint, APK build, TalkBack or device pass is claimed. Narrow-screen/200% font layout remains untested and is not fixed by this patch. Timer/equalizer labels sharing a callback remain a separate product issue. No deployment performed; installed Play build correspondence remains unknown.

Security review of this scoped diff: no new dependency, credential, permission, network destination, signing or authorization change. This is not a repository-wide security audit. No DeepSeek connector or DEEPSEEK_API_KEY is exposed to this runtime; no secret search, transmission or provider call was made.

Next verification: use a trusted Android SDK/dependency-enabled checkout of this candidate; run full unit tests, lint and assembleDebug plus device/TalkBack checks. Reconcile package/version/signing and immutable source-to-artifact provenance before preparing release. Do not weaken checks to obtain a green build.

## 2026-10-25 — Verification-only handoff

Recovered the committed accessibility patch into a complete checkout after the temporary parent worktree directory expired. Added `.github/workflows/android-accessibility-checks.yml` on the candidate branch only: unit tests, lint and debug assemble, `contents: read`, SHA-named report artifacts, no release/signing step. Independent source review found no workflow blocker. The existing base-branch workflow still publishes `mornica-latest`; it was not changed or triggered by a base push.

Local Gradle 9.3.1 was recovered again from its pinned-checksum distribution. Standard Java proxy properties derived from the existing proxy environment allow task configuration to start; this is not yet a compilation or test result. Android SDK availability and package setup remain unresolved. Do not interpret a prepared workflow or artifact definition as a hosted pass or installable APK.

## 2026-11-08 — Loop-4 interaction audit (branch agent/mornica-loop4-as-inspect-20261107)

Green verification run: `./gradlew --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` =
BUILD SUCCESSFUL. **56/56 unit tests**, **0 lint errors** (35 warnings all informational:
GradleDependency/UseKtx/NewerVersionAvailable/UnusedResources/MonochromeLauncherIcon/OldTargetApi/
ObsoleteSdkInt/AndroidGradlePluginVersion), debug APK 20.4 MB produced.

Scoped UI changes (unit-tested, live-verified on emulator):
- `ui/LicensedCatalogSection.kt`: streaming-only tracks (offline NOT authorized) now render a muted
  "Streaming only · offline not licensed" label instead of a download/cancel control (no dead tap).
  Cut the two-paragraph intro to one subtitle line; tightened the Wi-Fi-only helper copy and spacing.
- `ui/MornicaApp.kt`: added `BackHandler` (expandable Now Playing sheet collapsed first, then return to
  My Music; disabled on the My Music home so system Back/exit still applies). The persistent mini player
  now also renders when a licensed-catalog track is active (previously it only appeared for local tracks).
- `domain/PlaybackRouter.kt`: `toggleLocalPlayPause()` is now engine-aware (resuming a playing catalog
  stream keeps `ActiveEngine.LICENSED_CATALOG` instead of relabelling it LOCAL_AUDIO).

On-device interaction audit (headless emulator, ADB uiautomator + taps + screencap/logcat; evidence PNGs
in `screenshots/audit-20261108/`):
- VERIFIED bottom nav on all 3 screens (Explore Videos external-YouTube cards, Creator Hub, My Music).
- VERIFIED all 5 library tabs switch content.
- VERIFIED Stream reaches a real PLAYING state on the live source (MediaSession position advanced
  0:00→0:21+; audio focus + ExoPlayer Init logged).
- VERIFIED persistent mini player shows for catalog playback ("… · 0:53 / 6:12").
- VERIFIED Download: queued banner + CatalogDownloadService FGS started + button → "Downloading…/Cancel".
- VERIFIED Cancel: cancelled banner + service FGS stopped + revert to "Download for later".
- VERIFIED streaming-only track exposes no download/cancel control.
- VERIFIED Back: Creator Hub → My Music; My Music home Back → launcher (app exits).
- BLOCKED on device — "Offline ready": a completed download never flips the card. Root cause:
  `MornicaRuntime.router` builds `PlaybackRouter` without a `downloadStore` (ctor default null) and no
  code observes DownloadManager completion to set `catalogDownloadStates[id]=COMPLETED`, so
  `offlinePlaybackAllowed` is always false at runtime and the card stays "Downloading…" forever.
  Offline render/playback is proven only by the Loop-3 instrumented tests. Recommended fix (not in scope
  here): persist a real `DownloadStore` + observe `DownloadManager.Listener` completion → router
  COMPLETED. Not merged or published.

  > **HISTORICAL / SUPERSEDED (2026-11-08, Loop-6).** This defect was fixed in the Antigravity UI-polish
  > branch (`9bf463f`: attachable `downloadStore` + `onDownloadStateChanged` + `DownloadManager.Listener`
  > completion → `COMPLETED`) and its runtime behaviour was positively re-verified in the Loop-5
  > airplane-mode proof below. The Loop-6 closure additionally hardened it: "Ready offline" is now gated
  > by the real Media3 `DownloadIndex` + cache bytes via `OfflineReadinessVerifier` (see the Loop-6
  > section), so a stored rights snapshot alone can never show ready-offline.
  > This paragraph is preserved verbatim as historical evidence; do not treat it as current state.

## 2026-11-08 — Loop-5 airplane-mode offline playback proof (branch agent/antigravity-mornica-ui-polish-20261108)

Took over from the Antigravity UI-polish branch (HEAD `9bf463f`). First re-verified the recorded baseline before
working (STATE notes are self-reports, not evidence): `./gradlew --no-daemon :app:testDebugUnitTest
:app:lintDebug :app:assembleDebug` = BUILD SUCCESSFUL, **65/65 unit tests / 0 failures / 0 lint errors**,
debug APK 20.4 MB (`sha256 3f22b58fb5dc386c5cb7f0ef248475f7132f8fb454eeb7c3fa9ba0a8ae424f04` — run-specific,
differs from the older `90105a…` recorded in STATE.md; hashes are build-run-specific, not claims of source drift).

Then exercised the smallest next objective per STATE.md active loop 5 / sync-prompt PHASE 4:
**device-verified airplane-mode offline playback** of a previously-licensed downloaded catalog track, on the
Antigravity branch build.

On-device (headless API-37 x86_64 emulator `emulator-5554`, AVD `Medium_Phone`, freshly installed
`-r -g` the tree's APK; offline snapshot in `no_backup/catalog_downloads.json` for
`catalog-demo-afrobeat-01` PRESERVED across reinstall):
- Isolated network: `svc wifi disable` + `svc data disable` (literal `AIRPLANE_MODE` broadcast is
  Permission-denied from shell, as expected). `ping 8.8.8.8` → `Network is unreachable`, 0 active networks.
- [x] **`1 track saved · ready offline`** + afrobeat card shows a **"Ready offline"** control (uiautomator:
  text nodes `1 track saved · ready offline` bounds[37,560][459,602]; `Ready offline` bounds[320,863][534,916])
  — i.e. the Antigravity Offline-Ready wiring (attachDownloadStore + DownloadManager completion → router
  COMPLETED) surfaces at runtime, previously BLOCKED.
- [x] Streaming-only demo track still renders **"Streaming only"** (no download control) while offline — gate
  fail-closed preserved.
- [x] Tap Play → MediaSession `state=PLAYING(3)` progressing 0→3.1s→**18.2s**; audio MediaCodec initialized,
  audio focus granted; **zero** network errors in logcat (no ExoPlaybackException/IoException/Socket/SSL), all
  bytes served from the local SimpleCache (`cache/mornica-media3-cache/*.v3.exo`, 11M) with RF unreachable.
- [x] Force-stop → relaunch fully offline → card still reads `1 track saved · ready offline` + `Ready offline`
  (offline-ready survives process restart — the persistence-invariant fix).
- Evidence: `screenshots/loop5-offline-ready.png` (`sha256 a1090204…`), uiautomator trees in this run.

Verdict: **Loop-5 airplane-mode offline playback DEVICE-VERIFIED** on this emulator (structural/uiautomator +
MediaSession/logcat, not pixel-reviewed; audible sound on a physical speaker still needs a human listen).
No code change was required this cycle — this was pure verification of the branch that was previously only
unit/instrumented-verified. No push/merge/publish.  Physical-device radio toggle remains UNVERIFIED.

## 2026-11-08 — Loop-6 independent QA + release-readiness gate (branch agent/antigravity-mornica-ui-polish-20261108)

Opened **PR #17** (`feat(android): Mornica UI polish + Offline-Ready fix & Loop-5 airplane-mode offline
proof`) — base `agent/mornica-loop4-catalog-ui-20261107` (PR #14 head) per the repo's chained-PR convention;
branch↔PR alignment verified (`12a3874` == headRefOid). Pushed `12a3874` (owner-approved). SATISFIED AGENTS.md:
no automated CI runs on this base (both workflows only trigger on
`agent/antigravity-mornica-launch-contract-20261019`) — flagged on the PR; verification stands on the
local run + on-device proof.

Independent skeptical QA subagent (moronto-qa contract, read-only `base..HEAD`, re-ran tests with
`--rerun-tasks`, did not trust claims) returned **PARTIAL**: code-level PASS, release-level incomplete.
Reconciled on the PR:
- Real numbers (QA-observed): **65/65 unit tests / 0 fail / 0 error / 0 skipped**, **lint 0 errors / 34
  warnings**, debug APK sha256 `3f22b58f…` (20,394,879 B).
- VERIFIED: repo hygiene (only intended files; no test deletions), rights/provenance (licenseReference
  only `DEMO-CATALOG-*`, `rightsConfirmed=true` only on demo fixtures, evaluate() default-denies and runs
  before enqueue/play, YouTube hosts blocked at gate + allowlist `{soundhelix.com}`), no secrets leaked
  (`YOUTUBE_API_KEY` BuildConfig empty), offline fail-closed (record persisted only on STATE_COMPLETED;
  reconcile purges non-COMPLETED; cancel/FAILED removes; `offlinePlaybackAllowed` requires COMPLETED +
  mayPlayOffline).
- F1 (Play streams via URI-keyed cache, no dedicated offline file-read): addressed empirically by Loop-5
  device proof; durable instrumented test still requested (future cycle). N3 "no device evidence" corrected
  (on-device Loop-5 proof on record); multimodal pixel review still pending. N2 `isLocalPlaying` direct-set
  tension pre-existing (PlaybackRouter.kt :283/:471/:513/:534/:677/:689/:700) — separate AGENTS.md-hardening
  item, needs owner decision. N4 AGP 9.1.1→9.5.1 / Gradle 9.3.1→9.6.0 from baseline commit `329c74f`, not
  this change.
- **Release gate: BLOCKED — signing creds unset** (`UPLOAD_KEYSTORE_ALIAS`/`UPLOAD_KEYSTORE_PASSWORD`/
  `UPLOAD_KEY_PASSWORD` and STORE/KEY equivalents all absent; keystore present but unusable). No signed
  release APK/AAB can be produced until the owner supplies credentials. No merge/publish/Play Console action.
  QA audit transcript: `/home/moronto/.hermes/cache/delegation/live/deleg_5a13090e/task-0.log`.

## 2026-11-08 — P0 release-candidate closure (Loop-6 hardened, branch agent/antigravity-mornica-ui-polish-20261108)

Closed the P0 acceptance gates flagged by the independent QA (F1/N3/N2 concerns). Additive change; NOT
merging/releasing. No signing change, no Play Console action, no secret touch.

**Gate 1+2 — "Ready offline" driven by the REAL Media3 DownloadIndex + cache, not the stored snapshot.**
- New `domain/OfflineReadinessVerifier.kt`: `OfflineReadinessVerifier` seam + production
  `Media3OfflineReadinessVerifier` that reads `CatalogDownloads.realDownloadManager(context).downloadIndex`
  (`Download.STATE_COMPLETED`) AND the shared `MediaCache.cache(context).getCachedBytes(mediaUrl,0,MAX) > 0`.
  Two defensive `runCatching`s fail closed on any I/O error.
- `PlaybackRouter`: `offlinePlaybackAllowed()` now requires state COMPLETED + valid stored snapshot AND
  (when a verifier is attached) genuine index/cache readiness. `attachOfflineReadiness()` re-checks and
  drops any COMPLETED state the real engine can no longer honour (legacy/interrupted/evicted/corrupt).
  `attachDownloadStore()` re-hydrates COMPLETED only when the verifier confirms bytes (when attached).
- New `playDownloadedCatalogTrack()` routes offline play through the `PLAY_DOWNLOADED` gate
  (`downloadCompleted = true`) and refuses (banner) if not genuinely ready — a COMPLETED state alone can
  never override a failed offline entitlement.
- `MornicaRuntime.wireCatalogDownloads()` attaches `OfflineReadinessVerifier.real(context)`.
- `ui/LicensedCatalogSection.kt`: Play button + "Ready offline" route via `onPlayOffline` →
  `playDownloadedCatalogTrack`; "N tracks saved" count now uses `offlinePlaybackAllowed(track)` (genuine)
  not raw COMPLETED map entries.
- New `OfflineReadinessVerifierTest.kt` (8 tests) covering: legacy record no bytes; stale COMPLETED
  dropped on attach; verifier-confirm allows; expired rights block even with bytes; legacy-not-ready
  restart; Play fail-closed when not ready; Play allowed when ready; failed download never ready.

**Verification (Gate 4) — real numbers from this run:**
- `:app:testDebugUnitTest` = **73/73 pass, 0 fail, 0 error, 0 skipped** (9 classes; +8 new verifier tests).
- `:app:lintDebug` = **0 errors, 34 warnings** (unchanged informational set).
- `:app:assembleDebug` = debug APK **sha256 `dad1b417c225836bff81df08db39e402338f29f3deacb94eef7f129a527eed23`**, 20,549,045 B. Source = branch HEAD after this change (to be committed).
- `:app:connectedDebugAndroidTest -P…class=LicensedCatalogPlaybackInstrumentedTest` = **1 test, 0 failures, 0 errors** on `Medium_Phone` (API-36 emulator).
- On-device Gate-1 proof (current build): after `-r -g` reinstall (app data reset, so no downloads present,
  correct fail-closed "Save offline"), drove a real download to completion → card flipped to
  "1 track saved · ready offline" + "Ready offline"; persisted `no_backup/catalog_downloads.json`;
  `exoplayer_internal.db` populated. Then disabled Wi-Fi+data (`ping` → Network is unreachable),
  force-stop + relaunch fully offline → "ready offline" survived restart, tapped Play → `PLAYING(3)` at
  3.5s with **zero network errors** — bytes genuinely from cache, gated by the real verifier.

**Gate 3 — theme/header/copy preserved; screenshots captured (NOT independently pixel-reviewed).**
- No change to `MornicaTheme.kt` palette, the `Mornica` header/copy, or Nigerian-friendly strings this
  closure. Structural uiautomator on the current build confirms brand header ("Mornica" / "Your music,
  your way"), "Ready to play" section, Wi-Fi downloads toggle, 5 library tabs, bottom nav unchanged.
- Captured for review: `screenshots/rc_412dp.png`, `screenshots/rc_360dp.png`,
  `screenshots/rc_360dp_13.png` (1080×2400 each; 412dp@420dpi, 360dp via wm density 480, and 360dp+1.3×
  font via `settings put system font_scale 1.3`). These are **captured evidence, NOT independently
  visually reviewed** — the host model is non-multimodal, so pixel-level visual review (incl. 1.3× font
  legibility) remains UNVERIFIED pending a multimodal/human look.

**Gate 6 — commit + push + DRAFT PR (this authorizes draft-PR prep only, not merge/publish).**
- Committed the closure on the isolated branch, pushed, and converted/open a DRAFT PR to the correct
  integration base (PR #14 head, the same chain used by the prior non-draft PR #17). See PR URL / HEAD /
  APK hash in the completion report.

**Gate 7 — independent review requested.** The isolated offline-readiness change deserves a fresh
independent pass (Media3 playback-state handling, offline rights, navigation, accessibility, final APK)
before any release; noted as outstanding. No production merge, release signing, Play upload, or
alternative-store publication performed.

## 2026-11-08 — Gate-7 independent re-review of the P0 closure + wiring-order fix

A second independent skeptical QA pass ran over the closure (delegated; full log
`/home/moronto/.hermes/cache/delegation/live/deleg_ba9846c8/task-0.log`). Verdict by source-reading:
**PASS on gate logic** — the offline gate is genuinely fail-closed; a COMPLETED state alone cannot
bypass Play-offline; no wrong cache key; YouTube/rights invariants intact; no secrets in the diff;
verifier==null preserves the old snapshot-only path for the pre-existing unit tests.

One real, correct finding → FIXED:
- `attachDownloadStore`'s bytes-check was dead at runtime because `wireCatalogDownloads` called it
  BEFORE `attachOfflineReadiness` (so `offlineReadiness` was still null and `?: true` always won). The
  end-state was still safe only because the later attach dropped stale COMPLETED, but the intent was not
  honoured. **Fixed by attaching the verifier first, then the store** — so persisted legacy/interrupted/
  evicted records are genuinely not rehydrated as "ready" at startup.
  Re-verified: `:app:testDebugUnitTest` = **73/73 / 0 failures**, `:app:lintDebug` = 0 errors,
  `:app:assembleDebug` APK = sha256 `096299bc70a85b00f7ed465bf1d3c6059e68d670527ac385153acab2bc7018ff`
  (20,549,045 B) — build-run-specific.

Non-blocking follow-ups recorded (not blockers, not fixed this closure to keep it scoped):
- Main-thread I/O in `downloadedCount` (per-card `DownloadIndex` SQLite read + `SimpleCache` bytes) is a
  jank risk once a real catalogue exceeds ~10 tracks; acceptable now (demo = 2 tracks) but should move to
  an async/cached readiness flow before production scale.
- `evaluate(PLAY_DOWNLOADED)` still requires `streamAllowed` even for offline play — over-strict, not a
  bypass; keep unless a real stream-not/offline-yes entitlement exists.
- N2 `isLocalPlaying` direct-set hardening remains out of scope (pre-existing, owner decision).

