# Mornica Nigeria-first native music: streaming + licensed offline

**Decision:** 2026-11-07. **Status:** Proposed product requirements; engineering implementation not yet verified.

## Product outcome
Build a genuinely native Mornica music-listening experience for Nigerian users: one search surface, one Mornica-branded player, reliable streaming, explicit user-triggered save-for-offline, playlists, mobile-data awareness, and full offline playback when content rights allow. Source must never be misrepresented.

## Hard architectural decision
**Do not embed, hide, proxy, restyle, overlay, or background-play a YouTube player.** Do not route YouTube media bytes into Media3, extract audio, use yt-dlp/Snaptube-like extraction, download YouTube content, create offline YouTube copies or use undocumented endpoints. A Terms/Privacy-only disclosure is not a substitute for truthful in-context source attribution. If an artist/video has a YouTube discovery link, show a clearly marked `Watch on YouTube` action opening the external YouTube app/browser. YouTube-sourced search metadata, if ever introduced, needs separately approved attribution/API/branding/privacy review. Initial Mornica release should not require YouTube APIs or any YouTube audio/video.

## Sources allowed in the native Mornica player
1. **User's own local library:** device MediaStore audio, with permission and reliable folder/artist/album browsing. Only local files accessible to the user/device; no unauthorized fetching of web music.
2. **Rights-cleared Mornica catalogue:** direct HTTPS audio streams supplied through owned hosting or a properly licensed content partner, with documented master/composition/territory/streaming/offline rights and expiry. Each track carries rights and offline entitlement metadata.
3. **Independent artist uploads:** only after signed permission, provenance checks, moderation and rights attestation; verify actual authorization rather than relying on self-attestation alone for commercial release.
4. **Explicitly licensed external downloads:** ONLY when provider grants actual downloadable media URLs and the app has permission to save offline. Never presume a stream is downloadable.

## Domain models and separation
TrackOrigin = LOCAL_FILE | LICENSED_CATALOG | ARTIST_AUTHORIZED | EXTERNAL_YOUTUBE_LINK.
PlaybackMode = LOCAL | AUTHORIZED_STREAM | AUTHORIZED_OFFLINE; an EXTERNAL_YOUTUBE_LINK is not a playable Mornica audio source.
Track fields: id, artist, title, album, artworkRights, sourceLabel, licenseRef, availableTerritories, streamAllowed, offlineAllowed, expiry, streamUrl (secure short-lived when applicable), downloadAssetId, checksum, mediaType. Never embed secrets in catalog JSON.
PlaybackRouter must accept only local content URI or authorized first-party/partner media endpoint; fail closed on arbitrary webpages, YouTube URLs/video IDs, unapproved hosts or expired entitlements.

## Offline download design
- Use AndroidX Media3 ExoPlayer DownloadManager + DownloadService + DownloadIndex + CacheDataSource for eligible licensed remote audio, with Room metadata/queue and user-visible progress. Consider WorkManager/PlatformScheduler only as supported by current target SDK.
- `Save offline` control appears only for catalog entries with `offlineAllowed=true` and valid entitlement; user initiates download. Local library tracks show `On this device`, not `Download`.
- Handle start/pause/resume/cancel/retry, Wi-Fi-only choice, data-use warning for mobile networks, storage capacity, duplicate requests, interrupted network, checksum failure, provider license expiry/revocation, and clear deletion.
- Keep content access scoped; protect saved assets when license requires it. Do not falsely promise that every downloaded track is an exportable MP3.
- On offline playback, serve bytes from the authorized download cache with no network dependency and no accidental streaming fallback. Track offline availability accurately.
- A rights-expired track must not play offline if the license does not allow it; show understandable renewal/unavailable message. Remove inaccessible cached copies as rights require.
- MediaSessionService provides legitimate background audio only for user local files and licensed Mornica catalogue music; follows Android lifecycle, audio focus and notification policy.
- Audio should never start invisibly without user action and should not leak during calls/headset unplug.

