package com.shadowspark.morontomornica

import androidx.media3.common.MediaItem
import com.shadowspark.morontomornica.domain.CatalogDownloadState
import com.shadowspark.morontomornica.domain.DemoCatalogSource
import com.shadowspark.morontomornica.domain.DownloadRecord
import com.shadowspark.morontomornica.domain.InMemoryDownloadStore
import com.shadowspark.morontomornica.domain.LicensedCatalogTrack
import com.shadowspark.morontomornica.domain.LocalAudioController
import com.shadowspark.morontomornica.domain.OfflineReadinessVerifier
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.domain.toMedia3Item
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Loop-6 / P0 acceptance: "Ready offline" must be driven by the REAL Media3 DownloadIndex + cache
 * bytes (via OfflineReadinessVerifier), never merely by a stored rights snapshot or a COMPLETED
 * in-memory flag. Each test uses a fake verifier to exercise a failure mode deterministically and
 * asserts the UI authority (offlinePlaybackAllowed) and the offline-play action both fail closed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineReadinessVerifierTest {

  private class FakeVerifier(private val ready: Boolean = false) : OfflineReadinessVerifier {
    override fun isGenuinelyReadyForOffline(track: LicensedCatalogTrack): Boolean = ready
  }

  private val offlineTrack: LicensedCatalogTrack by lazy {
    DemoCatalogSource().tracks().first { it.offlineAllowed }
  }

  private fun readyStore(): InMemoryDownloadStore =
    InMemoryDownloadStore().apply {
      save(DownloadRecord.fromTrack(offlineTrack, "NG", System.currentTimeMillis()))
    }

  /** Records whether the actual player layer was handed any MediaItems (Gate-2 network-fallback spy). */
  private class RecordingAudioController : LocalAudioController {
    var playMediaItemsCalls = 0
    var lastItems: List<MediaItem> = emptyList()
    override fun bindRouter(router: PlaybackRouter) {}
    override fun playTrack(track: LocalMediaTrack, queue: List<LocalMediaTrack>, repeatMode: RepeatMode, shuffleEnabled: Boolean) {}
    override fun playMediaItems(items: List<MediaItem>, startIndex: Int) {
      playMediaItemsCalls++
      lastItems = items
    }
    override fun setPlayWhenReady(playWhenReady: Boolean) {}
    override fun seekTo(positionMs: Long) {}
    override fun setShuffleEnabled(shuffleEnabled: Boolean) {}
    override fun setRepeatMode(repeatMode: RepeatMode) {}
    override fun stop() {}
  }

  // --- Gate 1: real index+cache is the authority, not the stored snapshot ---

  @Test
  fun completedStateAndSnapshotButIndexNotReady_blocksOfflinePlayback() {
    val store = readyStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    // Simulate: a legacy record + a COMPLETED in-memory state exists, but the real DownloadIndex
    // (verifier) says the bytes are NOT recoverable (interrupted/evicted/legacy queued record).
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)
    assertTrue("sanity: without a verifier the snapshot governs", router.offlinePlaybackAllowed(offlineTrack))

    router.attachOfflineReadiness(FakeVerifier(ready = false))
    assertFalse(
      "a COMPLETED state + stored snapshot must NOT win when the real engine has no bytes",
      router.offlinePlaybackAllowed(offlineTrack),
    )
  }

  @Test
  fun attachOfflineReadiness_dropsStaleCompletedStates() {
    val store = readyStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)
    assertTrue(router.offlinePlaybackAllowed(offlineTrack))

    // Attach a verifier that finds the media no longer genuinely present (cache evicted /
    // interrupted download / deleted cache). The exposed COMPLETED state must be dropped.
    router.attachOfflineReadiness(FakeVerifier(ready = false))
    assertEquals(
      "stale COMPLETED state must be removed from the UI map when the engine cannot honour it",
      CatalogDownloadState.NONE,
      router.state.value.catalogDownloadStates[offlineTrack.id] ?: CatalogDownloadState.NONE,
    )
  }

  @Test
  fun verifierConfirmingReady_allowsOfflinePlayback() {
    val store = readyStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)
    router.attachOfflineReadiness(FakeVerifier(ready = true))
    assertTrue("genuinely ready media must allow offline playback", router.offlinePlaybackAllowed(offlineTrack))
  }

  // --- Gate 1: expiry is still enforced even when verifier has bytes ---

  @Test
  fun expiredRights_blockOfflineEvenIfBytesPresent() {
    val pastTime = System.currentTimeMillis() - 100_000L
    val store = InMemoryDownloadStore().apply {
      save(DownloadRecord.fromTrack(offlineTrack.copy(licenseExpiresAtEpochMs = pastTime), "NG", pastTime))
    }
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    router.attachOfflineReadiness(FakeVerifier(ready = true))
    assertFalse(
      "expired rights must block offline playback even when bytes exist",
      router.offlinePlaybackAllowed(offlineTrack),
    )
  }

  // --- Gate 1: a legacy stored record without real completion never rehydrates as ready ---

  @Test
  fun legacyStoredRecordWithoutRealIndexDoesNotRehydrateCompleted() {
    val store = readyStore()
    val router = PlaybackRouter().apply { attachOfflineReadiness(FakeVerifier(ready = false)) }
    // Restart simulation: attach store AFTER verifier; bytes absent so the record must stay not-ready.
    router.attachDownloadStore(store)
    assertFalse(
      "a stored snapshot from a previous session must not rehydrate 'ready' if the engine has no bytes",
      router.offlinePlaybackAllowed(offlineTrack),
    )
    assertEquals(
      CatalogDownloadState.NONE,
      router.state.value.catalogDownloadStates[offlineTrack.id] ?: CatalogDownloadState.NONE,
    )
  }

  // --- Gate 2: COMPLETED state alone cannot override a failed offline entitlement on Play ---

  @Test
  fun playDownloadedCatalogTrack_failsClosed_whenNotGenuinelyReady() {
    // State says COMPLETED + snapshot exists, but verifier (real index) says no bytes.
    val store = readyStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)
    router.attachOfflineReadiness(FakeVerifier(ready = false))

    assertFalse("Play offline must refuse when the engine has no bytes", router.playDownloadedCatalogTrack(offlineTrack))
    assertTrue(
      router.state.value.statusBannerMessage.orEmpty().contains("Not ready offline", ignoreCase = true),
    )
    assertEquals(
      "no active catalog track may be set when offline play is refused",
      null,
      router.state.value.activeCatalogTrack,
    )
  }

  @Test
  fun playDownloadedCatalogTrack_allowed_whenGenuinelyReady() {
    val store = readyStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)
    router.attachOfflineReadiness(FakeVerifier(ready = true))

    assertTrue("Play offline must succeed when bytes are genuinely present", router.playDownloadedCatalogTrack(offlineTrack))
    assertEquals(offlineTrack.id, router.state.value.activeCatalogTrack?.id)
    assertTrue(router.state.value.statusBannerMessage.orEmpty().contains("Playing offline", ignoreCase = true))
  }

  @Test
  fun playDownloadedCatalogTrack_neverHandsPlayerAnIncompleteItem() {
    // Gate 2: the player layer must NEVER receive an incomplete/not-genuinely-downloaded item,
    // otherwise Media3's cache+upstream source could silently fall back to network streaming.
    val controller = RecordingAudioController()
    val store = readyStore()
    val router = PlaybackRouter(localAudioController = controller).apply {
      attachDownloadStore(store)
      attachOfflineReadiness(FakeVerifier(ready = false))
    }
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)

    // Even though state+snapshot say COMPLETED, the real engine lacks the bytes -> offline play
    // must be refused BEFORE the controller/player is touched.
    assertFalse(router.playDownloadedCatalogTrack(offlineTrack))
    assertEquals(
      "player must not be handed an incomplete offline item (no network-stream fallback)",
      0,
      controller.playMediaItemsCalls,
    )
    assertEquals("player must not receive any media item", emptyList<MediaItem>(), controller.lastItems)
  }

  @Test
  fun playDownloadedCatalogTrack_handsPlayerOnlyGenuinelyReadyItem() {
    val controller = RecordingAudioController()
    val store = readyStore()
    val router = PlaybackRouter(localAudioController = controller).apply {
      attachDownloadStore(store)
      attachOfflineReadiness(FakeVerifier(ready = true))
    }
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.COMPLETED)

    assertTrue(router.playDownloadedCatalogTrack(offlineTrack))
    assertEquals(
      "player must be handed exactly the genuinely-ready offline item",
      1,
      controller.playMediaItemsCalls,
    )
    assertEquals(listOf(offlineTrack.toMedia3Item()), controller.lastItems)
  }

  // --- Gate 1: failed downloads never leave bytes/ready behind ---

  @Test
  fun failedDownloadWithStoredSnapshot_doesNotAllowOffline() {
    val store = readyStore()
    val router = PlaybackRouter().apply { attachDownloadStore(store) }
    // A record may linger in the store; a FAILED live state + no genuine bytes must not enable offline.
    router.attachOfflineReadiness(FakeVerifier(ready = false))
    router.onDownloadStateChanged(offlineTrack.id, CatalogDownloadState.FAILED)
    assertFalse("a failed download must never enable offline playback", router.offlinePlaybackAllowed(offlineTrack))
  }
}
