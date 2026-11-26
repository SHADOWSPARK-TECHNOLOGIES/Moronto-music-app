package com.shadowspark.morontomornica.domain

import com.shadowspark.morontomornica.model.CrossfadeSettings
import com.shadowspark.morontomornica.model.CustomPlaylist
import com.shadowspark.morontomornica.model.EQ_PRESETS
import com.shadowspark.morontomornica.model.EqualizerSettings
import com.shadowspark.morontomornica.model.MAX_QUEUES
import com.shadowspark.morontomornica.model.OfflineTrack
import com.shadowspark.morontomornica.model.PlaybackQueue
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.SMART_PLAYLIST_GENRES
import com.shadowspark.morontomornica.model.SPONSOR_TIERS
import com.shadowspark.morontomornica.model.SortOption
import com.shadowspark.morontomornica.model.SponsorPledge
import com.shadowspark.morontomornica.model.TrackSourceType
import java.util.Locale
import java.util.UUID

data class QueueSwitchResult(
  val queues: List<PlaybackQueue>,
  val activeQueue: PlaybackQueue,
)

data class QueueCreateResult(
  val queues: List<PlaybackQueue>,
  val created: PlaybackQueue?,
  val error: String? = null,
)

data class QueueDeleteResult(
  val queues: List<PlaybackQueue>,
  val nextActiveQueueId: String,
  val deleted: Boolean,
)

data class NextTrackStep(
  val nextTrackId: String?,
  val shouldStop: Boolean,
  val restartCurrent: Boolean,
)

data class PrevTrackStep(
  val prevTrackId: String?,
  val restartCurrent: Boolean,
)

data class TrackTagPatch(
  val title: String,
  val artist: String,
  val album: String,
  val folderName: String,
  val releaseYear: Int,
  val lyricsText: String? = null,
)

data class SmartPlaylistGroup(
  val genre: String,
  val tracks: List<OfflineTrack>,
)

data class StorageStats(
  val totalTracks: Int,
  val bundledTracks: Int,
  val deviceTracks: Int,
  val bundledEstimatedBytes: Long,
  val deviceEstimatedBytes: Long,
  val totalEstimatedBytes: Long,
)

object MusicoletEngine {
  fun formatDurationMs(ms: Long): String {
    val safeSec = (ms.coerceAtLeast(0L)) / 1000L
    val minutes = safeSec / 60L
    val seconds = safeSec % 60L
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
  }

  fun filterAndSortTracks(
    tracks: List<OfflineTrack>,
    query: String,
    sortOption: SortOption,
  ): List<OfflineTrack> {
    val q = query.trim().lowercase(Locale.ROOT)
    val filtered =
      if (q.isEmpty()) {
        tracks
      } else {
        tracks.filter {
          it.title.lowercase(Locale.ROOT).contains(q) ||
            it.artist.lowercase(Locale.ROOT).contains(q) ||
            it.album.lowercase(Locale.ROOT).contains(q) ||
            it.folderName.lowercase(Locale.ROOT).contains(q)
        }
      }
    return when (sortOption) {
      SortOption.TITLE -> filtered.sortedBy { it.title.lowercase(Locale.ROOT) }
      SortOption.ARTIST ->
        filtered.sortedWith(
          compareBy(
            { it.artist.lowercase(Locale.ROOT) },
            { it.title.lowercase(Locale.ROOT) },
          ),
        )
      SortOption.ALBUM ->
        filtered.sortedWith(
          compareBy(
            { it.album.lowercase(Locale.ROOT) },
            { it.title.lowercase(Locale.ROOT) },
          ),
        )
      SortOption.DURATION -> filtered.sortedByDescending { it.durationMs }
      SortOption.YEAR ->
        filtered.sortedWith(
          compareByDescending<OfflineTrack> { it.releaseYear }
            .thenBy { it.title.lowercase(Locale.ROOT) },
        )
    }
  }

