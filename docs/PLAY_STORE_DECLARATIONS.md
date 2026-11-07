# MORONTO MORNICA — Google Play Console Declarations & Review Packet (2026-10-20)

This packet contains the verified, copy-paste-ready declarations for **Google Play Console → App Content** for `com.shadowspark.morontomornica` (`versionCode = 1`, `versionName = "1.0.0-oct19"`, `targetSdk = 36`).

---

## 1. Public Privacy Policy URL (`VERIFIED`)

- **Public GitHub Blob URL (HTML-rendered):**  
  `https://github.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/blob/agent/antigravity-mornica-launch-contract-20261019/docs/PRIVACY_POLICY.md`
- **Public Raw HTTPS URL:**  
  `https://raw.githubusercontent.com/SHADOWSPARK-TECHNOLOGIES/Moronto-music-app/agent/antigravity-mornica-launch-contract-20261019/docs/PRIVACY_POLICY.md`
- **In-App Link:** Wired directly in `CreatorHubScreen` (`app/src/main/java/com/shadowspark/morontomornica/ui/MornicaApp.kt`) via the **"Open Public Privacy Policy"** button (`Intent.ACTION_VIEW`).

---

## 2. Google Play Data Safety Questionnaire Answers (`VERIFIED`)

### Step 1: Data Collection and Security
| Question | Answer | Evidence / Code Reference |
| :--- | :--- | :--- |
| **Does your app collect or share any of the required user data types?** | **No** *(for the standalone MVP build)* | `LocalMediaRepository.queryDeviceMediaStore()` processes `MediaStore.Audio.Media` strictly on-device (`0` network upload calls). `PlaybackRouter.submitArtistLaunchRequest()` validates and stores Creator Hub demo inquiries in local in-memory state only (`0` backend/analytics endpoints in `1.0.0-oct19`). |
| *(Note if you later connect a backend endpoint for the Creator Hub form)* | Select **Yes** → **Personal Info (Name, Email address, Phone number)** → *Collected, Optional, App functionality / Developer communications, Encrypted in transit, User can request deletion*. | Only needed once an external HTTP submission endpoint is enabled. For `1.0.0-oct19`, all form validation stays on-device. |

### Step 2: Audio & Media Files Disclosure Note
- **Audio files (`android.permission.READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`):** Under Google Play Data Safety rules, accessing user audio files purely locally on the device without transmitting them off the device **does not constitute "collection" or "sharing."**
- **Embedded YouTube Player (`WebView` loading `https://www.youtube.com/embed/{videoId}`):** Disclosed in `docs/PRIVACY_POLICY.md` Section 2.D. Uses HTTPS (`TLS`) exclusively.

---

## 3. Content Rating (IARC) Questionnaire Answers (`VERIFIED`)

- **App Category:** **Utility, Productivity, Communication, or Other** *(or Music & Audio / Entertainment)*
- **Questionnaire Answers:**
  - **Violence / Blood / Gore:** **No**
  - **Fear / Horrifying Content:** **No**
  - **Sexuality / Nudity:** **No**
  - **Simulated Gambling / Real-Money Gambling:** **No**
  - **Language (Profanity / Crude Humor in bundled app content):** **No** *(Bundled demo tracks in `res/raw/mornica_afrobeats_demo.wav` are instrumental synth previews; official YouTube embeds stream standard YouTube music videos).*
  - **Controlled Substances (Drugs / Alcohol / Tobacco references in bundled app):** **No**
  - **User-Generated Content / User-to-User Interaction:** **No** *(Users cannot chat, post public comments, share files with other users, or share physical location).*
  - **Unrestricted Web Access:** **No** *(The in-app `WebView` is restricted strictly to official YouTube video embeds `https://www.youtube.com/embed/{videoId}`).*
  - **Digital Purchases / Paid Subscriptions for YouTube:** **No** *(100% of YouTube discovery and local audio playback is free and unpaywalled).*
