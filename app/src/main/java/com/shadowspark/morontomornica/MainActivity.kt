package com.shadowspark.morontomornica

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.shadowspark.morontomornica.domain.LocalAudioPlaybackService
import com.shadowspark.morontomornica.domain.LocalMediaRepository
import com.shadowspark.morontomornica.domain.MornicaRuntime
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.ui.MornicaApp

class MainActivity : ComponentActivity() {
  private val localRepo: LocalMediaRepository = MornicaRuntime.localRepo
  private val router: PlaybackRouter = MornicaRuntime.router

  private val audioPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      refreshDeviceAudioLibrary(granted)
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appContext = applicationContext
    MornicaRuntime.serviceStarter = {
      try {
        appContext.startService(Intent(appContext, LocalAudioPlaybackService::class.java))
      } catch (_: Exception) {
        // Defensive catch for restricted test environments
      }
    }
    ensurePlaybackServiceStarted()

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

  override fun onStart() {
    super.onStart()
    ensurePlaybackServiceStarted()
  }

  private fun ensurePlaybackServiceStarted() {
    try {
      startService(Intent(this, LocalAudioPlaybackService::class.java))
    } catch (_: Exception) {
      // Defensive catch for restricted test environments
    }
  }

  override fun onStop() {
    super.onStop()
    // Enforce YouTube Visible Player Policy: pause any embedded YouTube playback when backgrounded.
    // Local audio continues playing inside LocalAudioPlaybackService.
    router.onAppBackgrounded()
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
}