  fun groupTracksByFolder(tracks: List<OfflineTrack>): List<Pair<String, List<OfflineTrack>>> =
    tracks.groupBy { it.folderName }
      .entries
      .sortedBy { it.key.lowercase(Locale.ROOT) }
      .map { it.key to it.value }

  fun groupTracksByAlbum(tracks: List<OfflineTrack>): List<Pair<String, List<OfflineTrack>>> =
    tracks.groupBy { it.album }
      .entries
      .sortedBy { it.key.lowercase(Locale.ROOT) }
      .map { it.key to it.value }

  fun groupTracksByArtist(tracks: List<OfflineTrack>): List<Pair<String, List<OfflineTrack>>> =
    tracks.groupBy { it.artist }
      .entries
      .sortedBy { it.key.lowercase(Locale.ROOT) }
      .map { it.key to it.value }

  fun switchQueueWithResume(
    queues: List<PlaybackQueue>,
    activeQueueId: String,
    targetQueueId: String,
    currentPositionMs: Long,
  ): QueueSwitchResult {
    val updated = queues.map { q ->
      if (q.id == activeQueueId) q.copy(positionMs = currentPositionMs.coerceAtLeast(0L))
      else q
    }
    val found = updated.find { it.id == targetQueueId } ?: updated.first()
    return QueueSwitchResult(queues = updated, activeQueue = found)
  }

  fun createNewQueue(
    queues: List<PlaybackQueue>,
    name: String,
    initialTrackIds: List<String> = emptyList(),
  ): QueueCreateResult {
    if (queues.size >= MAX_QUEUES) {
      return QueueCreateResult(
        queues = queues,
        created = null,
        error = "Maximum of $MAX_QUEUES independent queues reached.",
      )
    }
    val cleanName = name.trim()
    if (cleanName.isEmpty()) {
      return QueueCreateResult(queues = queues, created = null, error = "Queue name cannot be empty.")
    }
    val uniqueIds = initialTrackIds.distinct()
    val created = PlaybackQueue(
      id = "queue-${UUID.randomUUID().toString().take(8)}",
      name = cleanName,
      trackIds = uniqueIds,
      currentTrackId = uniqueIds.firstOrNull(),
      positionMs = 0L,
    )
    return QueueCreateResult(queues = queues + created, created = created)
  }

  fun deleteQueueById(
    queues: List<PlaybackQueue>,
    activeQueueId: String,
    queueIdToDelete: String,
  ): QueueDeleteResult {
    if (queues.size <= 1) {
      return QueueDeleteResult(queues, activeQueueId, deleted = false)
    }
    val remaining = queues.filterNot { it.id == queueIdToDelete }
    if (remaining.size == queues.size) {
      return QueueDeleteResult(queues, activeQueueId, deleted = false)
    }
    val nextActive = if (activeQueueId == queueIdToDelete) remaining.first().id else activeQueueId
    return QueueDeleteResult(remaining, nextActive, deleted = true)
  }

  fun addTracksToQueue(
    queues: List<PlaybackQueue>,
    queueId: String,
    trackIds: List<String>,
    playNextAfterCurrent: Boolean = false,
  ): List<PlaybackQueue> =
    queues.map { q ->
      if (q.id != queueId) return@map q
      val existing = q.trackIds.toSet()
      val incoming = trackIds.filter { it !in existing }
      if (incoming.isEmpty()) return@map q
      val nextIds =
        if (playNextAfterCurrent && q.currentTrackId != null) {
          val idx = q.trackIds.indexOf(q.currentTrackId)
          if (idx >= 0) {
            q.trackIds.subList(0, idx + 1) + incoming + q.trackIds.subList(idx + 1, q.trackIds.size)
          } else {
            q.trackIds + incoming
          }
        } else {
          q.trackIds + incoming
        }
      q.copy(
        trackIds = nextIds,
        currentTrackId = q.currentTrackId ?: nextIds.firstOrNull(),
      )
    }

