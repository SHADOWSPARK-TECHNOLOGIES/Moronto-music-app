# MORONTO MORNICA — Architectural Decision Records (ADR)

## ADR-001: Hybrid YouTube Music × Musicolet Dual-Engine Architecture (2026-10-19)

### Status
Accepted (`VERIFIED`)

### Context
Moronto Mornica requires:
1. Instant discovery of top Nigerian and Pan-African music videos and artist channels (informed by third-party vidIQ Nigerian monthly YouTube search volume estimates: Asake `207,295`, Davido `123,964`, Wizkid `94,555`, Burna Boy `94,014`, Rema `82,179`, Omah Lay `30,694`, Amapiano `25,401`).
2. A reliable, offline-first local audio player ("Musicolet-style") for user-owned device audio files (`MP3`, `FLAC`, `M4A`, `WAV`, `OGG`) and rights-cleared independent creator `DEMO` tracks.
3. Unconditional compliance with Google Play Developer Program Policies and YouTube API Services Terms of Service.

### Options Evaluated

| Option | Description | Pros | Cons / Risks | Decision |
| :--- | :--- | :--- | :--- | :--- |
| **Option 1: Unified Stream-Extractor Player** | Extract audio streams from YouTube URLs and feed them into the local audio queue/cache. | Single audio engine. | **Illegal / Policy Violation**: Violates YouTube ToS and Google Play Device & Network Abuse policy; guaranteed Play Store rejection/ban. | **Rejected** |
| **Option 2: Web-Only Wrapper without Native MediaStore** | Package a web page in a thin WebView without native Android `MediaStore` or `AudioManager` integration. | Fast initial prototype. | Poor offline folder/library experience; fails Musicolet-grade local playback expectations; weak Play Store utility value. | **Rejected** |
| **Option 3: Strict Dual-Adapter Architecture (`YouTubeDiscoveryRepository` + `LocalMediaRepository` + `PlaybackRouter`)** | Keep YouTube discovery/visible IFrame playback and Android `MediaStore` local audio playback in strictly isolated engines coordinated by a single `PlaybackRouter`. | 100% YouTube & Play policy compliant; full offline local library support; clear commercial separation. | Requires explicit arbitration when switching between video embeds and local audio. | **Selected** |

### Consequences & Ponytail Simplifications
- `PlaybackRouter` enforces single-active-engine arbitration: starting a YouTube embed immediately pauses local audio, and starting a local/demo track stops the active YouTube embed.
- `YouTubeDiscoveryRepository` caches official YouTube Data API v3 responses with a bounded `<= 24h` TTL (`# ponytail: simple in-memory + SharedPreferences JSON cache with 24h TTL avoids SQLite cache schema bloat until >500 cached queries`).
- Zero paid entitlements or gates wrap YouTube playback; monetization is strictly scoped to independent artist launch services.

---

## ADR-002: Native Kotlin + Jetpack Compose Targeting Android 16 / API 36.1 (2026-10-19)

### Status
Accepted (`VERIFIED`)

### Context
The October 19 launch contract targets Google Play Store submission readiness requiring `targetSdk = 36` (`compileSdk = 36` minor release `1`) using modern Android platform conventions.

### Decision
- Build the primary Android client in Kotlin 2.2.10 + Jetpack Compose (Material 3) with `compileSdk { version = release(36) { minorApiLevel = 1 } }`, `targetSdk = 36`, `minSdk = 26`.
- Use scoped storage permissions (`READ_MEDIA_AUDIO` on API 33+, `READ_EXTERNAL_STORAGE` `maxSdkVersion=32`) and standard Android `MediaStore.Audio.Media` queries so no broad `MANAGE_EXTERNAL_STORAGE` permission is ever requested.

---

## ADR-003: Media3 `LocalAudioPlaybackService` & `LocalPlaybackController` Contract (2026-10-20)

### Status
Accepted (`VERIFIED`)

