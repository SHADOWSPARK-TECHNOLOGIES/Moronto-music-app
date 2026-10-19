package com.shadowspark.morontomornica.model

enum class PrimaryScreen(val title: String) {
  DISCOVER_WATCH("Discover & Watch"),
  MY_MUSIC("My Music"),
  CREATOR_HUB("Creator Hub"),
}

enum class ActiveEngine {
  NONE,
  YOUTUBE_EMBED,
  LOCAL_AUDIO,
}

enum class YouTubeSourceBadge(val label: String) {
  LIVE_YOUTUBE_API("LIVE · Official YouTube Data API v3"),
  CURATED_FALLBACK("CURATED · Offline / Quota-Safe Official Metadata"),
}

enum class ProvenanceStatus(val label: String, val isPlayableByDefault: Boolean) {
  LOCAL_DEVICE("LOCAL · Device File", true),
  DEMO("DEMO · Cleared Preview", true),
  LICENSED("LICENSED · Artist Attested", true),
  UNAVAILABLE("UNAVAILABLE · Awaiting Clearance", false),
}

enum class RepeatMode {
  OFF,
  ALL,
  ONE,
}

enum class LibraryTab(val label: String) {
  SONGS("Songs"),
  FOLDERS("Folders"),
  ARTISTS("Artists"),
  ALBUMS("Albums"),
  PLAYLISTS("Playlists"),
}

data class VidIqSearchEstimate(
  val query: String,
  val monthlyNigerianSearches: Int,
  val category: String,
  val isThirdPartyEstimate: Boolean = true,
  val framingNote: String =
    "Third-party vidIQ Nigerian monthly YouTube search estimate (directional indicator, never guaranteed demand)",
)

val VIDIQ_NIGERIA_SEARCH_ESTIMATES: List<VidIqSearchEstimate> =
  listOf(
    VidIqSearchEstimate(query = "Asake", monthlyNigerianSearches = 207_295, category = "Street-Pop / Afrobeats"),
    VidIqSearchEstimate(query = "Davido", monthlyNigerianSearches = 123_964, category = "Afrobeats Essentials"),
    VidIqSearchEstimate(query = "Wizkid", monthlyNigerianSearches = 94_555, category = "Afrobeats Essentials"),
    VidIqSearchEstimate(query = "Burna Boy", monthlyNigerianSearches = 94_014, category = "Afro-Fusion"),
    VidIqSearchEstimate(query = "Rema", monthlyNigerianSearches = 82_179, category = "Global Afrobeats"),
    VidIqSearchEstimate(query = "Omah Lay", monthlyNigerianSearches = 30_694, category = "Afro-Soul"),
    VidIqSearchEstimate(query = "Amapiano", monthlyNigerianSearches = 25_401, category = "Amapiano Crossover"),
  )

data class YouTubeVideoCard(
  val videoId: String,
  val title: String,
  val channelTitle: String,
  val channelId: String,
  val category: String,
  val publishedAt: String,
  val sourceBadge: YouTubeSourceBadge,
  val isRisingCreator: Boolean = false,
  val isFreeUnpaywalled: Boolean = true,
) {
  fun officialEmbedUrl(): String =
    "https://www.youtube.com/embed/$videoId?playsinline=1&rel=0&modestbranding=1"

  fun officialWatchUrl(): String =
    "https://www.youtube.com/watch?v=$videoId"

  fun isPolicyCompliant(): Boolean =
    videoId.isNotBlank() &&
      !videoId.contains("http", ignoreCase = true) &&
      isFreeUnpaywalled
}

data class LocalMediaTrack(
  val id: String,
  val title: String,
  val artist: String,
  val album: String,
  val folderName: String,
  val durationMs: Long,
  val uri: String,
  val provenance: ProvenanceStatus,
  val rightsConfirmed: Boolean,
) {
  fun isPlayableOffline(): Boolean {
    if (!rightsConfirmed || provenance == ProvenanceStatus.UNAVAILABLE) return false
    if (isYouTubeUri(uri)) return false
    return uri.startsWith("content://") ||
      uri.startsWith("file://") ||
      uri.startsWith("android.resource://")
  }

  companion object {
    fun isYouTubeUri(candidateUri: String): Boolean {
      val lower = candidateUri.trim().lowercase()
      return lower.contains("youtube.com") ||
        lower.contains("youtu.be") ||
        lower.contains("googlevideo.com") ||
        lower.startsWith("yt://")
    }
  }
}

data class CreatorProfile(
  val id: String,
  val artistName: String,
  val city: String,
  val genre: String,
  val bio: String,
  val provenanceBadge: ProvenanceStatus,
  val rightsConfirmed: Boolean,
  val previewTrackIds: List<String>,
  val officialYouTubeChannelUrl: String? = null,
)

data class ArtistLaunchRequest(
  val artistName: String,
  val trackTitle: String,
  val genre: String,
  val contactEmailOrWhatsApp: String,
  val rightsAttested: Boolean,
)

data class ValidationResult(
  val valid: Boolean,
  val errorMessage: String? = null,
)

fun validateArtistLaunchRequest(request: ArtistLaunchRequest): ValidationResult {
  if (request.artistName.trim().length < 2) {
    return ValidationResult(false, "Artist or collective name must be at least 2 characters.")
  }
  if (request.trackTitle.trim().length < 2) {
    return ValidationResult(false, "Track or EP title must be at least 2 characters.")
  }
  if (request.contactEmailOrWhatsApp.trim().length < 5) {
    return ValidationResult(false, "Provide a valid email or WhatsApp contact for onboarding.")
  }
  if (!request.rightsAttested) {
    return ValidationResult(
      false,
      "You must attest that you own or control 100% of the preview rights before submitting.",
    )
  }
  return ValidationResult(true, null)
}
