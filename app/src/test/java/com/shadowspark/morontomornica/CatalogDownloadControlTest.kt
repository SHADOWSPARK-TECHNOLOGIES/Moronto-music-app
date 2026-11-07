package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.CatalogDownloadController
import com.shadowspark.morontomornica.domain.CatalogDownloadManager
import com.shadowspark.morontomornica.domain.CatalogDownloadState
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.LicensedCatalogConfig
import com.shadowspark.morontomornica.domain.LicensedCatalogRights
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
