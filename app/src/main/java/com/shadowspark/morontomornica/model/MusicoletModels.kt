package com.shadowspark.morontomornica.model

import com.shadowspark.morontomornica.R

enum class LibraryViewTab(val label: String) {
  TRACKS("Tracks"),
  QUEUES("Queues"),
  PLAYLISTS("Playlists"),
  FOLDERS("Folders"),
  ALBUMS("Albums"),
  ARTISTS("Artists"),
  LIVE("Live · Coming Soon"),
  UPGRADE("Pro · Coming Soon"),
  STORAGE("Storage"),
  GUIDE("User Guide"),
  SPONSOR("Sponsor"),
  BLOG("Blog · Pidgin Guide"),
  ABOUT("About"),
  PRIVACY("Privacy"),
}

enum class SortOption(val label: String) {
  TITLE("Title"),
  ARTIST("Artist"),
  ALBUM("Album"),
  DURATION("Duration"),
  YEAR("Year"),
}

enum class RepeatMode {
  OFF,
  ALL,
  ONE,
}

enum class TrackSourceType {
  BUNDLED,
  DEVICE,
}

enum class AdConsentStatus(val key: String) {
  UNSET("unset"),
  PERSONALIZED("personalized"),
  NON_PERSONALIZED("non_personalized"),
  ;

  companion object {
    fun fromKey(raw: String?): AdConsentStatus =
      entries.find { it.key == raw } ?: UNSET
  }
}

object AdMobTestIds {
  const val BANNER_TRACKS = "ca-app-pub-3940256099942544/6300978111"
  const val REWARDED_EQ = "ca-app-pub-3940256099942544/5224354917"
}

data class MorontoThemeOption(
  val id: String,
  val name: String,
  val description: String,
  val bgHex: Long,
  val accentHex: Long,
  val proOnly: Boolean,
)

val MORONTO_THEMES = listOf(
  MorontoThemeOption(
    id = "emerald-console",
    name = "Emerald Console",
    description = "Deep acoustic midnight slate with studio emerald clarity.",
    bgHex = 0xFF071321L,
    accentHex = 0xFF12D9A1L,
    proOnly = false,
  ),
  MorontoThemeOption(
    id = "sunset-brass",
    name = "Sunset Brass",
    description = "Warm espresso mastering room with highlife brass gold.",
    bgHex = 0xFF130E0BL,
    accentHex = 0xFFFFBE55L,
    proOnly = false,
  ),
  MorontoThemeOption(
    id = "lagos-indigo",
    name = "Lagos Indigo",
    description = "Late-night Victoria Island cobalt with crisp neon cyan.",
    bgHex = 0xFF090C1EL,
    accentHex = 0xFF38BDF8L,
    proOnly = false,
  ),
  MorontoThemeOption(
    id = "obsidian-rose",
    name = "Obsidian Rose",
    description = "Velvet dark plum console with warm choral rose.",
    bgHex = 0xFF120B11L,
    accentHex = 0xFFFB7185L,
    proOnly = false,
  ),
)

data class OfflineTrack(
  val id: String,
  val title: String,
  val artist: String,
  val album: String,
  val folderName: String,
  val durationMs: Long,
  val uri: String,
  val releaseYear: Int,
  val sourceType: TrackSourceType,
  val lyricsText: String = "",
)

data class PlaybackQueue(
  val id: String,
  val name: String,
  val trackIds: List<String>,
  val currentTrackId: String?,
  val positionMs: Long,
)

data class CustomPlaylist(
  val id: String,
  val name: String,
  val trackIds: List<String>,
)

data class EqPreset(
  val id: String,
  val name: String,
  val bandsDb: List<Int>,
  val bassBoostPercent: Int,
  val freeTierAllowed: Boolean = false,
)

data class EqualizerSettings(
  val enabled: Boolean = true,
  val presetId: String = "flat",
  val bandsDb: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
  val bassBoostPercent: Int = 0,
  val preampDb: Int = 0,
  val virtualizerPercent: Int = 15,
)

data class CrossfadeSettings(
  val enabled: Boolean = false,
  val durationSec: Int = 4,
  val fadeOnManualSkip: Boolean = true,
  val gaplessEnabled: Boolean = true,
)

