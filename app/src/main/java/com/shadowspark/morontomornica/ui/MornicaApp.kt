package com.shadowspark.morontomornica.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shadowspark.morontomornica.domain.MusicoletEngine
import com.shadowspark.morontomornica.domain.MusicoletUiState
import com.shadowspark.morontomornica.domain.MusicoletViewModel
import com.shadowspark.morontomornica.domain.TrackTagPatch
import com.shadowspark.morontomornica.i18n.AppLanguage
import com.shadowspark.morontomornica.i18n.MorontoLocalizedStrings
import com.shadowspark.morontomornica.i18n.morontoStringsFor
import com.shadowspark.morontomornica.model.BLOG_PREVIEW_POSTS
import com.shadowspark.morontomornica.model.EQ_BAND_LABELS
import com.shadowspark.morontomornica.model.EQ_PRESETS
import com.shadowspark.morontomornica.model.LIVE_SHOW_PREVIEWS
import com.shadowspark.morontomornica.model.LibraryViewTab
import com.shadowspark.morontomornica.model.MAX_QUEUES
import com.shadowspark.morontomornica.model.MORONTO_THEMES
import com.shadowspark.morontomornica.model.OfflineTrack
import com.shadowspark.morontomornica.model.PIDGIN_OFFLINE_ARTICLE
import com.shadowspark.morontomornica.model.PRO_COMING_SOON_HIGHLIGHTS
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.SPONSOR_TIERS
import com.shadowspark.morontomornica.model.STUDIO_TEMPLATES
import com.shadowspark.morontomornica.model.SUPPORT_EMAIL
import com.shadowspark.morontomornica.model.SortOption
import com.shadowspark.morontomornica.model.TrackSourceType
import com.shadowspark.morontomornica.model.parseLrcLyrics
import kotlinx.coroutines.delay

@Composable
fun MornicaApp(viewModel: MusicoletViewModel) {
  val state by viewModel.uiState.collectAsState()
  val strings = remember(state.language) { morontoStringsFor(state.language) }
  val audioPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
      if (uris.isNotEmpty()) {
        viewModel.importAudioUris(uris)
      }
    }

  val filteredTracks = remember(state.tracks, state.searchQuery, state.sortOption) {
    MusicoletEngine.filterAndSortTracks(state.tracks, state.searchQuery, state.sortOption)
  }

  val isLibrarySearchTab =
    state.selectedTab == LibraryViewTab.TRACKS ||
      state.selectedTab == LibraryViewTab.FOLDERS ||
      state.selectedTab == LibraryViewTab.ALBUMS ||
      state.selectedTab == LibraryViewTab.ARTISTS

  Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
      containerColor = MornicaMidnightBg,
      topBar = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(MornicaSlateHeader)
            .statusBarsPadding(),
        ) {
          MusicoletTopBar(
            state = state,
            strings = strings,
            onOpenQueues = { viewModel.selectTab(LibraryViewTab.QUEUES) },
            onOpenEq = { viewModel.openAudioDialog(0) },
            onOpenCrossfade = { viewModel.openAudioDialog(1) },
            onOpenTimer = { viewModel.setSleepTimerDialogOpen(true) },
            onOpenUpgrade = { viewModel.selectTab(LibraryViewTab.UPGRADE) },
            onSelectLanguage = { viewModel.setLanguage(it) },
            onImportAudio = { audioPicker.launch(arrayOf("audio/*")) },
          )
          if (state.statusMessage != null) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .background(MornicaSlateCard)
                .padding(horizontal = 16.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = state.statusMessage!!,
                color = MornicaAmberStart,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
              )
              IconButton(
                onClick = { viewModel.dismissStatus() },
                modifier = Modifier.size(28.dp),
              ) {
                Icon(Icons.Rounded.Close, contentDescription = "Dismiss", tint = MornicaTextMuted)
              }
            }
          }
          MusicoletTabStrip(
            selectedTab = state.selectedTab,
            strings = strings,
            onSelectTab = { viewModel.selectTab(it) },
          )
        }
      },
      bottomBar = {
        if (!state.isNowPlayingOpen && state.activeTrack != null) {
          MiniPlayerBar(
            state = state,
            track = state.activeTrack!!,
            onOpenNowPlaying = { viewModel.setNowPlayingOpen(true) },
            onPrevious = { viewModel.skipPrevious() },
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onNext = { viewModel.skipNext(false) },
          )
        }
      },
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(horizontal = 14.dp),
      ) {
        if (isLibrarySearchTab) {
          Spacer(modifier = Modifier.height(10.dp))
          SearchAndSortControls(
            searchQuery = state.searchQuery,
            sortOption = state.sortOption,
            trackCount = filteredTracks.size,
            placeholder = strings.searchPlaceholder,
            onSearchChange = { viewModel.setSearchQuery(it) },
            onSortSelect = { viewModel.setSortOption(it) },
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (state.selectedTab) {
          LibraryViewTab.TRACKS -> TracksScreen(
            tracks = filteredTracks,
            state = state,
            strings = strings,
            viewModel = viewModel,
            onImportAudio = { audioPicker.launch(arrayOf("audio/*")) },
          )
          LibraryViewTab.QUEUES -> QueuesScreen(state = state, viewModel = viewModel)
          LibraryViewTab.PLAYLISTS -> PlaylistsScreen(
            state = state,
            strings = strings,
            viewModel = viewModel,
          )
          LibraryViewTab.FOLDERS -> GroupedTracksScreen(
            groups = MusicoletEngine.groupTracksByFolder(filteredTracks),
            label = "Folder",
            state = state,
            viewModel = viewModel,
          )
          LibraryViewTab.ALBUMS -> GroupedTracksScreen(
            groups = MusicoletEngine.groupTracksByAlbum(filteredTracks),
            label = "Album",
            state = state,
            viewModel = viewModel,
          )
          LibraryViewTab.ARTISTS -> GroupedTracksScreen(
            groups = MusicoletEngine.groupTracksByArtist(filteredTracks),
            label = "Artist",
            state = state,
            viewModel = viewModel,
          )
          LibraryViewTab.LIVE -> LiveScreen(state = state, strings = strings, viewModel = viewModel)
          LibraryViewTab.UPGRADE -> UpgradeScreen(state = state, strings = strings, viewModel = viewModel)
          LibraryViewTab.STORAGE -> StorageScreen(
            state = state,
            viewModel = viewModel,
            onImportAudio = { audioPicker.launch(arrayOf("audio/*")) },
          )
          LibraryViewTab.GUIDE -> GuideScreen(strings = strings, viewModel = viewModel)
          LibraryViewTab.SPONSOR -> SponsorScreen(state = state, viewModel = viewModel)
          LibraryViewTab.BLOG -> BlogScreen(state = state, viewModel = viewModel)
          LibraryViewTab.ABOUT -> AboutScreen(state = state, viewModel = viewModel)
          LibraryViewTab.PRIVACY -> PrivacyScreen(state = state, viewModel = viewModel)
        }
      }
    }

    if (state.isNowPlayingOpen && state.activeTrack != null) {
      NowPlayingFullDialog(
        state = state,
        track = state.activeTrack!!,
        viewModel = viewModel,
      )
    }

    if (state.isEqDialogOpen) {
      EqualizerDialog(state = state, viewModel = viewModel)
    }

    if (state.isSleepTimerDialogOpen) {
      SleepTimerDialog(state = state, viewModel = viewModel)
    }

    if (!state.showSplash && !state.hasCompletedOnboarding) {
      OnboardingDialog(
        state = state,
        onImportAudio = { audioPicker.launch(arrayOf("audio/*")) },
        onPickEqPreset = { presetId -> viewModel.applyEqPreset(presetId, bypassGate = true) },
        onFinishFree = { viewModel.completeOnboarding() },
        onUpgradePro = {
          viewModel.completeOnboarding()
          viewModel.selectTab(LibraryViewTab.UPGRADE)
        },
      )
    }

    if (state.isRatingPromptOpen && !state.showSplash && state.hasCompletedOnboarding) {
      RatingPromptDialog(
        sessionCount = state.sessionCount,
        onSubmit = { stars, comment -> viewModel.submitRating(stars, comment) },
        onDismiss = { viewModel.dismissRatingPrompt() },
      )
    }

    val sharingTrack = state.tracks.find { it.id == state.sharingTrackId }
    if (sharingTrack != null) {
      WhatsAppSharePreviewDialog(
        track = sharingTrack,
        onDismiss = { viewModel.setSharingTrackId(null) },
        onShareSheet = { viewModel.shareTrackWhatsAppCard(sharingTrack) },
      )
    }

    val editingTrack = state.tracks.find { it.id == state.editingTrackId }
    if (editingTrack != null) {
      TagEditorDialog(
        track = editingTrack,
        onDismiss = { viewModel.setEditingTrackId(null) },
        onSave = { patch -> viewModel.saveTrackTags(editingTrack.id, patch) },
      )
    }

    if (state.showSplash) {
      LoadingSplashOverlay(onEnterApp = { viewModel.dismissSplash() })
    }
  }
}