  fun removeTrackFromQueue(
    queues: List<PlaybackQueue>,
    queueId: String,
    trackId: String,
  ): List<PlaybackQueue> =
    queues.map { q ->
      if (q.id != queueId) return@map q
      val idx = q.trackIds.indexOf(trackId)
      if (idx < 0) return@map q
      val nextIds = q.trackIds.filterNot { it == trackId }
      val nextCurrent =
        if (q.currentTrackId == trackId) {
          nextIds.getOrNull(idx) ?: nextIds.getOrNull(idx - 1)
        } else {
          q.currentTrackId
        }
      val nextPos = if (q.currentTrackId == trackId) 0L else q.positionMs
      q.copy(trackIds = nextIds, currentTrackId = nextCurrent, positionMs = nextPos)
    }

  fun moveQueueTrack(
    queues: List<PlaybackQueue>,
    queueId: String,
    fromIndex: Int,
    toIndex: Int,
  ): List<PlaybackQueue> =
    queues.map { q ->
      if (q.id != queueId) return@map q
      if (fromIndex !in q.trackIds.indices || toIndex !in q.trackIds.indices || fromIndex == toIndex) {
        return@map q
      }
      val copy = q.trackIds.toMutableList()
      val moved = copy.removeAt(fromIndex)
      copy.add(toIndex, moved)
      q.copy(trackIds = copy)
    }

  private fun gcd(a: Int, b: Int): Int {
    var x = kotlin.math.abs(a)
    var y = kotlin.math.abs(b)
    while (y != 0) {
      val t = y
      y = x % y
      x = t
    }
    return x
  }

  private fun getCoprimeShuffleStride(length: Int): Int {
    if (length <= 2) return 1
    var candidate = maxOf(2, length / 2 - 1)
    while (candidate < length && gcd(candidate, length) != 1) {
      candidate++
    }
    return if (candidate < length) candidate else 1
  }

  fun computeNextQueueTrack(
    queue: PlaybackQueue,
    repeatMode: RepeatMode,
    shuffleEnabled: Boolean,
    isAutoAdvance: Boolean,
  ): NextTrackStep {
    val ids = queue.trackIds
    if (ids.isEmpty()) return NextTrackStep(null, shouldStop = true, restartCurrent = false)
    if (repeatMode == RepeatMode.ONE && queue.currentTrackId != null && isAutoAdvance) {
      return NextTrackStep(queue.currentTrackId, shouldStop = false, restartCurrent = true)
    }
    val currentIdx = queue.currentTrackId?.let { ids.indexOf(it) } ?: -1

    if (shuffleEnabled && ids.size > 1) {
      val stride = getCoprimeShuffleStride(ids.size)
      val baseIdx = if (currentIdx < 0) 0 else currentIdx
      val nextIdx = (baseIdx + stride) % ids.size
      val nextId =
        if (ids[nextIdx] == queue.currentTrackId) ids[(nextIdx + 1) % ids.size] else ids[nextIdx]
      return NextTrackStep(nextId, shouldStop = false, restartCurrent = false)
    }

    if (currentIdx < 0) return NextTrackStep(ids.first(), shouldStop = false, restartCurrent = false)
    if (currentIdx + 1 < ids.size) {
      return NextTrackStep(ids[currentIdx + 1], shouldStop = false, restartCurrent = false)
    }
    if (repeatMode == RepeatMode.ALL) {
      return NextTrackStep(ids.first(), shouldStop = false, restartCurrent = false)
    }
    return NextTrackStep(null, shouldStop = true, restartCurrent = false)
  }

  fun computePreviousQueueTrack(
    queue: PlaybackQueue,
    repeatMode: RepeatMode,
    positionMs: Long,
    restartThresholdMs: Long = 3_000L,
  ): PrevTrackStep {
    val ids = queue.trackIds
    if (ids.isEmpty()) return PrevTrackStep(null, restartCurrent = false)
    if (queue.currentTrackId != null && positionMs > restartThresholdMs) {
      return PrevTrackStep(queue.currentTrackId, restartCurrent = true)
    }
    val currentIdx = queue.currentTrackId?.let { ids.indexOf(it) } ?: -1
    if (currentIdx > 0) return PrevTrackStep(ids[currentIdx - 1], restartCurrent = false)
    if (repeatMode == RepeatMode.ALL) return PrevTrackStep(ids.last(), restartCurrent = false)
    return PrevTrackStep(ids.first(), restartCurrent = true)
  }

