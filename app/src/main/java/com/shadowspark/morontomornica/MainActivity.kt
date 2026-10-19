package com.shadowspark.morontomornica

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.shadowspark.morontomornica.domain.LocalMediaRepository
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.domain.YouTubeDiscoveryRepository
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.ui.MornicaApp

class MainActivity : ComponentActivity() {
  private lateinit var localRepo: LocalMediaRepository
  private lateinit var router: PlaybackRouter
  private var mediaPlayer: MediaPlayer? = null

  private val noisyAudioReceiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
          router.onHeadsetDisconnected()
        }
      }
    }

  private val audioPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      refreshDeviceAudioLibrary(granted)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    localRepo = LocalMediaRepository(packageName)
    val ytRepo = YouTubeDiscoveryRepository(apiKey = BuildConfig.YOUTUBE_API_KEY)
    router =
      PlaybackRouter(
        youtubeRepository = ytRepo,
        localMediaRepository = localRepo,
        onLocalAudioCommand = ::handleHardwareLocalAudio,
      )

    registerReceiver(
      noisyAudioReceiver,
      IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
    )

    val initialGranted = hasAudioReadPermission()
    if (initialGranted) {
      refreshDeviceAudioLibrary(true)
    }

    setContent {
      MornicaApp(
        router = router,
        localRepo = localRepo,
        onRequestAudioPermission = ::requestAudioReadPermission,
      )
    }
  }

  override fun onStop() {
    super.onStop()
    // Enforce YouTube Visible Player Policy: pause any embedded YouTube playback when backgrounded
    router.onAppBackgrounded()
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      unregisterReceiver(noisyAudioReceiver)
    } catch (_: Exception) {}
    releaseMediaPlayer()
  }

  private fun hasAudioReadPermission(): Boolean {
    val perm =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
      } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
      }
    return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
  }

  private fun requestAudioReadPermission() {
    val perm =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
      } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
      }
    audioPermissionLauncher.launch(perm)
  }

  private fun refreshDeviceAudioLibrary(granted: Boolean) {
    val tracks =
      if (granted) {
        localRepo.queryDeviceMediaStore(contentResolver)
      } else {
        localRepo.defaultDemoAndCatalogTracks()
      }
    router.updateDeviceAudioTracks(hasPermission = granted, tracks = tracks)
  }

  private fun handleHardwareLocalAudio(track: LocalMediaTrack?, shouldPlay: Boolean) {
    if (!shouldPlay || track == null) {
      try {
        if (mediaPlayer?.isPlaying == true) {
          mediaPlayer?.pause()
        }
      } catch (_: Exception) {}
      return
    }

    try {
      releaseMediaPlayer()
      val mp =
        if (track.uri.startsWith("android.resource://")) {
          MediaPlayer.create(this, R.raw.mornica_afrobeats_demo)
        } else {
          MediaPlayer().apply {
            setDataSource(this@MainActivity, Uri.parse(track.uri))
            prepare()
          }
        }
      mediaPlayer = mp
      mp?.start()
    } catch (_: Exception) {
      // Defensive catch if audio hardware unavailable in headless environment
    }
  }

  private fun releaseMediaPlayer() {
    try {
      mediaPlayer?.stop()
    } catch (_: Exception) {}
    try {
      mediaPlayer?.release()
    } catch (_: Exception) {}
    mediaPlayer = null
  }
}
