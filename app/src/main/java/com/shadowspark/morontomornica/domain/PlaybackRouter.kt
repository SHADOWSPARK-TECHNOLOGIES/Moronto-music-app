package com.shadowspark.morontomornica.domain

import com.shadowspark.morontomornica.model.ActiveEngine
import com.shadowspark.morontomornica.model.ArtistLaunchRequest
import com.shadowspark.morontomornica.model.CreatorProfile
import com.shadowspark.morontomornica.model.LibraryTab
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.PrimaryScreen
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.VidIqSearchEstimate
import com.shadowspark.morontomornica.model.YouTubeVideoCard
import com.shadowspark.morontomornica.model.validateArtistLaunchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MornicaUiState(
  val currentScreen: PrimaryScreen = PrimaryScreen.DISCOVER_WATCH,
  val activeEngine: ActiveEngine = ActiveEngine.NONE,
  // Domain A: YouTube Discover & Watch
  val searchQuery: String = "Asake",
  val risingCreatorsOnly: Boolean = false,
  val researchEstimates: List<VidIqSearchEstimate> = emptyList(),
  val youtubeVideos: List<YouTubeVideoCard> = emptyList(),
  val activeYouTubeVideo: YouTubeVideoCard? = null,
  val isYouTubeEmbedPlaying: Boolean = false,
  val isYouTubeViewportVisible: Boolean = true,
  // Domain B: Musicolet-style Local Audio
  val hasAudioPermission: Boolean = false,
  val selectedLibraryTab: LibraryTab = LibraryTab.SONGS,
  val localTracks: List<LocalMediaTrack> = emptyList(),
  val activeLocalTrack: LocalMediaTrack? = null,
  val isLocalPlaying: Boolean = false,
  val localPositionMs: Long = 0L,
  val shuffleEnabled: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.OFF,
  val favoriteTrackIds: Set<String> = emptySet(),
  val playlists: Map<String, List<String>> = mapOf("Afrobeats Offline Queue" to listOf("demo-lagos-pulse", "demo-lekki-logdrum")),
  // Domain C: Creator Launch Hub
  val creatorProfiles: List<CreatorProfile> = emptyList(),
  val submittedLaunchRequests: List<ArtistLaunchRequest> = emptyList(),
  val statusBannerMessage: String? = null,
)

