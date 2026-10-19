# MORONTO MORNICA — Product & Architectural Specification (2026-10-19)

## 1. Product Thesis & Core Positioning

**Moronto Mornica** ("Music for Real People · African music at the center") is an African-first music discovery, offline library playback, and independent creator promotion platform built by **ShadowSpark Technologies**.

It combines two complementary user experiences under one cohesive dark-neon Material 3 interface while maintaining a **strict technical and legal boundary** between their underlying media engines:

1. **Discover & Watch (YouTube Discovery Layer)**: Artist-first discovery, official music video embeds, artist channel shelves, and curated Afrobeats / Amapiano editorial discovery powered by official **YouTube Data API v3** metadata and the **YouTube IFrame Player**.
2. **My Music (Musicolet-Inspired Offline Audio Layer)**: A fast, zero-cloud-dependency local audio player for user-owned device files (`MP3`, `FLAC`, `M4A`, `WAV`, `OGG`) plus rights-cleared `DEMO` previews, featuring folder/artist/album/song browsing, persistent queues, playlists, shuffle/repeat modes, audio focus management, and headset-unplug protection.
3. **Creator Launch Hub (Commercial Services Layer)**: Verified independent artist profiles, rights-attested demo showcase tracks, and an opt-in **"Request Artist Launch Package"** commercial intake workflow.

---

## 2. Market Research Input & Epistemic Framing

Product discovery defaults and initial artist/genre shelves are informed by third-party **vidIQ monthly Nigerian YouTube search estimates**:

| Search Query | Estimated Monthly Nigerian YouTube Searches (vidIQ) | Classification | Initial Product Shelf Role |
| :--- | ---: | :--- | :--- |
| **Asake** | `207,295` | `INFERRED (Third-Party Estimate)` | Tier-1 Artist Quick-Chip & Street-Pop Anchor |
| **Davido** | `123,964` | `INFERRED (Third-Party Estimate)` | Tier-1 Artist Quick-Chip & Afrobeats Essentials |
| **Wizkid** | `94,555` | `INFERRED (Third-Party Estimate)` | Tier-1 Artist Quick-Chip & Afrobeats Essentials |
| **Burna Boy** | `94,014` | `INFERRED (Third-Party Estimate)` | Tier-1 Artist Quick-Chip & Afro-Fusion Shelf |
| **Rema** | `82,179` | `INFERRED (Third-Party Estimate)` | Tier-1 Artist Quick-Chip & Global Afrobeats |
| **Omah Lay** | `30,694` | `INFERRED (Third-Party Estimate)` | Mid-Tier Artist Quick-Chip & Afro-Soul Discovery |
| **Amapiano** | `25,401` | `INFERRED (Third-Party Estimate)` | Genre Pillar Shelf & Log-Drum Crossover |

> **Mandatory Framing Rule**: These figures are treated strictly as **directional third-party search estimates** indicating strong existing Nigerian audience intent on YouTube for top Afrobeats artists and Amapiano. They are **never** presented in product copy, investor docs, or telemetry as guaranteed app installs, active listeners, or revenue projections.

---

## 3. Non-Negotiable Policy & Legal Guardrails

1. **Zero Stream Extraction or Downloading**: Moronto Mornica never downloads, rips, caches, muxes, or extracts raw audio/video streams from YouTube (`yt-dlp`, `newpipe`, or undocumented `googlevideo.com` URLs are strictly prohibited).
2. **Visible Official Player Only**: Embedded YouTube playback uses the official YouTube IFrame Player inside a visible viewport (`>= 200dp x 200dp`) with native YouTube branding and controls intact. When the app enters the background or the screen turns off, YouTube playback is paused immediately.
3. **External YouTube Fallback**: When a video owner disables embedding (`error 101` / `150`) or when the user prefers the native app, Moronto Mornica provides a clearly labeled **"Watch on YouTube"** deep-link action (`https://www.youtube.com/watch?v={videoId}`).
4. **Strict Monetization Separation**: Embedded YouTube playback is **100% free and never gated** behind subscriptions, coins, or paywalls. Commercial monetization applies strictly to independent creator promotion services and verified artist profile packages.
5. **YouTube API Quota & Cache Discipline**: Responses from YouTube Data API v3 (`search.list`, `videos.list`, `channels.list`) are cached for at most **24 hours (`TTL <= 86,400s`)** in a bounded store to protect daily quota (`search.list` = 100 units; `videos.list` / `channels.list` = 1 unit) and degrade gracefully to curated fallback metadata when offline or when `YOUTUBE_API_KEY` is unconfigured.

---

## 4. Three-Domain System Architecture

