package com.shadowspark.morontomornica

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.ui.LicensedCatalogSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Loop 4: Robolectric Compose interaction/semantics tests for the "Licensed & Downloaded" surface.
 * This verifies structure/labels/actions/empty-state render and are clickable — NOT a screenshot,
 * so rendered pixel fidelity on a device is still unverified.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LicensedCatalogSectionTest {

  @get:Rule val compose = createComposeRule()

  @Test
  fun catalogSectionShowsDemoLabelsAndDownloadActions() {
    val router = PlaybackRouter()
    compose.setContent {
      LicensedCatalogSection(uiState = router.state.value, router = router)
    }

    compose.onNodeWithText("Ready to play").assertIsDisplayed()
    compose.onNodeWithText("Wi-Fi downloads").assertIsDisplayed()
    // Multiple demo tracks each expose a Stream/Play action and a DEMO source label.
    assertTrue(compose.onAllNodes(hasText("Stream", substring = true)).fetchSemanticsNodes().isNotEmpty())
    assertTrue(compose.onAllNodes(hasText("Save offline", substring = true)).fetchSemanticsNodes().isNotEmpty())
    assertTrue(compose.onAllNodes(hasText("DEMO", substring = true)).fetchSemanticsNodes().isNotEmpty())
  }

  @Test
  fun emptyCatalogShowsEmptyState() {
    val router = PlaybackRouter()
    val empty: MornicaUiState = MornicaUiState(catalogTracks = emptyList())
    compose.setContent {
      LicensedCatalogSection(uiState = empty, router = router)
    }
    compose.onNodeWithText("No tracks available yet — check back soon").assertIsDisplayed()
  }

  @Test
  fun wifiOnlySwitchTogglesPreferenceThroughRouter() {
    val router = PlaybackRouter()
    compose.setContent {
      LicensedCatalogSection(uiState = router.state.value, router = router)
    }
    // The Switch is clickable; toggling it must flip the router preference.
    compose.onNode(isToggleable()).performClick()
    compose.runOnIdle { assertTrue(router.state.value.preferWifiOnlyDownloads) }
  }

  @Test
  fun streamOnlyTrackHidesDownloadControl() {
    val router = PlaybackRouter()
    val state = router.state.value
    val streamOnly = state.catalogTracks.first { !it.offlineAllowed }
    assertFalse("Demo catalog must include a streaming-only (no-offline) track", streamOnly.offlineAllowed)

    compose.setContent {
      LicensedCatalogSection(uiState = state, router = router)
    }

    // Streaming-only track must expose the visibly-unavailable label, never a download control.
    assertTrue(
      compose.onAllNodes(hasText("Streaming only", substring = true)).fetchSemanticsNodes().isNotEmpty(),
    )
    assertEquals(
      "Only the offline-allowed track may offer 'Save offline'; streaming-only hides it",
      1,
      compose.onAllNodes(hasText("Save offline", substring = true)).fetchSemanticsNodes().size,
    )
    // And it must not offer a Retry/Cancel download control either.
    assertEquals(
      "Streaming-only track must not expose download cancellation",
      0,
      compose.onAllNodes(hasText("Cancel", substring = true)).fetchSemanticsNodes().size,
    )
  }
}
