# Privacy Policy — Moronto Mornica

**Effective Date:** October 20, 2026  
**Developer / Data Controller:** ShadowSpark Technologies  
**Application:** Moronto Mornica (`com.shadowspark.morontomornica`)  
**Contact Email:** privacy@shadowspark.tech (or via GitHub Issues at `https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/issues`)

---

## 1. Overview

Moronto Mornica ("the App") is an artist-first African music discovery, offline local audio player, and independent creator promotion application developed by ShadowSpark Technologies. We design the App around strict local-first privacy: your personal device music library stays on your device, and we never sell user data.

---

## 2. Information We Access and How It Is Used

### A. On-Device Audio Files (`android.permission.READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`)
- **What we access:** When you grant audio library permission, the App queries Android `MediaStore.Audio.Media` on your device (track title, artist, album, folder name, duration, release year, and local content URI).
- **How it is used:** Strictly on-device to organize and play your personal audio files (`MP3`, `FLAC`, `M4A`, `WAV`, `OGG`) in the **My Music** tabs (*Tracks*, *Queues*, *Albums*, *Artists*, *Folders*) and in our background `MediaSessionService` (`LocalAudioPlaybackService`).
- **No server upload:** Your local audio files and library metadata are **never uploaded, transmitted, or synced** to ShadowSpark Technologies servers or any third party.

### B. Foreground Service for Offline Audio Playback (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`)
- **How it is used:** When you play a local device audio file or a bundled first-party synth demo preview, the App runs an AndroidX Media3 `MediaSessionService` (`LocalAudioPlaybackService`) so your offline audio continues playing when you switch apps or lock your screen, with standard system notification and lock-screen transport controls.
- **No background tracking:** This service does not collect location, microphone, sensor, or behavioral telemetry.

### C. Voluntary Creator Launch Package Inquiries (Creator Hub)
- **What you may provide:** If you are an independent artist or manager and voluntarily submit the **Request Artist Launch Package** form in the **Creator Hub**, you enter your *Artist/Label Name*, *Track/EP Title*, *Genre*, *Contact Email or WhatsApp*, and *Rights Ownership Attestation*.
- **How it is used:** Used solely to respond to your commercial onboarding inquiry and verify master/publishing rights ownership before featuring any creator demo.
- **Retention & Deletion:** In the current MVP build, submitted inquiries are held in local session state until cleared or explicitly sent by you. You may request deletion of any correspondence at any time by contacting `privacy@shadowspark.tech`.

### D. YouTube Discovery & Official Embedded Player (`android.permission.INTERNET`)
- **How it works:** The **Discover & Watch** tab displays official music videos using YouTube's official embedded IFrame player (`https://www.youtube.com/embed/{videoId}`) and official YouTube Data API v3 / oEmbed metadata, or opens links directly in the official YouTube app/website (`https://www.youtube.com/watch?v={videoId}`).
- **Third-Party Policies:** Interacting with the embedded YouTube player connects directly to Google/YouTube servers over HTTPS (`TLS`) and is governed by the [Google Privacy Policy](https://policies.google.com/privacy) and [YouTube Terms of Service](https://www.youtube.com/t/terms).
- **No stream extraction:** Moronto Mornica never downloads, rips, caches, or extracts audio/video streams from YouTube, and automatically pauses any embedded YouTube video when the App enters the background.

---

## 3. Data Sharing and Third-Party SDKs

- **No Advertising or Analytics SDKs:** The App contains zero third-party behavioral advertising SDKs, cross-app trackers, or data brokers.
- **No Sale of Personal Data:** ShadowSpark Technologies does not sell or rent user data to any third party.

---

## 4. Data Security

All network requests made by the App (specifically to official YouTube HTTPS endpoints) are encrypted in transit using industry-standard HTTPS/TLS. Local audio playback occurs entirely within the Android application sandbox on your device.

---

## 5. Children's Privacy

Moronto Mornica is intended for a general audience (ages 13+) and does not knowingly collect personal information from children under the age of 13.

---

## 6. Your Rights and Data Deletion

- **Revoking Permissions:** You may revoke `READ_MEDIA_AUDIO` permission at any time in **Android Settings → Apps → Moronto Mornica → Permissions**. When revoked, the App immediately falls back to bundled first-party synth demo previews.
- **Clearing App Data:** You may erase all local app state at any time via **Android Settings → Apps → Moronto Mornica → Storage & cache → Clear storage**.
- **Contact & Deletion Requests:** To request access to or deletion of any Creator Launch inquiry submitted to ShadowSpark Technologies, email `privacy@shadowspark.tech` or open a request at `https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/issues`.
