package com.shadowspark.morontomornica

import androidx.media3.common.C
import com.shadowspark.morontomornica.domain.LocalAudioController
import com.shadowspark.morontomornica.domain.LocalAudioPlaybackService
import com.shadowspark.morontomornica.domain.LocalMediaRepository
import com.shadowspark.morontomornica.domain.MornicaRuntime
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.domain.YouTubeDiscoveryRepository
import com.shadowspark.morontomornica.model.ActiveEngine
import com.shadowspark.morontomornica.model.ArtistLaunchRequest
import com.shadowspark.morontomornica.model.LibraryTab
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.PrimaryScreen
import com.shadowspark.morontomornica.model.ProvenanceStatus
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.TrackSortOption
import com.shadowspark.morontomornica.model.VIDIQ_NIGERIA_SEARCH_ESTIMATES
import com.shadowspark.morontomornica.model.YouTubeSourceBadge
import com.shadowspark.morontomornica.model.YouTubeVideoCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MornicaAcceptanceTest {

  private class FakeLocalAudioController : LocalAudioController {
    var boundRouter: PlaybackRouter? = null
    var lastPlayedTrack: LocalMediaTrack? = null
    var lastQueueIds: List<String> = emptyList()
    var isPlayingState: Boolean = false
    var lastSeekPositionMs: Long = -1L
    var isShuffleOn: Boolean = false
    var currentRepeatMode: RepeatMode = RepeatMode.OFF
    var stoppedCount: Int = 0

    override fun bindRouter(router: PlaybackRouter) {
      boundRouter = router
    }

    override fun playTrack(
      track: LocalMediaTrack,
      queue: List<LocalMediaTrack>,
      repeatMode: RepeatMode,
      shuffleEnabled: Boolean,
    ) {
      lastPlayedTrack = track
      lastQueueIds = queue.map { it.id }
      this.currentRepeatMode = repeatMode
      this.isShuffleOn = shuffleEnabled
      this.isPlayingState = true
      this.lastSeekPositionMs = 0L
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
      this.isPlayingState = playWhenReady
    }

    override fun seekTo(positionMs: Long) {
      this.lastSeekPositionMs = positionMs
    }

    override fun setShuffleEnabled(shuffleEnabled: Boolean) {
      this.isShuffleOn = shuffleEnabled
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
      this.currentRepeatMode = repeatMode
    }

    override fun stop() {
      this.isPlayingState = false
      this.stoppedCount += 1
    }
  }

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
        uri = "https://www.youtube.com/watch?v=prqNqQUWXBE",
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

  @Test
  fun ac8_phase1_media3BackgroundService_ownsExoPlayerAndContinuesAcrossActivityBackgrounding() {
    val serviceController = Robolectric.buildService(LocalAudioPlaybackService::class.java).create()
    val service = serviceController.get()
    try {
      val player = service.exoPlayer
      val session = service.mediaSession
      assertNotNull("LocalAudioPlaybackService must create ExoPlayer", player)
      assertNotNull("LocalAudioPlaybackService must create MediaSession", session)
      assertEquals(C.USAGE_MEDIA, player?.audioAttributes?.usage)
      assertEquals(C.AUDIO_CONTENT_TYPE_MUSIC, player?.audioAttributes?.contentType)

      val router = MornicaRuntime.router
      val demoTrack = router.state.value.localTracks.first { it.isPlayableOffline() }
      assertTrue(router.playLocalTrack(demoTrack))
      assertTrue(router.state.value.isLocalPlaying)
      assertEquals(true, player?.playWhenReady)
      assertEquals(demoTrack.id, player?.currentMediaItem?.mediaId)

      // Backgrounding the Activity pauses YouTube embeds, but MUST keep local audio playing in service
      router.onAppBackgrounded()
      assertTrue("Local audio must continue in background service", router.state.value.isLocalPlaying)
      assertEquals(true, player?.playWhenReady)

      // Seeking via PlaybackRouter forwards directly to ExoPlayer in LocalAudioPlaybackService
      router.seekLocalTo(12_500L)
      assertEquals(12_500L, router.state.value.localPositionMs)
      assertEquals(12_500L, player?.currentPosition)

      // Audio focus loss or headset unplug pauses ExoPlayer
      router.onAudioFocusLost()
      assertFalse(router.state.value.isLocalPlaying)
      assertEquals(false, player?.playWhenReady)
    } finally {
      serviceController.destroy()
    }
  }

  @Test
  fun ac9_phase2_playbackCorrectness_seekShuffleRepeatSkipPositionAndErrorConnectedToPlayer() {
    val fakeController = FakeLocalAudioController()
    val router = PlaybackRouter(localAudioController = fakeController)
    val playableTracks = router.state.value.localTracks.filter { it.isPlayableOffline() }
    assertEquals(3, playableTracks.size)
    val firstTrack = playableTracks[0]
    val secondTrack = playableTracks[1]
    val thirdTrack = playableTracks[2]

    // 1. Start first track -> controller receives playTrack & initial queue
    assertTrue(router.playLocalTrack(firstTrack))
    assertEquals(firstTrack.id, fakeController.lastPlayedTrack?.id)
    assertTrue(fakeController.isPlayingState)
    assertEquals(listOf(firstTrack.id, secondTrack.id, thirdTrack.id), router.state.value.queueTrackIds)

    // 2. Seeking updates both UI state and actual audio player controller (and clamps to [0, duration])
    router.seekLocalTo(14_200L)
    assertEquals(14_200L, router.state.value.localPositionMs)
    assertEquals(14_200L, fakeController.lastSeekPositionMs)
    router.seekLocalTo(999_999L)
    assertEquals(firstTrack.durationMs, router.state.value.localPositionMs)
    assertEquals(firstTrack.durationMs, fakeController.lastSeekPositionMs)

    // 3. Position reporting from player updates UI state
    router.onPlaybackPositionUpdated(positionMs = 5_400L, durationMs = 30_000L)
    assertEquals(5_400L, router.state.value.localPositionMs)
    assertEquals(30_000L, router.state.value.localDurationMs)

    // 4. skipPrevious when position > 3000ms restarts current track at 0ms instead of jumping back
    router.skipPrevious()
    assertEquals(firstTrack.id, router.state.value.activeLocalTrack?.id)
    assertEquals(0L, router.state.value.localPositionMs)
    assertEquals(0L, fakeController.lastSeekPositionMs)

    // 5. skipNext advances to secondTrack and updates actual audio player
    router.skipNext()
    assertEquals(secondTrack.id, router.state.value.activeLocalTrack?.id)
    assertEquals(secondTrack.id, fakeController.lastPlayedTrack?.id)

    // 6. Shuffle toggle updates controller.isShuffleOn and reorders queue deterministically
    router.toggleShuffle()
    assertTrue(router.state.value.shuffleEnabled)
    assertTrue(fakeController.isShuffleOn)
    assertEquals(secondTrack.id, router.state.value.queueTrackIds.first())

    // Disable shuffle to restore natural order [firstTrack, secondTrack, thirdTrack]
    router.toggleShuffle()
    assertFalse(router.state.value.shuffleEnabled)
    assertFalse(fakeController.isShuffleOn)
    assertEquals(listOf(firstTrack.id, secondTrack.id, thirdTrack.id), router.state.value.queueTrackIds)

    // 7. Repeat cycling updates controller.currentRepeatMode
    assertEquals(RepeatMode.OFF, router.state.value.repeatMode)
    router.cycleRepeatMode()
    assertEquals(RepeatMode.ALL, router.state.value.repeatMode)
    assertEquals(RepeatMode.ALL, fakeController.currentRepeatMode)
    router.cycleRepeatMode()
    assertEquals(RepeatMode.ONE, router.state.value.repeatMode)
    assertEquals(RepeatMode.ONE, fakeController.currentRepeatMode)
    router.cycleRepeatMode()
    assertEquals(RepeatMode.OFF, router.state.value.repeatMode)
    assertEquals(RepeatMode.OFF, fakeController.currentRepeatMode)

    // 8. Player error stops playback and surfaces error message in state
    router.onPlayerError("Audio decoder initialization failed")
    assertFalse(router.state.value.isLocalPlaying)
    assertEquals("Audio decoder initialization failed", router.state.value.playbackError)
    assertTrue(fakeController.stoppedCount > 0)
  }

  @Test
  fun ac10_phase2_endOfTrackBehavior_handlesRepeatOffRepeatAllAndRepeatOne() {
    val fakeController = FakeLocalAudioController()
    val router = PlaybackRouter(localAudioController = fakeController)
    val playable = router.state.value.localTracks.filter { it.isPlayableOffline() }
    val firstTrack = playable[0]
    val secondTrack = playable[1]
    val thirdTrack = playable[2]

    // Case A: RepeatMode.OFF -> advances through queue, then stops at end of queue
    router.playLocalTrack(firstTrack)
    router.onTrackEnded()
    assertEquals(secondTrack.id, router.state.value.activeLocalTrack?.id)
    assertTrue(router.state.value.isLocalPlaying)

    router.onTrackEnded()
    assertEquals(thirdTrack.id, router.state.value.activeLocalTrack?.id)
    assertTrue(router.state.value.isLocalPlaying)

    router.onTrackEnded()
    assertFalse("RepeatMode.OFF must stop at end of queue", router.state.value.isLocalPlaying)
    assertFalse(fakeController.isPlayingState)
    assertEquals(0L, router.state.value.localPositionMs)

    // Case B: RepeatMode.ALL -> wraps around from thirdTrack back to firstTrack
    router.playLocalTrack(thirdTrack)
    router.cycleRepeatMode() // OFF -> ALL
    assertEquals(RepeatMode.ALL, router.state.value.repeatMode)
    router.onTrackEnded()
    assertEquals(firstTrack.id, router.state.value.activeLocalTrack?.id)
    assertTrue(router.state.value.isLocalPlaying)

    // Case C: RepeatMode.ONE -> stays on current track and seeks to 0L
    router.cycleRepeatMode() // ALL -> ONE
    assertEquals(RepeatMode.ONE, router.state.value.repeatMode)
    router.seekLocalTo(18_000L)
    router.onTrackEnded()
    assertEquals(firstTrack.id, router.state.value.activeLocalTrack?.id)
    assertEquals(0L, router.state.value.localPositionMs)
    assertEquals(0L, fakeController.lastSeekPositionMs)
    assertTrue(router.state.value.isLocalPlaying)
  }

  @Test
  fun ac11_phase3_verifiedContentAndProvenance_rejectsFabricatedIdsAndProvidesHonestEmptyState() {
    val ytRepo = YouTubeDiscoveryRepository(apiKey = "")
    val allCurated = ytRepo.searchOfficialVideos("All", risingCreatorsOnly = false).items
    assertEquals(7, allCurated.size)

    // Every curated video must have a real 11-character YouTube video ID and 24-character UC channel ID
    val validVideoIdRegex = Regex("^[a-zA-Z0-9_-]{11}$")
    val validChannelIdRegex = Regex("^UC[a-zA-Z0-9_-]{22}$")
    allCurated.forEach { video ->
      assertTrue("Invalid videoId: ${video.videoId}", validVideoIdRegex.matches(video.videoId))
      assertTrue("Invalid channelId: ${video.channelId}", validChannelIdRegex.matches(video.channelId))
      assertFalse(video.channelId.startsWith("UC_"))
      assertTrue(video.isPolicyCompliant())
    }

    // Fabricated placeholder IDs from earlier drafts must fail policy validation
    val fabricatedVideo =
      YouTubeVideoCard(
        videoId = "7f8d9Asake01",
        title = "Fabricated Placeholder",
        channelId = "UC_AsakeOfficial",
        channelTitle = "Fake Channel",
        category = "Afrobeats",
        publishedAt = "2024-01-01",
        sourceBadge = YouTubeSourceBadge.CURATED_FALLBACK,
      )
    assertFalse(fabricatedVideo.isPolicyCompliant())

    // Rising creators filter or unmatched query returns honest empty state + external YouTube search URL
    val router = PlaybackRouter(youtubeRepository = ytRepo)
    router.updateSearchQuery("All", risingOnly = true)
    assertTrue(
      "Rising creators must return honest empty list when no verified rising channel is cached",
      router.state.value.youtubeVideos.isEmpty(),
    )
    assertTrue(
      router.state.value.externalYouTubeSearchUrl.startsWith(
        "https://www.youtube.com/results?search_query=",
      ),
    )

    router.updateSearchQuery("NonExistentArtistXYZ999", risingOnly = false)
    assertTrue(router.state.value.youtubeVideos.isEmpty())
    assertTrue(router.state.value.externalYouTubeSearchUrl.contains("NonExistentArtistXYZ999"))

    // Audit local tracks and creator profiles: zero unsupported LICENSED assertions or invented channels
    val localRepo = LocalMediaRepository()
    val bundledTracks = localRepo.defaultDemoAndCatalogTracks()
    bundledTracks.forEach { track ->
      assertNotEquals(
        "Bundled tracks must not assert LICENSED without signed agreement",
        ProvenanceStatus.LICENSED,
        track.provenance,
      )
      if (track.isPlayableOffline()) {
        assertEquals(ProvenanceStatus.DEMO, track.provenance)
      }
    }

    val profiles = localRepo.verifiedCreatorProfiles()
    profiles.forEach { profile ->
      assertEquals(ProvenanceStatus.DEMO, profile.provenanceBadge)
      assertNull(
        "Demo creator profiles must not invent unverified YouTube channel URLs",
        profile.officialYouTubeChannelUrl,
      )
    }
  }

  @Test
  fun ac12_originalDesignRecovery_libraryTabsSearchSortAndNowPlayingSheetState() {
    val router = PlaybackRouter()

    // 1. Default screen opens to MY_MUSIC matching original Moronto Music (old-home.png)
    assertEquals(PrimaryScreen.MY_MUSIC, router.state.value.currentScreen)
    assertEquals(
      listOf("Tracks", "Queues", "Albums", "Artists", "Folders"),
      LibraryTab.entries.map { it.label },
    )

    // 2. Sort options: Title (asc), Artist (asc), Duration (desc), Year (desc)
    router.selectTrackSortOption(TrackSortOption.TITLE)
    val byTitle = router.filteredAndSortedLocalTracks()
    assertEquals(byTitle.map { it.title }.sorted(), byTitle.map { it.title })

    router.selectTrackSortOption(TrackSortOption.ARTIST)
    val byArtist = router.filteredAndSortedLocalTracks()
    assertEquals(byArtist.map { it.artist }.sorted(), byArtist.map { it.artist })

    router.selectTrackSortOption(TrackSortOption.DURATION)
    val byDuration = router.filteredAndSortedLocalTracks()
    assertTrue(byDuration.first().durationMs >= byDuration.last().durationMs)

    router.selectTrackSortOption(TrackSortOption.YEAR)
    val byYear = router.filteredAndSortedLocalTracks()
    assertTrue(byYear.first().releaseYear >= byYear.last().releaseYear)

    // 3. Live local search filters across title, artist, album, and folder
    router.updateLocalSearchQuery("Teniola")
    val searched = router.filteredAndSortedLocalTracks()
    assertEquals(1, searched.size)
    assertEquals("Teniola Blaze", searched.first().artist)

    router.updateLocalSearchQuery("")
    assertEquals(router.state.value.localTracks.size, router.filteredAndSortedLocalTracks().size)

    // 4. Full-screen Now Playing sheet expansion state
    assertFalse(router.state.value.isNowPlayingExpanded)
    router.setNowPlayingExpanded(true)
    assertTrue(router.state.value.isNowPlayingExpanded)
    router.setNowPlayingExpanded(false)
    assertFalse(router.state.value.isNowPlayingExpanded)
  }
}
