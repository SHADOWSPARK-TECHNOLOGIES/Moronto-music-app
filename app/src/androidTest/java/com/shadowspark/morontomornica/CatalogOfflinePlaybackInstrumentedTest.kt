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
import com.shadowspark.morontomornica.domain.MediaCache
import com.shadowspark.morontomornica.domain.toMediaId
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Loop 3 OFFLINE-READINESS proof (self-contained, deterministic):
 * after the authorized track downloads to COMPLETED through the real CatalogDownloadService,
 * the bytes are durably present in the on-device Media3 cache keyed by the track URI — i.e. the
 * app holds offline-ready audio. This documents that the download lands in the cache playback
 * reads from.
 *
 * HONEST SCOPE: this does NOT yet prove rendering playback with the radio truly off. Airplane-mode
 * toggling from an app is blocked by Android's protected AIRPLANE_MODE broadcast, and the
 * dead-upstream CacheDataSource probe is a pending investigation. Live Offline PLAYBACK device
 * verification is tracked in docs/agent-loop/STATE.md (pended), not claimed here.
 */
@RunWith(AndroidJUnit4::class)
class CatalogOfflinePlaybackInstrumentedTest {

  @Test
  fun downloadedTrackBytesAreActuallyInTheOnDeviceCache() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val real = CatalogDownloads.realDownloadManager(context)
    val track = DemoCatalogSource().tracks().first { it.offlineAllowed }
    val streamOnly = DemoCatalogSource().tracks().first { !it.offlineAllowed }

    val gate =
      CatalogDownloadManager(
        rights = LicensedCatalogConfig.demoRights(),
        territoryProvider = { "NG" },
        enqueueRequest = { url, id ->
          val request = DownloadRequest.Builder(id, Uri.parse(url)).build()
          val intent =
            DownloadService.buildAddDownloadIntent(
              context, CatalogDownloadService::class.java, request, /* serialize = */ false,
            )
          ContextCompat.startForegroundService(context, intent)
        },
      )
    assertTrue("gate must grant SAVE_OFFLINE", gate.enqueue(track).allowed)

    val deadline = System.currentTimeMillis() + 240_000L
    var state = real.downloadIndex.getDownload(track.toMediaId())
    while (System.currentTimeMillis() < deadline) {
      state = real.downloadIndex.getDownload(track.toMediaId())
      if (state?.state == Download.STATE_COMPLETED) break
      if (state?.state == Download.STATE_FAILED) throw AssertionError("download failed: ${state.failureReason}")
      Thread.sleep(1_000L)
    }
    assertTrue("authorized track did not reach COMPLETED", state?.state == Download.STATE_COMPLETED)

    // Offline-ready bytes must be durably in the playback cache under the track URI.
    val cacheKeys = MediaCache.cache(context).getKeys()
    assertTrue(
      "expected the downloaded track keyed by URI in the cache, got keys=$cacheKeys",
      cacheKeys.contains(track.mediaUrl),
    )

    // And the stream-only track is refused + not part of the download.
    assertTrue(!gate.enqueue(streamOnly).allowed)
    assertTrue(
      "stream-only track must not be in the download index",
      real.downloadIndex.getDownload(streamOnly.toMediaId()) == null,
    )
  }
}
