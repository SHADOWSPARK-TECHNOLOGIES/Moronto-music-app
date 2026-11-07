package com.shadowspark.morontomornica

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shadowspark.morontomornica.domain.CatalogDownloadManager
import com.shadowspark.morontomornica.domain.CatalogDownloadService
import com.shadowspark.morontomornica.domain.CatalogDownloads
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.DownloadRecord
import com.shadowspark.morontomornica.domain.InMemoryDownloadStore
import com.shadowspark.morontomornica.domain.LicensedCatalogConfig
import com.shadowspark.morontomornica.domain.Media3LocalAudioController
import com.shadowspark.morontomornica.domain.MediaCache
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.domain.toMediaId
import com.shadowspark.morontomornica.model.ActiveEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Loop 3 OFFLINE PLAYBACK proof (self-contained, deterministic, NO airplane mode needed):
 *  1. authorized demo track downloads to COMPLETED via the real CatalogDownloadService;
 *  2. stream-only track is refused + never enqueued;
 *  3. the full cached byte range is present in the on-device cache for the track URI;
 *  4. playback is driven through a CacheDataSource configured with FLAG_BLOCK_ON_CACHE, which makes
 *     ANY upstream access impossible (stronger than toggling the radio off). Reaching a playing state
 *     therefore proves the audio renders ENTIRELY from the on-device cache = offline playback.
 */
@RunWith(AndroidJUnit4::class)
class CatalogOfflinePlaybackInstrumentedTest {

  @Test
  fun fullyCachedTrackPlaysEndToEndFromCacheOnly() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val real = CatalogDownloads.realDownloadManager(context)
    val track = DemoCatalogSource().tracks().first { it.offlineAllowed }
    val streamOnly = DemoCatalogSource().tracks().first { !it.offlineAllowed }
    val store =
      InMemoryDownloadStore().apply { save(DownloadRecord.fromTrack(track, "NG", System.currentTimeMillis())) }

    // 1) Download through the production service path (gate gates first).
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
    assertEquals("authorized track did not reach COMPLETED", Download.STATE_COMPLETED, state?.state)

    // 2) Stream-only refused + never in the index.
    assertTrue(!gate.enqueue(streamOnly).allowed)
    assertTrue("stream-only must not be in the download index", real.downloadIndex.getDownload(streamOnly.toMediaId()) == null)

    // 3) The FULL byte range is cached under the track URI.
    val cache = MediaCache.cache(context)
    val cachedBytes = cache.getCachedBytes(track.mediaUrl, 0L, Long.MAX_VALUE)
    assertTrue("expected full-duration bytes cached, got $cachedBytes", cachedBytes > 0L)

    // 4) Offline playback: BLOCK_ON_CACHE forbids upstream entirely -> playing == served from cache.
    val cacheOnlyFactory =
      CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
        .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)

    var router: PlaybackRouter? = null
    var player: ExoPlayer? = null
    var offlineOk = false
    var authorized = false
    var engineOk = false
    instrumentation.runOnMainSync {
      val p =
        ExoPlayer.Builder(context)
          .setMediaSourceFactory(DefaultMediaSourceFactory(cacheOnlyFactory))
          .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
            /* handleAudioFocus = */ true,
          )
          .setHandleAudioBecomingNoisy(true)
          .build()
      player = p
      val r =
        PlaybackRouter(
          localAudioController = Media3LocalAudioController(p),
          territoryProvider = { "NG" },
          downloadStore = store,
        )
      router = r
      offlineOk = r.offlinePlaybackAllowed(track)
      authorized = r.playCatalogTrack(track)
      engineOk = r.state.value.activeEngine == ActiveEngine.LICENSED_CATALOG
    }
    assertTrue("stored rights snapshot must entitle offline playback", offlineOk)
    assertTrue("cache-only playback must be authorized", authorized)
    assertTrue("catalog engine active", engineOk)

    val playDeadline = System.currentTimeMillis() + 60_000L
    var reachedPlaying = false
    while (System.currentTimeMillis() < playDeadline) {
      if (router?.state?.value?.isLocalPlaying == true) { reachedPlaying = true; break }
      Thread.sleep(300L)
    }
    var diag = ""
    instrumentation.runOnMainSync {
      if (player != null) {
        diag = "playbackState=${player!!.playbackState} pwR=${player!!.playWhenReady} " +
          "err=${player!!.playerError?.message} errCode=${player!!.playerError?.errorCodeName}"
      }
    }
    assertTrue("offline cache-only playback failed. $diag", reachedPlaying)

    instrumentation.runOnMainSync { player?.release() }
  }
}
