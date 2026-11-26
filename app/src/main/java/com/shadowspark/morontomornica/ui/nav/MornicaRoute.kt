package com.shadowspark.morontomornica.ui.nav

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lightweight overlay navigation for secondary screens (search, downloads, settings,
 * playlist detail). The three bottom-tab PrimaryScreens are unchanged; these routes
 * render as full-screen overlays on top of the active tab so existing navigation and
 * accessibility tests keep their assumptions.
 */
sealed interface MornicaRoute {
  data object HomeFeed : MornicaRoute
  data object Search : MornicaRoute
  data object Downloads : MornicaRoute
  data object Settings : MornicaRoute
  data object Playlists : MornicaRoute
  data class PlaylistDetail(val name: String) : MornicaRoute
}

object MornicaNav {
  private val _route = MutableStateFlow<MornicaRoute?>(null)
  val route: StateFlow<MornicaRoute?> = _route.asStateFlow()

  fun open(route: MornicaRoute) {
    _route.value = route
  }

  fun close() {
    _route.value = null
  }
}
