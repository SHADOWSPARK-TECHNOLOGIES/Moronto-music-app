package com.shadowspark.morontomornica.domain

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.shadowspark.morontomornica.MainActivity
import com.shadowspark.morontomornica.model.ActiveEngine
import com.shadowspark.morontomornica.model.ArtistLaunchRequest
import com.shadowspark.morontomornica.model.CreatorProfile
import com.shadowspark.morontomornica.model.LibraryTab
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.PrimaryScreen
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.TrackSortOption
import com.shadowspark.morontomornica.model.VidIqSearchEstimate
import com.shadowspark.morontomornica.model.YouTubeVideoCard
import com.shadowspark.morontomornica.model.validateArtistLaunchRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface LocalAudioController {
  fun bindRouter(router: PlaybackRouter)
  fun playTrack(
    track: LocalMediaTrack,
    queue: List<LocalMediaTrack>,
    repeatMode: RepeatMode,
    shuffleEnabled: Boolean,
  )
  fun setPlayWhenReady(playWhenReady: Boolean)
  fun seekTo(positionMs: Long)
  fun setShuffleEnabled(shuffleEnabled: Boolean)
  fun setRepeatMode(repeatMode: RepeatMode)
  fun stop()
}

data class MornicaUiState(
  val currentScreen: PrimaryScreen = PrimaryScreen.MY_MUSIC,
  val activeEngine: ActiveEngine = ActiveEngine.NONE,
  // Domain A: YouTube Discover & Watch
  val searchQuery: String = "Asake",
  val risingCreatorsOnly: Boolean = false,
  val researchEstimates: List<VidIqSearchEstimate> = emptyList(),
  val youtubeVideos: List<YouTubeVideoCard> = emptyList(),
  val externalYouTubeSearchUrl: String = "https://www.youtube.com/results?search_query=Asake",
  val activeYouTubeVideo: YouTubeVideoCard? = null,
  val isYouTubeEmbedPlaying: Boolean = false,
  val isYouTubeViewportVisible: Boolean = true,
  // Domain B: Musicolet-style Local Audio
  val hasAudioPermission: Boolean = false,
  val selectedLibraryTab: LibraryTab = LibraryTab.SONGS,
  val localSearchQuery: String = "",
  val trackSortOption: TrackSortOption = TrackSortOption.TITLE,
  val isNowPlayingExpanded: Boolean = false,
  val localTracks: List<LocalMediaTrack> = emptyList(),
  val queueTrackIds: List<String> = emptyList(),
  val activeLocalTrack: LocalMediaTrack? = null,
  val isLocalPlaying: Boolean = false,
  val localPositionMs: Long = 0L,
  val localDurationMs: Long = 0L,
  val shuffleEnabled: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.OFF,
  val playbackError: String? = null,
  val favoriteTrackIds: Set<String> = emptySet(),
  val playlists: Map<String, List<String>> =
    mapOf("Afrobeats Offline Queue" to listOf("demo-lagos-pulse", "demo-lekki-logdrum")),
  // Domain C: Creator Launch Hub
  val creatorProfiles: List<CreatorProfile> = emptyList(),
  val submittedLaunchRequests: List<ArtistLaunchRequest> = emptyList(),
  val statusBannerMessage: String? = null,
)

