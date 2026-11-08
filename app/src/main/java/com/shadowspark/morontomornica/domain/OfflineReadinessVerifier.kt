package com.shadowspark.morontomornica.domain

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download

/**
 * Loop-6 readiness verifier: the single authority for "is this catalogue track genuinely
 * available offline RIGHT NOW". It answers against the REAL Media3 artifacts (the persisted
 * DownloadIndex + the shared SimpleCache bytes), NOT against the app's stored rights snapshot
 * alone. A stored `catalog_downloads.json` record only proves a download was once authorized and
 * completed; it does NOT prove the bytes are still present and downloadable/cache-recoverable after
 * process restart, cache eviction, or DownloadIndex rollback. `offlinePlaybackAllowed` / "Ready
 * offline" must therefore go through this verifier so the UI never claims offline readiness that
 * the engine cannot honour.
 *
 * The seam is injectable so unit tests can drive every failure mode (interrupted download, expired
 * rights, deleted cache, failed/legacy/queued records) deterministically without a live media
 * pipeline.
 */
@OptIn(UnstableApi::class)
fun interface OfflineReadinessVerifier {
  /**
   * Returns true only when the real Media3 DownloadIndex reports this media as STATE_COMPLETED
   * AND the shared cache holds a positive number of bytes for that media. Anything less (not in
   * the index, incomplete, evicted, corrupt) returns false -> the UI falls back to Stream /
   * Save offline and never shows "Ready offline".
   */
  fun isGenuinelyReadyForOffline(track: LicensedCatalogTrack): Boolean

  companion object {
    /** Production verifier attached by MornicaRuntime — reads the real store + cache. */
    fun real(context: Context): OfflineReadinessVerifier = Media3OfflineReadinessVerifier(context)
  }
}

/**
 * Production offline-readiness check reading the real Media3 DownloadIndex and the shared cache.
 *
 * - [CatalogDownloads.realDownloadManager] owns the persisted DownloadIndex (mediaId -> state).
 * - [MediaCache.cache] is the SAME SimpleCache playback reads through; bytes keyed by the plain
 *   media URI (no customCacheKey, so download and playback share the URI key).
 *
 * Both reads are defensive (`runCatching`): an I/O failure on the index/cache must fail-closed
 * (return false) rather than risk claiming a ready-offline state that cannot be honoured.
 */
@OptIn(UnstableApi::class)
class Media3OfflineReadinessVerifier(context: Context) : OfflineReadinessVerifier {
  private val downloadManager = CatalogDownloads.realDownloadManager(context)
  private val cache = MediaCache.cache(context)

  override fun isGenuinelyReadyForOffline(track: LicensedCatalogTrack): Boolean {
    // 1) The real DownloadIndex must show STATE_COMPLETED for this media id.
    val completedInIndex =
      runCatching { downloadManager.downloadIndex.getDownload(track.toMediaId())?.state }
        .getOrNull() == Download.STATE_COMPLETED
    if (!completedInIndex) return false
    // 2) The shared cache must actually hold bytes for the media URI playback will read.
    //    Cache key == the plain media URL (shared with download, no customCacheKey).
    val cachedBytes =
      runCatching { cache.getCachedBytes(track.mediaUrl, 0L, Long.MAX_VALUE) }.getOrNull() ?: 0L
    return cachedBytes > 0L
  }
}
