package com.shadowspark.morontomornica

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shadowspark.morontomornica.domain.CatalogDownloadManager
import com.shadowspark.morontomornica.domain.CatalogDownloadService
import com.shadowspark.morontomornica.domain.CatalogDownloads
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.LicensedCatalogConfig
import com.shadowspark.morontomornica.domain.toMediaId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Loop 3 device proof, PART 1 (run with network ON):
 * - the authorized offline-allowed demo track downloads to COMPLETED, driven through the
 *   real CatalogDownloadService + Media3 DownloadManager (production path), after the
 *   fail-closed gate grants SAVE_OFFLINE;
 * - the stream-only track is REFUSED by the gate and never appears in the download index.
 * PART 2 (CatalogOfflinePlaybackInstrumentedTest) runs after the host toggles airplane mode.
 */
@RunWith(AndroidJUnit4::class)
class CatalogDownloadInstrumentedTest {

  @Test
  fun authorizedTrackDownloadsAndStreamOnlyIsNeverEnqueued() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val real = CatalogDownloads.realDownloadManager(context)
    val gate =
      CatalogDownloadManager(
        rights = LicensedCatalogConfig.demoRights(),
        territoryProvider = { "NG" },
        enqueueRequest = { url, id ->
          val request = DownloadRequest.Builder(id, Uri.parse(url)).build()
          val intent =
            DownloadService.buildAddDownloadIntent(
              context,
              CatalogDownloadService::class.java,
              request,
              /* serialize = */ false,
            )
          ContextCompat.startForegroundService(context, intent)
        },
        indexHasDownload = { id ->
          real.downloadIndex.getDownload(id)?.let { it.state == Download.STATE_COMPLETED } ?: false
        },
      )
    val tracks = DemoCatalogSource().tracks()
    val offlineAllowed = tracks.first { it.offlineAllowed }

    val decision = gate.enqueue(offlineAllowed)
    assertTrue("gate must grant SAVE_OFFLINE for the demo track", decision.allowed)

    val downloadId = offlineAllowed.toMediaId()
    val deadline = System.currentTimeMillis() + 240_000L
    var last = real.downloadIndex.getDownload(downloadId)
    while (System.currentTimeMillis() < deadline) {
      last = real.downloadIndex.getDownload(downloadId)
      when (last?.state) {
        Download.STATE_COMPLETED -> break
        Download.STATE_FAILED ->
          throw AssertionError("download failed: ${last.failureReason} stop=${last.stopReason}")
        else -> Thread.sleep(1_000L)
      }
    }
    assertEquals(
      "authorized demo track must reach COMPLETED on-device (state=${last?.state})",
      Download.STATE_COMPLETED,
      last?.state,
    )
    assertTrue(gate.isDownloadedOrQueued(offlineAllowed))

    // Stream-only must be refused and never present in the index.
    val streamOnly = tracks.first { !it.offlineAllowed }
    val refused = gate.enqueue(streamOnly)
    assertFalse("stream-only track must be refused", refused.allowed)
    assertNull(
      "stream-only track must never appear in the download index",
      real.downloadIndex.getDownload(streamOnly.toMediaId()),
    )
  }
}
