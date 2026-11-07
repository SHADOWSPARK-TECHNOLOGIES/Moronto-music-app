package com.shadowspark.morontomornica.domain

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadRequest
import java.io.File

/**
 * Shared on-device media cache. Both download (DownloadManager) and playback (CacheDataSource)
 * read/write the SAME SimpleCache by URL, so a fully-downloaded licensed track becomes
 * playable offline without a network connection. Singleton by process so all services agree.
 */
@OptIn(UnstableApi::class)
object MediaCache {
  const val MAX_BYTES: Long = 512L * 1024L * 1024L // 512 MiB demo cap

  @Volatile private var simpleCache: SimpleCache? = null
  @Volatile private var playbackFactory: DataSource.Factory? = null

  @Synchronized
  fun cache(context: Context): SimpleCache {
    simpleCache?.let { return it }
    val dir = File(context.cacheDir, "mornica-media3-cache")
    val created =
      SimpleCache(
        dir,
        LeastRecentlyUsedCacheEvictor(MAX_BYTES),
        StandaloneDatabaseProvider(context),
      )
    simpleCache = created
    return created
  }

  /** DataSource.Factory for EXOPLAYER PLAYBACK that reads/writes the shared cache. */
  @Synchronized
  fun playbackDataSourceFactory(context: Context): DataSource.Factory {
    playbackFactory?.let { return it }
    val built =
      CacheDataSource.Factory()
        .setCache(cache(context))
        .setUpstreamDataSourceFactory(DefaultDataSource.Factory(context))
    playbackFactory = built
    return built
  }
}

/** mediaId convention for catalogue items; shared with PlaybackRouter. */
@OptIn(UnstableApi::class)
fun LicensedCatalogTrack.toMediaId(): String = "${PlaybackRouter.CATALOG_MEDIA_ID_PREFIX}$id"

/** Builds the Media3 download request a track must satisfy before enqueue.
 *  NOTE: no customCacheKey — playback's CacheDataSource keys the cache by the URI, so
 *  download and playback MUST share that key for offline reads to be found. */
@OptIn(UnstableApi::class)
fun LicensedCatalogTrack.toDownloadRequest(): DownloadRequest =
  DownloadRequest.Builder(toMediaId(), Uri.parse(mediaUrl))
    // NO customCacheKey so download and playback key the cache by the same URI.
    .build()

/**
 * Loop 3: authorization in FRONT of Media3's DownloadManager. A download is enqueued only when
 * `LicensedCatalogRights.evaluate(SAVE_OFFLINE)` returns ALLOW; a DENIAL is surfaced and never
 * translated into a network download. The rights gate remains the single authority and the
 * host allowlist (incl. the hard YouTube block) is already applied by the gate. The actual
 * enqueue is injected so this is unit-testable without a running DownloadManager.
 */
@OptIn(UnstableApi::class)
class CatalogDownloadManager(
  private val rights: LicensedCatalogRights,
  private val territoryProvider: () -> String,
  private val enqueueRequest: (mediaUrl: String, mediaId: String) -> Unit,
  private val indexHasDownload: (mediaId: String) -> Boolean = { false },
) {
  fun canDownload(track: LicensedCatalogTrack): CatalogAccessDecision =
    rights.evaluate(track, CatalogAccess.SAVE_OFFLINE, territoryProvider())

  /** Returns ALLOW and forwards the URL to the downloader; otherwise returns the denial and does NOT enqueue. */
  fun enqueue(track: LicensedCatalogTrack): CatalogAccessDecision {
    val decision = canDownload(track)
    if (decision.allowed) {
      enqueueRequest(track.mediaUrl, track.toMediaId())
    }
    return decision
  }

  /** Whether Media3 already tracks this item as queued/downloading/completed (not just REMOVED/FAILED). */
  fun isDownloadedOrQueued(track: LicensedCatalogTrack): Boolean = indexHasDownload(track.toMediaId())
}

/** Download lifecycle surfaced to the UI (subset of the Media3 Download state machine). */
enum class CatalogDownloadState {
  NONE,
  QUEUED,
  DOWNLOADING,
  COMPLETED,
  FAILED,
}