data class SponsorTier(
  val id: String,
  val name: String,
  val monthlyUsd: Int,
  val oneTimeUsd: Int,
  val summary: String,
)

data class SponsorPledge(
  val id: String,
  val name: String,
  val email: String,
  val tierId: String,
  val billing: String,
  val amountUsd: Int,
  val note: String,
)

data class BlogPreviewArticle(
  val id: String,
  val title: String,
  val category: String,
  val readTime: String,
  val summary: String,
)

data class LiveShowPreview(
  val id: String,
  val title: String,
  val host: String,
  val genre: String,
  val scheduleLabel: String,
  val summary: String,
)

data class StudioTemplate(
  val id: String,
  val name: String,
  val tagline: String,
  val queueId: String,
  val eqPresetId: String,
  val crossfadeSec: Int,
  val crossfadeEnabled: Boolean,
)

val SPONSOR_TIERS = listOf(
  SponsorTier(
    id = "supporter",
    name = "Listener Supporter",
    monthlyUsd = 5,
    oneTimeUsd = 15,
    summary = "Backs ongoing offline playback polish and clean audio tools.",
  ),
  SponsorTier(
    id = "patron",
    name = "Studio Patron",
    monthlyUsd = 15,
    oneTimeUsd = 45,
    summary = "Funds mastering presets, 10-band EQ curves, and local DSP research.",
  ),
  SponsorTier(
    id = "label",
    name = "Cultural Partner",
    monthlyUsd = 50,
    oneTimeUsd = 150,
    summary = "Sponsors licensed African artist showcases and upcoming Live sessions.",
  ),
)

const val SUPPORT_EMAIL = "support@morontomusic.app"

data class PidginArticleSection(
  val heading: String,
  val body: String,
)

data class PidginOfflineArticle(
  val eyebrow: String,
  val title: String,
  val subtitle: String,
  val readTime: String,
  val sections: List<PidginArticleSection>,
)

val PIDGIN_OFFLINE_ARTICLE = PidginOfflineArticle(
  eyebrow = "Moronto Journal · Featured Naija Pidgin Special",
  title = "Why You Supposed Get Your Own Offline Songs Inside Phone (No Let Network Disgrace You Inside Danfo!)",
  subtitle = "Right now Moronto Music na 100% offline music studio — every 10-band EQ preset, 0–12s crossfade, Sleep Timer, and all 4 Themes don open free for everybody while Global Catalog Streaming Pro dey cook.",
  readTime = "3 min laugh & learn",
  sections = listOf(
    PidginArticleSection(
      heading = "1. Third Mainland Bridge No Send Your 5G Bar",
      body = "Imagine say you just wear your fine earpiece inside Oshodi-to-VI traffic, spirit don high, beat about to drop — gbam! Circle start to spin like ceiling fan because network carry \"E\" enter bridge. With local MP3, WAV, or FLAC files inside your phone, even inside NEPA blackout or underground parking lot, your Afrobeats bass dey knock nonstop.",
    ),
    PidginArticleSection(
      heading = "2. Save Your Data Money for Suya & Shawarma",
      body = "Why you go dey dash telecom company 500MB every week just to replay that same jam wey you fit keep for phone storage once? Keep your own bought albums, producer studio demos, Bandcamp/artist downloads, and church choir recordings inside your Downloads or Music folder, import am once, and enjoy zero-data playback forever.",
    ),
    PidginArticleSection(
      heading = "3. Every Paid Tool Don Free Right Now (No Plan Stress!)",
      body = "We don comot every lock for now! Open 10-Band EQ (Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife, Flat), set 0–12s smooth DJ crossfade, put Sleep Timer before night sleep, and switch to any Studio Theme wey sweet your eye. Pro go only land when our Global Catalog Streaming engine enter town.",
    ),
    PidginArticleSection(
      heading = "4. Your Files Dey Hide? Holla Support Sharp-Sharp!",
      body = "If you get music files inside WhatsApp Documents, Telegram folder, SD Card, or laptop and you no sabi move am enter Moronto Music, no stress yourself. Email support@morontomusic.app or tap Support Desk right here — we go guide you step-by-step on Android, iPhone, Windows, or Mac!",
    ),
  ),
)

