package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.CatalogDenial
import com.shadowspark.morontomornica.domain.CatalogDownloadManager
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.LicensedCatalogConfig
import com.shadowspark.morontomornica.domain.LicensedCatalogRights
import com.shadowspark.morontomornica.domain.LicensedCatalogTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Loop 3: the download manager must enqueue ONLY tracks the fail-closed SAVE_OFFLINE gate
 * authorizes. Denials surface the concrete reason and MUST NOT reach the downloader.
 */
class CatalogDownloadManagerTest {

  private val rights: LicensedCatalogRights = LicensedCatalogConfig.demoRights()
  private val now: Long = System.currentTimeMillis()

  private fun allTracks(): List<LicensedCatalogTrack> = DemoCatalogSource().tracks()

  private fun buildManager(
    territory: String = "NG",
    enqueue: (String, String) -> Unit = { _, _ -> },
    indexHas: (String) -> Boolean = { false },
  ): CatalogDownloadManager =
    CatalogDownloadManager(
      rights = rights,
      territoryProvider = { territory },
      enqueueRequest = enqueue,
      indexHasDownload = indexHas,
    )

  @Test
  fun authorizedOfflineAllowedTrackIsEnqueued() {
    val requests = mutableListOf<Pair<String, String>>()
    val manager = buildManager(enqueue = { url, id -> requests.add(url to id) })
    val track = allTracks().first { it.offlineAllowed }

    val decision = manager.enqueue(track)

    assertTrue(decision.allowed)
    assertEquals(CatalogDenial.NONE, decision.denial)
    assertEquals(listOf(track.mediaUrl to "catalog:${track.id}"), requests)
  }

  @Test
  fun streamOnlyTrackIsRefusedAndNeverEnqueued() {
    var called = false
    val manager = buildManager(enqueue = { _, _ -> called = true })
    val track = allTracks().first { !it.offlineAllowed }

    val decision = manager.enqueue(track)

    assertFalse(decision.allowed)
    assertEquals(CatalogDenial.OFFLINE_NOT_AUTHORIZED, decision.denial)
    assertFalse("stream-only track must not be enqueued", called)
  }

  @Test
  fun expiredTrackIsRefusedAndNeverEnqueued() {
    var called = false
    val manager = buildManager(enqueue = { _, _ -> called = true })
    val track = allTracks().first { it.offlineAllowed }.copy(licenseExpiresAtEpochMs = now - 1L)

    assertFalse(manager.enqueue(track).allowed)
    assertFalse(called)
  }

  @Test
  fun unverifiedRightsTrackIsRefusedAndNeverEnqueued() {
    var called = false
    val manager = buildManager(enqueue = { _, _ -> called = true })
    val track = allTracks().first { it.offlineAllowed }.copy(rightsConfirmed = false)

    assertFalse(manager.enqueue(track).allowed)
    assertEquals(CatalogDenial.UNVERIFIED_RIGHTS, manager.enqueue(track.copy()).denial)
    assertFalse(called)
  }

  @Test
  fun outOfTerritoryTrackIsRefusedAndNeverEnqueued() {
    var called = false
    val manager = buildManager(territory = "US", enqueue = { _, _ -> called = true })
    val track = allTracks().first { it.offlineAllowed }

    assertFalse(manager.enqueue(track).allowed)
    assertEquals(CatalogDenial.TERRITORY_UNAVAILABLE, manager.enqueue(track.copy()).denial)
    assertFalse(called)
  }

  @Test
  fun youtubeUrlTrackIsRefusedAndNeverEnqueued() {
    var called = false
    val manager = buildManager(enqueue = { _, _ -> called = true })
    val track =
      allTracks().first { it.offlineAllowed }
        .copy(mediaUrl = "https://www.youtube.com/watch?v=prqNqQUWXBE")

    assertFalse(manager.enqueue(track).allowed)
    assertEquals(CatalogDenial.INVALID_SOURCE, manager.enqueue(track.copy()).denial)
    assertFalse(called)
  }

  @Test
  fun gateDecisionIsPureAndSeparateFromEnqueue() {
    val manager = buildManager()
    val track = allTracks().first { it.offlineAllowed }
    assertTrue(manager.canDownload(track).allowed)
  }

  @Test
  fun isDownloadedOrQueuedReflectsIndex() {
    val manager = buildManager(indexHas = { it == "catalog:catalog-demo-afrobeat-01" })
    val offlineAllowed = allTracks().first { it.offlineAllowed }
    val streamOnly = allTracks().first { !it.offlineAllowed }
    assertTrue(manager.isDownloadedOrQueued(offlineAllowed))
    assertFalse(manager.isDownloadedOrQueued(streamOnly))
  }
}