@Composable
private fun MusicoletTopBar(
  state: MusicoletUiState,
  strings: MorontoLocalizedStrings,
  onOpenQueues: () -> Unit,
  onOpenEq: () -> Unit,
  onOpenCrossfade: () -> Unit,
  onOpenTimer: () -> Unit,
  onOpenUpgrade: () -> Unit,
  onSelectLanguage: (AppLanguage) -> Unit,
  onImportAudio: () -> Unit,
) {
  var langMenuOpen by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.size(36.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MornicaEmerald)
        }
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = strings.brandName,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 14.sp,
          color = MornicaTextLight,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "Offline Studio · All Tools Free",
          fontSize = 10.sp,
          color = MornicaEmerald,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Box {
        Surface(
          onClick = { langMenuOpen = true },
          shape = RoundedCornerShape(10.dp),
          color = MornicaSlateCard,
          border = BorderStroke(1.dp, MornicaSlateBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(Icons.Rounded.Language, contentDescription = "Language", tint = MornicaCyan, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(state.language.code.uppercase(), color = MornicaTextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
        DropdownMenu(
          expanded = langMenuOpen,
          onDismissRequest = { langMenuOpen = false },
          modifier = Modifier.background(MornicaSlateHeader),
        ) {
          AppLanguage.entries.forEach { lang ->
            DropdownMenuItem(
              text = {
                Text(
                  text = "${lang.label} · ${lang.nativeName}",
                  color = if (lang == state.language) MornicaEmerald else MornicaTextLight,
                  fontWeight = if (lang == state.language) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp,
                )
              },
              onClick = {
                langMenuOpen = false
                onSelectLanguage(lang)
              },
            )
          }
        }
      }

      Surface(
        onClick = onOpenQueues,
        shape = RoundedCornerShape(10.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(Icons.Rounded.Layers, contentDescription = "Queues", tint = MornicaEmerald, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("${state.queues.size}", color = MornicaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }

      Surface(
        onClick = onOpenEq,
        shape = RoundedCornerShape(10.dp),
        color = if (state.equalizer.enabled) MornicaEmeraldDark else MornicaSlateCard,
        border = BorderStroke(1.dp, if (state.equalizer.enabled) MornicaEmerald else MornicaSlateBorder),
      ) {
        Box(modifier = Modifier.padding(7.dp), contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.Equalizer,
            contentDescription = "10-Band EQ",
            tint = if (state.equalizer.enabled) MornicaEmerald else MornicaTextMuted,
            modifier = Modifier.size(17.dp),
          )
        }
      }

      Surface(
        onClick = onOpenCrossfade,
        shape = RoundedCornerShape(10.dp),
        color = if (state.crossfade.enabled) MornicaSlateElevated else MornicaSlateCard,
        border = BorderStroke(1.dp, if (state.crossfade.enabled) MornicaCyan else MornicaSlateBorder),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            Icons.Rounded.SwapHoriz,
            contentDescription = "Crossfade",
            tint = if (state.crossfade.enabled) MornicaCyan else MornicaTextMuted,
            modifier = Modifier.size(16.dp),
          )
          if (state.crossfade.enabled && state.crossfade.durationSec > 0) {
            Spacer(modifier = Modifier.width(2.dp))
            Text("${state.crossfade.durationSec}s", color = MornicaCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Surface(
        onClick = onOpenTimer,
        shape = RoundedCornerShape(10.dp),
        color = if (state.sleepTimerRemainingSec != null) MornicaSlateElevated else MornicaSlateCard,
        border = BorderStroke(1.dp, if (state.sleepTimerRemainingSec != null) MornicaAmberStart else MornicaSlateBorder),
      ) {
        Box(modifier = Modifier.padding(7.dp), contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.Timer,
            contentDescription = "Sleep Timer",
            tint = if (state.sleepTimerRemainingSec != null) MornicaAmberStart else MornicaTextMuted,
            modifier = Modifier.size(17.dp),
          )
        }
      }

      Surface(
        onClick = onOpenUpgrade,
        shape = RoundedCornerShape(10.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaAmberStart),
      ) {
        Box(modifier = Modifier.padding(7.dp), contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.WorkspacePremium,
            contentDescription = "Pro Coming Soon",
            tint = MornicaAmberStart,
            modifier = Modifier.size(17.dp),
          )
        }
      }

      Surface(
        onClick = onImportAudio,
        shape = RoundedCornerShape(10.dp),
        color = MornicaEmerald,
      ) {
        Box(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), contentAlignment = Alignment.Center) {
          Icon(
            Icons.Rounded.CreateNewFolder,
            contentDescription = "Import Audio",
            tint = Color(0xFF04130E),
            modifier = Modifier.size(17.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun MusicoletTabStrip(
  selectedTab: LibraryViewTab,
  strings: MorontoLocalizedStrings,
  onSelectTab: (LibraryViewTab) -> Unit,
) {
  ScrollableTabRow(
    selectedTabIndex = selectedTab.ordinal,
    containerColor = MornicaSlateHeader,
    contentColor = MornicaEmerald,
    edgePadding = 8.dp,
    divider = { HorizontalDivider(color = MornicaSlateBorder) },
  ) {
    LibraryViewTab.entries.forEach { tab ->
      val selected = tab == selectedTab
      val label = when (tab) {
        LibraryViewTab.TRACKS -> strings.tracksTab
        LibraryViewTab.QUEUES -> strings.queuesTab
        LibraryViewTab.PLAYLISTS -> strings.playlistsTab
        LibraryViewTab.LIVE -> strings.liveTab
        LibraryViewTab.UPGRADE -> strings.upgradeTab
        LibraryViewTab.STORAGE -> strings.storageTab
        LibraryViewTab.GUIDE -> strings.guideTab
        else -> tab.label
      }
      val icon = when (tab) {
        LibraryViewTab.TRACKS -> Icons.Rounded.MusicNote
        LibraryViewTab.QUEUES -> Icons.Rounded.Layers
        LibraryViewTab.PLAYLISTS -> Icons.Rounded.PlaylistPlay
        LibraryViewTab.FOLDERS -> Icons.Rounded.Folder
        LibraryViewTab.ALBUMS -> Icons.Rounded.Album
        LibraryViewTab.ARTISTS -> Icons.Rounded.Person
        LibraryViewTab.LIVE -> Icons.Rounded.Radio
        LibraryViewTab.UPGRADE -> Icons.Rounded.WorkspacePremium
        LibraryViewTab.STORAGE -> Icons.Rounded.Storage
        LibraryViewTab.GUIDE -> Icons.Rounded.MenuBook
        LibraryViewTab.SPONSOR -> Icons.Rounded.VolunteerActivism
        LibraryViewTab.BLOG -> Icons.Rounded.Article
        LibraryViewTab.ABOUT -> Icons.Rounded.Info
        LibraryViewTab.PRIVACY -> Icons.Rounded.PrivacyTip
      }
      Tab(
        selected = selected,
        onClick = { onSelectTab(tab) },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
          }
        },
        selectedContentColor = MornicaEmerald,
        unselectedContentColor = MornicaTextMuted,
      )
    }
  }
}

@Composable
private fun SearchAndSortControls(
  searchQuery: String,
  sortOption: SortOption,
  trackCount: Int,
  placeholder: String,
  onSearchChange: (String) -> Unit,
  onSortSelect: (SortOption) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      singleLine = true,
      leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MornicaTextMuted) },
      placeholder = { Text(placeholder, color = MornicaTextMuted, fontSize = 13.sp, maxLines = 1) },
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MornicaSlateCard,
        unfocusedContainerColor = MornicaSlateCard,
        focusedBorderColor = MornicaEmerald,
        unfocusedBorderColor = MornicaSlateBorder,
        focusedTextColor = MornicaTextLight,
        unfocusedTextColor = MornicaTextLight,
      ),
      shape = RoundedCornerShape(14.dp),
      modifier = Modifier.fillMaxWidth(),
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = "$trackCount track${if (trackCount == 1) "" else "s"}",
        color = MornicaTextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(end = 6.dp),
      )
      SortOption.entries.forEach { option ->
        val selected = option == sortOption
        Surface(
          onClick = { onSortSelect(option) },
          shape = RoundedCornerShape(10.dp),
          color = if (selected) MornicaEmerald else MornicaSlateCard,
          border = if (selected) null else BorderStroke(1.dp, MornicaSlateBorder),
        ) {
          Text(
            text = option.label,
            color = if (selected) Color(0xFF04130E) else MornicaTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun TrackRowCard(
  track: OfflineTrack,
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  val isActive = state.activeTrack?.id == track.id
  val isSaved = track.id in state.favoriteTrackIds
  var menuOpen by remember { mutableStateOf(false) }

  Surface(
    onClick = { viewModel.playTrackInActiveQueue(track.id, 0L) },
    shape = RoundedCornerShape(14.dp),
    color = if (isActive) MornicaSlateElevated else MornicaSlateCard,
    border = BorderStroke(1.dp, if (isActive) MornicaEmerald else MornicaSlateBorder),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) MornicaEmeraldDark else MornicaSlateHeader,
        border = BorderStroke(1.dp, if (isActive) MornicaEmerald else MornicaSlateBorder),
        modifier = Modifier.size(44.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (isActive && state.isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = MornicaEmerald,
          )
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = track.title,
          color = if (isActive) MornicaEmerald else MornicaTextLight,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "${track.artist} · ${track.album}",
          color = MornicaTextMuted,
          fontSize = 12.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = "${track.folderName} · ${MusicoletEngine.formatDurationMs(track.durationMs)} · ${track.releaseYear}",
          color = MornicaCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      IconButton(onClick = { viewModel.setSharingTrackId(track.id) }) {
        Icon(
          imageVector = Icons.Rounded.Share,
          contentDescription = "Share WhatsApp Status Card",
          tint = MornicaCyan,
          modifier = Modifier.size(19.dp),
        )
      }

      IconButton(onClick = { viewModel.toggleFavorite(track.id) }) {
        Icon(
          imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (isSaved) MornicaDangerRed else MornicaTextMuted,
        )
      }

      Box {
        IconButton(onClick = { menuOpen = true }) {
          Icon(Icons.Rounded.MoreVert, contentDescription = "Track options", tint = MornicaTextMuted)
        }
        DropdownMenu(
          expanded = menuOpen,
          onDismissRequest = { menuOpen = false },
          modifier = Modifier.background(MornicaSlateHeader),
        ) {
          DropdownMenuItem(
            text = { Text("Play Now", color = MornicaTextLight) },
            onClick = {
              menuOpen = false
              viewModel.playTrackInActiveQueue(track.id, 0L)
            },
          )
          DropdownMenuItem(
            text = { Text("Share WhatsApp Status Card", color = MornicaCyan) },
            onClick = {
              menuOpen = false
              viewModel.setSharingTrackId(track.id)
            },
          )
          DropdownMenuItem(
            text = { Text("Play Next in Active Queue", color = MornicaTextLight) },
            onClick = {
              menuOpen = false
              viewModel.addTracksToQueue(state.activeQueueId, listOf(track.id), playNext = true)
            },
          )
          DropdownMenuItem(
            text = { Text("Edit Tags & Lyrics", color = MornicaEmerald) },
            onClick = {
              menuOpen = false
              viewModel.setEditingTrackId(track.id)
            },
          )
          HorizontalDivider(color = MornicaSlateBorder)
          state.queues.forEach { q ->
            DropdownMenuItem(
              text = { Text("+ Queue: ${q.name}", color = MornicaTextLight, fontSize = 12.sp) },
              onClick = {
                menuOpen = false
                viewModel.addTracksToQueue(q.id, listOf(track.id))
              },
            )
          }
          if (state.playlists.isNotEmpty()) {
            HorizontalDivider(color = MornicaSlateBorder)
            state.playlists.forEach { pl ->
              DropdownMenuItem(
                text = { Text("+ Playlist: ${pl.name}", color = MornicaCyan, fontSize = 12.sp) },
                onClick = {
                  menuOpen = false
                  viewModel.addToPlaylist(pl.id, track.id)
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TracksScreen(
  tracks: List<OfflineTrack>,
  state: MusicoletUiState,
  strings: MorontoLocalizedStrings,
  viewModel: MusicoletViewModel,
  onImportAudio: () -> Unit,
) {
  val deviceCount = state.tracks.count { it.sourceType == TrackSourceType.DEVICE }
  LazyColumn(
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    val activeTrack = state.activeTrack
    if (activeTrack != null) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MornicaSlateElevated,
          border = BorderStroke(1.dp, MornicaEmerald),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${strings.continueListening} · ${state.activeQueue.name}",
                  color = MornicaEmerald,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = activeTrack.title,
                  color = MornicaTextLight,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.ExtraBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "${activeTrack.artist} · ${MusicoletEngine.formatDurationMs(state.positionMs)} / ${MusicoletEngine.formatDurationMs(state.durationMs)}",
                  color = MornicaTextMuted,
                  fontSize = 12.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = { viewModel.togglePlayPause() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MornicaEmerald,
                  contentColor = Color(0xFF04130E),
                ),
                shape = RoundedCornerShape(12.dp),
              ) {
                Icon(
                  imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  if (state.isPlaying) strings.pauseTrack else strings.resumePlayback,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                )
              }
            }

            Row(
              modifier = Modifier.horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = { viewModel.setNowPlayingOpen(true) },
                border = BorderStroke(1.dp, MornicaSlateBorder),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(strings.openStudioPlayer, color = MornicaTextLight, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
              OutlinedButton(
                onClick = { viewModel.openAudioDialog(0) },
                border = BorderStroke(1.dp, MornicaSlateBorder),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(
                  text = "10-EQ ${if (state.equalizer.enabled) "ON" else "OFF"}",
                  color = MornicaCyan,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                )
              }
              OutlinedButton(
                onClick = { viewModel.openAudioDialog(1) },
                border = BorderStroke(1.dp, MornicaSlateBorder),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(
                  text = if (state.crossfade.enabled && state.crossfade.durationSec > 0) "Fade ${state.crossfade.durationSec}s" else "Gapless",
                  color = MornicaAmberStart,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
          }
        }
      }
    }

    // One-Click Studio Templates
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = strings.studioTemplatesHeading,
          color = MornicaCyan,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
        )
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          STUDIO_TEMPLATES.forEach { tpl ->
            Surface(
              onClick = { viewModel.applyStudioTemplate(tpl.id) },
              shape = RoundedCornerShape(12.dp),
              color = MornicaSlateCard,
              border = BorderStroke(1.dp, MornicaSlateBorder),
              modifier = Modifier.width(180.dp),
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                  Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MornicaAmberStart, modifier = Modifier.size(14.dp))
                  Text(tpl.name, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                }
                Text(tpl.tagline, color = MornicaTextMuted, fontSize = 10.sp, maxLines = 2)
              }
            }
          }
        }
      }
    }

    if (deviceCount == 0) {
      item {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MornicaSlateCard,
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("100% Offline Audio Library · All Studio Tools Free", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                  "Import MP3, WAV, FLAC, M4A, or OGG files from device storage.",
                  color = MornicaTextMuted,
                  fontSize = 12.sp,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = onImportAudio,
                colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
                shape = RoundedCornerShape(12.dp),
              ) {
                Text(strings.importAudio, fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
            OutlinedButton(
              onClick = { viewModel.selectTab(LibraryViewTab.BLOG) },
              border = BorderStroke(1.dp, MornicaAmberStart),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Read Pidgin Offline Guide & File Help Desk", color = MornicaAmberStart, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
    }

    items(tracks, key = { it.id }) { track ->
      TrackRowCard(track = track, state = state, viewModel = viewModel)
    }
  }
}

@Composable
private fun QueuesScreen(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  var newQueueName by remember { mutableStateOf("") }
  val activeQueue = state.activeQueue

  LazyColumn(
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Multiple Independent Queues (${state.queues.size} / $MAX_QUEUES)",
            color = MornicaEmerald,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
          )
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = newQueueName,
              onValueChange = { newQueueName = it },
              singleLine = true,
              placeholder = { Text("New queue name", color = MornicaTextMuted, fontSize = 13.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MornicaSlateHeader,
                unfocusedContainerColor = MornicaSlateHeader,
                focusedBorderColor = MornicaEmerald,
                unfocusedBorderColor = MornicaSlateBorder,
                focusedTextColor = MornicaTextLight,
                unfocusedTextColor = MornicaTextLight,
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f),
            )
            Button(
              onClick = {
                if (viewModel.createQueue(newQueueName)) {
                  newQueueName = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
              shape = RoundedCornerShape(12.dp),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    items(state.queues, key = { it.id }) { queue ->
      val isCurrent = queue.id == state.activeQueueId
      val savedTrack = state.tracks.find { it.id == queue.currentTrackId }
      val pos = if (isCurrent) state.positionMs else queue.positionMs
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) MornicaSlateElevated else MornicaSlateCard,
        border = BorderStroke(1.dp, if (isCurrent) MornicaEmerald else MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(queue.name, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("${queue.trackIds.size} tracks", color = MornicaTextMuted, fontSize = 12.sp)
          }
          Text(
            text = savedTrack?.let { "Saved at ${MusicoletEngine.formatDurationMs(pos)} · ${it.title}" }
              ?: "Queue is empty",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
              if (isCurrent) {
                Text("Active Queue", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              } else {
                OutlinedButton(
                  onClick = { viewModel.switchQueue(queue.id, autoPlay = false) },
                  border = BorderStroke(1.dp, MornicaSlateBorder),
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Text("Switch & Resume", color = MornicaTextLight, fontSize = 12.sp)
                }
              }
              Button(
                onClick = {
                  if (isCurrent) viewModel.togglePlayPause()
                  else viewModel.switchQueue(queue.id, autoPlay = true)
                },
                enabled = queue.trackIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
                shape = RoundedCornerShape(10.dp),
              ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Play", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
            if (state.queues.size > 1) {
              IconButton(onClick = { viewModel.deleteQueue(queue.id) }) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete queue", tint = MornicaDangerRed)
              }
            }
          }
        }
      }
    }

    item {
      Text(
        text = "Up Next in ${activeQueue.name}",
        color = MornicaCyan,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 6.dp),
      )
    }

    itemsIndexed(activeQueue.trackIds) { idx, trackId ->
      val track = state.tracks.find { it.id == trackId }
      if (track != null) {
        val isPlayingHere = activeQueue.currentTrackId == track.id
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MornicaSlateHeader,
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .clickable { viewModel.playTrackInActiveQueue(track.id, 0L) }
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("${idx + 1}", color = MornicaTextMuted, fontSize = 12.sp, modifier = Modifier.width(24.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                track.title,
                color = if (isPlayingHere) MornicaEmerald else MornicaTextLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(track.artist, color = MornicaTextMuted, fontSize = 11.sp, maxLines = 1)
            }
            IconButton(
              onClick = { viewModel.reorderInQueue(activeQueue.id, idx, idx - 1) },
              enabled = idx > 0,
            ) {
              Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move up", tint = MornicaTextMuted)
            }
            IconButton(
              onClick = { viewModel.reorderInQueue(activeQueue.id, idx, idx + 1) },
              enabled = idx < activeQueue.trackIds.lastIndex,
            ) {
              Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move down", tint = MornicaTextMuted)
            }
            IconButton(onClick = { viewModel.removeFromQueue(activeQueue.id, track.id) }) {
              Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = MornicaDangerRed)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GroupedTracksScreen(
  groups: List<Pair<String, List<OfflineTrack>>>,
  label: String,
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  LazyColumn(
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    groups.forEach { (name, tracks) ->
      item(key = "header-$label-$name") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("$label · $name", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("${tracks.size} track(s)", color = MornicaTextMuted, fontSize = 12.sp)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(
              onClick = { viewModel.addTracksToQueue(state.activeQueueId, tracks.map { it.id }) },
              border = BorderStroke(1.dp, MornicaSlateBorder),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("+ Queue", color = MornicaTextLight, fontSize = 11.sp)
            }
            Button(
              onClick = { viewModel.playTrackListAsActiveQueue(tracks.map { it.id }) },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Play All", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
          }
        }
      }
      items(tracks, key = { "$label-$name-${it.id}" }) { track ->
        TrackRowCard(track = track, state = state, viewModel = viewModel)
      }
    }
  }
}

@Composable
private fun PlaylistsScreen(
  state: MusicoletUiState,
  strings: MorontoLocalizedStrings,
  viewModel: MusicoletViewModel,
) {
  var playlistName by remember { mutableStateOf("") }
  val favorites = state.tracks.filter { it.id in state.favoriteTrackIds }
  val smartGroups = remember(state.tracks) { MusicoletEngine.buildGenreSmartPlaylists(state.tracks) }

  LazyColumn(
    contentPadding = PaddingValues(bottom = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(strings.smartGenresHeading, color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Row(
          modifier = Modifier.horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          smartGroups.forEach { group ->
            Surface(
              onClick = { viewModel.playSmartGenrePlaylist(group.genre) },
              shape = RoundedCornerShape(14.dp),
              color = MornicaSlateCard,
              border = BorderStroke(1.dp, MornicaSlateBorder),
              modifier = Modifier.width(155.dp),
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(group.genre, color = MornicaTextLight, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text("${group.tracks.size} track(s)", color = MornicaCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Tap to play mix", color = MornicaEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            singleLine = true,
            placeholder = { Text("New playlist name", color = MornicaTextMuted, fontSize = 13.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MornicaSlateHeader,
              unfocusedContainerColor = MornicaSlateHeader,
              focusedBorderColor = MornicaEmerald,
              unfocusedBorderColor = MornicaSlateBorder,
              focusedTextColor = MornicaTextLight,
              unfocusedTextColor = MornicaTextLight,
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
          )
          Button(
            onClick = {
              if (viewModel.createPlaylist(playlistName)) {
                playlistName = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
            shape = RoundedCornerShape(12.dp),
          ) {
            Text("Create", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Favorites (${favorites.size})", color = MornicaDangerRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        if (favorites.isNotEmpty()) {
          Button(
            onClick = { viewModel.playTrackListAsActiveQueue(favorites.map { it.id }) },
            colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text("Play Favorites", fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }
    }
    items(favorites, key = { "fav-${it.id}" }) { track ->
      TrackRowCard(track = track, state = state, viewModel = viewModel)
    }

    state.playlists.forEach { pl ->
      val plTracks = pl.trackIds.mapNotNull { id -> state.tracks.find { it.id == id } }
      item(key = "pl-header-${pl.id}") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("${pl.name} (${plTracks.size})", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (plTracks.isNotEmpty()) {
              Button(
                onClick = { viewModel.playTrackListAsActiveQueue(plTracks.map { it.id }) },
                colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text("Play All", fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
            IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete playlist", tint = MornicaDangerRed)
            }
          }
        }
      }
      items(plTracks, key = { "pl-${pl.id}-${it.id}" }) { track ->
        TrackRowCard(track = track, state = state, viewModel = viewModel)
      }
    }
  }
}

@Composable
private fun MiniPlayerBar(
  state: MusicoletUiState,
  track: OfflineTrack,
  onOpenNowPlaying: () -> Unit,
  onPrevious: () -> Unit,
  onTogglePlayPause: () -> Unit,
  onNext: () -> Unit,
) {
  val maxDur = state.durationMs.coerceAtLeast(1_000L)
  val progress = (state.positionMs.toFloat() / maxDur.toFloat()).coerceIn(0f, 1f)

  Surface(
    color = MornicaSlateElevated,
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding(),
  ) {
    Column {
      LinearProgressIndicator(
        progress = { progress },
        color = MornicaEmerald,
        trackColor = MornicaSlateBorder,
        modifier = Modifier
          .fillMaxWidth()
          .height(3.dp),
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenNowPlaying() }
          .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MornicaSlateCard,
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.size(40.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MornicaAmberStart)
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(track.title, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Text(
            text = "${track.artist} · ${MusicoletEngine.formatDurationMs(state.positionMs)} / ${MusicoletEngine.formatDurationMs(maxDur)}",
            color = MornicaCyan,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        IconButton(onClick = onPrevious) {
          Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MornicaTextLight)
        }
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(MornicaAmberStart, MornicaAmberEnd)))
            .clickable { onTogglePlayPause() },
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            contentDescription = if (state.isPlaying) "Pause" else "Play",
            tint = Color(0xFF0B101B),
          )
        }
        IconButton(onClick = onNext) {
          Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MornicaTextLight)
        }
      }
    }
  }
}

@Composable
private fun NowPlayingFullDialog(
  state: MusicoletUiState,
  track: OfflineTrack,
  viewModel: MusicoletViewModel,
) {
  val activeQueue = state.activeQueue
  val maxDur = state.durationMs.coerceAtLeast(1_000L)
  val parsedLyrics = remember(track.lyricsText) { parseLrcLyrics(track.lyricsText) }
  val activeLyricIndex = parsedLyrics.indexOfLast { it.timeMs != null && state.positionMs >= it.timeMs }
  val isSaved = track.id in state.favoriteTrackIds

  Dialog(
    onDismissRequest = { viewModel.setNowPlayingOpen(false) },
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      color = MornicaMidnightBg,
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          IconButton(onClick = { viewModel.setNowPlayingOpen(false) }) {
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Collapse", tint = MornicaTextLight)
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(activeQueue.name, color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            Text(track.folderName, color = MornicaTextMuted, fontSize = 11.sp, maxLines = 1)
          }
          Row {
            IconButton(onClick = { viewModel.setSharingTrackId(track.id) }) {
              Icon(Icons.Rounded.Share, contentDescription = "Share Card", tint = MornicaCyan)
            }
            IconButton(onClick = { viewModel.openAudioDialog(0) }) {
              Icon(
                Icons.Rounded.Equalizer,
                contentDescription = "Equalizer",
                tint = if (state.equalizer.enabled) MornicaEmerald else MornicaTextMuted,
              )
            }
            IconButton(onClick = { viewModel.openAudioDialog(1) }) {
              Icon(
                Icons.Rounded.SwapHoriz,
                contentDescription = "Crossfade",
                tint = if (state.crossfade.enabled) MornicaCyan else MornicaTextMuted,
              )
            }
            IconButton(onClick = { viewModel.toggleFavorite(track.id) }) {
              Icon(
                imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isSaved) MornicaDangerRed else MornicaTextMuted,
              )
            }
          }
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MornicaSlateHeader)
            .padding(4.dp),
        ) {
          listOf("Player", "Lyrics", "Queue (${activeQueue.trackIds.size})").forEachIndexed { idx, label ->
            val selected = state.nowPlayingTab == idx
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) MornicaSlateCard else Color.Transparent)
                .clickable { viewModel.setNowPlayingTab(idx) }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = label,
                color = if (selected) MornicaEmerald else MornicaTextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
              )
            }
          }
        }

        when (state.nowPlayingTab) {
          0 -> Surface(
            shape = RoundedCornerShape(24.dp),
            color = MornicaSlateCard,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier
              .fillMaxWidth()
              .height(210.dp),
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Icon(Icons.Rounded.Album, contentDescription = null, tint = MornicaEmerald, modifier = Modifier.size(64.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                if (track.sourceType == TrackSourceType.DEVICE) "On-Device Offline Audio" else "Bundled Offline Audio",
                color = MornicaCyan,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
              )
              Text("${track.album} · ${track.releaseYear}", color = MornicaTextMuted, fontSize = 12.sp)
            }
          }
          1 -> Surface(
            shape = RoundedCornerShape(24.dp),
            color = MornicaSlateHeader,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier
              .fillMaxWidth()
              .height(210.dp),
          ) {
            if (parsedLyrics.isEmpty()) {
              Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                Text("No lyrics saved yet.", color = MornicaTextMuted, fontSize = 13.sp)
                TextButton(onClick = { viewModel.setEditingTrackId(track.id) }) {
                  Text("Add Synced (.lrc) or Plain Lyrics", color = MornicaEmerald)
                }
              }
            } else {
              LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                itemsIndexed(parsedLyrics) { idx, line ->
                  val current = idx == activeLyricIndex
                  Text(
                    text = line.text,
                    color = if (current) MornicaEmerald else MornicaTextMuted,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable(enabled = line.timeMs != null) {
                        line.timeMs?.let { viewModel.seekTo(it) }
                      },
                  )
                }
              }
            }
          }
          else -> Surface(
            shape = RoundedCornerShape(24.dp),
            color = MornicaSlateHeader,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier
              .fillMaxWidth()
              .height(210.dp),
          ) {
            LazyColumn(contentPadding = PaddingValues(12.dp)) {
              itemsIndexed(activeQueue.trackIds) { idx, id ->
                val qTrack = state.tracks.find { it.id == id }
                if (qTrack != null) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { viewModel.playTrackInActiveQueue(qTrack.id, 0L) }
                      .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text("${idx + 1}. ", color = MornicaTextMuted, fontSize = 12.sp)
                    Text(
                      qTrack.title,
                      color = if (qTrack.id == track.id) MornicaEmerald else MornicaTextLight,
                      fontWeight = FontWeight.SemiBold,
                      fontSize = 13.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f),
                    )
                  }
                }
              }
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(track.title, color = MornicaTextLight, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Text("${track.artist} · ${track.album}", color = MornicaCyan, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Speed:", color = MornicaTextMuted, fontSize = 12.sp)
            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { spd ->
              val selected = state.playbackRate == spd
              Surface(
                onClick = { viewModel.setPlaybackRate(spd) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected) MornicaEmerald else MornicaSlateCard,
                border = if (selected) null else BorderStroke(1.dp, MornicaSlateBorder),
              ) {
                Text(
                  "${spd}x",
                  color = if (selected) Color(0xFF04130E) else MornicaTextMuted,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
              }
            }
          }
          val eqLabel = if (state.equalizer.enabled) "10-Band EQ Active" else "EQ Bypassed"
          val cfLabel =
            if (state.crossfade.enabled && state.crossfade.durationSec > 0) "Crossfade ${state.crossfade.durationSec}s"
            else if (state.crossfade.gaplessEnabled) "Gapless"
            else "Crossfade Off"
          Text(
            text = "$eqLabel · $cfLabel",
            color = MornicaTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
          )
        }

        Column {
          Slider(
            value = state.positionMs.toFloat().coerceIn(0f, maxDur.toFloat()),
            onValueChange = { viewModel.seekTo(it.toLong()) },
            valueRange = 0f..maxDur.toFloat(),
            colors = SliderDefaults.colors(
              thumbColor = MornicaAmberStart,
              activeTrackColor = MornicaEmerald,
              inactiveTrackColor = MornicaSlateBorder,
            ),
          )
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(MusicoletEngine.formatDurationMs(state.positionMs), color = MornicaTextMuted, fontSize = 12.sp)
            Text(MusicoletEngine.formatDurationMs(maxDur), color = MornicaTextMuted, fontSize = 12.sp)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = { viewModel.toggleShuffle() }) {
            Icon(
              Icons.Rounded.Shuffle,
              contentDescription = "Shuffle",
              tint = if (state.shuffleEnabled) MornicaEmerald else MornicaTextMuted,
            )
          }
          IconButton(onClick = { viewModel.skipPrevious() }) {
            Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MornicaTextLight, modifier = Modifier.size(34.dp))
          }
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(Brush.horizontalGradient(listOf(MornicaAmberStart, MornicaAmberEnd)))
              .clickable { viewModel.togglePlayPause() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
              contentDescription = if (state.isPlaying) "Pause" else "Play",
              tint = Color(0xFF0B101B),
              modifier = Modifier.size(34.dp),
            )
          }
          IconButton(onClick = { viewModel.skipNext(false) }) {
            Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MornicaTextLight, modifier = Modifier.size(34.dp))
          }
          IconButton(onClick = { viewModel.cycleRepeatMode() }) {
            Icon(
              imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
              contentDescription = "Repeat",
              tint = if (state.repeatMode != RepeatMode.OFF) MornicaEmerald else MornicaTextMuted,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EqualizerDialog(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  val eq = state.equalizer
  val cf = state.crossfade
  val activeTab = state.audioDialogTab

  AlertDialog(
    onDismissRequest = { viewModel.setEqDialogOpen(false) },
    containerColor = MornicaSlateHeader,
    title = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("10-Band EQ & Crossfade", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 17.sp)
          if (activeTab == 0) {
            TextButton(onClick = { viewModel.resetEqualizerToFlat() }) {
              Text("Reset Flat", color = MornicaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MornicaSlateCard)
            .padding(3.dp),
        ) {
          listOf("10-Band EQ", "Crossfade").forEachIndexed { idx, label ->
            val selected = activeTab == idx
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selected) MornicaSlateElevated else Color.Transparent)
                .clickable { viewModel.setAudioDialogTab(idx) }
                .padding(vertical = 7.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = label,
                color = if (selected) MornicaEmerald else MornicaTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .heightIn(max = 430.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (activeTab == 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              if (eq.enabled) "10-Band Offline DSP Active" else "Equalizer Bypassed",
              color = if (eq.enabled) MornicaEmerald else MornicaTextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
            )
            Switch(
              checked = eq.enabled,
              onCheckedChange = { viewModel.updateEqualizer(eq.copy(enabled = it)) },
              colors = SwitchDefaults.colors(checkedThumbColor = MornicaMidnightBg, checkedTrackColor = MornicaEmerald),
            )
          }

          Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            EQ_PRESETS.forEach { preset ->
              val selected = eq.presetId == preset.id
              Surface(
                onClick = { viewModel.applyEqPreset(preset.id, bypassGate = true) },
                shape = RoundedCornerShape(8.dp),
                color = if (selected) MornicaEmerald else MornicaSlateCard,
                border = if (selected) null else BorderStroke(1.dp, MornicaSlateBorder),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    preset.name,
                    color = if (selected) Color(0xFF04130E) else MornicaTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }

          EQ_BAND_LABELS.forEachIndexed { idx, label ->
            val value = eq.bandsDb.getOrElse(idx) { 0 }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(label, color = MornicaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
              Slider(
                value = value.toFloat(),
                onValueChange = { v ->
                  val next = eq.bandsDb.toMutableList()
                  if (idx in next.indices) next[idx] = v.toInt()
                  viewModel.updateEqualizer(eq.copy(enabled = true, presetId = "custom", bandsDb = next))
                },
                valueRange = -12f..12f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(thumbColor = MornicaAmberStart, activeTrackColor = MornicaEmerald),
              )
              Text("${if (value > 0) "+" else ""}$value dB", color = MornicaTextLight, fontSize = 11.sp, modifier = Modifier.width(44.dp))
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Preamp", color = MornicaEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
            Slider(
              value = eq.preampDb.toFloat(),
              onValueChange = { v ->
                viewModel.updateEqualizer(eq.copy(enabled = true, preampDb = v.toInt()))
              },
              valueRange = -12f..12f,
              modifier = Modifier.weight(1f),
              colors = SliderDefaults.colors(thumbColor = MornicaEmerald, activeTrackColor = MornicaEmerald),
            )
            Text("${if (eq.preampDb > 0) "+" else ""}${eq.preampDb} dB", color = MornicaTextLight, fontSize = 11.sp, modifier = Modifier.width(44.dp))
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bass", color = MornicaAmberStart, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
            Slider(
              value = eq.bassBoostPercent.toFloat(),
              onValueChange = { v ->
                viewModel.updateEqualizer(eq.copy(enabled = true, presetId = "custom", bassBoostPercent = v.toInt()))
              },
              valueRange = 0f..100f,
              modifier = Modifier.weight(1f),
              colors = SliderDefaults.colors(thumbColor = MornicaAmberStart, activeTrackColor = MornicaAmberStart),
            )
            Text("${eq.bassBoostPercent}%", color = MornicaTextLight, fontSize = 11.sp, modifier = Modifier.width(44.dp))
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Space", color = MornicaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
            Slider(
              value = eq.virtualizerPercent.toFloat(),
              onValueChange = { v ->
                viewModel.updateEqualizer(eq.copy(enabled = true, virtualizerPercent = v.toInt()))
              },
              valueRange = 0f..100f,
              modifier = Modifier.weight(1f),
              colors = SliderDefaults.colors(thumbColor = MornicaCyan, activeTrackColor = MornicaCyan),
            )
            Text("${eq.virtualizerPercent}%", color = MornicaTextLight, fontSize = 11.sp, modifier = Modifier.width(44.dp))
          }
        } else {

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MornicaSlateCard,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Smooth Crossfade", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Overlap outgoing and incoming tracks seamlessly.", color = MornicaTextMuted, fontSize = 11.sp)
              }
              Switch(
                checked = cf.enabled,
                onCheckedChange = { viewModel.updateCrossfade(cf.copy(enabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = MornicaMidnightBg, checkedTrackColor = MornicaCyan),
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MornicaSlateCard,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text("Crossfade Duration (0–12s)", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${cf.durationSec}s", color = MornicaCyan, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
              }
              Slider(
                value = cf.durationSec.toFloat(),
                onValueChange = { v ->
                  val sec = v.toInt()
                  viewModel.updateCrossfade(cf.copy(enabled = sec > 0, durationSec = sec))
                },
                valueRange = 0f..12f,
                colors = SliderDefaults.colors(thumbColor = MornicaCyan, activeTrackColor = MornicaCyan),
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
              ) {
                listOf(0, 2, 4, 6, 8, 12).forEach { sec ->
                  val selected = cf.durationSec == sec && (sec == 0 || cf.enabled)
                  Surface(
                    onClick = { viewModel.updateCrossfade(cf.copy(enabled = sec > 0, durationSec = sec)) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) MornicaCyan else MornicaSlateHeader,
                    border = if (selected) null else BorderStroke(1.dp, MornicaSlateBorder),
                    modifier = Modifier.weight(1f),
                  ) {
                    Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                      Text(
                        "${sec}s",
                        color = if (selected) Color(0xFF04130E) else MornicaTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                  }
                }
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MornicaSlateCard,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Crossfade on Manual Skip", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Apply fade transition when tapping Next or Previous.", color = MornicaTextMuted, fontSize = 11.sp)
              }
              Switch(
                checked = cf.fadeOnManualSkip,
                onCheckedChange = { viewModel.updateCrossfade(cf.copy(fadeOnManualSkip = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = MornicaMidnightBg, checkedTrackColor = MornicaEmerald),
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MornicaSlateCard,
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Gapless Playback", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Zero silence between consecutive tracks when Crossfade is 0s.", color = MornicaTextMuted, fontSize = 11.sp)
              }
              Switch(
                checked = cf.gaplessEnabled,
                onCheckedChange = { viewModel.updateCrossfade(cf.copy(gaplessEnabled = it)) },
                colors = SwitchDefaults.colors(checkedThumbColor = MornicaMidnightBg, checkedTrackColor = MornicaEmerald),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = { viewModel.setEqDialogOpen(false) }) {
        Text("Done", color = MornicaEmerald, fontWeight = FontWeight.Bold)
      }
    },
  )
}

@Composable
private fun SleepTimerDialog(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  AlertDialog(
    onDismissRequest = { viewModel.setSleepTimerDialogOpen(false) },
    containerColor = MornicaSlateHeader,
    title = { Text("Sleep Timer", color = MornicaTextLight, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Pause offline playback automatically after:",
          color = MornicaTextMuted,
          fontSize = 12.sp,
        )
        listOf(5, 15, 30, 45, 60).forEach { mins ->
          val active = state.sleepTimerMinutes == mins
          OutlinedButton(
            onClick = { viewModel.setSleepTimer(mins) },
            border = BorderStroke(1.dp, if (active) MornicaAmberStart else MornicaSlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("$mins minutes", color = if (active) MornicaAmberStart else MornicaTextLight, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = {
        viewModel.setSleepTimer(null)
        viewModel.setSleepTimerDialogOpen(false)
      }) {
        Text("Turn Off", color = MornicaDangerRed)
      }
    },
    dismissButton = {
      TextButton(onClick = { viewModel.setSleepTimerDialogOpen(false) }) {
        Text("Close", color = MornicaTextMuted)
      }
    },
  )
}

@Composable
private fun OnboardingDialog(
  state: MusicoletUiState,
  onImportAudio: () -> Unit,
  onPickEqPreset: (String) -> Unit,
  onFinishFree: () -> Unit,
  onUpgradePro: () -> Unit,
) {
  var step by remember { mutableIntStateOf(1) }

  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      color = MornicaMidnightBg,
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding(),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Welcome to Moronto Music", color = MornicaEmerald, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
          Text("Step $step / 3", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
          when (step) {
            1 -> {
              Text("1. Import Your Offline Music", color = MornicaTextLight, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
              Text(
                "Add MP3, WAV, FLAC, M4A, or OGG files straight from your device, or explore the 6 studio tracks already bundled offline.",
                color = MornicaTextMuted,
                fontSize = 13.sp,
              )
              Button(
                onClick = onImportAudio,
                colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Local Audio Files (${state.tracks.size} in library)", fontWeight = FontWeight.Bold)
              }
            }
            2 -> {
              Text("2. Pick Your Favourite 10-Band EQ Preset", color = MornicaTextLight, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
              Text(
                "Preview any acoustic curve during setup — tuned for Nigerian clubs, choirs, brass, and studio monitors.",
                color = MornicaTextMuted,
                fontSize = 13.sp,
              )
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EQ_PRESETS.forEach { preset ->
                  val selected = state.equalizer.presetId == preset.id
                  Surface(
                    onClick = { onPickEqPreset(preset.id) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MornicaSlateElevated else MornicaSlateCard,
                    border = BorderStroke(1.dp, if (selected) MornicaEmerald else MornicaSlateBorder),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Row(
                      modifier = Modifier.padding(14.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(preset.name, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      Text(
                        if (selected) "Selected" else "Select",
                        color = if (selected) MornicaEmerald else MornicaTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                      )
                    }
                  }
                }
              }
            }
            else -> {
              Text("3. Full Access Free Today · Pro Coming Soon", color = MornicaTextLight, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
              Text(
                "All 10-band EQ presets, 0–12s dual-deck crossfade, Sleep Timer, and all 4 studio themes are 100% free right now. Pro launches when Global Catalog Streaming arrives!",
                color = MornicaTextMuted,
                fontSize = 13.sp,
              )
              PRO_COMING_SOON_HIGHLIGHTS.forEach { bullet ->
                Surface(
                  shape = RoundedCornerShape(14.dp),
                  color = MornicaSlateCard,
                  border = BorderStroke(1.dp, MornicaEmerald),
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Text(
                    text = "• $bullet",
                    color = MornicaTextLight,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp),
                  )
                }
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          if (step > 1) {
            OutlinedButton(onClick = { step-- }) {
              Text("Back", color = MornicaTextLight)
            }
          } else {
            TextButton(onClick = onFinishFree) {
              Text("Skip Setup", color = MornicaTextMuted)
            }
          }
          if (step < 3) {
            Button(
              onClick = { step++ },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
            ) {
              Text("Continue", fontWeight = FontWeight.Bold)
            }
          } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(onClick = onUpgradePro) {
                Text("Pro Roadmap", color = MornicaAmberStart)
              }
              Button(
                onClick = onFinishFree,
                colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
              ) {
                Text("Enter Studio", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RatingPromptDialog(
  sessionCount: Int,
  onSubmit: (Int, String) -> Unit,
  onDismiss: () -> Unit,
) {
  var stars by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MornicaSlateHeader,
    title = { Text("Enjoying Moronto Music? (Session #$sessionCount)", color = MornicaEmerald, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tap to rate your offline studio experience:", color = MornicaTextMuted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          (1..5).forEach { idx ->
            IconButton(onClick = { stars = idx }) {
              Icon(
                imageVector = if (idx <= stars) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = "$idx stars",
                tint = MornicaAmberStart,
                modifier = Modifier.size(30.dp),
              )
            }
          }
        }
        TagField("Optional feedback note", comment) { comment = it }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(stars, comment) },
        colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
      ) {
        Text("Submit Rating", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Not Now", color = MornicaTextMuted)
      }
    },
  )
}

@Composable
private fun WhatsAppSharePreviewDialog(
  track: OfflineTrack,
  onDismiss: () -> Unit,
  onShareSheet: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MornicaSlateHeader,
    title = { Text("WhatsApp Status Card (9:16)", color = MornicaEmerald, fontWeight = FontWeight.Bold) },
    text = {
      Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = MornicaMidnightBg,
          border = BorderStroke(1.5.dp, MornicaEmerald),
          modifier = Modifier
            .fillMaxWidth(0.7f)
            .aspectRatio(9f / 16f),
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text("MORONTO MUSIC", color = MornicaEmerald, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Surface(
              shape = CircleShape,
              color = MornicaEmeraldDark,
              border = BorderStroke(2.dp, MornicaEmerald),
              modifier = Modifier.size(84.dp),
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = MornicaEmerald, modifier = Modifier.size(40.dp))
              }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(track.title, color = MornicaTextLight, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 2)
              Text(track.artist, color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text("${track.album} · ${track.folderName}", color = MornicaTextMuted, fontSize = 10.sp)
            }
            Text("1080×1920 PNG · Ready for WhatsApp Status", color = MornicaAmberStart, fontSize = 9.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onShareSheet,
        colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
      ) {
        Text("Share to WhatsApp Status", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = MornicaTextMuted)
      }
    },
  )
}

@Composable
private fun TagEditorDialog(
  track: OfflineTrack,
  onDismiss: () -> Unit,
  onSave: (TrackTagPatch) -> Unit,
) {
  var title by remember(track.id) { mutableStateOf(track.title) }
  var artist by remember(track.id) { mutableStateOf(track.artist) }
  var album by remember(track.id) { mutableStateOf(track.album) }
  var folderName by remember(track.id) { mutableStateOf(track.folderName) }
  var yearText by remember(track.id) { mutableStateOf(track.releaseYear.toString()) }
  var lyricsText by remember(track.id) { mutableStateOf(track.lyricsText) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MornicaSlateHeader,
    title = { Text("Tag Editor & Lyrics", color = MornicaEmerald, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .heightIn(max = 380.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        TagField("Title", title) { title = it }
        TagField("Artist", artist) { artist = it }
        TagField("Album", album) { album = it }
        TagField("Folder", folderName) { folderName = it }
        TagField("Year", yearText) { yearText = it }
        TagField("Lyrics ([mm:ss.xx] LRC or plain)", lyricsText, singleLine = false) { lyricsText = it }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            TrackTagPatch(
              title = title,
              artist = artist,
              album = album,
              folderName = folderName,
              releaseYear = yearText.toIntOrNull() ?: track.releaseYear,
              lyricsText = lyricsText,
            ),
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
      ) {
        Text("Save Tags", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = MornicaTextMuted)
      }
    },
  )
}

@Composable
private fun TagField(
  label: String,
  value: String,
  singleLine: Boolean = true,
  onValueChange: (String) -> Unit,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, color = MornicaTextMuted, fontSize = 11.sp) },
    singleLine = singleLine,
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = MornicaSlateCard,
      unfocusedContainerColor = MornicaSlateCard,
      focusedBorderColor = MornicaEmerald,
      unfocusedBorderColor = MornicaSlateBorder,
      focusedTextColor = MornicaTextLight,
      unfocusedTextColor = MornicaTextLight,
    ),
    shape = RoundedCornerShape(10.dp),
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun LoadingSplashOverlay(
  onEnterApp: () -> Unit,
) {
  var targetProgress by remember { mutableStateOf(0.18f) }
  LaunchedEffect(Unit) {
    targetProgress = 1f
  }
  val animatedProgress by animateFloatAsState(
    targetValue = targetProgress,
    animationSpec = tween(durationMillis = 1250, easing = LinearEasing),
    label = "splashProgress",
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(MornicaMidnightBg, MornicaSlateHeader, MornicaMidnightBg),
        ),
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MornicaSlateCard,
      border = BorderStroke(1.dp, MornicaSlateBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Surface(
          shape = CircleShape,
          color = MornicaEmeraldDark,
          border = BorderStroke(1.5.dp, MornicaEmerald),
          modifier = Modifier.size(72.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Rounded.GraphicEq,
              contentDescription = null,
              tint = MornicaEmerald,
              modifier = Modifier.size(34.dp),
            )
          }
        }
        Text(
          text = "Moronto Music",
          color = MornicaTextLight,
          fontSize = 22.sp,
          fontWeight = FontWeight.ExtraBold,
        )
        Text(
          text = "ShadowSpark Technologies · Pure Offline Studio Audio",
          color = MornicaTextMuted,
          fontSize = 12.sp,
        )
        LinearProgressIndicator(
          progress = { animatedProgress },
          color = MornicaEmerald,
          trackColor = MornicaSlateHeader,
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
        )
        Text(
          text = "Preparing 20 queues · 10-band EQ · 0–12s dual-deck crossfade",
          color = MornicaCyan,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
        )
        Button(
          onClick = onEnterApp,
          colors = ButtonDefaults.buttonColors(
            containerColor = MornicaEmerald,
            contentColor = Color(0xFF04130E),
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Enter Studio", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun UpgradeScreen(
  state: MusicoletUiState,
  strings: MorontoLocalizedStrings,
  viewModel: MusicoletViewModel,
) {
  var emailInput by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaAmberStart),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("COMING SOON · GLOBAL CATALOG STREAMING", color = MornicaAmberStart, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
          Text(strings.proUpgradeHeading, color = MornicaTextLight, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "Every local offline tool is 100% free and unlocked for all listeners today. Moronto Music Pro will launch when global licensed catalog streaming arrives.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateElevated,
        border = BorderStroke(1.dp, MornicaEmerald),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Unlocked Today for Everyone (₦0)", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("• All 5 10-Band EQ presets (Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife, Flat)", color = MornicaTextLight, fontSize = 12.sp)
          Text("• Dual-deck 0–12s Crossfade, Gapless Playback & Sleep Timer", color = MornicaTextLight, fontSize = 12.sp)
          Text("• All 4 Studio Themes, 20 Queues & Zero Ads", color = MornicaTextLight, fontSize = 12.sp)
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("What Arrives with Pro Later", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          PRO_COMING_SOON_HIGHLIGHTS.forEach { bullet ->
            Text("• $bullet", color = MornicaTextMuted, fontSize = 12.sp)
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateElevated,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Global Streaming Launch Waitlist", color = MornicaAmberStart, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          if (state.proNotifyEmail != null) {
            Text("Waitlist confirmed: ${state.proNotifyEmail}", color = MornicaTextLight, fontSize = 12.sp)
            OutlinedButton(
              onClick = { viewModel.clearProNotifyEmail() },
              border = BorderStroke(1.dp, MornicaSlateBorder),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Change Email", color = MornicaAmberStart, fontSize = 12.sp)
            }
          } else {
            TagField("you@example.com", emailInput) { emailInput = it }
            if (errorText != null) {
              Text(errorText!!, color = MornicaDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
              onClick = {
                val err = viewModel.submitProNotifyEmail(emailInput)
                if (err != null) {
                  errorText = err
                } else {
                  errorText = null
                  emailInput = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaAmberStart, contentColor = Color(0xFF04130E)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(strings.restorePurchases, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    item {
      Text("Studio Color Themes (All 4 Unlocked for Everyone)", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }

    items(MORONTO_THEMES, key = { it.id }) { theme ->
      val selected = state.themeId == theme.id
      Surface(
        onClick = { viewModel.setThemeId(theme.id) },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MornicaSlateElevated else MornicaSlateCard,
        border = BorderStroke(1.dp, if (selected) MornicaEmerald else MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(theme.name, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(theme.description, color = MornicaTextMuted, fontSize = 11.sp)
          }
          Text(
            text = if (selected) "Active" else "Apply",
            color = if (selected) MornicaEmerald else MornicaCyan,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
          )
        }
      }
    }
  }
}

@Composable
private fun LiveScreen(
  state: MusicoletUiState,
  strings: MorontoLocalizedStrings,
  viewModel: MusicoletViewModel,
) {
  var emailInput by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(strings.liveComingSoonHeading, color = MornicaAmberStart, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "Low-data African acoustic sessions, Sunday gospel choir streams, and Lagos selector sets — launching soon inside Moronto Music.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
        }
      }
    }

    items(LIVE_SHOW_PREVIEWS, key = { it.id }) { show ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text("${show.genre} · ${show.scheduleLabel}", color = MornicaAmberStart, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Text(show.title, color = MornicaTextLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          Text(show.host, color = MornicaCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          Text(show.summary, color = MornicaTextMuted, fontSize = 12.sp)
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateElevated,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Live Broadcast Early Access", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          if (state.liveNotifyEmail != null) {
            Text("Subscribed for Live launch alerts: ${state.liveNotifyEmail}", color = MornicaTextLight, fontSize = 12.sp)
            OutlinedButton(
              onClick = { viewModel.clearLiveNotifyEmail() },
              border = BorderStroke(1.dp, MornicaSlateBorder),
            ) {
              Text("Change Email", color = MornicaAmberStart, fontSize = 12.sp)
            }
          } else {
            TagField("you@example.com", emailInput) { emailInput = it }
            if (errorText != null) {
              Text(errorText!!, color = MornicaDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
              onClick = {
                val err = viewModel.submitLiveNotifyEmail(emailInput)
                if (err != null) errorText = err
                else {
                  errorText = null
                  emailInput = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Notify Me When Live Launches", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StorageScreen(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
  onImportAudio: () -> Unit,
) {
  val stats = remember(state.tracks) { MusicoletEngine.estimateStorageStats(state.tracks) }
  val deviceTracks = remember(state.tracks) { state.tracks.filter { it.sourceType == TrackSourceType.DEVICE } }
  val totalMb = String.format(java.util.Locale.US, "%.1f MB", stats.totalEstimatedBytes / (1024.0 * 1024.0))

  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Data-Saver & Storage Manager", color = MornicaEmerald, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "Tracks: ${stats.totalTracks} total (${stats.bundledTracks} bundled, ${stats.deviceTracks} imported) · Est. $totalMb",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Strict Data-Saver Mode", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("Stay 100% local — block background artwork or preview fetches.", color = MornicaTextMuted, fontSize = 11.sp)
            }
            Switch(
              checked = state.dataSaverEnabled,
              onCheckedChange = { viewModel.toggleDataSaver() },
              colors = SwitchDefaults.colors(checkedThumbColor = MornicaMidnightBg, checkedTrackColor = MornicaEmerald),
            )
          }
          Button(
            onClick = onImportAudio,
            colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Import Offline Audio Files", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text("Imported Device Audio (${deviceTracks.size})", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }

    if (deviceTracks.isEmpty()) {
      item {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MornicaSlateCard,
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            "No device audio files imported yet. Tap Import Offline Audio Files above.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(14.dp),
          )
        }
      }
    } else {
      items(deviceTracks, key = { it.id }) { track ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MornicaSlateCard,
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(track.title, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
              Text("${track.artist} · ${MusicoletEngine.formatDurationMs(track.durationMs)}", color = MornicaTextMuted, fontSize = 11.sp)
            }
            IconButton(onClick = { viewModel.deleteImportedTrack(track.id) }) {
              Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove", tint = MornicaDangerRed)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GuideScreen(
  strings: MorontoLocalizedStrings,
  viewModel: MusicoletViewModel,
) {
  val steps = listOf(
    "01 · Import Local Audio" to "Tap the folder icon in the header or Storage tab to add MP3, WAV, FLAC, M4A, or OGG files.",
    "02 · Switch Independent Queues" to "Save up to 20 independent queues with per-queue playback timestamps.",
    "03 · Tune 10-Band EQ & Presets" to "Open 10-EQ from the header for 31Hz–16kHz bands plus Afrobeats Bass, Amapiano Log Drum, Gospel, Highlife & Flat presets — all unlocked free.",
    "04 · Crossfade, Sleep Timer & Themes" to "0–12s dual-deck crossfade, Sleep Timer, and all 4 Studio Color Themes are 100% free right now. Pro launches later with Global Catalog Streaming.",
    "05 · Share WhatsApp Status Cards" to "Tap Share on any track to generate a branded 1080×1920 9:16 PNG card.",
    "06 · Pidgin Guide & Support Desk" to "Check Blog · Pidgin Guide for our funny offline storage guide and direct Support contact (support@morontomusic.app) if you need help moving songs onto your phone.",
  )

  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(strings.userGuideHeading, color = MornicaEmerald, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text("Everything you need to run Moronto Music 100% offline with zero paywalls.", color = MornicaTextMuted, fontSize = 12.sp)
          Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { viewModel.replayOnboarding() }) {
              Text("Replay 3-Step Onboarding", color = MornicaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = { viewModel.triggerTestFifthSessionPrompt() }) {
              Text("Preview 5th-Session Rating", color = MornicaAmberStart, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = { viewModel.selectTab(LibraryViewTab.BLOG) }) {
              Text("Pidgin Guide & Support", color = MornicaEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    items(steps, key = { it.first }) { (title, body) ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(title, color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(body, color = MornicaTextMuted, fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
private fun PrivacyScreen(
  @Suppress("UNUSED_PARAMETER") state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Privacy Policy & Offline Ownership", color = MornicaEmerald, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "Moronto Music keeps your offline library, queues, tags, and equalizer settings strictly on your device — with zero third-party ad networks or tracking SDKs.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Text(
            "Need help getting locally downloaded music files onto your phone? Visit Blog · Pidgin Guide or contact $SUPPORT_EMAIL anytime.",
            color = MornicaTextLight,
            fontSize = 12.sp,
          )
          Button(
            onClick = { viewModel.selectTab(LibraryViewTab.BLOG) },
            colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text("Open Pidgin Guide & Support", fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun SponsorScreen(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  var billing by remember { mutableStateOf("monthly") }
  var selectedTierId by remember { mutableStateOf(SPONSOR_TIERS[1].id) }
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }
  var copiedNotice by remember { mutableStateOf(false) }
  val clipboard = LocalClipboardManager.current

  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Sponsor Moronto Music", color = MornicaEmerald, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "Back ad-free offline playback, African indie licensing showcases, and hardware DSP engineering.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("monthly" to "Monthly", "one-time" to "One-Time").forEach { (key, label) ->
              val active = billing == key
              OutlinedButton(
                onClick = { billing = key },
                border = BorderStroke(1.dp, if (active) MornicaEmerald else MornicaSlateBorder),
                shape = RoundedCornerShape(10.dp),
              ) {
                Text(label, color = if (active) MornicaEmerald else MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Direct Desk: sponsor@morontomusic.app",
              color = MornicaCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = {
              clipboard.setText(AnnotatedString("sponsor@morontomusic.app"))
              copiedNotice = true
            }) {
              Text(if (copiedNotice) "Copied" else "Copy Email", color = MornicaAmberStart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    items(SPONSOR_TIERS, key = { it.id }) { tier ->
      val selected = selectedTierId == tier.id
      val price = if (billing == "monthly") "$${tier.monthlyUsd}/mo" else "$${tier.oneTimeUsd} once"
      Surface(
        onClick = { selectedTierId = tier.id },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MornicaSlateElevated else MornicaSlateCard,
        border = BorderStroke(1.dp, if (selected) MornicaEmerald else MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(tier.name, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(price, color = MornicaEmerald, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
          }
          Text(tier.summary, color = MornicaTextMuted, fontSize = 12.sp)
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Record Sponsorship Pledge", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          TagField("Your Name or Organization", name) { name = it }
          TagField("Contact Email", email) { email = it }
          TagField("Optional Dedication or Partner Note", note) { note = it }
          if (errorText != null) {
            Text(errorText!!, color = MornicaDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Button(
            onClick = {
              val err = viewModel.submitSponsorPledge(name, email, selectedTierId, billing, note)
              if (err != null) {
                errorText = err
              } else {
                errorText = null
                name = ""
                email = ""
                note = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Submit Sponsorship Pledge", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    if (state.sponsorPledges.isNotEmpty()) {
      item {
        Text(
          text = "Saved Sponsorship Ledger (${state.sponsorPledges.size} recorded)",
          color = MornicaCyan,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
        )
      }
      items(state.sponsorPledges, key = { it.id }) { pledge ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MornicaSlateCard,
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(pledge.name, color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text("$${pledge.amountUsd} · ${pledge.billing}", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Text("${pledge.tierId} · ${pledge.email}", color = MornicaTextMuted, fontSize = 11.sp)
            if (pledge.note.isNotBlank()) {
              Text(pledge.note, color = MornicaTextLight, fontSize = 11.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BlogScreen(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  var emailInput by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }
  var deviceType by remember { mutableStateOf("Android") }
  var supportTopic by remember { mutableStateOf("") }
  var supportMessage by remember { mutableStateOf("") }
  var supportError by remember { mutableStateOf<String?>(null) }
  var supportSentNote by remember { mutableStateOf<String?>(null) }
  var copiedSupport by remember { mutableStateOf(false) }
  val clipboard = LocalClipboardManager.current

  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaEmerald),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            "${PIDGIN_OFFLINE_ARTICLE.eyebrow} · ${PIDGIN_OFFLINE_ARTICLE.readTime}",
            color = MornicaAmberStart,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(PIDGIN_OFFLINE_ARTICLE.title, color = MornicaTextLight, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
          Text(PIDGIN_OFFLINE_ARTICLE.subtitle, color = MornicaCyan, fontSize = 12.sp)
          PIDGIN_OFFLINE_ARTICLE.sections.forEach { sec ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MornicaSlateElevated,
              border = BorderStroke(1.dp, MornicaSlateBorder),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(sec.heading, color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(sec.body, color = MornicaTextLight, fontSize = 12.sp)
              }
            }
          }
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaCyan),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Need Help Getting Music Files Into Your Device?", color = MornicaCyan, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "If your MP3, WAV, or FLAC songs are stuck in WhatsApp Documents, Telegram, SD card, or laptop, message Support directly or copy $SUPPORT_EMAIL.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(SUPPORT_EMAIL, color = MornicaEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = {
              clipboard.setText(AnnotatedString(SUPPORT_EMAIL))
              copiedSupport = true
            }) {
              Text(if (copiedSupport) "Copied!" else "Copy Support Email", color = MornicaAmberStart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
          TagField("Device (Android, iPhone, Windows, Mac)", deviceType) { deviceType = it }
          TagField("Short Subject (e.g. Moving WhatsApp MP3s)", supportTopic) { supportTopic = it }
          TagField("Where are your audio files right now?", supportMessage, singleLine = false) { supportMessage = it }
          if (supportError != null) {
            Text(supportError!!, color = MornicaDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          if (supportSentNote != null) {
            Text(supportSentNote!!, color = MornicaEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Button(
            onClick = {
              val err = MusicoletEngine.validateSupportRequestInput(deviceType, supportTopic, supportMessage)
              if (err != null) {
                supportError = err
                supportSentNote = null
              } else {
                supportError = null
                clipboard.setText(
                  AnnotatedString("To: $SUPPORT_EMAIL\nDevice: $deviceType\nSubject: $supportTopic\n\n$supportMessage"),
                )
                supportSentNote = "Request validated & copied! Email $SUPPORT_EMAIL or submit via Moronto Music Web Support Desk for cloud tracking."
                supportTopic = ""
                supportMessage = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MornicaCyan, contentColor = Color(0xFF04130E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text("Prepare & Copy Support Message", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    item {
      Text("Upcoming Engineering Dispatches · Coming Soon", color = MornicaAmberStart, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }

    items(BLOG_PREVIEW_POSTS, key = { it.id }) { post ->
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "${post.category} · ${post.readTime} · Coming Soon",
            color = MornicaAmberStart,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(post.title, color = MornicaTextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
          Text(post.summary, color = MornicaTextMuted, fontSize = 12.sp)
        }
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MornicaSlateElevated,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Launch Notification Waitlist", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          if (state.blogNotifyEmail != null) {
            Text(
              text = "Subscribed for first dispatch: ${state.blogNotifyEmail}",
              color = MornicaTextLight,
              fontSize = 12.sp,
            )
            OutlinedButton(
              onClick = { viewModel.clearBlogNotifyEmail() },
              border = BorderStroke(1.dp, MornicaSlateBorder),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Change Email", color = MornicaAmberStart, fontSize = 12.sp)
            }
          } else {
            TagField("you@example.com", emailInput) { emailInput = it }
            if (errorText != null) {
              Text(errorText!!, color = MornicaDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Button(
              onClick = {
                val err = viewModel.submitBlogNotifyEmail(emailInput)
                if (err != null) {
                  errorText = err
                } else {
                  errorText = null
                  emailInput = ""
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text("Notify Me When Blog Launches", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AboutScreen(
  state: MusicoletUiState,
  viewModel: MusicoletViewModel,
) {
  LazyColumn(
    contentPadding = PaddingValues(bottom = 24.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("About Moronto Music", color = MornicaEmerald, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
          Text(
            "Built by ShadowSpark Technologies in Nigeria for listeners who want folder-first offline ownership, persistent multi-queue memory, and studio-grade acoustic control.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { viewModel.selectTab(LibraryViewTab.TRACKS) },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald, contentColor = Color(0xFF04130E)),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Open Library", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            OutlinedButton(
              onClick = { viewModel.replaySplash() },
              border = BorderStroke(1.dp, MornicaSlateBorder),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text("Replay Intro", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AboutMetricCard("Queues", "$MAX_QUEUES", "Independent states", Modifier.weight(1f))
        AboutMetricCard("Equalizer", "10-Band", "Bass + Preamp DSP", Modifier.weight(1f))
      }
    }
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AboutMetricCard("Crossfade", "0–12s", "Dual ExoPlayer decks", Modifier.weight(1f))
        AboutMetricCard("Tracks", "${state.tracks.size}", "Offline & local URIs", Modifier.weight(1f))
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MornicaSlateCard,
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("Audio & Rights Architecture", color = MornicaTextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(
            "Dual ExoPlayer decks execute 40ms stepped crossfades with Equalizer, BassBoost, Virtualizer, and LoudnessEnhancer hardware binding — strictly offline-first with zero hidden network streaming.",
            color = MornicaTextMuted,
            fontSize = 12.sp,
          )
          Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { viewModel.selectTab(LibraryViewTab.GUIDE) }) {
              Text("User Guide", color = MornicaCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            TextButton(onClick = { viewModel.selectTab(LibraryViewTab.SPONSOR) }) {
              Text("Sponsor Us", color = MornicaEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            TextButton(onClick = { viewModel.selectTab(LibraryViewTab.BLOG) }) {
              Text("Blog · Coming Soon", color = MornicaAmberStart, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AboutMetricCard(
  label: String,
  value: String,
  sub: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MornicaSlateCard,
    border = BorderStroke(1.dp, MornicaSlateBorder),
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(label, color = MornicaTextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
      Text(value, color = MornicaEmerald, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
      Text(sub, color = MornicaCyan, fontSize = 11.sp)
    }
  }
}
