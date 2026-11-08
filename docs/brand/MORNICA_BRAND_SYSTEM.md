# MORNICA MUSIC — Original visual identity v0.1

## Decision and ownership
Proposed design system for Antigravity's native Compose implementation. Original to Mornica; NOT an Apple Music clone, no third-party album or artist images. The concept preview delivered separately is not a validated launcher/store asset. These vectors are transparent source design concepts; verify small-scale legibility and adapt them to Android safe zones.

## Visual story
**MORNICA:** Find your rhythm, keep your music. A soundwave doubles into a flowing `M`; one small amber spark means discovery; vertical equalizer strokes represent genuine audio, not Apple or Spotify's icon. Use confident asymmetry and generous breathing room rather than Apple's exact editorial typography or color arrangements.

## Design tokens
| Purpose | Token | Value |
| --- | --- | --- |
| Main background | `ink` | `#071321` |
| App canvas | `canvas` | `#0B1727` |
| Elevated surface | `surface` | `#14243A` |
| Foreground | `text` | `#F4FAFE` |
| Secondary text | `muted` | `#A5B6CA` |
| Now playing / affirmative | `jade` | `#12D9A1` |
| Links / secondary | `azure` | `#20B6EB` |
| Demo/risk notice | `amber` | `#FFBE55` |
| Errors / caution | `coral` | `#FF6479` |

Use accessible surface/background combinations. Verify actual contrast on every state; brand hues are proposals, not automatic accessible text colors. Maintain 4.5:1 for body copy and 3:1 for large text/non-text controls where relevant. Touchable controls ≥48dp where feasible.

## Semantic composables
- `MornicaHeader`: compact brand mark, complete title and right-side search overflow; remove truncated long developer tagline from title line.
- `MornicaHero`: only one short mood/context message, clear playable content; no wall of engineering/legal text.
- `WaveCoverPlaceholder`: abstract first-party wave art, title initials or provenance when artwork is unavailable; never fake actual cover rights.
- `MornicaMiniPlayer`: pinned above nav, owned by one MediaSession player; title, artist, artwork and true play state.
- `MornicaFullPlayer`: immersive but original M-pulse gradient background with queue/seek/offline badge and TalkBack labels.
- `MornicaDownloadState`: queued, downloading N%, pause/cancel, retry, saved, offline unavailable, license expired. Do not expose action when offline entitlement=false.
- `CreatorProfile`: honest `DEMO` / licensed status, clear attribution, concise bio, user-initiated creator inquiry.

## Required concept asset list / conversion
| Asset | Content rule | Minimum export/format |
| --- | --- | --- |
| Launcher foreground and monochrome | source `mornica-symbol.svg`; glyph inside adaptive-icon safe area | Android res/mipmap densities + monochrome drawable |
| Splash/wordmark | symbol + original wordmark | Android vector/PNG assets as appropriate |
| Play store icon | identifiable glyph, no words on tiny icon | 512×512 PNG 32-bit, verified per latest Play listing requirements |
| Play feature graphic | abstract `mornica-wave-backdrop.svg` / separate concept art; not duplicate giant launcher icon | 1024×500 JPEG or 24-bit PNG no alpha |
| Screenshot pack | ACTUAL screenshots of candidate APK, not composed marketing mockups of unavailable features | Play-accepted portrait screenshots; include a 360/412dp QA archive |
| 1:1 placeholder art | generative/procedural original pattern; DEMO when applicable | PNG/WebP resolutions appropriate to app |
| Error and empty states | compact icons, copy and retry affordance | Compose vector components, TalkBack description |
| Store promotional copy | promise only local library + demo/verified licensed features actually present | truthful descriptions + privacy/support URL |

## Copy system (Nigerian English, opt-in Pidgin supplement)
| Intent | Primary | Optional helper |
| --- | --- | --- |
| Home | `Your music, your way` | `Fresh sounds, easy listening` |
| Search | `Find a song or artist` | `Find your next jam` |
| Local library | `Music on your phone` | `Play without data` |
| Save available track | `Save offline` | `Wi-Fi downloads save your data` |
| No offline right | `Streaming only` | `This track can't be saved offline` |
| Downloaded | `Ready offline` | `No data? No wahala` |
| External video | `Watch on YouTube` | `Opens outside Mornica` |
| No source | `No tracks found` | `Check your music library or try another search` |
| Incomplete demo | `Demo track` | `Preview content, not a full music catalogue` |

Research must validate natural phrasing with actual Nigerian speakers; don't stereotype, invent endorsements, force Pidgin on unwilling users, or claim emotion recognition from listening behavior.

## Acceptance
- New icon visually distinguishable from Apple Music, Spotify, YouTube Music, Deezer and current Mornica note.
- Complete wordmark not truncated at 360dp; logo legible at 48px.
- No text clipped at 1.3× font scale; covers have alternative text and provenance; palette passes measured contrast.
- `Try before ship`: test 360dp, 412dp, dark mode, TalkBack, icon light/dark launcher backgrounds and store listing small thumbnails.
- No final asset is published without owner review and any necessary trademark/legal clearance.

## Sources
- Android Material Design and app icon principles; Play Console asset guidelines: https://support.google.com/googleplay/android-developer/answer/9866151
- Google Play impersonation rules: https://support.google.com/googleplay/android-developer/answer/9888374
- Apple design principles (inspiration, not assets): https://developer.apple.com/design/human-interface-guidelines/design-principles/
