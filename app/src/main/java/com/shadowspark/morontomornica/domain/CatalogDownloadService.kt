package com.shadowspark.morontomornica.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import androidx.media3.exoplayer.scheduler.Scheduler
import java.util.concurrent.Executors

/**
 * Process-wide owner of the real Media3 (1.5.x, `exoplayer.offline`) DownloadManager, sharing the
 * SAME SimpleCache (MediaCache) that playback reads through, so a completed download is served
 * offline by the cache. Pattern matches MornicaRuntime: lazily wired with a Context.
 */
@OptIn(UnstableApi::class)
object CatalogDownloads {
  @Volatile var mediaDownloadManager: DownloadManager? = null

  @Synchronized
  fun realDownloadManager(context: Context): DownloadManager {
    mediaDownloadManager?.let { return it }
    val manager =
      DownloadManager(
        /* context = */ context,
        /* databaseProvider = */ StandaloneDatabaseProvider(context),
        /* cache = */ MediaCache.cache(context),
        /* upstreamDataSourceFactory = */ DefaultDataSource.Factory(context),
        /* executor = */ Executors.newSingleThreadExecutor(),
      )
    // The DEFAULT requirements demand an unmetered network; the emulator's network is metered,
    // so relax to ANY to let downloads auto-start (device-verified run still needs the host to
    // be online). A shipping build should tighten this (e.g. Wi-Fi-only) via config.
    manager.setRequirements(Requirements(Requirements.NETWORK))
    manager.setMaxParallelDownloads(1)
    mediaDownloadManager = manager
    return manager
  }
}

/**
 * Foreground download service for authorized licensed catalogue tracks. Enqueue is authorized
 * upstream (CatalogDownloadManager/PlaybackRouter gate) BEFORE a request is sent here; this layer
 * gives Media3 a foreground notification and survives process death via the persisted DownloadIndex
 * (resume/restart handled by Media3 on service restart).
 */
@OptIn(UnstableApi::class)
class CatalogDownloadService :
  DownloadService(
    /* foregroundNotificationId = */ 1,
    /* foregroundComponentTaskIndex = */ 0L,
  ) {

  override fun getDownloadManager(): DownloadManager = CatalogDownloads.realDownloadManager(this)

  override fun getScheduler(): Scheduler? = null

  override fun getForegroundNotification(downloads: List<Download>, iconRes: Int): Notification {
    val text =
      if (downloads.isEmpty()) "Preparing…"
      else if (downloads.all { it.state == Download.STATE_COMPLETED }) "All downloads complete"
      else "Downloading… ${overallPercent(downloads)}%"
    return baseNotification("Mornica downloads", text)
  }

  private fun overallPercent(downloads: List<Download>): Int {
    if (downloads.isEmpty()) return 0
    val total = downloads.sumOf { it.contentLength.coerceAtLeast(0) }
    if (total <= 0L) return 0
    val bytes = downloads.sumOf { it.getBytesDownloaded().coerceAtLeast(0) }
    return ((bytes * 100L) / total).toInt().coerceIn(0, 100)
  }

  private fun baseNotification(title: String, text: String): Notification {
    getSystemService(NotificationManager::class.java)?.let { nm ->
      if (nm.getNotificationChannel(CHANNEL_ID) == null) {
        nm.createNotificationChannel(
          NotificationChannel(CHANNEL_ID, "Mornica downloads", NotificationManager.IMPORTANCE_LOW),
        )
      }
    }
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.stat_sys_download)
      .setContentTitle(title)
      .setContentText(text)
      .setOngoing(true)
      .setSilent(true)
      .build()
  }

  companion object {
    private const val CHANNEL_ID = "mornica_catalog_downloads"
  }
}
