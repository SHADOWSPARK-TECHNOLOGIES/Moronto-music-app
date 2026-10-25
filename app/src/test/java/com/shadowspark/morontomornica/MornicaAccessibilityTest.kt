package com.shadowspark.morontomornica

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.domain.LocalAudioController
import com.shadowspark.morontomornica.domain.LocalMediaRepository
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.LibraryTab
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.ui.MornicaApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp")
class MornicaAccessibilityTest {
  @get:Rule val compose = createComposeRule()

  private class RecordingAudioController : LocalAudioController {
    var lastSeekMs = -1L
    override fun bindRouter(router: PlaybackRouter) = Unit
    override fun playTrack(
      track: LocalMediaTrack,
      queue: List<LocalMediaTrack>,
      repeatMode: RepeatMode,
      shuffleEnabled: Boolean,
    ) = Unit
    override fun setPlayWhenReady(playWhenReady: Boolean) = Unit
    override fun seekTo(positionMs: Long) { lastSeekMs = positionMs }
    override fun setShuffleEnabled(shuffleEnabled: Boolean) = Unit
    override fun setRepeatMode(repeatMode: RepeatMode) = Unit
    override fun stop() = Unit
  }

  private fun show(router: PlaybackRouter) {
    compose.setContent {
      MornicaApp(router, LocalMediaRepository(), onRequestAudioPermission = {})
    }
  }

  private fun playingRouter(controller: LocalAudioController? = null): PlaybackRouter {
    val router = PlaybackRouter(localAudioController = controller)
    check(router.playLocalTrack(router.state.value.localTracks.first { it.isPlayableOffline() }))
    return router
  }

  @Test
  fun libraryTabs_exposeRoleAndOneSelectedTab_afterEachActivation() {
    val router = PlaybackRouter()
    show(router)
    val tabRole = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
    val selected = SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)
    val libraryLabels = LibraryTab.entries.map { hasText(it.label) }.reduce { labels, label -> labels or label }

    LibraryTab.entries.forEach { tab ->
      val node = compose.onNode(hasText(tab.label) and tabRole)
      node.performClick().assertIsSelected()
      compose.onAllNodes(tabRole and selected and libraryLabels).assertCountEquals(1)
      compose.runOnIdle { assertEquals(tab, router.state.value.selectedLibraryTab) }
    }
  }

  @Test
  fun favoriteAndShuffle_exposeToggleState_andActivateTwice() {
    val router = playingRouter()
    router.setNowPlayingExpanded(true)
    val trackId = router.state.value.activeLocalTrack!!.id
    show(router)

    val favorite = compose.onNodeWithContentDescription("Favorite Track")
    favorite.assertIsOff().performClick().assertIsOn()
    compose.runOnIdle { assertTrue(trackId in router.state.value.favoriteTrackIds) }
    favorite.performClick().assertIsOff()
    compose.runOnIdle { assertFalse(trackId in router.state.value.favoriteTrackIds) }

    val shuffle = compose.onNodeWithContentDescription("Toggle Shuffle")
    shuffle.performScrollTo().assertIsOff().performClick().assertIsOn()
    compose.runOnIdle { assertTrue(router.state.value.shuffleEnabled) }
    shuffle.performClick().assertIsOff()
    compose.runOnIdle { assertFalse(router.state.value.shuffleEnabled) }
  }

  @Test
  fun libraryRowFavorite_pointerActivation_changesOnlyFavoriteWithoutPlayingParentCard() {
    val router = PlaybackRouter()
    val track = router.state.value.localTracks.first { it.isPlayableOffline() }
    val initial = router.state.value
    show(router)
    compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollToIndex))
      .performScrollToNode(hasText(track.title))
    val trackCard = hasAnyAncestor(hasText(track.title))

    val save = compose.onNode(hasContentDescription("Save Favorite") and trackCard)
    save.assertIsOff().performTouchInput { click(center) }
    compose.onNode(hasContentDescription("Remove Favorite") and trackCard).assertIsOn()
    compose.runOnIdle {
      // Equality also guards active track, playback, queue, position and expansion state.
      assertEquals(initial.copy(favoriteTrackIds = setOf(track.id)), router.state.value)
    }

    compose.onNode(hasContentDescription("Remove Favorite") and trackCard)
      .performTouchInput { click(center) }
    compose.onNode(hasContentDescription("Save Favorite") and trackCard).assertIsOff()
    compose.runOnIdle { assertEquals(initial, router.state.value) }
  }

  @Test
  fun seekSlider_exposesLabelRangeAndAccessibleAction_connectedToPlayback() {
    val controller = RecordingAudioController()
    val router = playingRouter(controller)
    router.setNowPlayingExpanded(true)
    show(router)
    val slider = compose.onNodeWithContentDescription("Playback position").performScrollTo()
    val duration = router.state.value.localDurationMs.toFloat()
    val initial = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
    assertEquals(0f..duration, initial.range)
    assertEquals(0f, initial.current, 0.1f)
    assertEquals("0:00 of 0:30", slider.fetchSemanticsNode().config[SemanticsProperties.StateDescription])

    slider.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
      assertTrue(setProgress(duration / 2f))
    }
    compose.runOnIdle {
      assertEquals((duration / 2f).toLong(), router.state.value.localPositionMs)
      assertEquals(router.state.value.localPositionMs, controller.lastSeekMs)
    }
    val updated = slider.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
    assertEquals(duration / 2f, updated.current, 0.1f)
    assertEquals("0:15 of 0:30", slider.fetchSemanticsNode().config[SemanticsProperties.StateDescription])
  }

  @Test
  fun miniPlayer_playPause_has48dpTarget_andDoesNotExpandParent() {
    val router = playingRouter()
    show(router)
    val pause = compose.onNodeWithContentDescription("Pause")
    pause.assertWidthIsAtLeast(48.dp).assertHeightIsAtLeast(48.dp)
    // A real pointer event near the target's top edge also checks nested click dispatch.
    pause.performTouchInput { click(Offset(center.x, 1f)) }
    compose.runOnIdle {
      assertFalse(router.state.value.isLocalPlaying)
      assertFalse(router.state.value.isNowPlayingExpanded)
    }
    compose.onNodeWithContentDescription("Play").performClick()
    compose.runOnIdle {
      assertTrue(router.state.value.isLocalPlaying)
      assertFalse(router.state.value.isNowPlayingExpanded)
    }
  }
}