### Domain A — Discover & Watch (`YouTubeDiscoveryRepository` + `DiscoverScreen`)
- **Instant Artist & Genre Chips**: Pre-seeded with the 7 research-backed queries (*Asake*, *Davido*, *Wizkid*, *Burna Boy*, *Rema*, *Omah Lay*, *Amapiano*) ordered by estimated Nigerian search intent, plus a spotlight lane for **Rising Independent Creators**.
- **Official Metadata Adapter**: Queries YouTube Data API v3 when `YOUTUBE_API_KEY` is present, enriching results via `videos.list` and `channels.list`, and falls back deterministically to curated official metadata with an explicit `CURATED / OFFLINE FALLBACK` badge when unconfigured, offline, or quota-limited (`HTTP 403/429`).
- **Visible Embedded Player & External Launch**: Renders the official YouTube IFrame embed in a visible card (`>= 200dp` height) and provides a direct **"Open in YouTube"** intent button.

### Domain B — My Music (`LocalMediaRepository` + `LocalAudioPlayer` + `MyMusicScreen`)
- **Device MediaStore Integration**: Scans user-owned local audio files (`MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`) with runtime permission handling (`READ_MEDIA_AUDIO` on Android 13+ / API 33+, `READ_EXTERNAL_STORAGE` on API 26–32).
- **Musicolet-Style Library Views**: Organizes local tracks by **Songs**, **Folders**, **Artists**, **Albums**, and **Playlists**, alongside bundled rights-cleared `DEMO` tracks so the player works immediately on fresh devices.
- **Offline Queue & Transport Controls**: Play, pause, next, previous, seek, shuffle, repeat mode (`OFF`, `ALL`, `ONE`), persistent queue/favorites storage, Android audio focus handling, and automatic pause on `AudioManager.ACTION_AUDIO_BECOMING_NOISY` (headset disconnect).
- **Strict Media Engine Separation**: `LocalMediaTrack` URIs only accept `content://`, `file://`, or `android.resource://` schemes—never YouTube URLs or `videoId`s.

### Domain C — Mornica Experience & Creator Launch Hub (`PlaybackRouter` + `CreatorHubScreen`)
- **Single-Active-Source Arbitration (`PlaybackRouter`)**: Starting a YouTube embed automatically pauses local audio playback; starting a local or demo audio track automatically pauses/collapses any active YouTube embed.
- **Verified Independent Artist Profiles**: Displays provenance-attested independent artist cards (`rightsConfirmed = true`), up to 3 gesture-initiated `DEMO` audio previews, and an opt-in **"Request Artist Launch Package"** lead capture form with strict field validation (`artistName`, `contactEmailOrWhatsApp`, `trackTitle`, `rightsAttested == true`).

---

## 5. Android Target & Google Play Launch Readiness (October 19 Contract)

- **Application ID**: `com.shadowspark.morontomornica`
- **SDK Targets**: `compileSdk = 36 (minorApiLevel = 1)`, `targetSdk = 36`, `minSdk = 26`
- **UI Stack**: Kotlin 2.2 + Jetpack Compose + Material 3 dark neon Afrobeats aesthetic (deep purple `#0D0714`, electric magenta `#FF2E93`, cyber blue `#00E5FF`).
- **Permissions**:
  - `android.permission.INTERNET` (YouTube embeds & official metadata)
  - `android.permission.READ_MEDIA_AUDIO` (Android 13+ user-selected/device audio library)
  - `android.permission.READ_EXTERNAL_STORAGE` (`maxSdkVersion="32"` for older devices)
  - `android.permission.FOREGROUND_SERVICE` & `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK` (local audio playback continuity only)

---

## 6. Objective Acceptance Criteria

| ID | Criterion | Verification Method |
| :--- | :--- | :--- |
| **AC-1** | **Engine Isolation**: Local audio player rejects YouTube URLs/IDs; YouTube player never invokes download/stream-extraction routines. | Automated unit tests |
| **AC-2** | **Playback Arbitration**: Starting YouTube pauses local audio; starting local audio stops YouTube embed. | Automated unit tests |
| **AC-3** | **Research-Seeded Discovery**: All 7 vidIQ queries (*Asake*, *Davido*, *Wizkid*, *Burna Boy*, *Rema*, *Omah Lay*, *Amapiano*) are present with third-party estimate framing. | Automated unit tests |
| **AC-4** | **Quota-Aware Caching & Fallback**: YouTube API cache enforces `TTL <= 24h` and gracefully degrades on missing key or `403/429` error. | Automated unit tests |
| **AC-5** | **Visible Player & Background Pause**: Embedded YouTube player remains visible (`>= 200dp`) and pauses on lifecycle background/stop. | Component/lifecycle tests |
| **AC-6** | **Musicolet-Style Offline Library**: Supports Songs, Folders, Artists, Albums, Queue, Shuffle/Repeat, and honest permission/empty states. | Automated unit tests |
| **AC-7** | **Entitlement Separation & Creator Intake**: Zero paywall on YouTube playback; Creator Launch Package form requires explicit rights attestation. | Automated unit tests |
| **AC-8** | **Android API 36 Build, Lint, Test & REA Audit**: `./gradlew test`, `./gradlew lint`, `./gradlew assembleDebug`, and REA APK inspection pass with 0 errors. | CLI & REA MCP output |

