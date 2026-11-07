package com.shadowspark.morontomornica

import androidx.media3.common.MediaItem
import com.shadowspark.morontomornica.domain.CatalogAccess
import com.shadowspark.morontomornica.domain.CatalogDenial
import com.shadowspark.morontomornica.domain.LicensedCatalogConfig
import com.shadowspark.morontomornica.domain.LicensedCatalogRights
import com.shadowspark.morontomornica.domain.LocalAudioController
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.ActiveEngine
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Loop 2: proves the fail-closed CatalogAccess gate is wired into the router so that
 * ONLY authorized licensed tracks reach the same native Media3 player. Nothing here
 * renders audio; it asserts the gate -> MediaItem handoff contract and that every
 * denial path stops before touching the controller.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LicensedCatalogPlaybackTest {

  private class RecordingCatalogController : LocalAudioController {
    val playedMediaItems = mutableListOf<MediaItem>()
    var lastPlayWhenReady: Boolean = false
    var stopped: Boolean = false

    override fun bindRouter(router: PlaybackRouter) = Unit
    override fun playTrack(
      track: LocalMediaTrack,
      queue: List<LocalMediaTrack>,
      repeatMode: RepeatMode,
      shuffleEnabled: Boolean,
    ) {
      playedMediaItems.clear()
    }

    override fun playMediaItems(items: List<MediaItem>, startIndex: Int) {
      playedMediaItems.clear()
      playedMediaItems.addAll(items)
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) { this.lastPlayWhenReady = playWhenReady }
    override fun seekTo(positionMs: Long) = Unit
    override fun setShuffleEnabled(shuffleEnabled: Boolean) = Unit
    override fun setRepeatMode(repeatMode: RepeatMode) = Unit
    override fun stop() { stopped = true }
  }

  private fun ngRouter(controller: RecordingCatalogController): PlaybackRouter =
    PlaybackRouter(
      localAudioController = controller,
      territoryProvider = { "NG" },
    )

  @Test
  fun authorizedNigerianDemoTrackReachesNativePlayerAsMediaItem() {
    val controller = RecordingCatalogController()
    val router = ngRouter(controller)
    val track = router.state.value.catalogTracks.first { it.offlineAllowed }

    assertTrue(router.playCatalogTrack(track))

    assertEquals(1, controller.playedMediaItems.size)
    val item = controller.playedMediaItems.single()
    assertEquals("catalog:${track.id}", item.mediaId)
    assertEquals(track.mediaUrl, item.localConfiguration?.uri?.toString())
    assertTrue(item.localConfiguration!!.uri.scheme == "https")

    // isLocalPlaying is NOT optimistic: it flips true only when the controller confirms
    // the player actually reached a playing state (mirroring the real Media3 listener).
    assertFalse(router.state.value.isLocalPlaying)
    router.onPlayerIsPlayingChanged(true)
    assertTrue(router.state.value.isLocalPlaying)

    val state = router.state.value
    assertEquals(ActiveEngine.LICENSED_CATALOG, state.activeEngine)
    assertEquals(track, state.activeCatalogTrack)
    assertNull(state.activeLocalTrack)
  }

  @Test
  fun streamOnlyTrackStreamsButWouldNotBeSavedOffline() {
    val controller = RecordingCatalogController()
    val router = ngRouter(controller)
    val track = router.state.value.catalogTracks.first { !it.offlineAllowed }

    // Streaming is allowed...
    assertTrue(router.playCatalogTrack(track))
    assertTrue(controller.playedMediaItems.isNotEmpty())

    // ...but the gate still refuses offline saving for the same track.
    val decision = LicensedCatalogConfig.demoRights().evaluate(track, CatalogAccess.SAVE_OFFLINE, "NG")
    assertFalse(decision.allowed)
    assertEquals(CatalogDenial.OFFLINE_NOT_AUTHORIZED, decision.denial)
  }

  @Test
  fun outOfTerritoryTrackIsDeniedWithoutTouchingThePlayer() {
    val controller = RecordingCatalogController()
    val router = PlaybackRouter(
      localAudioController = controller,
      territoryProvider = { "US" },
    )
    val track = router.state.value.catalogTracks.first { it.id == "catalog-demo-afrobeat-01" }

    assertFalse(router.playCatalogTrack(track))
    assertTrue(controller.playedMediaItems.isEmpty())
    assertNull(router.state.value.activeCatalogTrack)
    assertTrue(router.state.value.statusBannerMessage.orEmpty().contains("region", ignoreCase = true))
  }

  @Test
  fun expiredAndUnverifiedTracksFailClosedBeforeReachingPlayer() {
    val controller = RecordingCatalogController()
    val router = ngRouter(controller)
    val sample = router.state.value.catalogTracks.first()
    val now = System.currentTimeMillis()

    val expired = sample.copy(licenseExpiresAtEpochMs = now - 1L)
    assertFalse(router.playCatalogTrack(expired))
    assertTrue(controller.playedMediaItems.isEmpty())

    val unverified = sample.copy(rightsConfirmed = false)
    assertFalse(router.playCatalogTrack(unverified))
    assertTrue(controller.playedMediaItems.isEmpty())
  }

  @Test
  fun youtubeUrlCannotEnterCatalogPlaybackPath() {
    val controller = RecordingCatalogController()
    val router = ngRouter(controller)
    val sample = router.state.value.catalogTracks.first()
    val youtubeTrack =
      sample.copy(mediaUrl = "https://www.youtube.com/watch?v=prqNqQUWXBE")

    assertFalse(router.playCatalogTrack(youtubeTrack))
    assertTrue(controller.playedMediaItems.isEmpty())
  }

  @Test
  fun emptyTrustedHostConfigDisablesAllCatalogPlayback() {
    val controller = RecordingCatalogController()
    val router = PlaybackRouter(
      localAudioController = controller,
      catalogRights = LicensedCatalogRights(emptySet()),
      territoryProvider = { "NG" },
    )
    val track = router.state.value.catalogTracks.first()

    assertFalse(router.playCatalogTrack(track))
    assertTrue(controller.playedMediaItems.isEmpty())
  }

  @Test
  fun catalogMediaTransitionMarksActiveCatalogTrack() {
    val router = ngRouter(RecordingCatalogController())
    val id = router.state.value.catalogTracks.first().id

    router.onMediaTrackTransitioned("catalog:$id", positionMs = 1500L)

    assertEquals(ActiveEngine.LICENSED_CATALOG, router.state.value.activeEngine)
    assertNotNull(router.state.value.activeCatalogTrack)
    assertEquals(id, router.state.value.activeCatalogTrack!!.id)
  }
}
