package com.shadowspark.morontomornica.ui.home

import com.shadowspark.morontomornica.core.reco.ArtistShelf
import com.shadowspark.morontomornica.core.reco.OnDeviceRecommender
import com.shadowspark.morontomornica.core.reco.ShelfModel
import com.shadowspark.morontomornica.model.LocalMediaTrack

/**
 * Pure feed builder for the Home shelf screen. Framework-free so it can be unit-tested
 * without Robolectric; the Compose screen only renders [Feed].
 */
object HomeFeedModel {
  data class Feed(
    val shelves: List<ShelfModel>,
    val artistShelves: List<ArtistShelf>,
    val totalTracks: Int,
  )

  fun build(
    tracks: List<LocalMediaTrack>,
    favorites: Set<String>,
    recommender: OnDeviceRecommender,
  ): Feed {
    val playable = tracks.filter { it.isPlayableOffline() }
    val shelves = buildList {
      val recent = recommender.recentTracks(playable, limit = 10)
      if (recent.isNotEmpty()) add(ShelfModel("recent", "Recent", recent))

      val liked = recommender.favoriteTracks(playable, favorites)
      if (liked.isNotEmpty()) add(ShelfModel("liked", "Liked songs", liked))

      val top = recommender.mostPlayed(playable, limit = 10)
      if (top.isNotEmpty()) add(ShelfModel("top", "Most played", top))
    }
    val artistShelves = recommender.topArtists(playable)
    return Feed(shelves, artistShelves, playable.size)
  }
}
