package com.shadowspark.morontomornica.ui.home

import com.shadowspark.morontomornica.core.reco.OnDeviceRecommender
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.ProvenanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFeedModelTest {

  private fun track(id: String, artist: String = "Artist") =
    LocalMediaTrack(
      id = id,
      title = "Title $id",
      artist = artist,
      album = "Album",
      folderName = "Folder",
      durationMs = 200_000L,
      uri = "content://media/external/audio/media/$id",
      provenance = ProvenanceStatus.LOCAL_DEVICE,
      rightsConfirmed = true,
    )

  @Test
  fun build_emptyLibrary_yieldsEmptyShelves() {
    val feed = HomeFeedModel.build(emptyList(), favorites = emptySet(), recommender = OnDeviceRecommender())
    assertTrue(feed.shelves.isEmpty())
    assertTrue(feed.artistShelves.isEmpty())
    assertEquals(0, feed.totalTracks)
  }

  @Test
  fun build_favoritesYieldLikedShelf() {
    val tracks = listOf(track("1"), track("2"))
    val feed = HomeFeedModel.build(tracks, favorites = setOf("1"), recommender = OnDeviceRecommender())

    val liked = feed.shelves.firstOrNull { it.key == "liked" }
    assertTrue(liked != null)
    assertEquals(1, liked!!.tracks.size)
    assertEquals("1", liked.tracks.first().id)
  }

  @Test
  fun build_recentHistoryYieldsRecentShelf() {
    val reco = OnDeviceRecommender()
    reco.recordPlay("1")
    val tracks = listOf(track("1"), track("2"))

    val feed = HomeFeedModel.build(tracks, favorites = emptySet(), recommender = reco)
    val recent = feed.shelves.firstOrNull { it.key == "recent" }
    assertTrue(recent != null)
    assertEquals("1", recent!!.tracks.first().id)
  }
}