### Context
1. Previously, `MainActivity` owned an Android `MediaPlayer` directly and released it in `onDestroy()`, terminating playback on background/configuration change.
2. `PlaybackRouter.seekLocalTo` only updated UI state without seeking the actual audio engine, and Shuffle, Repeat (`OFF`, `ALL`, `ONE`), Skip Next/Previous, and end-of-track transitions were not wired to a real playback controller.
3. Unverified YouTube video/channel IDs and unsupported `VERIFIED`/`LICENSED` claims existed in default seed repositories.

### Decision
- Introduce `LocalAudioController` and `Media3LocalAudioController` (`domain/PlaybackRouter.kt`) decoupled from UI components for deterministic unit testing (`ac8`..`ac10`).
- Implement `LocalAudioPlaybackService` (`domain/PlaybackRouter.kt`) extending AndroidX Media3 `MediaSessionService` backed by `ExoPlayer` with `AudioAttributes` (`USAGE_MEDIA`, `AUDIO_CONTENT_TYPE_MUSIC`, `handleAudioFocus = true`) and `setHandleAudioBecomingNoisy(true)` for automatic pause on headset unplug.
- Connect `LocalAudioPlaybackService` directly to `MornicaRuntime.router.attachAudioController(bridge)`, reporting live `currentPosition` and `duration` into `PlaybackRouter.onPlaybackPositionUpdated()`.
- Declare `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK` with `android:foregroundServiceType="mediaPlayback"` strictly for local/device audio, keeping YouTube playback visible-only (`ac5`, `ac8`).
- Replace unverified YouTube seed video/channel IDs with 7 verified official oEmbed video/channel IDs plus an honest empty state + official external YouTube search URL for rising/unmatched queries (`ac11`), and reclassify bundled synthesized WAV previews (`res/raw/mornica_afrobeats_demo.wav`) strictly as `ProvenanceStatus.DEMO`.

---

## ADR-004: ECC Design Recovery (`com.aistudio.morontomusic.xkpqr` Visual System + Native Compose Tokens) (2026-10-20)

### Status
Accepted (`VERIFIED`)

### Context
Visual comparison between the original Google AI Studio build (`com.aistudio.morontomusic.xkpqr`, screenshots `old-home.png` and `old-player.png`) and the initial Antigravity build (`new-home.png` and `new-player.png`) showed that the original app had significantly stronger visual hierarchy, surface contrast, navigation ergonomics, and player affordances.

### Decision
- Recover the original Midnight Slate (`#0B101B`), Slate Header (`#0E1525`), Slate Card (`#131D31` with `1.dp` `#24344D` hairline border), Emerald Mint (`#10B981`), Electric Cyan (`#06B6D4`), and Warm Amber-Orange Gradient (`#F59E0B` → `#EA580C`) palette in `MornicaTheme.kt`.
- Codify `MornicaTokens` (`CardOuterRadius = 16.dp`, `CardInnerPadding = 12.dp`, `CardInnerEmblemRadius = 10.dp`, `MinTouchTarget = 44.dp`, `withTabularNums()` OpenType `tnum` feature) and optical `PlayArrow` alignment (`+1.dp` horizontal offset) per ECC `design-system` and `make-interfaces-feel-better` skills.
- Restore the `MorontoTopHeader` (with `.statusBarsPadding()`), 5-tab Musicolet-inspired library sub-navigation (`Tracks`, `Queues`, `Albums`, `Artists`, `Folders`), live search + cyan rescan button, track counter + `Title / Artist / Duration / Year` sort pills, persistent `OfflineMiniPlayerBar`, and expandable full-screen `NowPlayingFullSheet` (`ac12`).


## 2026-10-25 — Native accessibility semantics without stack migration

Retain Kotlin/Compose/Material3 and existing PlaybackRouter contracts. Use native selectable/toggle components and additive Slider semantics instead of custom accessibility emulation. Reserve 48dp mini-player control space while preserving the compact visual. Add focused Compose tests using existing dependencies; do not add unrelated permissions, policy infrastructure or packages.

Rationale: platform-native semantics minimize custom code and preserve interaction behavior. Neither a language choice nor this patch removes future maintenance/security updates. All runtime claims remain pending the documented build/toolchain and device gates. Uploaded methodology guides workflow, without replacing host authority or permissions.
