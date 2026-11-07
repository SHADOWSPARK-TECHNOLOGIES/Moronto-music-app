package com.shadowspark.morontomornica

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.DownloadRecord
import com.shadowspark.morontomornica.domain.FileDownloadStore
import com.shadowspark.morontomornica.domain.InMemoryDownloadStore
import com.shadowspark.morontomornica.domain.LicensedCatalogTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Loop 3: offline playback must fail closed against the stored rights snapshot even AFTER
 * bytes are downloaded. A missing, expired, revoked, or mismatched-territory snapshot is a
 * denial, never a pass.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DownloadStoreTest {

  private val now: Long = 1_800_000_000_000L
  private val track: LicensedCatalogTrack =
    DemoCatalogSource().tracks().first { it.offlineAllowed }

  private fun validRecord(): DownloadRecord =
    DownloadRecord.fromTrack(track, "NG", now)

  @Test
  fun validSnapshotAllowsOfflinePlayback() {
    assertTrue(validRecord().mayPlayOffline("NG", now))
    assertTrue(validRecord().mayPlayOffline("ng", now))
  }

  @Test
  fun eachDenialFixtureFailsClosed() {
    assertFalse(validRecord().copy(offlineAllowed = false).mayPlayOffline("NG", now))
    assertFalse(validRecord().copy(rightsConfirmed = false).mayPlayOffline("NG", now))
    assertFalse(validRecord().copy(territoryGranted = "GH").mayPlayOffline("NG", now))
    assertFalse(validRecord().copy(licenseExpiresAtEpochMs = now).mayPlayOffline("NG", now))
    assertFalse(validRecord().copy(licenseExpiresAtEpochMs = now - 1L).mayPlayOffline("NG", now))
  }

  @Test
  fun noExpiryDoesNotChangeTierOfValidSnapshot() {
    assertTrue(validRecord().copy(licenseExpiresAtEpochMs = null).mayPlayOffline("NG", now))
  }

  @Test
  fun inMemoryStoreRoundTrips() {
    val store = InMemoryDownloadStore()
    assertNull(store.get(track.id))
    store.save(validRecord())
    assertEquals(track.id, store.get(track.id)!!.trackId)
    assertEquals("catalog:${track.id}", store.get(track.id)!!.downloadId)
    store.remove(track.id)
    assertNull(store.get(track.id))
  }

  @Test
  fun jsonRoundTripPreservesSnapshot() {
    val source = validRecord().copy(licenseExpiresAtEpochMs = now + 60_000L)
    val json = source.toJson()
    val decoded = DownloadRecord.fromJson(json)
    assertEquals(source.trackId, decoded.trackId)
    assertEquals(source.downloadId, decoded.downloadId)
    assertEquals(source.mediaUrl, decoded.mediaUrl)
    assertEquals(source.licenseReference, decoded.licenseReference)
    assertEquals(source.rightsConfirmed, decoded.rightsConfirmed)
    assertEquals(source.offlineAllowed, decoded.offlineAllowed)
    assertEquals(source.territoryGranted, decoded.territoryGranted)
    assertEquals(source.licenseExpiresAtEpochMs, decoded.licenseExpiresAtEpochMs)
    assertTrue(decoded.mayPlayOffline("NG", now))
  }

  @Test
  fun fileStorePersistsAndReloads() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val store1 = FileDownloadStore(context)
    store1.save(validRecord())

    val store2 = FileDownloadStore(context)
    assertEquals(track.id, store2.get(track.id)!!.trackId)
    assertTrue(store2.get(track.id)!!.mayPlayOffline("NG", now))

    store2.remove(track.id)
    assertNull(FileDownloadStore(context).get(track.id))
  }
}