val PRO_COMING_SOON_HIGHLIGHTS = listOf(
  "Global Licensed Music Catalog Streaming (Afrobeats, Amapiano, Gospel, Highlife & Worldwide)",
  "Cloud Queue & Playlist Sync across Android and Web devices",
  "All current offline studio tools (10-band EQ, 0–12s Crossfade, Sleep Timer, 4 Themes, 20 Queues) stay 100% free & unlocked right now",
)

val BLOG_PREVIEW_POSTS = listOf(
  BlogPreviewArticle(
    id = "blog-dual-deck-crossfade",
    title = "Dual-Deck Crossfading Without Cloud Latency",
    category = "Audio Engineering",
    readTime = "6 min read",
    summary = "How Moronto Music overlaps two local ExoPlayer decks cleanly and caps transitions at 40% of track length.",
  ),
  BlogPreviewArticle(
    id = "blog-afrobeats-eq-curves",
    title = "Tuning 10-Band EQ Curves for Afrobeats & Amapiano Log-Drums",
    category = "Sound Design",
    readTime = "5 min read",
    summary = "Balancing 31Hz sub-weight, 125Hz log-drum punch, and 4kHz vocal presence without clipping.",
  ),
  BlogPreviewArticle(
    id = "blog-20-queue-architecture",
    title = "Inside Our 20-Queue Offline Memory Architecture",
    category = "Product Craft",
    readTime = "4 min read",
    summary = "Why preserving independent timestamps across commute, gym, and studio queues keeps listening effortless.",
  ),
)

val LIVE_SHOW_PREVIEWS = listOf(
  LiveShowPreview(
    id = "live-lagos-friday",
    title = "Lagos Friday Analog Sessions",
    host = "Hosted by Teniola Blaze",
    genre = "Afrobeats & Brass",
    scheduleLabel = "Weekly Broadcast · Coming Soon",
    summary = "Intimate live horn and percussion sessions streamed directly from our Lagos partner studio.",
  ),
  LiveShowPreview(
    id = "live-sunday-gospel",
    title = "Sunday Morning Choir Room",
    host = "Hosted by Korede Worship",
    genre = "Gospel & Harmonies",
    scheduleLabel = "Sunday Special · Coming Soon",
    summary = "Live vocal harmonies and acoustic praise rehearsals with listener song requests.",
  ),
  LiveShowPreview(
    id = "live-amapiano-deck",
    title = "Midnight Log-Drum Selectors",
    host = "Hosted by Kofi & The LogDrums",
    genre = "Amapiano & Deep Groove",
    scheduleLabel = "Weekend Mix · Coming Soon",
    summary = "Real-time DJ sets mixed with 10-band studio EQ and low-data audio streams.",
  ),
)

val STUDIO_TEMPLATES = listOf(
  StudioTemplate(
    id = "tpl-afrobeats-commute",
    name = "Afrobeats Commute",
    tagline = "Queue 1 · Afrobeats Bass EQ · 4s Fade",
    queueId = "queue-1",
    eqPresetId = "afrobeats-bass",
    crossfadeSec = 4,
    crossfadeEnabled = true,
  ),
  StudioTemplate(
    id = "tpl-amapiano-club",
    name = "Amapiano Log Drum",
    tagline = "Queue 2 · Amapiano Log Drum EQ · 6s Fade",
    queueId = "queue-2",
    eqPresetId = "amapiano-log-drum",
    crossfadeSec = 6,
    crossfadeEnabled = true,
  ),
  StudioTemplate(
    id = "tpl-sunday-gospel",
    name = "Sunday Gospel",
    tagline = "Queue 3 · Gospel EQ · 3s Fade",
    queueId = "queue-3",
    eqPresetId = "gospel",
    crossfadeSec = 3,
    crossfadeEnabled = true,
  ),
  StudioTemplate(
    id = "tpl-studio-flat",
    name = "Pure Reference",
    tagline = "Queue 1 · Flat EQ · 0s Gapless",
    queueId = "queue-1",
    eqPresetId = "flat",
    crossfadeSec = 0,
    crossfadeEnabled = false,
  ),
)

