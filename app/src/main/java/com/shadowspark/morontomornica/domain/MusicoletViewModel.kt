package com.shadowspark.morontomornica.domain

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.shadowspark.morontomornica.billing.ProPlanId
import com.shadowspark.morontomornica.billing.morontoBillingProvider
import com.shadowspark.morontomornica.i18n.AppLanguage
import com.shadowspark.morontomornica.model.AdConsentStatus
import com.shadowspark.morontomornica.model.CrossfadeSettings
import com.shadowspark.morontomornica.model.CustomPlaylist
import com.shadowspark.morontomornica.model.DEFAULT_PLAYLISTS
import com.shadowspark.morontomornica.model.DEFAULT_QUEUES
import com.shadowspark.morontomornica.model.EQ_BAND_LABELS
import com.shadowspark.morontomornica.model.EQ_PRESETS
import com.shadowspark.morontomornica.model.EqualizerSettings
import com.shadowspark.morontomornica.model.LibraryViewTab
import com.shadowspark.morontomornica.model.MORONTO_THEMES
import com.shadowspark.morontomornica.model.OfflineTrack
import com.shadowspark.morontomornica.model.PlaybackQueue
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.STUDIO_TEMPLATES
import com.shadowspark.morontomornica.model.SortOption
import com.shadowspark.morontomornica.model.SponsorPledge
import com.shadowspark.morontomornica.model.TrackSourceType
import com.shadowspark.morontomornica.model.bundledOfflineTracks
import kotlin.math.pow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class MusicoletUiState(
  val selectedTab: LibraryViewTab = LibraryViewTab.TRACKS,
  val searchQuery: String = "",
  val sortOption: SortOption = SortOption.TITLE,
  val tracks: List<OfflineTrack> = emptyList(),
  val queues: List<PlaybackQueue> = DEFAULT_QUEUES,
  val activeQueueId: String = DEFAULT_QUEUES.first().id,
  val playlists: List<CustomPlaylist> = DEFAULT_PLAYLISTS,
  val favoriteTrackIds: List<String> = emptyList(),
  val isPlaying: Boolean = false,
  val positionMs: Long = 0L,
  val durationMs: Long = 30_000L,
  val playbackRate: Float = 1.0f,
  val shuffleEnabled: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.OFF,
  val equalizer: EqualizerSettings = EqualizerSettings(),
  val crossfade: CrossfadeSettings = CrossfadeSettings(),
  val sleepTimerMinutes: Int? = null,
  val sleepTimerRemainingSec: Int? = null,
  val isNowPlayingOpen: Boolean = false,
  val nowPlayingTab: Int = 0, // 0=Player, 1=Lyrics, 2=Queue
  val isEqDialogOpen: Boolean = false,
  val audioDialogTab: Int = 0, // 0=Equalizer, 1=Crossfade
  val isSleepTimerDialogOpen: Boolean = false,
  val editingTrackId: String? = null,
  val sharingTrackId: String? = null,
  val statusMessage: String? = null,
  val showSplash: Boolean = true,
  val sponsorPledges: List<SponsorPledge> = emptyList(),
  val blogNotifyEmail: String? = null,
  val liveNotifyEmail: String? = null,
  val proNotifyEmail: String? = null,
  val language: AppLanguage = AppLanguage.EN,
  val themeId: String = "emerald-console",
  val isPro: Boolean = false,
  val proPlan: ProPlanId? = null,
  val eqRewardUnlockExpiresAtMs: Long? = null,
  val isRewardedAdDialogOpen: Boolean = false,
  val adConsentStatus: AdConsentStatus = AdConsentStatus.UNSET,
  val isAdConsentDialogOpen: Boolean = false,
  val dataSaverEnabled: Boolean = false,
  val hasCompletedOnboarding: Boolean = false,
  val sessionCount: Int = 1,
  val hasDismissedOrRated: Boolean = false,
  val isRatingPromptOpen: Boolean = false,
) {
  val activeQueue: PlaybackQueue
    get() = queues.find { it.id == activeQueueId } ?: queues.first()

  val activeTrack: OfflineTrack?
    get() {
      val q = activeQueue
      if (q.trackIds.isEmpty()) return null
      val candidateId = q.currentTrackId ?: q.trackIds.first()
      return tracks.find { it.id == candidateId } ?: tracks.find { it.id == q.trackIds.first() }
    }
}

class MusicoletViewModel(application: Application) : AndroidViewModel(application) {
  private val prefs =
    application.getSharedPreferences("mornica_musicolet_prefs_v1", Context.MODE_PRIVATE)

  private val deckPlayers: List<ExoPlayer> = listOf(
    ExoPlayer.Builder(application).build(),
    ExoPlayer.Builder(application).build(),
  )
  private var activeDeckIndex: Int = 0
  private val player: ExoPlayer
    get() = deckPlayers[activeDeckIndex]
  private val standbyPlayer: ExoPlayer
    get() = deckPlayers[1 - activeDeckIndex]

  private var audioEq: Equalizer? = null
  private var bassBoost: BassBoost? = null
  private var virtualizer: Virtualizer? = null
  private var loudnessEnhancer: LoudnessEnhancer? = null
  private var eqBoundSessionId: Int = 0
  private var sleepJob: Job? = null
  private var positionJob: Job? = null
  private var crossfadeJob: Job? = null
  private var pendingStartPosMs: Long = 0L
  private var crossfadeTriggeredForTrack: Boolean = false

  private val tagOverrides = mutableMapOf<String, TrackTagPatch>()

  private val _uiState = MutableStateFlow(MusicoletUiState())
  val uiState: StateFlow<MusicoletUiState> = _uiState.asStateFlow()

