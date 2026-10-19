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
