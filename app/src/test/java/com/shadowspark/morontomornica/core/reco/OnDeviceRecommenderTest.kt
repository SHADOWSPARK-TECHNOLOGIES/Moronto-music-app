package com.shadowspark.morontomornica.core.reco

import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.ProvenanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnDeviceRecommenderTest {

  private fun track(
    id: String,
    title: String,
    artist: String,
    releaseYear: Int = 2026,
  ) =
    LocalMediaTrack(
      id = id,
      title = title,
      artist = artist,
      album = "Album",
      folderName = "Folder",
      durationMs = 200_000L,
      uri = "content://media/external/audio/media/$id",
      provenance = ProvenanceStatus.LOCAL_DEVICE,
      rightsConfirmed = true,
      releaseYear = releaseYear,
    )

  @Test
  fun recentTracks_ordersByLatestPlayFirst() {
    val reco = OnDeviceRecommender(clockMs = { 0L })
    val a = track("a", "A", "Artist One")
    val b = track("b", "B", "Artist One")
    reco.recordPlay("a", atMs = 1_000L)
    reco.recordPlay("b", atMs = 2_000L)

    val recent = reco.recentTracks(listOf(a, b))
    assertEquals("b", recent.first().id)
  }

  @Test
  fun favoriteTracks_filtersByFavoriteIdsAndPlayable() {
    val reco = OnDeviceRecommender()
    val liked = track("liked", "Liked", "Artist Two")
    val other = track("other", "Other", "Artist Two")

    val result = reco.favoriteTracks(listOf(liked, other), favorites = setOf("liked"))
    assertEquals(1, result.size)
    assertEquals("liked", result.first().id)
  }

  @Test
  fun mostPlayed_ordersByPlayCountDescending() {
    val reco = OnDeviceRecommender()
    val a = track("a", "A", "Artist One")
    val b = track("b", "B", "Artist One")
    repeat(3) { reco.recordPlay("a") }
    repeat(1) { reco.recordPlay("b") }

    val top = reco.mostPlayed(listOf(a, b))
    assertEquals("a", top.first().id)
  }

  @Test
  fun topArtists_groupsArtistsWithAtLeastTwoTracks() {
    val reco = OnDeviceRecommender()
    val tracks =
      listOf(
        track("a1", "A1", "Asake"),
        track("a2", "A2", "Asake"),
        track("b1", "B1", "Solo Artist"),
      )

    val shelves = reco.topArtists(tracks)
    assertEquals(1, shelves.size)
    assertEquals("Asake", shelves.first().artist)
    assertEquals(2, shelves.first().tracks.size)
  }

  @Test
  fun clear_resetsHistory() {
    val reco = OnDeviceRecommender()
    reco.recordPlay("a")
    reco.clear()
    assertTrue(reco.historySize() == 0)
  }
}