class PlaybackRouter(
  private val youtubeRepository: YouTubeDiscoveryRepository = YouTubeDiscoveryRepository(),
  private val localMediaRepository: LocalMediaRepository = LocalMediaRepository(),
  private val onLocalAudioCommand: ((LocalMediaTrack?, Boolean) -> Unit)? = null,
  private var localAudioController: LocalAudioController? = null,
) {
  companion object {
    const val MIN_YOUTUBE_VIEWPORT_DP: Int = 200
    const val RESTART_TRACK_THRESHOLD_MS: Long = 3_000L
  }

  private val _state = MutableStateFlow(MornicaUiState())
  val state: StateFlow<MornicaUiState> = _state.asStateFlow()

  init {
    val initialVideos = youtubeRepository.searchOfficialVideos("Asake", risingCreatorsOnly = false)
    val initialTracks = localMediaRepository.defaultDemoAndCatalogTracks()
    val playableIds = initialTracks.filter { it.isPlayableOffline() }.map { it.id }
    _state.value =
      MornicaUiState(
        researchEstimates = youtubeRepository.getResearchEstimates(),
        youtubeVideos = initialVideos.items,
        externalYouTubeSearchUrl = youtubeRepository.buildExternalSearchUrl("Asake"),
        localTracks = initialTracks,
        queueTrackIds = playableIds,
        creatorProfiles = localMediaRepository.verifiedCreatorProfiles(),
      )
    localAudioController?.bindRouter(this)
  }

  fun attachAudioController(controller: LocalAudioController?) {
    localAudioController = controller
    controller?.bindRouter(this)
    val current = _state.value
    controller?.setRepeatMode(current.repeatMode)
    controller?.setShuffleEnabled(current.shuffleEnabled)
    val active = current.activeLocalTrack
    if (active != null && current.isLocalPlaying) {
      val orderedQueue = buildOrderedPlayableQueue(current.localTracks, current.shuffleEnabled, active)
      controller?.playTrack(
        track = active,
        queue = orderedQueue,
        repeatMode = current.repeatMode,
        shuffleEnabled = current.shuffleEnabled,
      )
      if (current.localPositionMs > 0L) {
        controller?.seekTo(current.localPositionMs)
      }
    }
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
          statusBannerMessage =
            "YouTube embed paused while outside Discover & Watch (Visible Player Policy).",
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
    val searchUrl = youtubeRepository.buildExternalSearchUrl(query)
    _state.update {
      it.copy(
        searchQuery = query,
        risingCreatorsOnly = risingOnly,
        youtubeVideos = result.items,
        externalYouTubeSearchUrl = searchUrl,
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
    localAudioController?.setPlayWhenReady(false)
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
      // Pause YouTube when backgrounded; local audio in LocalAudioPlaybackService keeps playing
      if (current.isYouTubeEmbedPlaying) {
        current.copy(
          isYouTubeEmbedPlaying = false,
          statusBannerMessage =
            "YouTube playback paused in background per Google Play & YouTube policy.",
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
   * Rejects UNAVAILABLE/unlicensed tracks and rejects any YouTube URL/URI.
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

    val current = _state.value
    val orderedQueue = buildOrderedPlayableQueue(current.localTracks, current.shuffleEnabled, track)
    val duration = track.durationMs.coerceAtLeast(1_000L)

    onLocalAudioCommand?.invoke(track, true)
    localAudioController?.playTrack(
      track = track,
      queue = orderedQueue,
      repeatMode = current.repeatMode,
      shuffleEnabled = current.shuffleEnabled,
    )

    _state.update {
      it.copy(
        activeEngine = ActiveEngine.LOCAL_AUDIO,
        activeLocalTrack = track,
        queueTrackIds = orderedQueue.map { item -> item.id },
        isLocalPlaying = true,
        localPositionMs = 0L,
        localDurationMs = duration,
        playbackError = null,
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
    localAudioController?.setPlayWhenReady(nextPlaying)
    _state.update {
      it.copy(
        activeEngine = if (nextPlaying) ActiveEngine.LOCAL_AUDIO else it.activeEngine,
        isLocalPlaying = nextPlaying,
        playbackError = if (nextPlaying) null else it.playbackError,
        isYouTubeEmbedPlaying = if (nextPlaying) false else it.isYouTubeEmbedPlaying,
      )
    }
  }

  fun seekLocalTo(positionMs: Long) {
    val current = _state.value
    val maxDur =
      (current.localDurationMs.takeIf { it > 0L } ?: current.activeLocalTrack?.durationMs ?: 30_000L)
        .coerceAtLeast(1_000L)
    val clamped = positionMs.coerceIn(0L, maxDur)
    localAudioController?.seekTo(clamped)
    _state.update {
      it.copy(
        localPositionMs = clamped,
        localDurationMs = maxDur,
      )
    }
  }

  fun skipNext() {
    advanceToNextTrack(isAutoTrackEnd = false)
  }

  fun onTrackEnded() {
    advanceToNextTrack(isAutoTrackEnd = true)
  }

  private fun advanceToNextTrack(isAutoTrackEnd: Boolean) {
    val current = _state.value
    val playable = resolveActivePlayableQueue(current)
    if (playable.isEmpty()) return

    val activeTrack = current.activeLocalTrack
    if (current.repeatMode == RepeatMode.ONE && activeTrack != null) {
      onLocalAudioCommand?.invoke(activeTrack, true)
      localAudioController?.seekTo(0L)
      localAudioController?.setPlayWhenReady(true)
      _state.update {
        it.copy(
          isLocalPlaying = true,
          localPositionMs = 0L,
          playbackError = null,
          statusBannerMessage = "Repeating track: ${activeTrack.title}",
        )
      }
      return
    }

    val currentIndex = playable.indexOfFirst { it.id == activeTrack?.id }
    when {
      currentIndex < 0 -> playLocalTrack(playable.first())
      currentIndex + 1 < playable.size -> playLocalTrack(playable[currentIndex + 1])
      current.repeatMode == RepeatMode.ALL -> playLocalTrack(playable.first())
      else -> {
        // RepeatMode.OFF at the end of the queue stops playback cleanly
        onLocalAudioCommand?.invoke(activeTrack, false)
        localAudioController?.setPlayWhenReady(false)
        localAudioController?.seekTo(0L)
        _state.update {
          it.copy(
            isLocalPlaying = false,
            localPositionMs = 0L,
            statusBannerMessage =
              if (isAutoTrackEnd) {
                "Playback finished at end of queue."
              } else {
                "Reached end of offline queue (Repeat OFF)."
              },
          )
        }
      }
    }
  }

  fun skipPrevious() {
    val current = _state.value
    val playable = resolveActivePlayableQueue(current)
    if (playable.isEmpty()) return

    if (current.activeLocalTrack != null && current.localPositionMs > RESTART_TRACK_THRESHOLD_MS) {
      seekLocalTo(0L)
      return
    }

    val currentId = current.activeLocalTrack?.id
    val currentIndex = playable.indexOfFirst { it.id == currentId }
    val prevTrack =
      when {
        currentIndex > 0 -> playable[currentIndex - 1]
        current.repeatMode == RepeatMode.ALL -> playable.last()
        else -> playable.first()
      }
    playLocalTrack(prevTrack)
  }

  fun toggleShuffle() {
    val current = _state.value
    val nextShuffle = !current.shuffleEnabled
    val ordered = buildOrderedPlayableQueue(current.localTracks, nextShuffle, current.activeLocalTrack)
    localAudioController?.setShuffleEnabled(nextShuffle)
    _state.update {
      it.copy(
        shuffleEnabled = nextShuffle,
        queueTrackIds = ordered.map { track -> track.id },
        statusBannerMessage = if (nextShuffle) "Shuffle enabled." else "Shuffle disabled.",
      )
    }
  }

  fun cycleRepeatMode() {
    val next =
      when (_state.value.repeatMode) {
        RepeatMode.OFF -> RepeatMode.ALL
        RepeatMode.ALL -> RepeatMode.ONE
        RepeatMode.ONE -> RepeatMode.OFF
      }
    localAudioController?.setRepeatMode(next)
    _state.update { it.copy(repeatMode = next) }
  }

  fun onPlaybackPositionUpdated(positionMs: Long, durationMs: Long = _state.value.localDurationMs) {
    _state.update { current ->
      val effectiveDuration =
        when {
          durationMs > 0L -> durationMs
          current.localDurationMs > 0L -> current.localDurationMs
          else -> current.activeLocalTrack?.durationMs ?: 30_000L
        }.coerceAtLeast(1_000L)
      current.copy(
        localPositionMs = positionMs.coerceIn(0L, effectiveDuration),
        localDurationMs = effectiveDuration,
      )
    }
  }

  fun onPlayerIsPlayingChanged(isPlaying: Boolean) {
    _state.update { current ->
      if (current.activeLocalTrack == null) {
        current
      } else {
        current.copy(
          activeEngine = if (isPlaying) ActiveEngine.LOCAL_AUDIO else current.activeEngine,
          isLocalPlaying = isPlaying,
          isYouTubeEmbedPlaying = if (isPlaying) false else current.isYouTubeEmbedPlaying,
        )
      }
    }
  }

  fun onMediaTrackTransitioned(trackId: String?, positionMs: Long = 0L) {
    if (trackId.isNullOrBlank()) return
    val track = _state.value.localTracks.find { it.id == trackId } ?: return
    if (!track.isPlayableOffline()) return
    _state.update { current ->
      val dur = track.durationMs.coerceAtLeast(1_000L)
      current.copy(
        activeEngine = ActiveEngine.LOCAL_AUDIO,
        activeLocalTrack = track,
        localPositionMs = positionMs.coerceIn(0L, dur),
        localDurationMs = dur,
        playbackError = null,
      )
    }
  }

  fun onPlayerRepeatModeChanged(repeatMode: RepeatMode) {
    _state.update { it.copy(repeatMode = repeatMode) }
  }

  fun onPlayerShuffleChanged(shuffleEnabled: Boolean) {
    val current = _state.value
    if (current.shuffleEnabled == shuffleEnabled) return
    val ordered = buildOrderedPlayableQueue(current.localTracks, shuffleEnabled, current.activeLocalTrack)
    _state.update {
      it.copy(
        shuffleEnabled = shuffleEnabled,
        queueTrackIds = ordered.map { track -> track.id },
      )
    }
  }

  fun onPlayerError(errorMessage: String) {
    onLocalAudioCommand?.invoke(_state.value.activeLocalTrack, false)
    localAudioController?.stop()
    _state.update {
      it.copy(
        isLocalPlaying = false,
        playbackError = errorMessage,
        statusBannerMessage = "Playback error: $errorMessage",
      )
    }
  }

  fun onHeadsetDisconnected() {
    onLocalAudioCommand?.invoke(_state.value.activeLocalTrack, false)
    localAudioController?.setPlayWhenReady(false)
    _state.update {
      it.copy(
        isLocalPlaying = false,
        statusBannerMessage = "Headset unplugged: Offline audio paused automatically.",
      )
    }
  }

  fun onAudioFocusLost() {
    onLocalAudioCommand?.invoke(_state.value.activeLocalTrack, false)
    localAudioController?.setPlayWhenReady(false)
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

  fun updateLocalSearchQuery(query: String) {
    _state.update { it.copy(localSearchQuery = query) }
  }

  fun selectTrackSortOption(sortOption: TrackSortOption) {
    _state.update { it.copy(trackSortOption = sortOption) }
  }

  fun setNowPlayingExpanded(expanded: Boolean) {
    _state.update { it.copy(isNowPlayingExpanded = expanded) }
  }

  fun filteredAndSortedLocalTracks(): List<LocalMediaTrack> {
    val current = _state.value
    val q = current.localSearchQuery.trim().lowercase()
    val filtered =
      if (q.isEmpty()) {
        current.localTracks
      } else {
        current.localTracks.filter { track ->
          track.title.lowercase().contains(q) ||
            track.artist.lowercase().contains(q) ||
            track.album.lowercase().contains(q) ||
            track.folderName.lowercase().contains(q)
        }
      }
    return when (current.trackSortOption) {
      TrackSortOption.TITLE -> filtered.sortedBy { it.title.lowercase() }
      TrackSortOption.ARTIST ->
        filtered.sortedWith(
          compareBy<LocalMediaTrack> { it.artist.lowercase() }
            .thenBy { it.title.lowercase() },
        )
      TrackSortOption.DURATION -> filtered.sortedByDescending { it.durationMs }
      TrackSortOption.YEAR ->
        filtered.sortedWith(
          compareByDescending<LocalMediaTrack> { it.releaseYear }
            .thenBy { it.title.lowercase() },
        )
    }
  }

  fun updateDeviceAudioTracks(hasPermission: Boolean, tracks: List<LocalMediaTrack>) {
    val resolvedTracks = tracks.ifEmpty { localMediaRepository.defaultDemoAndCatalogTracks() }
    val ordered =
      buildOrderedPlayableQueue(
        resolvedTracks,
        _state.value.shuffleEnabled,
        _state.value.activeLocalTrack,
      )
    _state.update {
      it.copy(
        hasAudioPermission = hasPermission,
        localTracks = resolvedTracks,
        queueTrackIds = ordered.map { track -> track.id },
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

  private fun resolveActivePlayableQueue(current: MornicaUiState): List<LocalMediaTrack> {
    val playableById = current.localTracks.filter { it.isPlayableOffline() }.associateBy { it.id }
    val fromQueueIds = current.queueTrackIds.mapNotNull { playableById[it] }
    return if (fromQueueIds.size == playableById.size && fromQueueIds.isNotEmpty()) {
      fromQueueIds
    } else {
      buildOrderedPlayableQueue(current.localTracks, current.shuffleEnabled, current.activeLocalTrack)
    }
  }

  private fun buildOrderedPlayableQueue(
    tracks: List<LocalMediaTrack>,
    shuffleEnabled: Boolean,
    anchorTrack: LocalMediaTrack?,
  ): List<LocalMediaTrack> {
    val playable = tracks.filter { it.isPlayableOffline() }
    if (!shuffleEnabled || playable.size <= 1) return playable
    // Deterministic shuffle rotation so queue order changes predictably when shuffle is enabled
    val remaining = playable.filter { it.id != anchorTrack?.id }.reversed()
    return if (anchorTrack != null && playable.any { it.id == anchorTrack.id }) {
      listOf(anchorTrack) + remaining
    } else {
      playable.reversed()
    }
  }
}

fun RepeatMode.toMedia3RepeatMode(): Int =
  when (this) {
    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
    RepeatMode.ALL -> Player.REPEAT_MODE_ALL
    RepeatMode.ONE -> Player.REPEAT_MODE_ONE
  }

fun Int.toMornicaRepeatMode(): RepeatMode =
  when (this) {
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    else -> RepeatMode.OFF
  }

fun LocalMediaTrack.toMedia3Item(): MediaItem =
  MediaItem.Builder()
    .setMediaId(id)
    .setUri(Uri.parse(uri))
    .setMediaMetadata(
      MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .build(),
    )
    .build()

class Media3LocalAudioController(
  private val player: Player,
) : LocalAudioController {
  private var boundRouter: PlaybackRouter? = null

  private val listener =
    object : Player.Listener {
      override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (
          isPlaying ||
            !player.playWhenReady ||
            player.playbackState == Player.STATE_IDLE ||
            player.playbackState == Player.STATE_ENDED
        ) {
          boundRouter?.onPlayerIsPlayingChanged(isPlaying)
        }
      }

      override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (!playWhenReady) {
          boundRouter?.onPlayerIsPlayingChanged(false)
        }
      }

      override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
          Player.STATE_ENDED -> boundRouter?.onTrackEnded()
          Player.STATE_READY -> {
            val dur = player.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
            boundRouter?.onPlaybackPositionUpdated(player.currentPosition.coerceAtLeast(0L), dur)
          }
          else -> Unit
        }
      }

      override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val uriString = mediaItem?.localConfiguration?.uri?.toString().orEmpty()
        if (LocalMediaTrack.isYouTubeUri(uriString)) {
          player.stop()
          player.clearMediaItems()
          boundRouter?.onPlayerError("Blocked YouTube URI in background local audio service.")
          return
        }
        boundRouter?.onMediaTrackTransitioned(
          trackId = mediaItem?.mediaId,
          positionMs = player.currentPosition.coerceAtLeast(0L),
        )
      }

      override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
      ) {
        val dur = player.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
        boundRouter?.onPlaybackPositionUpdated(newPosition.positionMs.coerceAtLeast(0L), dur)
      }

      override fun onRepeatModeChanged(repeatMode: Int) {
        boundRouter?.onPlayerRepeatModeChanged(repeatMode.toMornicaRepeatMode())
      }

      override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        boundRouter?.onPlayerShuffleChanged(shuffleModeEnabled)
      }

      override fun onPlayerError(error: PlaybackException) {
        boundRouter?.onPlayerError(error.message ?: "Media3 error (${error.errorCodeName})")
      }
    }

  override fun bindRouter(router: PlaybackRouter) {
    if (boundRouter == null) {
      player.addListener(listener)
    }
    boundRouter = router
  }

  fun release() {
    player.removeListener(listener)
    boundRouter = null
  }

  override fun playTrack(
    track: LocalMediaTrack,
    queue: List<LocalMediaTrack>,
    repeatMode: RepeatMode,
    shuffleEnabled: Boolean,
  ) {
    if (LocalMediaTrack.isYouTubeUri(track.uri) || !track.isPlayableOffline()) return
    val safeQueue = queue.filter { it.isPlayableOffline() && !LocalMediaTrack.isYouTubeUri(it.uri) }
    val items = (if (safeQueue.isEmpty()) listOf(track) else safeQueue).map { it.toMedia3Item() }
    val startIndex = items.indexOfFirst { it.mediaId == track.id }.coerceAtLeast(0)

    player.repeatMode = repeatMode.toMedia3RepeatMode()
    player.shuffleModeEnabled = shuffleEnabled
    player.setMediaItems(items, startIndex, 0L)
    player.prepare()
    player.playWhenReady = true
  }

  override fun setPlayWhenReady(playWhenReady: Boolean) {
    player.playWhenReady = playWhenReady
    if (playWhenReady && player.playbackState == Player.STATE_IDLE && player.mediaItemCount > 0) {
      player.prepare()
    }
  }

  override fun seekTo(positionMs: Long) {
    player.seekTo(positionMs.coerceAtLeast(0L))
  }

  override fun setShuffleEnabled(shuffleEnabled: Boolean) {
    player.shuffleModeEnabled = shuffleEnabled
  }

  override fun setRepeatMode(repeatMode: RepeatMode) {
    player.repeatMode = repeatMode.toMedia3RepeatMode()
  }

  override fun stop() {
    player.playWhenReady = false
    player.pause()
  }
}

object MornicaRuntime {
  val localRepo: LocalMediaRepository = LocalMediaRepository()
  val youtubeRepo: YouTubeDiscoveryRepository = YouTubeDiscoveryRepository()
  val router: PlaybackRouter =
    PlaybackRouter(
      youtubeRepository = youtubeRepo,
      localMediaRepository = localRepo,
    )
}

/**
 * PHASE 1: Local-only background audio playback service using AndroidX Media3
 * ExoPlayer, MediaSession, and MediaSessionService.
 *
 * - Owns ExoPlayer & MediaSession across Activity destruction.
 * - Handles audio focus (C.USAGE_MEDIA + C.AUDIO_CONTENT_TYPE_MUSIC, handleAudioFocus = true).
 * - Handles headset disconnection (setHandleAudioBecomingNoisy(true)).
 * - Exposes system notification and lock-screen transport controls.
 * - Strictly rejects YouTube URIs so YouTube never plays in the background service.
 */
class LocalAudioPlaybackService : MediaSessionService() {
  internal var exoPlayer: ExoPlayer? = null
  internal var mediaSession: MediaSession? = null
  internal var controllerBridge: Media3LocalAudioController? = null
  private val positionHandler = Handler(Looper.getMainLooper())
  private val positionReporter =
    object : Runnable {
      override fun run() {
        val player = exoPlayer
        if (player != null && player.isPlaying) {
          val dur = player.duration.takeIf { it != C.TIME_UNSET && it > 0L } ?: 0L
          MornicaRuntime.router.onPlaybackPositionUpdated(
            player.currentPosition.coerceAtLeast(0L),
            dur,
          )
        }
        positionHandler.postDelayed(this, 500L)
      }
    }

  override fun onCreate() {
    super.onCreate()
    val audioAttributes =
      AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    val player =
      ExoPlayer.Builder(this)
        .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
        .setHandleAudioBecomingNoisy(true)
        .build()
    exoPlayer = player

    val launchIntent = Intent(this, MainActivity::class.java)
    val sessionActivityPendingIntent =
      PendingIntent.getActivity(
        this,
        0,
        launchIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )

    mediaSession =
      MediaSession.Builder(this, player)
        .setSessionActivity(sessionActivityPendingIntent)
        .build()

    val bridge = Media3LocalAudioController(player)
    controllerBridge = bridge
    MornicaRuntime.router.attachAudioController(bridge)
    positionHandler.postDelayed(positionReporter, 500L)
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
    mediaSession

  override fun onTaskRemoved(rootIntent: Intent?) {
    val player = mediaSession?.player
    if (
      player == null ||
        !player.playWhenReady ||
        player.mediaItemCount == 0 ||
        player.playbackState == Player.STATE_ENDED
    ) {
      stopSelf()
    }
  }

  override fun onDestroy() {
    positionHandler.removeCallbacks(positionReporter)
    controllerBridge?.release()
    controllerBridge = null
    MornicaRuntime.router.attachAudioController(null)
    mediaSession?.run {
      player.release()
      release()
    }
    mediaSession = null
    exoPlayer = null
    super.onDestroy()
  }
}

