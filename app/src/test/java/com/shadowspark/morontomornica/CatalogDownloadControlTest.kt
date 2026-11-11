package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.CatalogDownloadController
import com.shadowspark.morontomornica.domain.CatalogDownloadManager
import com.shadowspark.morontomornica.domain.CatalogDownloadState
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.DownloadRecord
import com.shadowspark.morontomornica.domain.InMemoryDownloadStore
import com.shadowspark.morontomornica.domain.LicensedCatalogConfig
import com.shadowspark.morontomornica.domain.LicensedCatalogRights
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Loop 4: the router's download-control surface (Wi-Fi-only preference + cancellation) and the
 * enablement path are unit-testable without a live DownloadManager.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CatalogDownloadControlTest {

  private class RecordingDownloadController : CatalogDownloadController {
    var wifiOnly = false
    var cancelledMediaIds = mutableListOf<String>()
    var canCancel = true
    override fun cancelDownload(mediaId: String): Boolean {
      cancelledMediaIds.add(mediaId)
      return canCancel
    }
    override fun setWifiOnlyDownload(enabled: Boolean) { wifiOnly = enabled }
  }

  private fun track() = DemoCatalogSource().tracks().first { it.offlineAllowed }

  @Test
  fun setWifiOnlyDownloadsTogglesStateAndController() {
    val controller = RecordingDownloadController()
    val router = PlaybackRouter().apply { attachDownloadController(controller) }

    router.setWifiOnlyDownloads(true)
    assertTrue(router.state.value.preferWifiOnlyDownloads)
    assertTrue(controller.wifiOnly)

    router.setWifiOnlyDownloads(false)
    assertFalse(router.state.value.preferWifiOnlyDownloads)
    assertFalse(controller.wifiOnly)
  }

  @Test
  fun attachDownloadControllerReplaysCurrentPreference() {
    val router = PlaybackRouter()
    router.setWifiOnlyDownloads(true) // no controller yet; state persists
    val controller = RecordingDownloadController()
    router.attachDownloadController(controller)
    assertTrue("preference must be pushed to a newly attached controller", controller.wifiOnly)
  }

  @Test
  fun cancelDownloadDelegatesAndClearsStateWhenHandled() {
    val controller = RecordingDownloadController()
    val router = PlaybackRouter().apply { attachDownloadController(controller) }
    val t = track()
    val state: MornicaUiState = router.state.value.copy(
      catalogDownloadStates = mapOf(t.id to CatalogDownloadState.DOWNLOADING),
    )
    // Use an internal reset for the test scenario
    router.cancelCatalogDownload(t.id)

    assertEquals(listOf("catalog:${t.id}"), controller.cancelledMediaIds)
    assertTrue(!router.state.value.catalogDownloadStates.containsKey(t.id))
  }

  @Test
  fun cancelDownloadWithoutControllerReturnsFalse() {
    val router = PlaybackRouter()
    assertFalse(router.cancelCatalogDownload(track().id))
  }

  @Test
  fun requestCatalogDownloadWorksAfterAttachingManager() {
    val enqueued = mutableListOf<Pair<String, String>>()
    val manager =
      CatalogDownloadManager(
        rights = LicensedCatalogRights(LicensedCatalogConfig.DEMO_TRUSTED_AUDIO_HOSTS),
        territoryProvider = { "NG" },
        enqueueRequest = { url, id -> enqueued.add(url to id) },
      )
    val t = track()
    val router = PlaybackRouter().apply { attachCatalogDownloadManager(manager) }

    assertTrue(router.requestCatalogDownload(t))
    assertEquals(listOf(t.mediaUrl to "catalog:${t.id}"), enqueued)
    assertEquals(
      CatalogDownloadState.QUEUED,
      router.state.value.catalogDownloadStates[t.id],
    )
  }

  @Test
  fun onCompletedDownloadPersistsSnapshotAndEnablesOfflinePlayback() {
    val store = InMemoryDownloadStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    val t = track() // offlineAllowed demo track
    assertFalse("not downloaded yet", router.offlinePlaybackAllowed(t))

    router.onDownloadStateChanged(t.id, CatalogDownloadState.COMPLETED)

    assertEquals(
      CatalogDownloadState.COMPLETED,
      router.state.value.catalogDownloadStates[t.id],
    )
    assertNotNull("rights snapshot must be persisted on completion", store.get(t.id))
    assertTrue(
      "a completed download must now be playable offline (fail-closed snapshot valid)",
      router.offlinePlaybackAllowed(t),
    )
  }

  @Test
  fun onFailedDownloadSurfacesRetryWithoutOfflineEntitlement() {
    val router = PlaybackRouter().apply { attachDownloadStore(InMemoryDownloadStore()) }
    val t = track()
    router.onDownloadStateChanged(t.id, CatalogDownloadState.FAILED)

    assertEquals(CatalogDownloadState.FAILED, router.state.value.catalogDownloadStates[t.id])
    assertFalse("a failed download must NOT enable offline playback", router.offlinePlaybackAllowed(t))
  }

  @Test
  fun attachDownloadStoreRehydratesCompletedStates() {
    val t = track()
    val store = InMemoryDownloadStore()
    store.save(DownloadRecord.fromTrack(t, "NG", System.currentTimeMillis()))

    val router = PlaybackRouter().apply { attachDownloadStore(store) }

    assertEquals(
      "a store record from a previous session must surface as Offline ready",
      CatalogDownloadState.COMPLETED,
      router.state.value.catalogDownloadStates[t.id],
    )
    assertTrue(router.offlinePlaybackAllowed(t))
  }

  @Test
  fun onAppBackgroundedPreservesBackgroundPlaybackAndClearsYouTubeState() {
    val router = PlaybackRouter()
    val playable = router.state.value.localTracks.first { it.isPlayableOffline() }
    assertTrue(router.playLocalTrack(playable))
    assertTrue(router.state.value.isLocalPlaying)

    router.onAppBackgrounded()

    assertTrue("Backgrounding must preserve active local audio playback", router.state.value.isLocalPlaying)
    assertFalse("Backgrounding must ensure YouTube embed is false", router.state.value.isYouTubeEmbedPlaying)
    assertFalse("Backgrounding must hide YouTube viewport", router.state.value.isYouTubeViewportVisible)
  }

  @Test
  fun headsetUnpluggedPausesLocalPlayback() {
    val router = PlaybackRouter()
    val playable = router.state.value.localTracks.first { it.isPlayableOffline() }
    router.playLocalTrack(playable)
    assertTrue(router.state.value.isLocalPlaying)

    router.onHeadsetDisconnected()

    assertFalse("Headset disconnect must pause playback", router.state.value.isLocalPlaying)
    assertTrue(
      router.state.value.statusBannerMessage.orEmpty().contains("Headset unplugged", ignoreCase = true),
    )
  }

  @Test
  fun audioFocusLostPausesLocalPlayback() {
    val router = PlaybackRouter()
    val playable = router.state.value.localTracks.first { it.isPlayableOffline() }
    router.playLocalTrack(playable)
    assertTrue(router.state.value.isLocalPlaying)

    router.onAudioFocusLost()

    assertFalse("Audio focus loss must pause playback", router.state.value.isLocalPlaying)
    assertTrue(
      router.state.value.statusBannerMessage.orEmpty().contains("Audio focus lost", ignoreCase = true),
    )
  }

  @Test
  fun airplaneModeExpiredLicenseDisallowsOfflinePlayback() {
    val t = track()
    val store = InMemoryDownloadStore()
    val pastTime = System.currentTimeMillis() - 100_000L
    val expiredTrack = t.copy(licenseExpiresAtEpochMs = pastTime)
    val expiredRecord = DownloadRecord.fromTrack(expiredTrack, "NG", pastTime)
    store.save(expiredRecord)

    val router = PlaybackRouter().apply { attachDownloadStore(store) }

    assertFalse(
      "Expired download record must not allow offline playback",
      router.offlinePlaybackAllowed(t),
    )
  }

  @Test
  fun interruptedDownloadDoesNotSurviveRestartAsCompleted() {
    val store = InMemoryDownloadStore()
    val manager = CatalogDownloadManager(
      rights = LicensedCatalogRights(LicensedCatalogConfig.DEMO_TRUSTED_AUDIO_HOSTS),
      territoryProvider = { "NG" },
      enqueueRequest = { _, _ -> },
    )
    val router1 = PlaybackRouter().apply {
      attachCatalogDownloadManager(manager)
      attachDownloadStore(store)
    }
    val t = track()

    // 1) Enqueue download (starts QUEUED, never completes)
    assertTrue(router1.requestCatalogDownload(t))
    assertEquals(CatalogDownloadState.QUEUED, router1.state.value.catalogDownloadStates[t.id])
    assertFalse("Incomplete download must not allow offline playback", router1.offlinePlaybackAllowed(t))

    // 2) Simulate app restart: attach same store to a new router
    val router2 = PlaybackRouter().apply { attachDownloadStore(store) }
    assertFalse(
      "Interrupted/incomplete download must NOT be marked COMPLETED or playable on restart",
      router2.offlinePlaybackAllowed(t),
    )
    assertFalse(router2.state.value.catalogDownloadStates.containsKey(t.id))
  }

  @Test
  fun cancelledDownloadRemovesStoreRecord() {
    val store = InMemoryDownloadStore()
    val controller = RecordingDownloadController()
    val router = PlaybackRouter().apply {
      attachDownloadStore(store)
      attachDownloadController(controller)
    }
    val t = track()

    // Simulate completion
    router.onDownloadStateChanged(t.id, CatalogDownloadState.COMPLETED)
    assertTrue(router.offlinePlaybackAllowed(t))

    // Cancel download
    assertTrue(router.cancelCatalogDownload(t.id))
    assertFalse("Cancelled download must not allow offline playback", router.offlinePlaybackAllowed(t))
    assertTrue("Store record must be removed on cancellation", store.get(t.id) == null)
  }
}

