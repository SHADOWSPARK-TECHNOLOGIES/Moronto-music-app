package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.LocalMediaRepository
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.domain.YouTubeDiscoveryRepository
import com.shadowspark.morontomornica.model.ActiveEngine
import com.shadowspark.morontomornica.model.ArtistLaunchRequest
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.PrimaryScreen
import com.shadowspark.morontomornica.model.ProvenanceStatus
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.VIDIQ_NIGERIA_SEARCH_ESTIMATES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MornicaAcceptanceTest {

  @Test
  fun ac1_engineIsolation_localAudioRejectsYouTubeUrisAndUnverifiedTracks() {
    val router = PlaybackRouter()
    val illegalYouTubeTrack =
      LocalMediaTrack(
        id = "illegal-yt",
        title = "Attempted YouTube Stream Rip",
        artist = "Asake",
        album = "Unauthorized",
        folderName = "Downloads",
        durationMs = 180_000L,
        uri = "https://www.youtube.com/watch?v=7f8d9Asake01",
        provenance = ProvenanceStatus.LOCAL_DEVICE,
        rightsConfirmed = true,
      )

    assertFalse(illegalYouTubeTrack.isPlayableOffline())
    assertFalse(router.playLocalTrack(illegalYouTubeTrack))
    assertNull(router.state.value.activeLocalTrack)

    val unavailableTrack =
      router.state.value.localTracks.first { it.provenance == ProvenanceStatus.UNAVAILABLE }
    assertFalse(unavailableTrack.isPlayableOffline())
    assertFalse(router.playLocalTrack(unavailableTrack))
  }

  @Test
  fun ac2_playbackArbitration_switchingBetweenYouTubeAndLocalAudioPausesOtherEngine() {
    val router = PlaybackRouter()
    val demoTrack = router.state.value.localTracks.first { it.isPlayableOffline() }
    val ytVideo = router.state.value.youtubeVideos.first()

    // 1. Start local audio first
    assertTrue(router.playLocalTrack(demoTrack))
    assertEquals(ActiveEngine.LOCAL_AUDIO, router.state.value.activeEngine)
    assertTrue(router.state.value.isLocalPlaying)
    assertFalse(router.state.value.isYouTubeEmbedPlaying)

    // 2. Start YouTube embed -> must pause local audio immediately
    assertTrue(router.startYouTubeEmbed(ytVideo, viewportWidthDp = 360, viewportHeightDp = 220))
    assertEquals(ActiveEngine.YOUTUBE_EMBED, router.state.value.activeEngine)
    assertTrue(router.state.value.isYouTubeEmbedPlaying)
    assertFalse(router.state.value.isLocalPlaying)

    // 3. Start local audio again -> must stop YouTube embed immediately
    assertTrue(router.playLocalTrack(demoTrack))
    assertEquals(ActiveEngine.LOCAL_AUDIO, router.state.value.activeEngine)
    assertTrue(router.state.value.isLocalPlaying)
    assertFalse(router.state.value.isYouTubeEmbedPlaying)
    assertNull(router.state.value.activeYouTubeVideo)
  }

  @Test
  fun ac3_researchSeededDiscovery_containsAllSevenVidIqNigeriaSearchEstimatesWithDisclaimer() {
    val expected =
      mapOf(
        "Asake" to 207_295,
        "Davido" to 123_964,
        "Wizkid" to 94_555,
        "Burna Boy" to 94_014,
        "Rema" to 82_179,
        "Omah Lay" to 30_694,
        "Amapiano" to 25_401,
      )
    assertEquals(7, VIDIQ_NIGERIA_SEARCH_ESTIMATES.size)
    VIDIQ_NIGERIA_SEARCH_ESTIMATES.forEach { item ->
      assertEquals(expected[item.query], item.monthlyNigerianSearches)
      assertTrue(item.isThirdPartyEstimate)
      assertTrue(item.framingNote.contains("directional", ignoreCase = true))
    }

    val repo = YouTubeDiscoveryRepository(apiKey = "")
    expected.keys.forEach { query ->
      val res = repo.searchOfficialVideos(query)
      assertTrue("Expected curated discovery results for $query", res.items.isNotEmpty())
      res.items.forEach { video ->
        assertTrue(video.isPolicyCompliant())
        assertTrue(video.officialEmbedUrl().startsWith("https://www.youtube.com/embed/"))
        assertTrue(video.officialWatchUrl().startsWith("https://www.youtube.com/watch?v="))
      }
    }
  }

  @Test
  fun ac4_quotaAwareCaching_enforces24HourMaxTtlAndEvictsExpiredEntries() {
    var fakeNowMs = 1_700_000_000_000L
    val repo = YouTubeDiscoveryRepository(apiKey = "", clockMs = { fakeNowMs })

    val firstFetch = repo.searchOfficialVideos("Davido")
    assertFalse(firstFetch.fromCache)
    assertEquals(
      YouTubeDiscoveryRepository.MAX_CACHE_TTL_MS,
      firstFetch.expiresAtEpochMs - firstFetch.fetchedAtEpochMs,
    )

    // Advance 2 hours -> must be served from cache
    fakeNowMs += 2L * 60L * 60L * 1000L
    val cachedFetch = repo.searchOfficialVideos("Davido")
    assertTrue(cachedFetch.fromCache)

    // Advance past 24 hours -> cache expires and refreshes
    fakeNowMs += 23L * 60L * 60L * 1000L
    val refreshedFetch = repo.searchOfficialVideos("Davido")
    assertFalse(refreshedFetch.fromCache)
  }

  @Test
  fun ac5_visiblePlayerAndBackgroundPause_blocksUndersizedEmbedAndPausesOnBackground() {
    val router = PlaybackRouter()
    val ytVideo = router.state.value.youtubeVideos.first()

    // Undersized viewport (< 200dp) must be rejected per YouTube policy
    assertFalse(router.startYouTubeEmbed(ytVideo, viewportWidthDp = 120, viewportHeightDp = 120))
    assertFalse(router.state.value.isYouTubeEmbedPlaying)

    // Valid visible viewport (>= 200dp) succeeds
    assertTrue(router.startYouTubeEmbed(ytVideo, viewportWidthDp = 320, viewportHeightDp = 220))
    assertTrue(router.state.value.isYouTubeEmbedPlaying)

    // App backgrounding or switching screens pauses YouTube embed immediately
    router.onAppBackgrounded()
    assertFalse(router.state.value.isYouTubeEmbedPlaying)

    // Restart and switch to My Music screen -> pauses YouTube embed
    assertTrue(router.startYouTubeEmbed(ytVideo, viewportWidthDp = 320, viewportHeightDp = 220))
    router.selectScreen(PrimaryScreen.MY_MUSIC)
    assertFalse(router.state.value.isYouTubeEmbedPlaying)
  }

  @Test
  fun ac6_musicoletOfflineLibrary_groupsByFolderArtistAlbumAndHandlesHeadsetUnplug() {
    val localRepo = LocalMediaRepository()
    val tracks = localRepo.defaultDemoAndCatalogTracks()

    val byFolder = localRepo.groupByFolder(tracks)
    val byArtist = localRepo.groupByArtist(tracks)
    val byAlbum = localRepo.groupByAlbum(tracks)

    assertTrue(byFolder.containsKey("Mornica Demos"))
    assertTrue(byArtist.containsKey("Teniola Blaze"))
    assertTrue(byAlbum.containsKey("Mornica Creator Sessions"))

    val router = PlaybackRouter()
    val playable = tracks.first { it.isPlayableOffline() }
    assertTrue(router.playLocalTrack(playable))
    assertTrue(router.state.value.isLocalPlaying)

    // Headset disconnect (ACTION_AUDIO_BECOMING_NOISY) pauses playback
    router.onHeadsetDisconnected()
    assertFalse(router.state.value.isLocalPlaying)

    // Resume and test repeat mode cycling + playlist addition
    router.toggleLocalPlayPause()
    assertTrue(router.state.value.isLocalPlaying)
    assertEquals(RepeatMode.OFF, router.state.value.repeatMode)
    router.cycleRepeatMode()
    assertEquals(RepeatMode.ALL, router.state.value.repeatMode)
    router.cycleRepeatMode()
    assertEquals(RepeatMode.ONE, router.state.value.repeatMode)

    assertTrue(router.addToPlaylist("Lagos Commute", playable.id))
    assertTrue(router.state.value.playlists["Lagos Commute"]?.contains(playable.id) == true)
  }

  @Test
  fun ac7_entitlementSeparationAndCreatorIntake_validatesRightsAttestation() {
    val router = PlaybackRouter()
    // All YouTube videos are strictly free/unpaywalled
    router.state.value.youtubeVideos.forEach { video ->
      assertTrue(video.isFreeUnpaywalled)
    }

    // Creator launch package submission without rights attestation is rejected
    val unattested =
      ArtistLaunchRequest(
        artistName = "Teniola Blaze",
        trackTitle = "Lagos Dawn",
        genre = "Afrobeats",
        contactEmailOrWhatsApp = "teniola@example.com",
        rightsAttested = false,
      )
    assertFalse(router.submitArtistLaunchRequest(unattested))
    assertTrue(router.state.value.submittedLaunchRequests.isEmpty())

    // Attested creator launch package submission succeeds
    val attested = unattested.copy(rightsAttested = true)
    assertTrue(router.submitArtistLaunchRequest(attested))
    assertEquals(1, router.state.value.submittedLaunchRequests.size)
    assertNotNull(router.state.value.statusBannerMessage)
  }
}