class PlaybackRouter(
  private val youtubeRepository: YouTubeDiscoveryRepository = YouTubeDiscoveryRepository(),
  private val localMediaRepository: LocalMediaRepository = LocalMediaRepository(),
  private val onLocalAudioCommand: ((LocalMediaTrack?, Boolean) -> Unit)? = null,
) {
  companion object {
    const val MIN_YOUTUBE_VIEWPORT_DP: Int = 200
  }

  private val _state = MutableStateFlow(MornicaUiState())
  val state: StateFlow<MornicaUiState> = _state.asStateFlow()

  init {
    val initialVideos = youtubeRepository.searchOfficialVideos("Asake", risingCreatorsOnly = false)
    val initialTracks = localMediaRepository.defaultDemoAndCatalogTracks()
    _state.value =
      MornicaUiState(
        researchEstimates = youtubeRepository.getResearchEstimates(),
        youtubeVideos = initialVideos.items,
        localTracks = initialTracks,
        creatorProfiles = localMediaRepository.verifiedCreatorProfiles(),
      )
  }

  fun selectScreen(screen: PrimaryScreen) {
    _state.update { current ->
      // If navigating away from Discover & Watch, pause embedded YouTube video so it never plays hidden
      val shouldPauseYouTube =
        screen != PrimaryScreen.DISCOVER_WATCH && current.isYouTubeEmbedPlaying
      if (shouldPauseYouTube) {
        current.copy(
          currentScreen = screen,
          isYouTubeEmbedPlaying = false,
          isYouTubeViewportVisible = false,
          statusBannerMessage = "YouTube embed paused while outside Discover & Watch (Visible Player Policy).",
        )
      } else {
        current.copy(
          currentScreen = screen,
          isYouTubeViewportVisible = screen == PrimaryScreen.DISCOVER_WATCH,
        )
      }
    }
  }

  fun updateSearchQuery(query: String, risingOnly: Boolean = _state.value.risingCreatorsOnly) {
    val result = youtubeRepository.searchOfficialVideos(query, risingCreatorsOnly = risingOnly)
    _state.update {
      it.copy(
        searchQuery = query,
        risingCreatorsOnly = risingOnly,
        youtubeVideos = result.items,
      )
    }
  }

  /**
   * AC-1, AC-2, AC-5, AC-7: Starts an official YouTube video in the visible embed player.
   * Immediately pauses local audio so both engines never collide.
   * Never checks any premium/subscription flag because YouTube embeds must remain 100% free.
   */
  fun startYouTubeEmbed(
    video: YouTubeVideoCard,
    viewportWidthDp: Int = 320,
    viewportHeightDp: Int = 220,
  ): Boolean {
    if (!video.isPolicyCompliant()) {
      _state.update {
        it.copy(statusBannerMessage = "Blocked: Video metadata failed YouTube policy validation.")
      }
      return false
    }
    if (viewportWidthDp < MIN_YOUTUBE_VIEWPORT_DP || viewportHeightDp < MIN_YOUTUBE_VIEWPORT_DP) {
      _state.update {
        it.copy(
          isYouTubeEmbedPlaying = false,
          statusBannerMessage =
            "Blocked: YouTube player viewport must be at least 200dp x 200dp and visible.",
        )
      }
      return false
    }

    onLocalAudioCommand?.invoke(null, false)
    _state.update {
      it.copy(
        currentScreen = PrimaryScreen.DISCOVER_WATCH,
        activeEngine = ActiveEngine.YOUTUBE_EMBED,
        activeYouTubeVideo = video,
        isYouTubeEmbedPlaying = true,
        isYouTubeViewportVisible = true,
        isLocalPlaying = false,
        statusBannerMessage = "Playing official YouTube embed: ${video.title}",
      )
    }
    return true
  }

  fun onAppBackgrounded() {
    _state.update { current ->
      if (current.isYouTubeEmbedPlaying) {
        current.copy(
          isYouTubeEmbedPlaying = false,
          statusBannerMessage = "YouTube playback paused in background per Google Play & YouTube policy.",
        )
      } else {
        current
      }
    }
  }

  fun onYouTubeVisibilityChanged(isVisible: Boolean) {
    _state.update { current ->
      if (!isVisible && current.isYouTubeEmbedPlaying) {
        current.copy(
          isYouTubeViewportVisible = false,
          isYouTubeEmbedPlaying = false,
          statusBannerMessage = "YouTube embed paused when player hidden.",
        )
      } else {
        current.copy(isYouTubeViewportVisible = isVisible)
      }
    }
  }

  /**
   * AC-1, AC-2, AC-6: Starts a local device track or rights-cleared DEMO preview.
   * Rejects UNAVAILABLE tracks and rejects any YouTube URL/URI.
   * Immediately stops any active YouTube embed.
   */
  fun playLocalTrack(track: LocalMediaTrack): Boolean {
    if (LocalMediaTrack.isYouTubeUri(track.uri)) {
      _state.update {
        it.copy(
          statusBannerMessage =
            "Blocked: YouTube streams cannot be loaded into the offline local audio engine.",
        )
      }
      return false
    }
    if (!track.isPlayableOffline()) {
      _state.update {
        it.copy(
          statusBannerMessage =
            "Track unavailable: '${track.title}' awaits rights clearance and cannot be played.",
        )
      }
      return false
    }

    onLocalAudioCommand?.invoke(track, true)
    _state.update {
      it.copy(
        activeEngine = ActiveEngine.LOCAL_AUDIO,
        activeLocalTrack = track,
        isLocalPlaying = true,
        localPositionMs = 0L,
        isYouTubeEmbedPlaying = false,
        activeYouTubeVideo = null,
        statusBannerMessage = "Playing offline audio: ${track.title} (${track.provenance.label})",
      )
    }
    return true
  }

  fun toggleLocalPlayPause() {
    val current = _state.value
    val targetTrack =
      current.activeLocalTrack ?: current.localTracks.firstOrNull { it.isPlayableOffline() }
    if (targetTrack == null) return

    if (current.activeLocalTrack == null) {
      playLocalTrack(targetTrack)
      return
    }

    val nextPlaying = !current.isLocalPlaying
    onLocalAudioCommand?.invoke(targetTrack, nextPlaying)
    _state.update {
      it.copy(
        activeEngine = if (nextPlaying) ActiveEngine.LOCAL_AUDIO else it.activeEngine,
        isLocalPlaying = nextPlaying,
        isYouTubeEmbedPlaying = if (nextPlaying) false else it.isYouTubeEmbedPlaying,
      )
    }
  }

  fun seekLocalTo(positionMs: Long) {
    _state.update { current ->
      val maxDur = current.activeLocalTrack?.durationMs ?: 30_000L
      current.copy(localPositionMs = positionMs.coerceIn(0L, maxDur))
    }
  }

  fun skipNext() {
    val playable = _state.value.localTracks.filter { it.isPlayableOffline() }
    if (playable.isEmpty()) return
    val currentId = _state.value.activeLocalTrack?.id
    val currentIndex = playable.indexOfFirst { it.id == currentId }
    val nextTrack =
      when {
        _state.value.repeatMode == RepeatMode.ONE && _state.value.activeLocalTrack != null ->
          _state.value.activeLocalTrack!!
        currentIndex < 0 -> playable.first()
        currentIndex + 1 < playable.size -> playable[currentIndex + 1]
        _state.value.repeatMode == RepeatMode.ALL -> playable.first()
        else -> playable.first()
      }
    playLocalTrack(nextTrack)
  }

  fun skipPrevious() {
    val playable = _state.value.localTracks.filter { it.isPlayableOffline() }
    if (playable.isEmpty()) return
    val currentId = _state.value.activeLocalTrack?.id
    val currentIndex = playable.indexOfFirst { it.id == currentId }
    val prevTrack =
      if (currentIndex > 0) playable[currentIndex - 1] else playable.last()
    playLocalTrack(prevTrack)
  }

  fun toggleShuffle() {
    _state.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
  }

  fun cycleRepeatMode() {
    _state.update { current ->
      val next =
        when (current.repeatMode) {
          RepeatMode.OFF -> RepeatMode.ALL
          RepeatMode.ALL -> RepeatMode.ONE
          RepeatMode.ONE -> RepeatMode.OFF
        }
      current.copy(repeatMode = next)
    }
  }

  fun onHeadsetDisconnected() {
    onLocalAudioCommand?.invoke(_state.value.activeLocalTrack, false)
    _state.update {
      it.copy(
        isLocalPlaying = false,
        statusBannerMessage = "Headset unplugged: Offline audio paused automatically.",
      )
    }
  }

  fun onAudioFocusLost() {
    onLocalAudioCommand?.invoke(_state.value.activeLocalTrack, false)
    _state.update {
      it.copy(
        isLocalPlaying = false,
        statusBannerMessage = "Audio focus lost: Offline audio paused.",
      )
    }
  }

  fun selectLibraryTab(tab: LibraryTab) {
    _state.update { it.copy(selectedLibraryTab = tab) }
  }

  fun updateDeviceAudioTracks(hasPermission: Boolean, tracks: List<LocalMediaTrack>) {
    _state.update {
      it.copy(
        hasAudioPermission = hasPermission,
        localTracks = tracks.ifEmpty { localMediaRepository.defaultDemoAndCatalogTracks() },
      )
    }
  }

  fun toggleFavorite(trackId: String) {
    _state.update { current ->
      val updated =
        if (current.favoriteTrackIds.contains(trackId)) {
          current.favoriteTrackIds - trackId
        } else {
          current.favoriteTrackIds + trackId
        }
      current.copy(favoriteTrackIds = updated)
    }
  }

  fun addToPlaylist(playlistName: String, trackId: String): Boolean {
    val track = _state.value.localTracks.find { it.id == trackId } ?: return false
    if (!track.isPlayableOffline()) return false
    _state.update { current ->
      val existing = current.playlists[playlistName].orEmpty()
      val updatedList = if (existing.contains(trackId)) existing else existing + trackId
      current.copy(
        playlists = current.playlists + (playlistName to updatedList),
        statusBannerMessage = "Added '${track.title}' to playlist '$playlistName'.",
      )
    }
    return true
  }

  fun submitArtistLaunchRequest(request: ArtistLaunchRequest): Boolean {
    val validation = validateArtistLaunchRequest(request)
    if (!validation.valid) {
      _state.update { it.copy(statusBannerMessage = validation.errorMessage) }
      return false
    }
    _state.update { current ->
      current.copy(
        submittedLaunchRequests = current.submittedLaunchRequests + request,
        statusBannerMessage =
          "Launch package inquiry recorded for ${request.artistName} (${request.trackTitle}).",
      )
    }
    return true
  }
}