val SMART_PLAYLIST_GENRES = listOf("Afrobeats", "Amapiano", "Gospel", "Highlife")

data class LrcLine(
  val timeMs: Long?,
  val text: String,
)

const val MAX_QUEUES = 20

val EQ_BAND_LABELS = listOf(
  "31Hz",
  "62Hz",
  "125Hz",
  "250Hz",
  "500Hz",
  "1kHz",
  "2kHz",
  "4kHz",
  "8kHz",
  "16kHz",
)

val EQ_PRESETS = listOf(
  EqPreset(
    id = "afrobeats-bass",
    name = "Afrobeats Bass",
    bandsDb = listOf(6, 5, 3, 1, 0, 1, 2, 3, 2, 2),
    bassBoostPercent = 50,
    freeTierAllowed = true,
  ),
  EqPreset(
    id = "amapiano-log-drum",
    name = "Amapiano Log Drum",
    bandsDb = listOf(7, 6, 4, 1, -1, 0, 1, 2, 2, 1),
    bassBoostPercent = 65,
    freeTierAllowed = true,
  ),
  EqPreset(
    id = "gospel",
    name = "Gospel",
    bandsDb = listOf(3, 2, 1, 2, 3, 4, 3, 2, 2, 1),
    bassBoostPercent = 25,
    freeTierAllowed = true,
  ),
  EqPreset(
    id = "highlife",
    name = "Highlife",
    bandsDb = listOf(2, 3, 2, 1, 2, 3, 4, 3, 2, 1),
    bassBoostPercent = 20,
    freeTierAllowed = true,
  ),
  EqPreset(
    id = "flat",
    name = "Flat",
    bandsDb = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    bassBoostPercent = 0,
    freeTierAllowed = true,
  ),
)

private val LRC_LINE_REGEX = Regex("""^\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?]\s*(.*)$""")

fun parseLrcLyrics(raw: String?): List<LrcLine> {
  if (raw.isNullOrBlank()) return emptyList()
  val result = mutableListOf<LrcLine>()
  for (line in raw.lines()) {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) continue
    val match = LRC_LINE_REGEX.matchEntire(trimmed)
    if (match != null) {
      val mins = match.groupValues[1].toLongOrNull() ?: 0L
      val secs = match.groupValues[2].toLongOrNull() ?: 0L
      val fracRaw = match.groupValues[3]
      val ms = fracRaw.padEnd(3, '0').take(3).toLongOrNull() ?: 0L
      val text = match.groupValues[4].trim()
      if (text.isNotEmpty()) {
        result += LrcLine(timeMs = mins * 60_000L + secs * 1_000L + ms, text = text)
      }
    } else {
      result += LrcLine(timeMs = null, text = trimmed)
    }
  }
  return result
}

fun bundledDemoAudioUri(packageName: String = "com.shadowspark.morontomornica"): String =
  "android.resource://$packageName/${R.raw.mornica_afrobeats_demo}"