- **Expected IARC Rating:** **Rated for 3+ / Everyone** (or **Teen / 12+** where music video streaming descriptors apply).

---

## 4. Foreground Service (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`) Declaration (`VERIFIED`)

Because `AndroidManifest.xml` declares:
- `android.permission.FOREGROUND_SERVICE`
- `android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `<service android:name="com.shadowspark.morontomornica.domain.LocalAudioPlaybackService" android:exported="true" android:foregroundServiceType="mediaPlayback">`

complete **Google Play Console → App Content → Foreground service permissions** as follows:

1. **Foreground Service Type Selected:** `Media playback` (`FOREGROUND_SERVICE_MEDIA_PLAYBACK`)
2. **Use Case Checkbox:**
   - [x] **Audio playback** (Playing music, podcasts, audiobooks, or other audio that continues when the user switches apps or turns off the screen)
3. **App Functionality Justification (Copy-Paste into Play Console):**
   > Moronto Mornica is an offline local music player ("My Music") paired with an official visible YouTube discovery tab ("Discover & Watch"). The `FOREGROUND_SERVICE_MEDIA_PLAYBACK` permission is used exclusively by `com.shadowspark.morontomornica.domain.LocalAudioPlaybackService` (an AndroidX Media3 `MediaSessionService` backed by `ExoPlayer`) so that when a user plays their own local device audio files (`MediaStore.Audio.Media`) or bundled first-party instrumental demo tracks, audio playback continues seamlessly when the user presses Home, switches apps, or locks the device screen, and exposes standard Android system media notification and lock-screen transport controls (Play/Pause, Seek, Skip Next/Previous). Embedded YouTube videos never use the foreground service and automatically pause when the app enters the background.
4. **Demonstration Video Evidence (`VERIFIED`):**
   - **Recorded on Physical Device:** `Infinix_X6877` (`0826317000000025`, Android 16 / API 36, `1208x2644`)
   - **Local Video Artifact Path:** `/home/moronto/src/moronto-design-compare/fgs-media-playback-demo.mp4` (`3.8 MB` / `3,889,112` bytes)
   - **Video SHA-256:** `bda83a8a600fb303c8d2cb8ca5e4bc3e45c70ef930a76dd0b717b88c7d42866d`
   - **Live System Verification During Recording (`dumpsys activity services` & `dumpsys media_session`):**
     - `ServiceRecord{82ed99d u0 com.shadowspark.morontomornica/.domain.LocalAudioPlaybackService}`
     - `isForeground=true foregroundId=1001 types=0x00000002` (`FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`)
     - `foregroundNoti=Notification(channel=default_channel_id ... flags=ONLY_ALERT_ONCE|NO_CLEAR|FOREGROUND_SERVICE ... category=transport groupKey=media3_group_key actions=3 vis=PUBLIC)`
     - `MediaSession`: `active=true`, `state=PlaybackState {state=PLAYING(3), position=7257, buffered position=103632, speed=1.0}`, `controllers: 10`, `queue size=368`
   - **What the Video Demonstrates:**
     1. User opens **Moronto Mornica** on the **My Music** screen showing filtered local tracks (`FOLA`).
     2. User taps a track; the bottom mini-player (`OfflineMiniPlayerBar`) and expanded `Now Playing` sheet show active offline playback (`LocalAudioPlaybackService` active).
     3. User presses the Android **Home** button to background the app while audio playback continues uninterrupted.
     4. User pulls down the Android system notification shade (`status bar`), showing the active **Moronto Mornica** Media3 `MediaSession` playback notification with transport controls.
     5. User collapses the shade and returns to **Moronto Mornica**, confirming continuous playback state synchronization.
   - **How to Submit Video URL in Play Console:** Upload `/home/moronto/src/moronto-design-compare/fgs-media-playback-demo.mp4` as an **Unlisted YouTube video** or a **Google Drive link ("Anyone with the link can view")** and paste that link into the *Video link* field of the Foreground Service declaration form.