## Suggested UI flow
Home → Explore → Track details → Play (native Mornica player) → Save offline (if authorized) → Downloads → Play with airplane mode.
Bottom tabs: Home | Explore | My Music | Downloads | Profile. Downloads must clearly distinguish cached licensed tracks from user files. Artist pages and optional external videos identify their source.
Prioritize low-data conditions, transient 2G/3G/4G/mobile hotspot networks, cheap-device storage limits and 360dp-width screens; test with font scale 1.3x.

## Nigerian-first language system
Tone: simple English with optional light Nigerian Pidgin that reads naturally; don't use caricature or assume all Nigerians speak Pidgin. Default to clear English, allow a `Naija English` localization pack via resource strings.

| Screen / state | Default English | Naija-friendly alternative |
| --- | --- | --- |
| Hero | Your music. Your way. | Your sound, your way. |
| Discovery | Discover fresh Naija sounds | Fresh Naija sounds, all in one place |
| Search hint | Search songs, artists and playlists | Find your next jam |
| Play CTA | Play now | Play am now |
| Offline CTA | Save for offline | Download for later |
| Offline success | Saved. Ready when you're offline. | No data? No wahala. |
| Data preference | Download on Wi-Fi only | Save your data: Wi-Fi downloads only |
| Library | Music on your phone | Your own music, ready to play |
| Network error | You're offline. Play your saved tracks. | Network no dey? Your downloads still dey. |
| Restricted rights | Offline saving isn't available for this track. | This track no dey available for download yet. |
| Optional YouTube link | Watch on YouTube | Watch video on YouTube |

Do not assert `Download any song` or `All Nigerian hits available offline` unless the actual licensed catalog and rights support that claim. Real metadata and artist attribution must remain visible. Use artist/track cover art only with rights or appropriate license.

## Accurate provenance / policy
- In-app: artists, creators and origin/labels shown where relevant; `Watch on YouTube` clearly names YouTube. Mornica's own legally hosted songs can carry `Available on Mornica` but never imply ownership or exclusive rights unless documented.
- Terms: explain local files, licensed streaming, license-limited downloads, third-party external links, geographic restrictions, removals, updates and rights holder contacts.
- Privacy: disclose precisely what is collected (e.g. creator intake, catalog analytics, download state) and third-party sharing. Request only permissions actually needed.
- No YouTube-source blanket disclaimer in terms is necessary if Mornica truly stops using YouTube APIs and media, though external links should be disclosed. If YouTube APIs are used later, follow their current API terms for prominent in-context attribution and applicable TOS/privacy notices.

## Acceptance gates
1. Stream a documented licensed track in the visible native player and continue in background via Media3.
2. Save one explicitly offline-licensed track on Wi-Fi; interrupt/resume and confirm progress/report.
3. Switch to airplane mode; launch from Downloads and play entire authorized media without a network request.
4. A track with `offlineAllowed=false` has no enabled offline-save path, including deep links/API requests; test fail-closed.
5. Searching or tapping any YouTube link cannot route content into Mornica player or download service. External watch label and external app/browser intent are unambiguous.
6. Local MediaStore files still work without network or catalog account; no original files deleted without user consent.
7. Test permissions, storage full, expiring license, no network, interrupted download, duplicate tap, corrupted file, mobile data, accessibility and 360dp/412dp layouts.
8. Unit tests + lint + debug build + emulator proof + independent QA. Release gate remains blocked pending licensed catalog contract and owner acceptance.

## Official references (review at implementation time)
- YouTube Developer Policies: https://developers.google.com/youtube/terms/developer-policies
- YouTube Developer Policies Guide: https://developers.google.com/youtube/terms/developer-policies-guide
- Google Play Intellectual Property: https://support.google.com/googleplay/android-developer/answer/9888072
- Media3 Downloading Media: https://developer.android.com/media/media3/exoplayer/downloading-media

**Status:** This specifies permitted target behavior only. It does not assert that a licensed catalog, server backend, download entitlement system or offline build has already been implemented.