fun bundledOfflineTracks(packageName: String = "com.shadowspark.morontomornica"): List<OfflineTrack> {
  val uri = bundledDemoAudioUri(packageName)
  return listOf(
    OfflineTrack(
      id = "track-eko-neon",
      title = "Eko Neon Pulse",
      artist = "Teniola Blaze",
      album = "Lagos Midnight Sessions",
      folderName = "Afrobeats",
      durationMs = 30_000L,
      uri = uri,
      releaseYear = 2026,
      sourceType = TrackSourceType.BUNDLED,
      lyricsText = """
        [00:00.00] Third Mainland bridge lights reflecting on the water
        [00:05.00] Midnight rhythm rolling through Victoria Island
        [00:10.00] Zero data needed when the bassline hits the speakers
        [00:16.00] Moronto Music groove, keep the queue alive
        [00:22.00] Eko never sleeps, turn the dial up one more time
      """.trimIndent(),
    ),
    OfflineTrack(
      id = "track-lekki-logdrum",
      title = "Lekki Sunrise Log-Drum",
      artist = "Kofi & The LogDrums",
      album = "Amapiano Pilot Tapes",
      folderName = "Amapiano",
      durationMs = 45_000L,
      uri = uri,
      releaseYear = 2025,
      sourceType = TrackSourceType.BUNDLED,
      lyricsText = """
        [00:00.00] Early morning breeze off the Lekki coast
        [00:07.00] Log-drums knocking steady in the back seat
        [00:14.00] Save the queue, switch the groove, never lose your spot
        [00:21.00] Pure offline percussion for the morning commute
      """.trimIndent(),
    ),
    OfflineTrack(
      id = "track-grace-overflow",
      title = "Grace Overflow (Choir Live)",
      artist = "Korede Worship",
      album = "Sunday Sanctuary Tapes",
      folderName = "Gospel",
      durationMs = 52_000L,
      uri = uri,
      releaseYear = 2026,
      sourceType = TrackSourceType.BUNDLED,
      lyricsText = """
        [00:00.00] Morning harmonies rising across the hall
        [00:08.00] Grateful hearts singing loud with zero cloud buffer
        [00:16.00] Lift the chorus higher in crystal offline clarity
      """.trimIndent(),
    ),
    OfflineTrack(
      id = "track-yaba-strings",
      title = "Yaba Midnight Strings",
      artist = "Zainab Soul",
      album = "Afro-Soul Showcase",
      folderName = "Afrobeats",
      durationMs = 60_000L,
      uri = uri,
      releaseYear = 2024,
      sourceType = TrackSourceType.BUNDLED,
      lyricsText = """
        [00:00.00] Warm acoustic strings over Yaba rooftops
        [00:08.00] Rain tapping softly on the studio window
        [00:16.00] Slow down the tempo, breathe in the melody
      """.trimIndent(),
    ),
    OfflineTrack(
      id = "track-abuja-highlife",
      title = "Abuja Sunset Highlife",
      artist = "Chinedu Brass",
      album = "Golden Palm Collective",
      folderName = "Highlife",
      durationMs = 42_000L,
      uri = uri,
      releaseYear = 2025,
      sourceType = TrackSourceType.BUNDLED,
      lyricsText = """
        [00:00.00] Brass horns echoing across Aso Rock at dusk
        [00:09.00] Classic highlife guitar lines woven into modern drums
      """.trimIndent(),
    ),
    OfflineTrack(
      id = "track-ibadan-groove",
      title = "Ibadan Acoustic Groove",
      artist = "Teniola Blaze",
      album = "Lagos Midnight Sessions",
      folderName = "Afrobeats",
      durationMs = 36_000L,
      uri = uri,
      releaseYear = 2026,
      sourceType = TrackSourceType.BUNDLED,
      lyricsText = "",
    ),
  )
}

val DEFAULT_QUEUES = listOf(
  PlaybackQueue(
    id = "queue-1",
    name = "Queue 1 · Main Mix",
    trackIds = listOf(
      "track-eko-neon",
      "track-lekki-logdrum",
      "track-grace-overflow",
      "track-yaba-strings",
      "track-abuja-highlife",
      "track-ibadan-groove",
    ),
    currentTrackId = "track-eko-neon",
    positionMs = 0L,
  ),
  PlaybackQueue(
    id = "queue-2",
    name = "Queue 2 · Lagos Commute",
    trackIds = listOf("track-lekki-logdrum", "track-eko-neon"),
    currentTrackId = "track-lekki-logdrum",
    positionMs = 0L,
  ),
  PlaybackQueue(
    id = "queue-3",
    name = "Queue 3 · Sunday Praise & Soul",
    trackIds = listOf("track-grace-overflow", "track-yaba-strings", "track-abuja-highlife"),
    currentTrackId = "track-grace-overflow",
    positionMs = 0L,
  ),
)

val DEFAULT_PLAYLISTS = listOf(
  CustomPlaylist(
    id = "pl-roadtrip",
    name = "Roadtrip Essentials",
    trackIds = listOf("track-eko-neon", "track-abuja-highlife"),
  ),
)
