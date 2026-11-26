package com.shadowspark.morontomornica.core.reco

import com.shadowspark.morontomornica.model.LocalMediaTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShelfModel(
  val key: String,
  val title: String,
  val tracks: List<LocalMediaTrack>,
)

data class ArtistShelf(
  val artist: String,
  val tracks: List<LocalMediaTrack>,
)

/**
 * On-device, zero-network recommendation heuristics. Pure Kotlin (no Android imports)
 * so the rules run under plain JUnit. Listening history stays in memory on-device;
 * nothing about the user's library leaves the device (privacy policy invariant).
 */
class OnDeviceRecommender(
  private val clockMs: () -> Long = { System.currentTimeMillis() },
) {
  private data class PlayCount(
    val trackId: String,
    var plays: Int,
    var lastPlayedAtMs: Long,
  )

  private val events = mutableMapOf<String, PlayCount>()
  private val _historyVersion = MutableStateFlow(0)

  /** Bumped on every history change so Compose shelves can observe it and refresh. */
  val historyVersion: StateFlow<Int> = _historyVersion.asStateFlow()

  fun recordPlay(trackId: String, atMs: Long = clockMs()) {
    val existing = events[trackId]
    if (existing == null) {
      events[trackId] = PlayCount(trackId, 1, atMs)
    } else {
      existing.plays += 1
      existing.lastPlayedAtMs = atMs
    }
    _historyVersion.value += 1
  }

  fun recentTracks(tracks: List<LocalMediaTrack>, limit: Int = 10): List<LocalMediaTrack> {
    val byId = tracks.filter { it.isPlayableOffline() }.associateBy { it.id }
    return events.values
      .sortedByDescending { it.lastPlayedAtMs }
      .mapNotNull { byId[it.trackId] }
      .distinct()
      .take(limit)
  }

  fun favoriteTracks(tracks: List<LocalMediaTrack>, favorites: Set<String>): List<LocalMediaTrack> =
    tracks.filter { it.isPlayableOffline() && it.id in favorites }

  fun mostPlayed(tracks: List<LocalMediaTrack>, limit: Int = 10): List<LocalMediaTrack> {
    val byId = tracks.filter { it.isPlayableOffline() }.associateBy { it.id }
    return events.values
      .sortedByDescending { it.plays }
      .mapNotNull { byId[it.trackId] }
      .distinct()
      .take(limit)
  }

  fun topArtists(
    tracks: List<LocalMediaTrack>,
    maxArtists: Int = 6,
    perArtist: Int = 4,
  ): List<ArtistShelf> =
    tracks
      .filter { it.isPlayableOffline() }
      .groupBy { it.artist }
      .filter { it.value.size >= 2 }
      .entries
      .sortedByDescending { it.value.size }
      .take(maxArtists)
      .map { ArtistShelf(it.key, it.value.take(perArtist)) }

  fun historySize(): Int = events.size

  fun clear() {
    events.clear()
    _historyVersion.value += 1
  }
}
