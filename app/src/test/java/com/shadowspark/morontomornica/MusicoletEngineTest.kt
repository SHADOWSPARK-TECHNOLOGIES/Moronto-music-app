package com.shadowspark.morontomornica

import com.shadowspark.morontomornica.domain.MusicoletEngine
import com.shadowspark.morontomornica.domain.TrackTagPatch
import com.shadowspark.morontomornica.model.CrossfadeSettings
import com.shadowspark.morontomornica.model.DEFAULT_QUEUES
import com.shadowspark.morontomornica.model.EQ_BAND_LABELS
import com.shadowspark.morontomornica.model.EQ_PRESETS
import com.shadowspark.morontomornica.model.EqualizerSettings
import com.shadowspark.morontomornica.model.MAX_QUEUES
import com.shadowspark.morontomornica.model.PlaybackQueue
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.SortOption
import com.shadowspark.morontomornica.model.bundledOfflineTracks
import com.shadowspark.morontomornica.model.parseLrcLyrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicoletEngineTest {
  @Test
  fun switchQueueWithResume_savesOutgoingPositionAndRestoresTarget() {
    val result = MusicoletEngine.switchQueueWithResume(
      queues = DEFAULT_QUEUES,
      activeQueueId = "queue-1",
      targetQueueId = "queue-2",
      currentPositionMs = 14_500L,
    )
    assertEquals("queue-2", result.activeQueue.id)
    assertEquals("track-lekki-logdrum", result.activeQueue.currentTrackId)
    val q1 = result.queues.find { it.id == "queue-1" }
    assertEquals(14_500L, q1?.positionMs)
  }

  @Test
  fun createNewQueue_enforcesMaxQueuesAndNonEmptyName() {
    val empty = MusicoletEngine.createNewQueue(DEFAULT_QUEUES, "   ")
    assertNull(empty.created)

    var current = DEFAULT_QUEUES
    while (current.size < MAX_QUEUES) {
      val res = MusicoletEngine.createNewQueue(current, "Extra ${current.size}")
      assertNotNull(res.created)
      current = res.queues
    }
    val overflow = MusicoletEngine.createNewQueue(current, "Too Many")
    assertNull(overflow.created)
    assertTrue(overflow.error?.contains("20") == true)
  }

  @Test
  fun deleteQueueById_refusesDeletingLastQueue() {
    val res = MusicoletEngine.deleteQueueById(listOf(DEFAULT_QUEUES.first()), "queue-1", "queue-1")
    assertFalse(res.deleted)
    assertEquals(1, res.queues.size)
  }

  @Test
  fun shuffle_visitsAllTracksInEvenLengthQueues() {
    for (len in listOf(2, 4, 6)) {
      val ids = (0 until len).map { "t-$it" }
      val visited = mutableSetOf(ids.first())
      var current = ids.first()
      repeat(len - 1) {
        val next = MusicoletEngine.computeNextQueueTrack(
          queue = PlaybackQueue("q", "Q", ids, current, 0L),
          repeatMode = RepeatMode.ALL,
          shuffleEnabled = true,
          isAutoAdvance = false,
        )
        assertNotNull(next.nextTrackId)
        current = next.nextTrackId!!
        visited += current
      }
      assertEquals(len, visited.size)
    }
  }

  @Test
  fun filterSortAndTags_updateMetadata() {
    val tracks = bundledOfflineTracks()
    val filtered = MusicoletEngine.filterAndSortTracks(tracks, "lekki", SortOption.TITLE)
    assertEquals(1, filtered.size)
    assertEquals("track-lekki-logdrum", filtered.first().id)

    val tagged = MusicoletEngine.applyTrackTags(
      tracks,
      "track-eko-neon",
      TrackTagPatch("Eko Pulse (Remastered)", "Teniola Blaze", "Victoria Island EP", "Lagos WAVs", 2026),
    )
    val updated = tagged.first { it.id == "track-eko-neon" }
    assertEquals("Eko Pulse (Remastered)", updated.title)
    assertEquals("Victoria Island EP", updated.album)
  }

  @Test
  fun parseLrcLyrics_parsesTimestamps() {
    val lines = parseLrcLyrics("[00:05.50] Midnight rhythm\n[00:12.00] Zero data needed")
    assertEquals(2, lines.size)
    assertEquals(5_500L, lines[0].timeMs)
    assertEquals("Midnight rhythm", lines[0].text)
    assertEquals(12_000L, lines[1].timeMs)
  }

  @Test
  fun clampEqualizerAndCrossfade_bounds10BandsAnd0To12Seconds() {
    assertEquals(10, EQ_BAND_LABELS.size)
    assertEquals(
      listOf("Afrobeats Bass", "Amapiano Log Drum", "Gospel", "Highlife", "Flat"),
      EQ_PRESETS.map { it.name },
    )

    val clampedEq = MusicoletEngine.clampEqualizerSettings(
      EqualizerSettings(
        enabled = true,
        presetId = "custom",
        bandsDb = listOf(-30, 20, 0, 15, -14, 9, -20, 5, 13, -1),
        bassBoostPercent = 140,
        preampDb = -25,
        virtualizerPercent = 200,
      ),
    )
    assertEquals(listOf(-12, 12, 0, 12, -12, 9, -12, 5, 12, -1), clampedEq.bandsDb)
    assertEquals(100, clampedEq.bassBoostPercent)
    assertEquals(-12, clampedEq.preampDb)
    assertEquals(100, clampedEq.virtualizerPercent)

    val clampedMax = MusicoletEngine.clampCrossfadeSettings(
      CrossfadeSettings(enabled = true, durationSec = 25, fadeOnManualSkip = false, gaplessEnabled = true),
    )
    assertEquals(12, clampedMax.durationSec)

    val clampedZero = MusicoletEngine.clampCrossfadeSettings(
      CrossfadeSettings(enabled = true, durationSec = -4, fadeOnManualSkip = true, gaplessEnabled = true),
    )
    assertEquals(0, clampedZero.durationSec)
  }

  @Test
  fun computeCrossfadeTriggerMs_capsAtFortyPercentAndRespectsZeroSeconds() {
    val cf = CrossfadeSettings(enabled = true, durationSec = 8, fadeOnManualSkip = true, gaplessEnabled = true)
    assertEquals(8_000L, MusicoletEngine.computeCrossfadeTriggerMs(30_000L, cf))
    assertEquals(4_000L, MusicoletEngine.computeCrossfadeTriggerMs(10_000L, cf))
    assertEquals(0L, MusicoletEngine.computeCrossfadeTriggerMs(2_500L, cf))
    assertEquals(0L, MusicoletEngine.computeCrossfadeTriggerMs(30_000L, cf.copy(durationSec = 0)))
    assertEquals(0L, MusicoletEngine.computeCrossfadeTriggerMs(30_000L, cf.copy(enabled = false)))
  }

  @Test
  fun eqPresetFullAccess_andFifthSessionRating_andSmartPlaylistsWork() {
    val now = 1_000_000L
    assertTrue(MusicoletEngine.isEqPresetAccessible("flat", isPro = false, rewardUnlockExpiresAtMs = null, nowMs = now))
    assertTrue(MusicoletEngine.isEqPresetAccessible("afrobeats-bass", isPro = false, rewardUnlockExpiresAtMs = null, nowMs = now))
    assertTrue(MusicoletEngine.isEqPresetAccessible("amapiano-log-drum", isPro = false, rewardUnlockExpiresAtMs = null, nowMs = now))
    assertTrue(MusicoletEngine.isEqPresetAccessible("highlife", isPro = true, rewardUnlockExpiresAtMs = null, nowMs = now))

    assertFalse(MusicoletEngine.shouldShowRatingPrompt(4, hasDismissedOrRated = false))
    assertTrue(MusicoletEngine.shouldShowRatingPrompt(5, hasDismissedOrRated = false))
    assertFalse(MusicoletEngine.shouldShowRatingPrompt(5, hasDismissedOrRated = true))

    val tracks = bundledOfflineTracks()
    val smart = MusicoletEngine.buildGenreSmartPlaylists(tracks)
    assertEquals(listOf("Afrobeats", "Amapiano", "Gospel", "Highlife"), smart.map { it.genre })
    assertTrue(smart.all { it.tracks.isNotEmpty() })

    val stats = MusicoletEngine.estimateStorageStats(tracks)
    assertEquals(tracks.size, stats.totalTracks)
    assertTrue(stats.totalEstimatedBytes > 0L)
  }

  @Test
  fun validateSponsorPledge_andBlogAndLiveEmail_workCleanly() {
    val (badPledge, badErr) = MusicoletEngine.validateSponsorPledge("A", "bad", "patron", "monthly")
    assertNull(badPledge)
    assertNotNull(badErr)

    val (goodPledge, goodErr) = MusicoletEngine.validateSponsorPledge(
      "Lagos Collective",
      "hello@lagos.ng",
      "patron",
      "one-time",
      "Supporting offline music",
    )
    assertNull(goodErr)
    assertEquals(45, goodPledge?.amountUsd)

    val (normEmail, emailErr) = MusicoletEngine.validateBlogNotifyEmail("  User@Moronto.App ")
    assertNull(emailErr)
    assertEquals("user@moronto.app", normEmail)

    val (liveEmail, liveErr) = MusicoletEngine.validateLiveNotifyEmail("Listener@Lagos.NG")
    assertNull(liveErr)
    assertEquals("listener@lagos.ng", liveEmail)

    val (proEmail, proErr) = MusicoletEngine.validateProStreamingNotifyEmail("Stream@Moronto.NG")
    assertNull(proErr)
    assertEquals("stream@moronto.ng", proEmail)

    assertNotNull(MusicoletEngine.validateSupportRequestInput("A", "Help", "Short"))
    assertNull(
      MusicoletEngine.validateSupportRequestInput(
        "Android Phone",
        "Transferring WhatsApp MP3s",
        "My songs are inside WhatsApp Documents folder and need importing.",
      ),
    )
  }
}