  init {
    loadTagOverrides()
    val savedQueues = loadQueues().ifEmpty { DEFAULT_QUEUES }
    val savedActiveId = prefs.getString("active_queue_id", savedQueues.first().id)
    val activeQ = savedQueues.find { it.id == savedActiveId } ?: savedQueues.first()
    val savedPlaylists = loadPlaylists()
    val savedFavs = loadStringList("favorites")
    val savedEq = loadEqualizer()
    val savedCrossfade = loadCrossfade()
    val savedPledges = loadSponsorPledges()
    val savedBlogEmail = prefs.getString("blog_notify_email", null)
    val savedLiveEmail = prefs.getString("live_notify_email", null)
    val savedProEmail = prefs.getString("pro_notify_email", null)
    val savedLang = AppLanguage.fromCode(prefs.getString("moronto_lang", "en"))
    val savedProPlan = ProPlanId.fromKey(prefs.getString("moronto_pro_plan", null))
    val isPro = savedProPlan != null
    val savedTheme = prefs.getString("moronto_theme_id", "emerald-console") ?: "emerald-console"
    val themeId = if (MORONTO_THEMES.any { it.id == savedTheme }) savedTheme else "emerald-console"
    val rewardExpRaw = prefs.getLong("moronto_eq_reward_exp", -1L)
    val eqRewardExp = if (rewardExpRaw > 0L) rewardExpRaw else null
    val adConsent = AdConsentStatus.fromKey(prefs.getString("moronto_ad_consent", "unset"))
    val dataSaver = prefs.getBoolean("moronto_data_saver", false)
    val onboardingDone = prefs.getBoolean("moronto_onboarding_done", false)
    val prevSessions = prefs.getInt("moronto_session_count", 0)
    val sessionCount = prevSessions + 1
    prefs.edit().putInt("moronto_session_count", sessionCount).apply()
    val ratingDone = prefs.getBoolean("moronto_rating_done", false)

    val baseTracks = bundledOfflineTracks(application.packageName) + loadImportedTracks()
    val mergedTracks = applyAllTagOverrides(baseTracks)
    val initialTrack =
      if (activeQ.trackIds.isEmpty()) null
      else mergedTracks.find { it.id == activeQ.currentTrackId }
        ?: mergedTracks.find { it.id == activeQ.trackIds.first() }

    _uiState.value =
      MusicoletUiState(
        tracks = mergedTracks,
        queues = savedQueues,
        activeQueueId = activeQ.id,
        playlists = savedPlaylists,
        favoriteTrackIds = savedFavs,
        positionMs = activeQ.positionMs,
        durationMs = initialTrack?.durationMs ?: 30_000L,
        equalizer = savedEq,
        crossfade = savedCrossfade,
        showSplash = true,
        sponsorPledges = savedPledges,
        blogNotifyEmail = savedBlogEmail,
        liveNotifyEmail = savedLiveEmail,
        proNotifyEmail = savedProEmail,
        language = savedLang,
        themeId = themeId,
        isPro = isPro,
        proPlan = savedProPlan,
        eqRewardUnlockExpiresAtMs = eqRewardExp,
        adConsentStatus = adConsent,
        isAdConsentDialogOpen = false,
        dataSaverEnabled = dataSaver,
        hasCompletedOnboarding = onboardingDone,
        sessionCount = sessionCount,
        hasDismissedOrRated = ratingDone,
        isRatingPromptOpen = MusicoletEngine.shouldShowRatingPrompt(sessionCount, ratingDone),
      )

    viewModelScope.launch {
      delay(1_400L)
      _uiState.update { it.copy(showSplash = false) }
    }

    deckPlayers.forEachIndexed { deckIdx, exo ->
      exo.addListener(
        object : Player.Listener {
          override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (deckIdx != activeDeckIndex) return
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
              attachEqualizerIfReady()
              startPositionLoop()
            } else {
              stopPositionLoop()
              persistQueues()
            }
          }

          override fun onPlaybackStateChanged(playbackState: Int) {
            if (deckIdx != activeDeckIndex) {
              if (playbackState == Player.STATE_ENDED) {
                exo.pause()
                exo.seekTo(0L)
              }
              return
            }
            if (playbackState == Player.STATE_READY) {
              if (pendingStartPosMs > 0L) {
                val maxDur = exo.duration.takeIf { it > 0L } ?: pendingStartPosMs
                exo.seekTo(pendingStartPosMs.coerceIn(0L, maxDur))
                pendingStartPosMs = 0L
              }
              val dur = exo.duration.takeIf { it > 0L } ?: _uiState.value.durationMs
              _uiState.update { it.copy(durationMs = dur) }
              attachEqualizerIfReady()
            } else if (playbackState == Player.STATE_ENDED) {
              if (!crossfadeTriggeredForTrack) {
                skipNext(isAutoAdvance = true)
              }
            }
          }

          override fun onPlayerError(error: PlaybackException) {
            if (deckIdx != activeDeckIndex) return
            _uiState.update {
              it.copy(
                isPlaying = false,
                statusMessage = "Unable to decode local audio file.",
              )
            }
          }
        },
      )
    }
  }

  private fun startPositionLoop() {
    positionJob?.cancel()
    positionJob = viewModelScope.launch {
      while (true) {
        val active = player
        val pos = active.currentPosition.coerceAtLeast(0L)
        val dur = active.duration.takeIf { it > 0L } ?: _uiState.value.durationMs
        _uiState.update { prev ->
          prev.copy(
            positionMs = pos,
            durationMs = dur,
            queues = prev.queues.map { q ->
              if (q.id == prev.activeQueueId) q.copy(positionMs = pos) else q
            },
          )
        }
        val snapshot = _uiState.value
        if (!crossfadeTriggeredForTrack &&
          snapshot.isPlaying &&
          snapshot.crossfade.enabled &&
          snapshot.crossfade.durationSec > 0 &&
          snapshot.repeatMode != RepeatMode.ONE
        ) {
          val triggerMs = MusicoletEngine.computeCrossfadeTriggerMs(dur, snapshot.crossfade)
          if (triggerMs > 0L && dur > 0L && (dur - pos) <= triggerMs) {
            val step = MusicoletEngine.computeNextQueueTrack(
              queue = snapshot.activeQueue,
              repeatMode = snapshot.repeatMode,
              shuffleEnabled = snapshot.shuffleEnabled,
              isAutoAdvance = true,
            )
            if (!step.shouldStop && step.nextTrackId != null) {
              crossfadeTriggeredForTrack = true
              skipNext(isAutoAdvance = true)
            }
          }
        }
        delay(250L)
      }
    }
  }

  private fun stopPositionLoop() {
    positionJob?.cancel()
    positionJob = null
  }

  private fun baseDeckVolume(settings: EqualizerSettings = _uiState.value.equalizer): Float {
    if (!settings.enabled || settings.preampDb >= 0) return 1.0f
    return 10.0.pow(settings.preampDb.coerceIn(-12, 0) / 20.0).toFloat().coerceIn(0.25f, 1.0f)
  }

  private fun attachEqualizerIfReady() {
    val sessionId = player.audioSessionId
    if (sessionId == 0) return
    if (sessionId != eqBoundSessionId) {
      runCatching { audioEq?.release() }
      runCatching { bassBoost?.release() }
      runCatching { virtualizer?.release() }
      runCatching { loudnessEnhancer?.release() }
      audioEq = runCatching { Equalizer(0, sessionId) }.getOrNull()
      bassBoost = runCatching { BassBoost(0, sessionId) }.getOrNull()
      virtualizer = runCatching { Virtualizer(0, sessionId) }.getOrNull()
      loudnessEnhancer = runCatching { LoudnessEnhancer(sessionId) }.getOrNull()
      eqBoundSessionId = sessionId
    }
    applyHardwareEqualizer(_uiState.value.equalizer)
  }

  private fun applyHardwareEqualizer(settings: EqualizerSettings) {
    val clamped = MusicoletEngine.clampEqualizerSettings(settings)
    if (crossfadeJob?.isActive != true) {
      player.volume = baseDeckVolume(clamped)
    }
    runCatching {
      audioEq?.let { eq ->
        eq.enabled = clamped.enabled
        val range = eq.bandLevelRange
        val minLevel = range[0].toInt()
        val maxLevel = range[1].toInt()
        val hwBands = eq.numberOfBands.toInt().coerceAtLeast(1)
        val uiBands = clamped.bandsDb
        for (i in 0 until hwBands) {
          val dbValue = when {
            hwBands == uiBands.size -> uiBands[i]
            hwBands == 5 && uiBands.size == 10 -> (uiBands[i * 2] + uiBands[i * 2 + 1]) / 2
            else -> {
              val mappedIdx = ((i.toFloat() / hwBands.toFloat()) * uiBands.size.toFloat())
                .toInt()
                .coerceIn(0, uiBands.lastIndex)
              uiBands[mappedIdx]
            }
          }
          val targetMb = (dbValue * 100).coerceIn(minLevel, maxLevel).toShort()
          eq.setBandLevel(i.toShort(), targetMb)
        }
      }
      bassBoost?.let { bb ->
        bb.enabled = clamped.enabled && clamped.bassBoostPercent > 0
        val strength = ((clamped.bassBoostPercent / 100f) * 1000f).toInt().toShort()
        bb.setStrength(strength)
      }
      virtualizer?.let { virt ->
        virt.enabled = clamped.enabled && clamped.virtualizerPercent > 0
        val strength = ((clamped.virtualizerPercent / 100f) * 1000f).toInt().toShort()
        virt.setStrength(strength)
      }
      loudnessEnhancer?.let { le ->
        val boostMb = if (clamped.enabled && clamped.preampDb > 0) clamped.preampDb * 100 else 0
        le.setTargetGain(boostMb)
        le.enabled = boostMb > 0
      }
    }
  }

  fun selectTab(tab: LibraryViewTab) {
    _uiState.update { it.copy(selectedTab = tab) }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun setSortOption(sortOption: SortOption) {
    _uiState.update { it.copy(sortOption = sortOption) }
  }

  fun dismissStatus() {
    _uiState.update { it.copy(statusMessage = null) }
  }

  fun setNowPlayingOpen(open: Boolean) {
    _uiState.update { it.copy(isNowPlayingOpen = open) }
  }

  fun setNowPlayingTab(tab: Int) {
    _uiState.update { it.copy(nowPlayingTab = tab) }
  }

  fun setEqDialogOpen(open: Boolean) {
    _uiState.update { it.copy(isEqDialogOpen = open) }
  }

  fun openAudioDialog(tab: Int) {
    _uiState.update {
      it.copy(
        isEqDialogOpen = true,
        audioDialogTab = tab.coerceIn(0, 1),
      )
    }
  }

  fun setAudioDialogTab(tab: Int) {
    _uiState.update { it.copy(audioDialogTab = tab.coerceIn(0, 1)) }
  }

  fun setSleepTimerDialogOpen(open: Boolean) {
    _uiState.update { it.copy(isSleepTimerDialogOpen = open) }
  }

  fun setEditingTrackId(trackId: String?) {
    _uiState.update { it.copy(editingTrackId = trackId) }
  }

  fun setSharingTrackId(trackId: String?) {
    _uiState.update { it.copy(sharingTrackId = trackId) }
  }

  fun dismissSplash() {
    _uiState.update { it.copy(showSplash = false) }
  }

  fun replaySplash() {
    _uiState.update { it.copy(showSplash = true) }
    viewModelScope.launch {
      delay(1_400L)
      _uiState.update { it.copy(showSplash = false) }
    }
  }

  fun setLanguage(language: AppLanguage) {
    prefs.edit().putString("moronto_lang", language.code).apply()
    _uiState.update {
      it.copy(
        language = language,
        statusMessage = "Language switched to ${language.label}.",
      )
    }
  }

  fun setThemeId(themeId: String) {
    val opt = MORONTO_THEMES.find { it.id == themeId } ?: return
    prefs.edit().putString("moronto_theme_id", themeId).apply()
    _uiState.update {
      it.copy(
        themeId = themeId,
        statusMessage = "Theme updated to ${opt.name}.",
      )
    }
  }

  fun purchaseProPlan(planId: ProPlanId) {
    val res = morontoBillingProvider.purchasePlan(planId)
    if (!res.success) return
    prefs.edit().putString("moronto_pro_plan", res.planId.key).apply()
    _uiState.update {
      it.copy(
        isPro = true,
        proPlan = res.planId,
        isAdConsentDialogOpen = false,
        statusMessage = res.message,
      )
    }
  }

  fun restoreProPurchases() {
    val stored = ProPlanId.fromKey(prefs.getString("moronto_pro_plan", null))
    val res = morontoBillingProvider.restorePurchases(stored)
    _uiState.update {
      it.copy(
        isPro = res.restored,
        proPlan = res.activePlan,
        statusMessage = res.message,
      )
    }
  }

  fun grantRewardedEqUnlock() {
    val expiresAt = System.currentTimeMillis() + 24L * 60L * 60L * 1_000L
    prefs.edit().putLong("moronto_eq_reward_exp", expiresAt).apply()
    _uiState.update {
      it.copy(
        eqRewardUnlockExpiresAtMs = expiresAt,
        isRewardedAdDialogOpen = false,
        statusMessage = "All 10-band EQ presets unlocked for 24 hours.",
      )
    }
  }

  fun setRewardedAdDialogOpen(open: Boolean) {
    _uiState.update { it.copy(isRewardedAdDialogOpen = open) }
  }

  fun setAdConsent(status: AdConsentStatus) {
    prefs.edit().putString("moronto_ad_consent", status.key).apply()
    _uiState.update {
      it.copy(
        adConsentStatus = status,
        isAdConsentDialogOpen = false,
        statusMessage = if (status == AdConsentStatus.PERSONALIZED) {
          "Saved ad preference: Personalized AdMob test ads."
        } else {
          "Saved ad preference: Non-personalized privacy ads."
        },
      )
    }
  }

  fun setAdConsentDialogOpen(open: Boolean) {
    _uiState.update { it.copy(isAdConsentDialogOpen = open) }
  }

  fun toggleDataSaver() {
    _uiState.update { prev ->
      val next = !prev.dataSaverEnabled
      prefs.edit().putBoolean("moronto_data_saver", next).apply()
      prev.copy(
        dataSaverEnabled = next,
        statusMessage = if (next) {
          "Data-Saver Mode enabled — purely offline assets active."
        } else {
          "Data-Saver Mode disabled."
        },
      )
    }
  }

  fun completeOnboarding() {
    prefs.edit().putBoolean("moronto_onboarding_done", true).apply()
    _uiState.update { prev ->
      prev.copy(
        hasCompletedOnboarding = true,
        isAdConsentDialogOpen = false,
      )
    }
  }

  fun replayOnboarding() {
    _uiState.update { it.copy(hasCompletedOnboarding = false) }
  }

  fun submitRating(stars: Int, comment: String) {
    prefs.edit().putBoolean("moronto_rating_done", true).apply()
    _uiState.update {
      it.copy(
        hasDismissedOrRated = true,
        isRatingPromptOpen = false,
        statusMessage = "Thank you for rating Moronto Music $stars/5" +
          if (comment.trim().isNotEmpty()) " — feedback recorded locally." else ".",
      )
    }
  }

  fun dismissRatingPrompt() {
    prefs.edit().putBoolean("moronto_rating_done", true).apply()
    _uiState.update {
      it.copy(
        hasDismissedOrRated = true,
        isRatingPromptOpen = false,
      )
    }
  }

  fun triggerTestFifthSessionPrompt() {
    prefs.edit()
      .putInt("moronto_session_count", 5)
      .putBoolean("moronto_rating_done", false)
      .apply()
    _uiState.update {
      it.copy(
        sessionCount = 5,
        hasDismissedOrRated = false,
        isRatingPromptOpen = true,
      )
    }
  }

  fun applyStudioTemplate(templateId: String) {
    val tpl = STUDIO_TEMPLATES.find { it.id == templateId } ?: return
    if (_uiState.value.queues.any { it.id == tpl.queueId }) {
      switchQueue(tpl.queueId, autoPlay = false)
    }
    applyEqPreset(tpl.eqPresetId, bypassGate = true)
    updateCrossfade(
      _uiState.value.crossfade.copy(
        enabled = tpl.crossfadeEnabled && tpl.crossfadeSec > 0,
        durationSec = tpl.crossfadeSec,
      ),
    )
    _uiState.update {
      it.copy(
        statusMessage = "Applied studio template '${tpl.name}' (10-band EQ + Crossfade active).",
      )
    }
  }

  fun playSmartGenrePlaylist(genre: String) {
    val groups = MusicoletEngine.buildGenreSmartPlaylists(_uiState.value.tracks)
    val group = groups.find { it.genre == genre }
    if (group == null || group.tracks.isEmpty()) {
      _uiState.update {
        it.copy(statusMessage = "No $genre tracks yet. Import audio or tag a folder as '$genre'.")
      }
      return
    }
    playTrackListAsActiveQueue(group.tracks.map { it.id })
    _uiState.update {
      it.copy(statusMessage = "Playing $genre Smart Playlist (${group.tracks.size} tracks).")
    }
  }

  fun submitSponsorPledge(
    name: String,
    email: String,
    tierId: String,
    billing: String,
    note: String,
  ): String? {
    val (pledge, err) = MusicoletEngine.validateSponsorPledge(name, email, tierId, billing, note)
    if (pledge == null || err != null) return err ?: "Invalid sponsorship details."
    _uiState.update { prev ->
      val next = (listOf(pledge) + prev.sponsorPledges).take(20)
      saveSponsorPledges(next)
      prev.copy(
        sponsorPledges = next,
        statusMessage = "Recorded sponsorship pledge from ${pledge.name} ($${pledge.amountUsd}).",
      )
    }
    return null
  }

  fun submitBlogNotifyEmail(email: String): String? {
    val (normalized, err) = MusicoletEngine.validateBlogNotifyEmail(email)
    if (normalized == null || err != null) return err ?: "Invalid email address."
    prefs.edit().putString("blog_notify_email", normalized).apply()
    _uiState.update {
      it.copy(
        blogNotifyEmail = normalized,
        statusMessage = "Subscribed $normalized for Blog launch updates.",
      )
    }
    return null
  }

  fun clearBlogNotifyEmail() {
    prefs.edit().remove("blog_notify_email").apply()
    _uiState.update {
      it.copy(
        blogNotifyEmail = null,
        statusMessage = "Removed email from Blog launch waitlist.",
      )
    }
  }

  fun submitLiveNotifyEmail(email: String): String? {
    val (normalized, err) = MusicoletEngine.validateLiveNotifyEmail(email)
    if (normalized == null || err != null) return err ?: "Invalid email address."
    prefs.edit().putString("live_notify_email", normalized).apply()
    _uiState.update {
      it.copy(
        liveNotifyEmail = normalized,
        statusMessage = "Subscribed $normalized for Moronto Live Streaming alerts.",
      )
    }
    return null
  }

  fun clearLiveNotifyEmail() {
    prefs.edit().remove("live_notify_email").apply()
    _uiState.update {
      it.copy(
        liveNotifyEmail = null,
        statusMessage = "Removed email from Live Streaming waitlist.",
      )
    }
  }

  fun submitProNotifyEmail(email: String): String? {
    val (normalized, err) = MusicoletEngine.validateProStreamingNotifyEmail(email)
    if (normalized == null || err != null) return err ?: "Invalid email address."
    prefs.edit().putString("pro_notify_email", normalized).apply()
    _uiState.update {
      it.copy(
        proNotifyEmail = normalized,
        statusMessage = "Added $normalized to the Global Catalog Streaming Pro waitlist.",
      )
    }
    return null
  }

  fun clearProNotifyEmail() {
    prefs.edit().remove("pro_notify_email").apply()
    _uiState.update {
      it.copy(
        proNotifyEmail = null,
        statusMessage = "Removed email from Pro waitlist.",
      )
    }
  }

  fun shareTrackWhatsAppCard(track: OfflineTrack) {
    val app = getApplication<Application>()
    runCatching {
      val bmp = renderWhatsAppStatusBitmap(track)
      val fileName = "moronto_status_${track.title.lowercase().replace(Regex("[^a-z0-9]+"), "_")}.png"
      val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/MorontoMusic")
          put(MediaStore.Images.Media.IS_PENDING, 1)
        }
      }
      val resolver = app.contentResolver
      val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
      if (uri != null) {
        resolver.openOutputStream(uri)?.use { out ->
          bmp.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val publish = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
          resolver.update(uri, publish, null, null)
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
          type = "image/png"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(
            Intent.EXTRA_TEXT,
            "Listening to ${track.title} by ${track.artist} offline on Moronto Music.",
          )
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share WhatsApp Status Card").apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        app.startActivity(chooser)
        _uiState.update {
          it.copy(
            sharingTrackId = null,
            statusMessage = "Generated 1080×1920 WhatsApp Status card for '${track.title}'.",
          )
        }
      } else {
        _uiState.update { it.copy(statusMessage = "Could not create MediaStore image URI.") }
      }
    }.onFailure {
      _uiState.update { it.copy(statusMessage = "Unable to launch system Share Sheet on this device.") }
    }
  }

  private fun renderWhatsAppStatusBitmap(track: OfflineTrack): Bitmap {
    val w = 1080
    val h = 1920
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      shader = LinearGradient(
        0f,
        0f,
        w.toFloat(),
        h.toFloat(),
        intArrayOf(0xFF071321.toInt(), 0xFF0E2A3A.toInt(), 0xFF071321.toInt()),
        null,
        Shader.TileMode.CLAMP,
      )
    }
    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bgPaint)

    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xEE102033.toInt() }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 4f
      color = 0xFF12D9A1.toInt()
    }
    val rect = RectF(84f, 260f, w - 84f, h - 260f)
    canvas.drawRoundRect(rect, 48f, 48f, cardPaint)
    canvas.drawRoundRect(rect, 48f, 48f, borderPaint)

    val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF12D9A1.toInt()
      textSize = 44f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("MORONTO MUSIC · OFFLINE STUDIO", 148f, 380f, brandPaint)

    val discBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF062E22.toInt() }
    val discStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 6f
      color = 0xFF12D9A1.toInt()
    }
    canvas.drawCircle(w / 2f, 780f, 210f, discBg)
    canvas.drawCircle(w / 2f, 780f, 210f, discStroke)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFFF4FAFE.toInt()
      textSize = 68f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(track.title.take(26), w / 2f, 1140f, titlePaint)

    val artistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFF20B6EB.toInt()
      textSize = 44f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(track.artist.take(32), w / 2f, 1220f, artistPaint)

    val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFFA5B6CA.toInt()
      textSize = 32f
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(
      "${track.album} · ${track.folderName} · ${MusicoletEngine.formatDurationMs(track.durationMs)}",
      w / 2f,
      1290f,
      metaPaint,
    )

    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = 0xFFFFBE55.toInt()
      textSize = 30f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
    }
    canvas.drawText(
      "10-Band EQ · Zero Data · Dual-Deck Crossfade",
      w / 2f,
      1540f,
      footerPaint,
    )
    return bmp
  }

  fun playTrackInActiveQueue(
    trackId: String,
    startPositionMs: Long = 0L,
    useCrossfadeIfPlaying: Boolean = false,
  ) {
    val track = _uiState.value.tracks.find { it.id == trackId } ?: return
    val shouldCrossfade =
      useCrossfadeIfPlaying &&
        _uiState.value.isPlaying &&
        _uiState.value.crossfade.enabled &&
        _uiState.value.crossfade.durationSec > 0
    _uiState.update { prev ->
      val nextQueues = prev.queues.map { q ->
        if (q.id != prev.activeQueueId) q
        else {
          val nextIds = if (trackId in q.trackIds) q.trackIds else q.trackIds + trackId
          q.copy(trackIds = nextIds, currentTrackId = trackId, positionMs = startPositionMs)
        }
      }
      prev.copy(
        queues = nextQueues,
        positionMs = startPositionMs,
        durationMs = track.durationMs,
        statusMessage = null,
      )
    }
    persistQueues()
    if (shouldCrossfade) {
      crossfadeToTrack(track, startPositionMs)
    } else {
      loadAndPlay(track, startPositionMs, autoPlay = true)
    }
  }

  fun playTrackListAsActiveQueue(trackIds: List<String>, startTrackId: String? = null) {
    if (trackIds.isEmpty()) return
    val targetId = startTrackId ?: trackIds.first()
    val track = _uiState.value.tracks.find { it.id == targetId } ?: return
    _uiState.update { prev ->
      val nextQueues = prev.queues.map { q ->
        if (q.id == prev.activeQueueId) {
          q.copy(trackIds = trackIds, currentTrackId = targetId, positionMs = 0L)
        } else {
          q
        }
      }
      prev.copy(queues = nextQueues, positionMs = 0L, durationMs = track.durationMs)
    }
    persistQueues()
    loadAndPlay(track, 0L, autoPlay = true)
  }

  private fun loadAndPlay(track: OfflineTrack, startPositionMs: Long, autoPlay: Boolean) {
    crossfadeJob?.cancel()
    standbyPlayer.pause()
    crossfadeTriggeredForTrack = false
    pendingStartPosMs = startPositionMs.coerceAtLeast(0L)
    player.volume = baseDeckVolume()
    player.playbackParameters = PlaybackParameters(_uiState.value.playbackRate)
    player.setMediaItem(MediaItem.fromUri(track.uri))
    player.prepare()
    player.playWhenReady = autoPlay
  }

  private fun crossfadeToTrack(track: OfflineTrack, startPositionMs: Long) {
    crossfadeJob?.cancel()
    val outgoing = player
    val incomingIdx = 1 - activeDeckIndex
    val incoming = deckPlayers[incomingIdx]
    val targetVol = baseDeckVolume()
    val overlapMs = MusicoletEngine
      .computeCrossfadeTriggerMs(_uiState.value.durationMs, _uiState.value.crossfade)
      .coerceIn(1_000L, 12_000L)

    activeDeckIndex = incomingIdx
    crossfadeTriggeredForTrack = false
    pendingStartPosMs = startPositionMs.coerceAtLeast(0L)

    incoming.volume = 0f
    incoming.playbackParameters = PlaybackParameters(_uiState.value.playbackRate)
    incoming.setMediaItem(MediaItem.fromUri(track.uri))
    incoming.prepare()
    incoming.playWhenReady = true
    attachEqualizerIfReady()

    crossfadeJob = viewModelScope.launch {
      val stepMs = 40L
      val steps = (overlapMs / stepMs).toInt().coerceAtLeast(1)
      val outgoingStartVol = outgoing.volume.coerceIn(0f, targetVol)
      for (i in 1..steps) {
        delay(stepMs)
        val progress = (i.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
        outgoing.volume = outgoingStartVol * (1f - progress)
        incoming.volume = targetVol * progress
      }
      outgoing.pause()
      outgoing.seekTo(0L)
      outgoing.volume = targetVol
      incoming.volume = targetVol
    }
  }

  fun togglePlayPause() {
    if (_uiState.value.isPlaying) {
      crossfadeJob?.cancel()
      standbyPlayer.pause()
      player.pause()
      persistQueues()
      return
    }
    val track = _uiState.value.activeTrack ?: return
    if (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) {
      player.volume = baseDeckVolume()
      player.play()
    } else {
      loadAndPlay(track, _uiState.value.positionMs, autoPlay = true)
    }
  }

  fun seekTo(positionMs: Long) {
    val clamped = positionMs.coerceIn(0L, _uiState.value.durationMs.coerceAtLeast(1_000L))
    crossfadeTriggeredForTrack = false
    player.seekTo(clamped)
    _uiState.update { prev ->
      prev.copy(
        positionMs = clamped,
        queues = prev.queues.map { q ->
          if (q.id == prev.activeQueueId) q.copy(positionMs = clamped) else q
        },
      )
    }
    persistQueues()
  }

  fun skipNext(isAutoAdvance: Boolean = false) {
    val state = _uiState.value
    val step = MusicoletEngine.computeNextQueueTrack(
      queue = state.activeQueue,
      repeatMode = state.repeatMode,
      shuffleEnabled = state.shuffleEnabled,
      isAutoAdvance = isAutoAdvance,
    )
    if (step.shouldStop || step.nextTrackId == null) {
      crossfadeJob?.cancel()
      standbyPlayer.pause()
      player.pause()
      player.seekTo(0L)
      _uiState.update { it.copy(positionMs = 0L, statusMessage = "Reached end of queue.") }
      persistQueues()
      return
    }
    val useFade =
      state.crossfade.enabled &&
        state.crossfade.durationSec > 0 &&
        (isAutoAdvance || state.crossfade.fadeOnManualSkip) &&
        !step.restartCurrent
    playTrackInActiveQueue(step.nextTrackId, 0L, useCrossfadeIfPlaying = useFade)
  }

  fun skipPrevious() {
    val state = _uiState.value
    val step = MusicoletEngine.computePreviousQueueTrack(
      queue = state.activeQueue,
      repeatMode = state.repeatMode,
      positionMs = state.positionMs,
    )
    val prevId = step.prevTrackId ?: return
    val useFade =
      state.crossfade.enabled &&
        state.crossfade.durationSec > 0 &&
        state.crossfade.fadeOnManualSkip &&
        !step.restartCurrent
    playTrackInActiveQueue(prevId, 0L, useCrossfadeIfPlaying = useFade)
  }

  fun toggleShuffle() {
    _uiState.update { prev ->
      val next = !prev.shuffleEnabled
      prev.copy(
        shuffleEnabled = next,
        statusMessage = if (next) "Shuffle enabled." else "Shuffle disabled.",
      )
    }
  }

  fun cycleRepeatMode() {
    _uiState.update { prev ->
      val next = when (prev.repeatMode) {
        RepeatMode.OFF -> RepeatMode.ALL
        RepeatMode.ALL -> RepeatMode.ONE
        RepeatMode.ONE -> RepeatMode.OFF
      }
      prev.copy(repeatMode = next)
    }
  }

  fun setPlaybackRate(rate: Float) {
    val clamped = rate.coerceIn(0.5f, 2.0f)
    player.playbackParameters = PlaybackParameters(clamped)
    _uiState.update { it.copy(playbackRate = clamped) }
  }

  fun switchQueue(targetQueueId: String, autoPlay: Boolean = false) {
    if (targetQueueId == _uiState.value.activeQueueId) return
    player.pause()
    val state = _uiState.value
    val res = MusicoletEngine.switchQueueWithResume(
      queues = state.queues,
      activeQueueId = state.activeQueueId,
      targetQueueId = targetQueueId,
      currentPositionMs = state.positionMs,
    )
    val targetTrack =
      if (res.activeQueue.trackIds.isEmpty()) null
      else state.tracks.find { it.id == res.activeQueue.currentTrackId }
        ?: state.tracks.find { it.id == res.activeQueue.trackIds.first() }

    _uiState.update { prev ->
      prev.copy(
        queues = res.queues,
        activeQueueId = res.activeQueue.id,
        positionMs = res.activeQueue.positionMs,
        durationMs = targetTrack?.durationMs ?: 30_000L,
        statusMessage = "Switched to ${res.activeQueue.name}",
      )
    }
    persistQueues()
    if (targetTrack != null) {
      loadAndPlay(targetTrack, res.activeQueue.positionMs, autoPlay = autoPlay)
    }
  }

  fun createQueue(name: String): Boolean {
    val res = MusicoletEngine.createNewQueue(_uiState.value.queues, name)
    if (res.created == null) {
      _uiState.update { it.copy(statusMessage = res.error ?: "Could not create queue.") }
      return false
    }
    _uiState.update {
      it.copy(queues = res.queues, statusMessage = "Created queue '${res.created.name}'.")
    }
    persistQueues()
    return true
  }

  fun deleteQueue(queueId: String) {
    val state = _uiState.value
    val res = MusicoletEngine.deleteQueueById(state.queues, state.activeQueueId, queueId)
    if (!res.deleted) {
      _uiState.update { it.copy(statusMessage = "At least one queue must remain active.") }
      return
    }
    val switched = res.nextActiveQueueId != state.activeQueueId
    if (switched) player.pause()
    val nextActive = res.queues.find { it.id == res.nextActiveQueueId } ?: res.queues.first()
    _uiState.update {
      it.copy(
        queues = res.queues,
        activeQueueId = nextActive.id,
        positionMs = nextActive.positionMs,
        statusMessage = "Queue removed.",
      )
    }
    persistQueues()
  }

  fun addTracksToQueue(queueId: String, trackIds: List<String>, playNext: Boolean = false) {
    val target = _uiState.value.queues.find { it.id == queueId } ?: return
    val nextQueues = MusicoletEngine.addTracksToQueue(_uiState.value.queues, queueId, trackIds, playNext)
    _uiState.update {
      it.copy(
        queues = nextQueues,
        statusMessage = if (playNext) "Added to play next in '${target.name}'."
        else "Added ${trackIds.size} track(s) to '${target.name}'.",
      )
    }
    persistQueues()
  }

  fun removeFromQueue(queueId: String, trackId: String) {
    val nextQueues = MusicoletEngine.removeTrackFromQueue(_uiState.value.queues, queueId, trackId)
    _uiState.update { it.copy(queues = nextQueues) }
    persistQueues()
  }

  fun reorderInQueue(queueId: String, fromIdx: Int, toIdx: Int) {
    val nextQueues = MusicoletEngine.moveQueueTrack(_uiState.value.queues, queueId, fromIdx, toIdx)
    _uiState.update { it.copy(queues = nextQueues) }
    persistQueues()
  }

  fun toggleFavorite(trackId: String) {
    _uiState.update { prev ->
      val next = if (trackId in prev.favoriteTrackIds) prev.favoriteTrackIds - trackId
      else prev.favoriteTrackIds + trackId
      saveStringList("favorites", next)
      prev.copy(favoriteTrackIds = next)
    }
  }

  fun createPlaylist(name: String): Boolean {
    val (next, created) = MusicoletEngine.createCustomPlaylist(_uiState.value.playlists, name)
    if (created == null) {
      _uiState.update { it.copy(statusMessage = "Enter a playlist name.") }
      return false
    }
    savePlaylists(next)
    _uiState.update {
      it.copy(playlists = next, statusMessage = "Created playlist '${created.name}'.")
    }
    return true
  }

  fun addToPlaylist(playlistId: String, trackId: String) {
    val pl = _uiState.value.playlists.find { it.id == playlistId }
    val next = MusicoletEngine.addTrackToCustomPlaylist(_uiState.value.playlists, playlistId, trackId)
    savePlaylists(next)
    _uiState.update {
      it.copy(
        playlists = next,
        statusMessage = pl?.let { p -> "Added to playlist '${p.name}'." } ?: it.statusMessage,
      )
    }
  }

  fun removeFromPlaylist(playlistId: String, trackId: String) {
    val next = _uiState.value.playlists.map { p ->
      if (p.id == playlistId) p.copy(trackIds = p.trackIds.filterNot { it == trackId }) else p
    }
    savePlaylists(next)
    _uiState.update { it.copy(playlists = next) }
  }

  fun deletePlaylist(playlistId: String) {
    val target = _uiState.value.playlists.find { it.id == playlistId }
    val next = MusicoletEngine.deleteCustomPlaylist(_uiState.value.playlists, playlistId)
    savePlaylists(next)
    _uiState.update {
      it.copy(
        playlists = next,
        statusMessage = target?.let { t -> "Deleted playlist '${t.name}'." } ?: it.statusMessage,
      )
    }
  }

  fun saveTrackTags(trackId: String, patch: TrackTagPatch) {
    tagOverrides[trackId] = patch
    saveTagOverrides()
    _uiState.update { prev ->
      prev.copy(
        tracks = MusicoletEngine.applyTrackTags(prev.tracks, trackId, patch),
        editingTrackId = null,
        statusMessage = "Updated tags for '${patch.title}'.",
      )
    }
  }

  fun updateEqualizer(nextEq: EqualizerSettings) {
    val clamped = MusicoletEngine.clampEqualizerSettings(nextEq)
    applyHardwareEqualizer(clamped)
    saveEqualizer(clamped)
    _uiState.update { it.copy(equalizer = clamped) }
  }

  fun resetEqualizerToFlat() {
    updateEqualizer(
      _uiState.value.equalizer.copy(
        enabled = true,
        presetId = "flat",
        bandsDb = List(EQ_BAND_LABELS.size) { 0 },
        bassBoostPercent = 0,
        preampDb = 0,
        virtualizerPercent = 0,
      ),
    )
  }

  fun applyEqPreset(presetId: String, @Suppress("UNUSED_PARAMETER") bypassGate: Boolean = true): Boolean {
    val preset = EQ_PRESETS.find { it.id == presetId } ?: return false
    updateEqualizer(
      _uiState.value.equalizer.copy(
        enabled = true,
        presetId = preset.id,
        bandsDb = preset.bandsDb,
        bassBoostPercent = preset.bassBoostPercent,
      ),
    )
    return true
  }

  fun updateCrossfade(nextCrossfade: CrossfadeSettings) {
    val clamped = MusicoletEngine.clampCrossfadeSettings(nextCrossfade)
    saveCrossfade(clamped)
    _uiState.update { it.copy(crossfade = clamped) }
  }

  fun setSleepTimer(minutes: Int?) {
    sleepJob?.cancel()
    sleepJob = null
    if (minutes == null || minutes <= 0) {
      _uiState.update {
        it.copy(
          sleepTimerMinutes = null,
          sleepTimerRemainingSec = null,
          statusMessage = "Sleep timer cleared.",
        )
      }
      return
    }
    val totalSec = minutes * 60
    _uiState.update {
      it.copy(
        sleepTimerMinutes = minutes,
        sleepTimerRemainingSec = totalSec,
        isSleepTimerDialogOpen = false,
        statusMessage = "Sleep timer set for $minutes min.",
      )
    }
    sleepJob = viewModelScope.launch {
      var remaining = totalSec
      while (remaining > 0) {
        delay(1_000L)
        remaining -= 1
        if (remaining <= 0) {
          player.pause()
          _uiState.update {
            it.copy(
              sleepTimerMinutes = null,
              sleepTimerRemainingSec = null,
              statusMessage = "Sleep timer ended — playback paused.",
            )
          }
          break
        }
        _uiState.update { it.copy(sleepTimerRemainingSec = remaining) }
      }
    }
  }

  fun deleteImportedTrack(trackId: String) {
    val target = _uiState.value.tracks.find { it.id == trackId } ?: return
    if (target.sourceType != TrackSourceType.DEVICE) return
    val wasActive = _uiState.value.activeTrack?.id == trackId
    if (wasActive) player.pause()

    _uiState.update { prev ->
      val nextTracks = prev.tracks.filterNot { it.id == trackId }
      saveImportedTracks(nextTracks.filter { it.sourceType == TrackSourceType.DEVICE })
      val nextFavs = prev.favoriteTrackIds.filterNot { it == trackId }
      saveStringList("favorites", nextFavs)
      val nextPlaylists = prev.playlists.map { pl ->
        pl.copy(trackIds = pl.trackIds.filterNot { it == trackId })
      }
      savePlaylists(nextPlaylists)
      val nextQueues = prev.queues.map { q ->
        val nextIds = q.trackIds.filterNot { it == trackId }
        q.copy(
          trackIds = nextIds,
          currentTrackId = if (q.currentTrackId == trackId) nextIds.firstOrNull() else q.currentTrackId,
          positionMs = if (q.currentTrackId == trackId) 0L else q.positionMs,
        )
      }
      prev.copy(
        isPlaying = if (wasActive) false else prev.isPlaying,
        positionMs = if (wasActive) 0L else prev.positionMs,
        tracks = nextTracks,
        favoriteTrackIds = nextFavs,
        playlists = nextPlaylists,
        queues = nextQueues,
        statusMessage = "Removed '${target.title}' from local library.",
      )
    }
    persistQueues()
  }

  fun importAudioUris(uris: List<Uri>) {
    if (uris.isEmpty()) return
    val app = getApplication<Application>()
    val imported = mutableListOf<OfflineTrack>()
    for (uri in uris) {
      runCatching {
        app.contentResolver.takePersistableUriPermission(
          uri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
      }
      val track = readMetadataForUri(app, uri)
      imported += track
    }
    val existingIds = _uiState.value.tracks.map { it.id }.toSet()
    val deduped = imported.filter { it.id !in existingIds }
    if (deduped.isEmpty()) {
      _uiState.update { it.copy(statusMessage = "Selected files are already in your library.") }
      return
    }
    saveImportedTracks(_uiState.value.tracks.filter { it.sourceType == TrackSourceType.DEVICE } + deduped)
    val patched = applyAllTagOverrides(deduped)
    _uiState.update { prev ->
      val nextQueues = MusicoletEngine.addTracksToQueue(
        prev.queues,
        prev.activeQueueId,
        patched.map { it.id },
      )
      prev.copy(
        tracks = patched + prev.tracks,
        queues = nextQueues,
        statusMessage = "Imported ${patched.size} audio file(s) into offline library.",
      )
    }
    persistQueues()
  }

  private fun readMetadataForUri(context: Context, uri: Uri): OfflineTrack {
    var displayName = "Local Track"
    runCatching {
      context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
          if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) displayName = cursor.getString(idx) ?: displayName
          }
        }
    }
    val cleanBase = displayName.substringBeforeLast('.').ifBlank { "Local Track" }
    val retriever = MediaMetadataRetriever()
    var title = cleanBase
    var artist = "Device Library"
    var album = "Local Device Audio"
    var durationMs = 180_000L
    var year = 2026
    runCatching {
      retriever.setDataSource(context, uri)
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        ?.takeIf { it.isNotBlank() }?.let { title = it }
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        ?.takeIf { it.isNotBlank() }?.let { artist = it }
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        ?.takeIf { it.isNotBlank() }?.let { album = it }
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        ?.toLongOrNull()?.takeIf { it > 0L }?.let { durationMs = it }
      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
        ?.toIntOrNull()?.takeIf { it in 1900..2100 }?.let { year = it }
    }
    runCatching { retriever.release() }
    return OfflineTrack(
      id = "device-${uri.toString().hashCode()}",
      title = title,
      artist = artist,
      album = album,
      folderName = "Device Audio",
      durationMs = durationMs,
      uri = uri.toString(),
      releaseYear = year,
      sourceType = TrackSourceType.DEVICE,
    )
  }

  // --- SharedPreferences Persistence ---
  private fun persistQueues() {
    val state = _uiState.value
    val arr = JSONArray()
    for (q in state.queues) {
      val obj = JSONObject()
      obj.put("id", q.id)
      obj.put("name", q.name)
      obj.put("trackIds", JSONArray(q.trackIds))
      obj.put("currentTrackId", q.currentTrackId ?: JSONObject.NULL)
      obj.put("positionMs", q.positionMs)
      arr.put(obj)
    }
    prefs.edit()
      .putString("queues_json", arr.toString())
      .putString("active_queue_id", state.activeQueueId)
      .apply()
  }

  private fun loadQueues(): List<PlaybackQueue> {
    val raw = prefs.getString("queues_json", null) ?: return emptyList()
    return runCatching {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        val idsJson = obj.getJSONArray("trackIds")
        val ids = (0 until idsJson.length()).map { idx -> idsJson.getString(idx) }
        val curr = if (obj.isNull("currentTrackId")) null else obj.optString("currentTrackId")
        PlaybackQueue(
          id = obj.getString("id"),
          name = obj.getString("name"),
          trackIds = ids,
          currentTrackId = curr?.takeIf { it.isNotEmpty() },
          positionMs = obj.optLong("positionMs", 0L),
        )
      }
    }.getOrElse { emptyList() }
  }

  private fun savePlaylists(list: List<CustomPlaylist>) {
    val arr = JSONArray()
    for (pl in list) {
      arr.put(
        JSONObject()
          .put("id", pl.id)
          .put("name", pl.name)
          .put("trackIds", JSONArray(pl.trackIds)),
      )
    }
    prefs.edit().putString("playlists_json", arr.toString()).apply()
  }

  private fun loadPlaylists(): List<CustomPlaylist> {
    val raw = prefs.getString("playlists_json", null) ?: return DEFAULT_PLAYLISTS
    return runCatching {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        val ids = obj.getJSONArray("trackIds")
        CustomPlaylist(
          id = obj.getString("id"),
          name = obj.getString("name"),
          trackIds = (0 until ids.length()).map { ids.getString(it) },
        )
      }
    }.getOrElse { DEFAULT_PLAYLISTS }
  }

  private fun saveStringList(key: String, list: List<String>) {
    prefs.edit().putString(key, JSONArray(list).toString()).apply()
  }

  private fun loadStringList(key: String): List<String> {
    val raw = prefs.getString(key, null) ?: return emptyList()
    return runCatching {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { arr.getString(it) }
    }.getOrElse { emptyList() }
  }

  private fun saveEqualizer(eq: EqualizerSettings) {
    val obj = JSONObject()
      .put("enabled", eq.enabled)
      .put("presetId", eq.presetId)
      .put("bandsDb", JSONArray(eq.bandsDb))
      .put("bassBoostPercent", eq.bassBoostPercent)
      .put("preampDb", eq.preampDb)
      .put("virtualizerPercent", eq.virtualizerPercent)
    prefs.edit().putString("eq_json", obj.toString()).apply()
  }

  private fun loadEqualizer(): EqualizerSettings {
    val raw = prefs.getString("eq_json", null) ?: return EqualizerSettings()
    return runCatching {
      val obj = JSONObject(raw)
      val arr = obj.getJSONArray("bandsDb")
      MusicoletEngine.clampEqualizerSettings(
        EqualizerSettings(
          enabled = obj.optBoolean("enabled", true),
          presetId = obj.optString("presetId", "flat"),
          bandsDb = (0 until arr.length()).map { arr.getInt(it) },
          bassBoostPercent = obj.optInt("bassBoostPercent", 0),
          preampDb = obj.optInt("preampDb", 0),
          virtualizerPercent = obj.optInt("virtualizerPercent", 15),
        ),
      )
    }.getOrElse { EqualizerSettings() }
  }

  private fun saveCrossfade(cf: CrossfadeSettings) {
    val obj = JSONObject()
      .put("enabled", cf.enabled)
      .put("durationSec", cf.durationSec)
      .put("fadeOnManualSkip", cf.fadeOnManualSkip)
      .put("gaplessEnabled", cf.gaplessEnabled)
    prefs.edit().putString("crossfade_json", obj.toString()).apply()
  }

  private fun loadCrossfade(): CrossfadeSettings {
    val raw = prefs.getString("crossfade_json", null) ?: return CrossfadeSettings()
    return runCatching {
      val obj = JSONObject(raw)
      MusicoletEngine.clampCrossfadeSettings(
        CrossfadeSettings(
          enabled = obj.optBoolean("enabled", false),
          durationSec = obj.optInt("durationSec", 4),
          fadeOnManualSkip = obj.optBoolean("fadeOnManualSkip", true),
          gaplessEnabled = obj.optBoolean("gaplessEnabled", true),
        ),
      )
    }.getOrElse { CrossfadeSettings() }
  }

  private fun saveSponsorPledges(list: List<SponsorPledge>) {
    val arr = JSONArray()
    for (p in list) {
      arr.put(
        JSONObject()
          .put("id", p.id)
          .put("name", p.name)
          .put("email", p.email)
          .put("tierId", p.tierId)
          .put("billing", p.billing)
          .put("amountUsd", p.amountUsd)
          .put("note", p.note),
      )
    }
    prefs.edit().putString("sponsor_pledges_json", arr.toString()).apply()
  }

  private fun loadSponsorPledges(): List<SponsorPledge> {
    val raw = prefs.getString("sponsor_pledges_json", null) ?: return emptyList()
    return runCatching {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        SponsorPledge(
          id = obj.getString("id"),
          name = obj.getString("name"),
          email = obj.getString("email"),
          tierId = obj.getString("tierId"),
          billing = obj.getString("billing"),
          amountUsd = obj.getInt("amountUsd"),
          note = obj.optString("note", ""),
        )
      }
    }.getOrElse { emptyList() }
  }

  private fun saveTagOverrides() {
    val root = JSONObject()
    for ((id, patch) in tagOverrides) {
      root.put(
        id,
        JSONObject()
          .put("title", patch.title)
          .put("artist", patch.artist)
          .put("album", patch.album)
          .put("folderName", patch.folderName)
          .put("releaseYear", patch.releaseYear)
          .put("lyricsText", patch.lyricsText ?: ""),
      )
    }
    prefs.edit().putString("tag_overrides_json", root.toString()).apply()
  }

  private fun loadTagOverrides() {
    val raw = prefs.getString("tag_overrides_json", null) ?: return
    runCatching {
      val root = JSONObject(raw)
      val keys = root.keys()
      while (keys.hasNext()) {
        val id = keys.next()
        val obj = root.getJSONObject(id)
        tagOverrides[id] = TrackTagPatch(
          title = obj.optString("title"),
          artist = obj.optString("artist"),
          album = obj.optString("album"),
          folderName = obj.optString("folderName"),
          releaseYear = obj.optInt("releaseYear", 2026),
          lyricsText = obj.optString("lyricsText"),
        )
      }
    }
  }

  private fun applyAllTagOverrides(tracks: List<OfflineTrack>): List<OfflineTrack> {
    var result = tracks
    for ((id, patch) in tagOverrides) {
      result = MusicoletEngine.applyTrackTags(result, id, patch)
    }
    return result
  }

  private fun saveImportedTracks(tracks: List<OfflineTrack>) {
    val arr = JSONArray()
    for (t in tracks) {
      arr.put(
        JSONObject()
          .put("id", t.id)
          .put("title", t.title)
          .put("artist", t.artist)
          .put("album", t.album)
          .put("folderName", t.folderName)
          .put("durationMs", t.durationMs)
          .put("uri", t.uri)
          .put("releaseYear", t.releaseYear),
      )
    }
    prefs.edit().putString("device_tracks_json", arr.toString()).apply()
  }

  private fun loadImportedTracks(): List<OfflineTrack> {
    val raw = prefs.getString("device_tracks_json", null) ?: return emptyList()
    return runCatching {
      val arr = JSONArray(raw)
      (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        OfflineTrack(
          id = obj.getString("id"),
          title = obj.getString("title"),
          artist = obj.getString("artist"),
          album = obj.getString("album"),
          folderName = obj.getString("folderName"),
          durationMs = obj.optLong("durationMs", 180_000L),
          uri = obj.getString("uri"),
          releaseYear = obj.optInt("releaseYear", 2026),
          sourceType = TrackSourceType.DEVICE,
        )
      }
    }.getOrElse { emptyList() }
  }

  override fun onCleared() {
    super.onCleared()
    stopPositionLoop()
    crossfadeJob?.cancel()
    sleepJob?.cancel()
    persistQueues()
    runCatching { audioEq?.release() }
    runCatching { bassBoost?.release() }
    runCatching { virtualizer?.release() }
    runCatching { loudnessEnhancer?.release() }
    deckPlayers.forEach { runCatching { it.release() } }
  }
}
