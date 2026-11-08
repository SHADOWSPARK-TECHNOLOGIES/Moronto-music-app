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

    compose.onNodeWithText("Licensed & Downloaded").assertIsDisplayed()
    compose.onNodeWithText("Wi-Fi only for downloads").assertIsDisplayed()
    // Multiple demo tracks each expose a Stream + Download action and a DEMO source label.
    assertTrue(compose.onAllNodes(hasText("Stream", substring = true)).fetchSemanticsNodes().isNotEmpty())
    assertTrue(compose.onAllNodes(hasText("Download for later", substring = true)).fetchSemanticsNodes().isNotEmpty())
    assertTrue(compose.onAllNodes(hasText("DEMO", substring = true)).fetchSemanticsNodes().isNotEmpty())
  }

  @Test
  fun emptyCatalogShowsEmptyState() {
    val router = PlaybackRouter()
    val empty: MornicaUiState = MornicaUiState(catalogTracks = emptyList())
    compose.setContent {
      LicensedCatalogSection(uiState = empty, router = router)
    }
    compose.onNodeWithText("No licensed tracks available yet. Check back soon.").assertIsDisplayed()
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
}