  fun applyTrackTags(
    tracks: List<OfflineTrack>,
    trackId: String,
    patch: TrackTagPatch,
  ): List<OfflineTrack> =
    tracks.map { t ->
      if (t.id != trackId) t
      else {
        t.copy(
          title = patch.title.trim().ifEmpty { t.title },
          artist = patch.artist.trim().ifEmpty { t.artist },
          album = patch.album.trim().ifEmpty { t.album },
          folderName = patch.folderName.trim().ifEmpty { t.folderName },
          releaseYear = if (patch.releaseYear in 1900..2100) patch.releaseYear else t.releaseYear,
          lyricsText = patch.lyricsText ?: t.lyricsText,
        )
      }
    }

  fun createCustomPlaylist(
    playlists: List<CustomPlaylist>,
    name: String,
  ): Pair<List<CustomPlaylist>, CustomPlaylist?> {
    val clean = name.trim()
    if (clean.isEmpty()) return playlists to null
    val created = CustomPlaylist(
      id = "pl-${UUID.randomUUID().toString().take(8)}",
      name = clean,
      trackIds = emptyList(),
    )
    return (playlists + created) to created
  }

  fun addTrackToCustomPlaylist(
    playlists: List<CustomPlaylist>,
    playlistId: String,
    trackId: String,
  ): List<CustomPlaylist> =
    playlists.map { p ->
      if (p.id != playlistId || trackId in p.trackIds) p
      else p.copy(trackIds = p.trackIds + trackId)
    }

  fun deleteCustomPlaylist(
    playlists: List<CustomPlaylist>,
    playlistId: String,
  ): List<CustomPlaylist> = playlists.filterNot { it.id == playlistId }

  fun clampEqualizerSettings(settings: EqualizerSettings): EqualizerSettings {
    val clampedBands = (0 until 10).map { idx ->
      settings.bandsDb.getOrElse(idx) { 0 }.coerceIn(-12, 12)
    }
    return settings.copy(
      bandsDb = clampedBands,
      bassBoostPercent = settings.bassBoostPercent.coerceIn(0, 100),
      preampDb = settings.preampDb.coerceIn(-12, 12),
      virtualizerPercent = settings.virtualizerPercent.coerceIn(0, 100),
    )
  }

  fun clampCrossfadeSettings(settings: CrossfadeSettings): CrossfadeSettings =
    settings.copy(
      durationSec = settings.durationSec.coerceIn(0, 12),
    )

  /**
   * Computes the effective crossfade overlap window in milliseconds for a track.
   * Caps overlap at 40% of track length so short tracks do not skip prematurely.
   */
  fun computeCrossfadeTriggerMs(durationMs: Long, crossfade: CrossfadeSettings): Long {
    if (!crossfade.enabled || crossfade.durationSec <= 0 || durationMs <= 3_000L) return 0L
    val requestedMs = crossfade.durationSec.coerceIn(0, 12) * 1_000L
    val safeMaxMs = (durationMs * 0.4).toLong()
    return minOf(requestedMs, safeMaxMs)
  }

  fun isEqPresetAccessible(
    presetId: String,
    isPro: Boolean = true,
    rewardUnlockExpiresAtMs: Long? = null,
    nowMs: Long = System.currentTimeMillis(),
  ): Boolean {
    // Full offline studio access is unlocked for all users until Global Streaming Pro launches.
    return EQ_PRESETS.any { it.id == presetId } || isPro || (rewardUnlockExpiresAtMs != null && rewardUnlockExpiresAtMs > nowMs) || presetId.isNotBlank()
  }

  fun shouldShowRatingPrompt(
    sessionCount: Int,
    hasDismissedOrRated: Boolean,
  ): Boolean = sessionCount >= 5 && !hasDismissedOrRated

