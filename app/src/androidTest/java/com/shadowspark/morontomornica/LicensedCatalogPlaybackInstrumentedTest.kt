package com.shadowspark.morontomornica

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shadowspark.morontomornica.domain.Media3LocalAudioController
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.ActiveEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * On-device Loop 2 proof: authorize a licensed catalogue demo track through the real
 * fail-closed gate, hand it to the native Media3 ExoPlayer, and wait until the router
 * reports that the player actually reached a live playing state against the network
 * audio source. ExoPlayer is created/released on the main (application) thread as
 * Media3 requires; playback-state confirmation flows through the controller listener
 * back into the router (isLocalPlaying is not set optimistically).
 */
@RunWith(AndroidJUnit4::class)
class LicensedCatalogPlaybackInstrumentedTest {

  @Test
  fun authorizedCatalogTrackReachesPlayingStateOnRealNetworkAudio() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    var router: PlaybackRouter? = null
    var player: ExoPlayer? = null
    var authorized = false
    var engineIsCatalog = false

    // Media3 requires the ExoPlayer to be created and released on the main thread.
    instrumentation.runOnMainSync {
      val p =
        ExoPlayer.Builder(context)
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(C.USAGE_MEDIA)
              .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
              .build(),
            /* handleAudioFocus = */ true,
          )
          .setHandleAudioBecomingNoisy(true)
          .build()
      player = p
      val controller = Media3LocalAudioController(p)
      val r = PlaybackRouter(localAudioController = controller, territoryProvider = { "NG" })
      router = r
      val track = r.state.value.catalogTracks.first { it.offlineAllowed }
      authorized = r.playCatalogTrack(track)
      engineIsCatalog = r.state.value.activeEngine == ActiveEngine.LICENSED_CATALOG
    }

    assertTrue("the catalog gate must authorize the demo stream", authorized)
    assertTrue("router must switch to catalog engine", engineIsCatalog)

    // Wait on the test thread (main is free so the player processes + posts the
    // isPlaying callback into the router). isLocalPlaying only flips true when the
    // controller confirms the player actually started against the live source.
    val deadline = System.currentTimeMillis() + 120_000L
    var playing = false
    while (System.currentTimeMillis() < deadline) {
      if (router?.state?.value?.isLocalPlaying == true) {
        playing = true
        break
      }
      Thread.sleep(500L)
    }
    assertTrue(
      "router never reported live playback of the network audio source within 120s",
      playing,
    )
    assertTrue(
      "catalog track must remain the active engine during playback",
      router!!.state.value.activeEngine == ActiveEngine.LICENSED_CATALOG,
    )

    instrumentation.runOnMainSync { player?.release() }
  }
}