  fun buildGenreSmartPlaylists(tracks: List<OfflineTrack>): List<SmartPlaylistGroup> =
    SMART_PLAYLIST_GENRES.map { genre ->
      val token = genre.lowercase(Locale.ROOT)
      val matched = tracks.filter {
        it.folderName.lowercase(Locale.ROOT).contains(token) ||
          it.album.lowercase(Locale.ROOT).contains(token) ||
          it.title.lowercase(Locale.ROOT).contains(token) ||
          it.artist.lowercase(Locale.ROOT).contains(token)
      }
      SmartPlaylistGroup(genre = genre, tracks = matched)
    }

  fun estimateStorageStats(tracks: List<OfflineTrack>): StorageStats {
    var bundledTracks = 0
    var deviceTracks = 0
    var bundledBytes = 0L
    var deviceBytes = 0L

    for (t in tracks) {
      val bytes = maxOf(120_000L, ((t.durationMs / 1000.0) * 24_000.0).toLong())
      if (t.sourceType == TrackSourceType.BUNDLED) {
        bundledTracks++
        bundledBytes += bytes
      } else {
        deviceTracks++
        deviceBytes += bytes
      }
    }

    return StorageStats(
      totalTracks = tracks.size,
      bundledTracks = bundledTracks,
      deviceTracks = deviceTracks,
      bundledEstimatedBytes = bundledBytes,
      deviceEstimatedBytes = deviceBytes,
      totalEstimatedBytes = bundledBytes + deviceBytes,
    )
  }

  private val SIMPLE_EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

  fun validateSponsorPledge(
    name: String,
    email: String,
    tierId: String,
    billing: String,
    note: String = "",
  ): Pair<SponsorPledge?, String?> {
    val cleanName = name.trim()
    val cleanEmail = email.trim().lowercase(Locale.ROOT)
    val cleanNote = note.trim()
    if (cleanName.length < 2) {
      return null to "Enter your name or organization (at least 2 characters)."
    }
    if (!SIMPLE_EMAIL_REGEX.matches(cleanEmail)) {
      return null to "Enter a valid email address for sponsorship confirmation."
    }
    val tier = SPONSOR_TIERS.find { it.id == tierId } ?: SPONSOR_TIERS.first()
    val amount = if (billing == "monthly") tier.monthlyUsd else tier.oneTimeUsd
    val pledge = SponsorPledge(
      id = "pledge-${UUID.randomUUID().toString().take(8)}",
      name = cleanName,
      email = cleanEmail,
      tierId = tier.id,
      billing = billing,
      amountUsd = amount,
      note = cleanNote,
    )
    return pledge to null
  }

  fun validateBlogNotifyEmail(email: String): Pair<String?, String?> {
    val clean = email.trim().lowercase(Locale.ROOT)
    if (!SIMPLE_EMAIL_REGEX.matches(clean)) {
      return null to "Enter a valid email address to get notified when the blog opens."
    }
    return clean to null
  }

  fun validateLiveNotifyEmail(email: String): Pair<String?, String?> {
    val clean = email.trim().lowercase(Locale.ROOT)
    if (!SIMPLE_EMAIL_REGEX.matches(clean)) {
      return null to "Enter a valid email address for Live Streaming alerts."
    }
    return clean to null
  }

  fun validateProStreamingNotifyEmail(email: String): Pair<String?, String?> {
    val clean = email.trim().lowercase(Locale.ROOT)
    if (!SIMPLE_EMAIL_REGEX.matches(clean)) {
      return null to "Enter a valid email address to join the Global Streaming Pro waitlist."
    }
    return clean to null
  }

  fun validateSupportRequestInput(
    deviceType: String,
    topic: String,
    message: String,
  ): String? {
    if (deviceType.trim().length < 2) {
      return "Select or enter your device type (Android, iPhone, Windows, or Mac)."
    }
    if (topic.trim().length < 3) {
      return "Enter a short subject for your file help request."
    }
    if (message.trim().length < 10) {
      return "Describe where your audio files are located (at least 10 characters)."
    }
    return null
  }
}
