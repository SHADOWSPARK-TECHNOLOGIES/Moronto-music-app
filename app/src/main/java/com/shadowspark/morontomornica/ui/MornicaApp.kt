package com.shadowspark.morontomornica.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.shadowspark.morontomornica.domain.LocalMediaRepository
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.ArtistLaunchRequest
import com.shadowspark.morontomornica.model.LibraryTab
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.model.PrimaryScreen
import com.shadowspark.morontomornica.model.ProvenanceStatus
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.model.TrackSortOption
import com.shadowspark.morontomornica.model.YouTubeVideoCard

@Composable
fun MornicaApp(
  router: PlaybackRouter,
  localRepo: LocalMediaRepository,
  onRequestAudioPermission: () -> Unit,
) {
  val uiState by router.state.collectAsState()
  val context = LocalContext.current

  MornicaTheme {
    Scaffold(
      containerColor = MornicaMidnightBg,
      topBar = {
        MorontoTopHeader(
          uiState = uiState,
          onAiDiscoveryClick = { router.selectScreen(PrimaryScreen.DISCOVER_WATCH) },
          onEqualizerClick = {
            if (uiState.activeLocalTrack != null) {
              router.setNowPlayingExpanded(true)
            } else {
              router.selectScreen(PrimaryScreen.MY_MUSIC)
            }
          },
          onInfoClick = { router.selectScreen(PrimaryScreen.CREATOR_HUB) },
        )
      },
      bottomBar = {
        Column(modifier = Modifier.navigationBarsPadding()) {
          if (uiState.activeLocalTrack != null && !uiState.isNowPlayingExpanded) {
            OfflineMiniPlayerBar(uiState = uiState, router = router)
          }
          HorizontalDivider(color = MornicaSlateBorder, thickness = 1.dp)
          NavigationBar(
            containerColor = MornicaSlateHeader,
            tonalElevation = 0.dp,
          ) {
            PrimaryScreen.entries.forEach { screen ->
              val selected = uiState.currentScreen == screen && !uiState.isNowPlayingExpanded
              NavigationBarItem(
                selected = selected,
                onClick = {
                  router.setNowPlayingExpanded(false)
                  router.selectScreen(screen)
                },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = MornicaEmerald,
                    selectedTextColor = MornicaEmerald,
                    indicatorColor = MornicaEmeraldDark,
                    unselectedIconColor = MornicaTextMuted,
                    unselectedTextColor = MornicaTextMuted,
                  ),
                icon = {
                  Icon(
                    imageVector =
                      when (screen) {
                        PrimaryScreen.MY_MUSIC -> Icons.Default.MusicNote
                        PrimaryScreen.DISCOVER_WATCH -> Icons.Default.OndemandVideo
                        PrimaryScreen.CREATOR_HUB -> Icons.Default.WorkspacePremium
                      },
                    contentDescription = screen.title,
                  )
                },
                label = {
                  Text(
                    text = screen.title,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  )
                },
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .background(MornicaMidnightBg)
            .padding(innerPadding),
      ) {
        when (uiState.currentScreen) {
          PrimaryScreen.DISCOVER_WATCH ->
            DiscoverWatchScreen(
              uiState = uiState,
              router = router,
              onOpenExternalYouTube = { watchUrl ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(watchUrl))
                context.startActivity(intent)
              },
            )
          PrimaryScreen.MY_MUSIC ->
            MyMusicOfflineScreen(
              uiState = uiState,
              router = router,
              localRepo = localRepo,
              onRequestAudioPermission = onRequestAudioPermission,
            )
          PrimaryScreen.CREATOR_HUB ->
            CreatorHubScreen(
              uiState = uiState,
              router = router,
            )
        }

        if (uiState.isNowPlayingExpanded && uiState.activeLocalTrack != null) {
          NowPlayingFullSheet(
            uiState = uiState,
            router = router,
          )
        }
      }
    }
  }
}

@Composable
private fun MorontoTopHeader(
  uiState: MornicaUiState,
  onAiDiscoveryClick: () -> Unit,
  onEqualizerClick: () -> Unit,
  onInfoClick: () -> Unit,
) {
  Surface(
    color = MornicaSlateHeader,
    modifier = Modifier.fillMaxWidth().statusBarsPadding(),
  ) {
    Column {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier =
              Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MornicaSlateCard)
                .border(1.dp, MornicaSlateBorder, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = "Moronto Music Emblem",
              tint = MornicaEmerald,
              modifier = Modifier.size(22.dp),
            )
          }
          Column {
            Text(
              text = "Moronto Music",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MornicaTextLight,
            )
            Text(
              text = "by ShadowSpark Technologies ⚡",
              style = MaterialTheme.typography.labelMedium,
              color = MornicaCyan,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          IconButton(onClick = onAiDiscoveryClick) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "AI & YouTube Discovery",
              tint = MornicaGoldSparkle,
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(onClick = onEqualizerClick) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = "Playback Timer & Controls",
              tint = MornicaTextMuted,
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(onClick = onEqualizerClick) {
            Icon(
              imageVector = Icons.Default.Equalizer,
              contentDescription = "Audio Equalizer & Now Playing",
              tint = MornicaCyan,
              modifier = Modifier.size(20.dp),
            )
          }
          IconButton(onClick = onInfoClick) {
            Box(
              modifier =
                Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(MornicaInfoBlue),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "About & Rights Info",
                tint = MornicaTextLight,
                modifier = Modifier.size(16.dp),
              )
            }
          }
        }
      }

      uiState.statusBannerMessage?.let { msg ->
        Text(
          text = msg,
          style = MaterialTheme.typography.labelSmall,
          color = MornicaAmberStart,
          modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        )
      }
      HorizontalDivider(color = MornicaSlateBorder, thickness = 1.dp)
    }
  }
}

private fun libraryTabIcon(tab: LibraryTab): ImageVector =
  when (tab) {
    LibraryTab.SONGS -> Icons.Default.MusicNote
    LibraryTab.PLAYLISTS -> Icons.AutoMirrored.Filled.QueueMusic
    LibraryTab.ALBUMS -> Icons.Default.Album
    LibraryTab.ARTISTS -> Icons.Default.Person
    LibraryTab.FOLDERS -> Icons.Default.Folder
  }

@Composable
private fun MyMusicOfflineScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  localRepo: LocalMediaRepository,
  onRequestAudioPermission: () -> Unit,
) {
  val filteredSortedTracks =
    remember(uiState.localTracks, uiState.localSearchQuery, uiState.trackSortOption) {
      router.filteredAndSortedLocalTracks()
    }

  Column(modifier = Modifier.fillMaxSize()) {
    // 1. 5-Tab Musicolet-Style Icon + Label Top Bar (Tracks | Queues | Albums | Artists | Folders)
    Surface(
      color = MornicaSlateHeader,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        LibraryTab.entries.forEach { tab ->
          val isSelected = uiState.selectedLibraryTab == tab
          val tint = if (isSelected) MornicaEmerald else MornicaTextMuted
          Column(
            modifier =
              Modifier
                .weight(1f)
                .selectable(
                  selected = isSelected,
                  role = Role.Tab,
                  onClick = { router.selectLibraryTab(tab) },
                )
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Icon(
              imageVector = libraryTabIcon(tab),
              contentDescription = null,
              tint = tint,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = tab.label,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = tint,
              maxLines = 1,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .height(3.dp)
                  .background(if (isSelected) MornicaEmerald else Color.Transparent),
            )
          }
        }
      }
    }
    HorizontalDivider(color = MornicaSlateBorder, thickness = 1.dp)

    // 2. Main Library Content
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Search input + Square Refresh/Rescan button row (matching old-home.png)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = uiState.localSearchQuery,
            onValueChange = { router.updateLocalSearchQuery(it) },
            placeholder = {
              Text(
                text = "Search title, artist, genre...",
                color = MornicaTextMuted,
                style = MaterialTheme.typography.bodyMedium,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search tracks",
                tint = MornicaTextMuted,
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MornicaSlateCard,
                unfocusedContainerColor = MornicaSlateCard,
                focusedBorderColor = MornicaEmerald,
                unfocusedBorderColor = MornicaSlateBorder,
                focusedTextColor = MornicaTextLight,
                unfocusedTextColor = MornicaTextLight,
              ),
            modifier = Modifier.weight(1f),
          )

          Surface(
            onClick = onRequestAudioPermission,
            color = MornicaSlateCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier.size(54.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Scan Device Audio Files",
                tint = MornicaCyan,
                modifier = Modifier.size(24.dp),
              )
            }
          }
        }
      }

      // Live track count + Sort pills (Title | Artist | Duration | Year) matching old-home.png
      item {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${filteredSortedTracks.size} tracks",
            style = MaterialTheme.typography.labelLarge.withTabularNums(),
            color = MornicaTextMuted,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 12.dp),
          )

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = "Sort:",
              style = MaterialTheme.typography.labelMedium,
              color = MornicaTextMuted,
            )
            TrackSortOption.entries.forEach { option ->
              val selected = uiState.trackSortOption == option
              Surface(
                onClick = { router.selectTrackSortOption(option) },
                color = if (selected) MornicaEmerald else MornicaSlateCard,
                shape = RoundedCornerShape(MornicaTokens.SortPillRadius),
                border =
                  if (selected) {
                    null
                  } else {
                    BorderStroke(1.dp, MornicaSlateBorder)
                  },
              ) {
                Text(
                  text = option.label,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (selected) Color(0xFF04130E) else MornicaTextMuted,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }
            }
          }
        }
      }

      // Subtle device MediaStore scan banner when permission is not yet granted
      if (!uiState.hasAudioPermission) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
            shape = RoundedCornerShape(MornicaTokens.CardOuterRadius),
            border = BorderStroke(1.dp, MornicaSlateBorder),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(MornicaTokens.CardInnerPadding),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                  text = "Bundled First-Party Synth Previews Active",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold,
                  color = MornicaEmerald,
                )
                Text(
                  text = "Tap Rescan or Grant Audio Access to load your phone's MP3, FLAC, and WAV folders.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MornicaTextMuted,
                )
              }
              Button(
                onClick = onRequestAudioPermission,
                colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
                shape = RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius),
              ) {
                Text("Scan Phone", color = Color(0xFF04130E), fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      when (uiState.selectedLibraryTab) {
        LibraryTab.SONGS -> {
          items(filteredSortedTracks, key = { it.id }) { track ->
            LocalTrackItemCard(track = track, uiState = uiState, router = router)
          }
        }
        LibraryTab.FOLDERS -> {
          val grouped = localRepo.groupByFolder(filteredSortedTracks)
          grouped.forEach { (folder, tracks) ->
            item {
              LibraryGroupHeader(title = "Folder · $folder", count = tracks.size)
            }
            items(tracks, key = { "folder-${it.id}" }) { track ->
              LocalTrackItemCard(track = track, uiState = uiState, router = router)
            }
          }
        }
        LibraryTab.ARTISTS -> {
          val grouped = localRepo.groupByArtist(filteredSortedTracks)
          grouped.forEach { (artist, tracks) ->
            item {
              LibraryGroupHeader(title = "Artist · $artist", count = tracks.size)
            }
            items(tracks, key = { "artist-${it.id}" }) { track ->
              LocalTrackItemCard(track = track, uiState = uiState, router = router)
            }
          }
        }
        LibraryTab.ALBUMS -> {
          val grouped = localRepo.groupByAlbum(filteredSortedTracks)
          grouped.forEach { (album, tracks) ->
            item {
              LibraryGroupHeader(title = "Album · $album", count = tracks.size)
            }
            items(tracks, key = { "album-${it.id}" }) { track ->
              LocalTrackItemCard(track = track, uiState = uiState, router = router)
            }
          }
        }
        LibraryTab.PLAYLISTS -> {
          uiState.playlists.forEach { (playlistName, trackIds) ->
            item {
              LibraryGroupHeader(title = "Queue · $playlistName", count = trackIds.size)
            }
            val tracks = filteredSortedTracks.filter { trackIds.contains(it.id) }
            items(tracks, key = { "pl-$playlistName-${it.id}" }) { track ->
              LocalTrackItemCard(track = track, uiState = uiState, router = router)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LibraryGroupHeader(
  title: String,
  count: Int,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      color = MornicaCyan,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = "$count tracks",
      style = MaterialTheme.typography.labelSmall.withTabularNums(),
      color = MornicaTextMuted,
    )
  }
}

@Composable
private fun LocalTrackItemCard(
  track: LocalMediaTrack,
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  val isSaved = uiState.favoriteTrackIds.contains(track.id)
  val isActive = uiState.activeLocalTrack?.id == track.id
  val isPlayable = track.isPlayableOffline()
  var menuExpanded by remember { mutableStateOf(false) }

  val provenanceShort =
    when (track.provenance) {
      ProvenanceStatus.LOCAL_DEVICE -> "Device Audio"
      ProvenanceStatus.DEMO -> "Synth Preview"
      ProvenanceStatus.LICENSED -> "Licensed"
      ProvenanceStatus.UNAVAILABLE -> "Awaiting Clearance"
    }

  Card(
    colors =
      CardDefaults.cardColors(
        containerColor = if (isActive) MornicaSlateCardElevated else MornicaSlateCard,
      ),
    shape = RoundedCornerShape(MornicaTokens.CardOuterRadius),
    border =
      BorderStroke(
        width = 1.dp,
        color = if (isActive) MornicaEmerald else MornicaSlateBorder,
      ),
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(enabled = isPlayable) { router.playLocalTrack(track) },
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = MornicaTokens.CardInnerPadding),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Box(
          modifier =
            Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius))
              .background(if (isActive) MornicaEmeraldDark else MornicaSlateHeader)
              .border(
                1.dp,
                if (isActive) MornicaEmerald else MornicaSlateBorder,
                RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius),
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector =
              if (isActive && uiState.isLocalPlaying) {
                Icons.Default.Equalizer
              } else {
                Icons.Default.MusicNote
              },
            contentDescription = null,
            tint = if (isPlayable) MornicaEmerald else MornicaTextMuted,
            modifier = Modifier.size(22.dp),
          )
        }

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = track.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isActive) MornicaEmerald else MornicaTextLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${track.artist} • ${track.folderName}",
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$provenanceShort • ${formatDurationMs(track.durationMs)} • ${track.releaseYear}",
            style = MaterialTheme.typography.labelSmall.withTabularNums(),
            color = if (isPlayable) MornicaCyan else MornicaAmberStart,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconToggleButton(
          checked = isSaved,
          onCheckedChange = { router.toggleFavorite(track.id) },
        ) {
          Icon(
            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isSaved) "Remove Favorite" else "Save Favorite",
            tint = if (isSaved) MornicaDangerRed else MornicaTextMuted,
            modifier = Modifier.size(20.dp),
          )
        }

        Box {
          IconButton(onClick = { menuExpanded = true }) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Track Options",
              tint = MornicaTextMuted,
              modifier = Modifier.size(20.dp),
            )
          }
          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            containerColor = MornicaSlateHeader,
          ) {
            DropdownMenuItem(
              text = {
                Text(
                  text = if (isPlayable) "Play Offline Now" else "Awaiting Rights Clearance",
                  color = MornicaTextLight,
                )
              },
              enabled = isPlayable,
              onClick = {
                menuExpanded = false
                router.playLocalTrack(track)
              },
            )
            if (isPlayable) {
              DropdownMenuItem(
                text = { Text("Open Full Now Playing", color = MornicaTextLight) },
                onClick = {
                  menuExpanded = false
                  router.playLocalTrack(track)
                  router.setNowPlayingExpanded(true)
                },
              )
              DropdownMenuItem(
                text = { Text("Add to Lagos Commute Queue", color = MornicaTextLight) },
                onClick = {
                  menuExpanded = false
                  router.addToPlaylist("Lagos Commute", track.id)
                },
              )
            }
          }
        }
      }
    }
  }
}

private fun formatDurationMs(ms: Long): String {
  val totalSeconds = (ms.coerceAtLeast(0L) / 1000L).toInt()
  val minutes = totalSeconds / 60
  val seconds = totalSeconds % 60
  return "%d:%02d".format(minutes, seconds)
}

@Composable
private fun OfflineMiniPlayerBar(
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  val active = uiState.activeLocalTrack ?: return
  val maxDuration = uiState.localDurationMs.coerceAtLeast(1_000L)
  val position = uiState.localPositionMs.coerceIn(0L, maxDuration)
  val progress = (position.toFloat() / maxDuration.toFloat()).coerceIn(0f, 1f)

  Surface(
    color = MornicaSlateCardElevated,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column {
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(3.dp),
        color = MornicaEmerald,
        trackColor = MornicaSlateBorder,
      )
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clickable { router.setNowPlayingExpanded(true) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Box(
            modifier =
              Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MornicaSlateCard)
                .border(1.dp, MornicaSlateBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = null,
              tint = MornicaAmberStart,
              modifier = Modifier.size(20.dp),
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = active.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MornicaTextLight,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "${active.artist} • ${formatDurationMs(position)} / ${formatDurationMs(maxDuration)}",
              style = MaterialTheme.typography.labelSmall.withTabularNums(),
              color = MornicaCyan,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          IconButton(onClick = { router.skipPrevious() }) {
            Icon(
              imageVector = Icons.Default.SkipPrevious,
              contentDescription = "Previous Track",
              tint = MornicaTextLight,
            )
          }

          // Signature Warm Amber-Orange Gradient Play/Pause Pill (matching old-player.png)
          Box(
            modifier =
              Modifier
                .sizeIn(minWidth = MornicaTokens.MinTouchTarget, minHeight = MornicaTokens.MinTouchTarget)
                .clickable(role = Role.Button) { router.toggleLocalPlayPause() },
            contentAlignment = Alignment.Center,
          ) {
            Box(
              modifier =
                Modifier
                  .height(38.dp)
                  .clip(RoundedCornerShape(19.dp))
                  .background(MornicaAmberGradient)
                  .padding(horizontal = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (uiState.isLocalPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (uiState.isLocalPlaying) "Pause" else "Play",
                tint = Color(0xFF0B101B),
                modifier =
                  Modifier
                    .size(22.dp)
                    .offset(x = if (uiState.isLocalPlaying) 0.dp else 1.dp),
              )
            }
          }

          IconButton(onClick = { router.skipNext() }) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Next Track",
              tint = MornicaTextLight,
            )
          }

          IconButton(onClick = { router.setNowPlayingExpanded(true) }) {
            Icon(
              imageVector = Icons.Default.ExpandLess,
              contentDescription = "Expand Now Playing",
              tint = MornicaTextMuted,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NowPlayingFullSheet(
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  val active = uiState.activeLocalTrack ?: return
  val maxDuration = uiState.localDurationMs.coerceAtLeast(1_000L)
  val position = uiState.localPositionMs.coerceIn(0L, maxDuration)
  val isSaved = uiState.favoriteTrackIds.contains(active.id)

  Surface(
    color = MornicaMidnightBg,
    modifier = Modifier.fillMaxSize(),
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Sheet Bar
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = { router.setNowPlayingExpanded(false) }) {
            Icon(
              imageVector = Icons.Default.KeyboardArrowDown,
              contentDescription = "Collapse Player",
              tint = MornicaTextLight,
              modifier = Modifier.size(28.dp),
            )
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "NOW PLAYING OFFLINE",
              style = MaterialTheme.typography.labelSmall,
              color = MornicaEmerald,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = active.folderName,
              style = MaterialTheme.typography.bodySmall,
              color = MornicaTextMuted,
            )
          }
          IconToggleButton(
            checked = isSaved,
            onCheckedChange = { router.toggleFavorite(active.id) },
          ) {
            Icon(
              imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorite Track",
              tint = if (isSaved) MornicaDangerRed else MornicaTextMuted,
            )
          }
        }
      }

      // Large Artwork Showcase Card
      item {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(240.dp)
              .clip(RoundedCornerShape(MornicaTokens.NowPlayingArtworkRadius))
              .background(
                Brush.linearGradient(
                  colors =
                    listOf(
                      MornicaSlateCardElevated,
                      MornicaEmeraldDark,
                      MornicaSlateCard,
                    ),
                ),
              )
              .border(1.dp, MornicaSlateBorder, RoundedCornerShape(MornicaTokens.NowPlayingArtworkRadius)),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier =
                Modifier
                  .size(84.dp)
                  .clip(CircleShape)
                  .background(MornicaSlateHeader)
                  .border(2.dp, MornicaEmerald, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Album,
                contentDescription = null,
                tint = MornicaEmerald,
                modifier = Modifier.size(44.dp),
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = active.provenance.label,
              style = MaterialTheme.typography.labelSmall,
              color = MornicaCyan,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "${active.album} • ${active.releaseYear}",
              style = MaterialTheme.typography.bodySmall.withTabularNums(),
              color = MornicaTextMuted,
            )
          }
        }
      }

      // Track Title & Artist
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = active.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MornicaTextLight,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${active.artist} • ${active.album}",
            style = MaterialTheme.typography.bodyMedium,
            color = MornicaCyan,
            fontWeight = FontWeight.SemiBold,
          )
          uiState.playbackError?.let { err ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Playback error: $err",
              style = MaterialTheme.typography.labelSmall,
              color = MornicaDangerRed,
            )
          }
        }
      }

      // Interactive Seek Bar + Timestamps
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          Slider(
            value = position.toFloat(),
            onValueChange = { newPos -> router.seekLocalTo(newPos.toLong()) },
            valueRange = 0f..maxDuration.toFloat(),
            colors =
              SliderDefaults.colors(
                thumbColor = MornicaAmberStart,
                activeTrackColor = MornicaEmerald,
                inactiveTrackColor = MornicaSlateBorder,
              ),
            modifier =
              Modifier.fillMaxWidth().semantics {
                contentDescription = "Playback position"
                stateDescription = "${formatDurationMs(position)} of ${formatDurationMs(maxDuration)}"
              },
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = formatDurationMs(position),
              style = MaterialTheme.typography.labelMedium.withTabularNums(),
              color = MornicaTextMuted,
            )
            Text(
              text = formatDurationMs(maxDuration),
              style = MaterialTheme.typography.labelMedium.withTabularNums(),
              color = MornicaTextMuted,
            )
          }
        }
      }

      // Primary Transport Controls (Shuffle | Prev | Amber-Orange Play/Pause | Next | Repeat)
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconToggleButton(
            checked = uiState.shuffleEnabled,
            onCheckedChange = { router.toggleShuffle() },
          ) {
            Icon(
              imageVector = Icons.Default.Shuffle,
              contentDescription = "Toggle Shuffle",
              tint = if (uiState.shuffleEnabled) MornicaEmerald else MornicaTextMuted,
              modifier = Modifier.size(26.dp),
            )
          }

          IconButton(onClick = { router.skipPrevious() }) {
            Icon(
              imageVector = Icons.Default.SkipPrevious,
              contentDescription = "Skip Previous",
              tint = MornicaTextLight,
              modifier = Modifier.size(34.dp),
            )
          }

          Box(
            modifier =
              Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MornicaAmberGradient)
                .clickable { router.toggleLocalPlayPause() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (uiState.isLocalPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (uiState.isLocalPlaying) "Pause" else "Play",
              tint = Color(0xFF0B101B),
              modifier =
                Modifier
                  .size(34.dp)
                  .offset(x = if (uiState.isLocalPlaying) 0.dp else 1.dp),
            )
          }

          IconButton(onClick = { router.skipNext() }) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Skip Next",
              tint = MornicaTextLight,
              modifier = Modifier.size(34.dp),
            )
          }

          IconButton(onClick = { router.cycleRepeatMode() }) {
            Icon(
              imageVector =
                if (uiState.repeatMode == RepeatMode.ONE) {
                  Icons.Default.RepeatOne
                } else {
                  Icons.Default.Repeat
                },
              contentDescription = "Repeat Mode: ${uiState.repeatMode.name}",
              tint =
                if (uiState.repeatMode != RepeatMode.OFF) {
                  MornicaEmerald
                } else {
                  MornicaTextMuted
                },
              modifier = Modifier.size(26.dp),
            )
          }
        }
      }

      // Active Up-Next Queue Section
      item {
        HorizontalDivider(color = MornicaSlateBorder, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "ACTIVE OFFLINE QUEUE (${uiState.queueTrackIds.size} tracks)",
          style = MaterialTheme.typography.labelMedium,
          color = MornicaCyan,
          fontWeight = FontWeight.Bold,
        )
      }

      val queueTracks =
        uiState.queueTrackIds.mapNotNull { id ->
          uiState.localTracks.firstOrNull { it.id == id }
        }
      items(queueTracks, key = { "np-queue-${it.id}" }) { qTrack ->
        LocalTrackItemCard(track = qTrack, uiState = uiState, router = router)
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun DiscoverWatchScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  onOpenExternalYouTube: (String) -> Unit,
) {
  var searchText by remember(uiState.searchQuery) { mutableStateOf(uiState.searchQuery) }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MornicaSlateBorder),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Discover & Watch · Official YouTube Layer (Free & Unpaywalled)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MornicaEmerald,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text =
              "Seeded with third-party vidIQ Nigerian monthly YouTube search estimates (directional indicator only, never guaranteed demand). Videos play in a visible official YouTube embed or open directly in YouTube.",
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedTextField(
          value = searchText,
          onValueChange = { searchText = it },
          label = { Text("Search Artist, Afrobeats or Amapiano") },
          modifier = Modifier.weight(1f),
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
        )
        Button(
          onClick = { router.updateSearchQuery(searchText, uiState.risingCreatorsOnly) },
          colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Search", color = Color(0xFF04130E), fontWeight = FontWeight.Bold)
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(
          selected = uiState.searchQuery.equals("All", ignoreCase = true) && !uiState.risingCreatorsOnly,
          onClick = { router.updateSearchQuery("All", risingOnly = false) },
          label = { Text("All African Hits") },
        )
        uiState.researchEstimates.forEach { est ->
          FilterChip(
            selected = uiState.searchQuery.equals(est.query, ignoreCase = true) && !uiState.risingCreatorsOnly,
            onClick = { router.updateSearchQuery(est.query, risingOnly = false) },
            label = {
              Text("${est.query} (${est.monthlyNigerianSearches / 1000}K est.)")
            },
          )
        }
        FilterChip(
          selected = uiState.risingCreatorsOnly,
          onClick = { router.updateSearchQuery("All", risingOnly = !uiState.risingCreatorsOnly) },
          label = { Text("Rising Creators") },
        )
      }
    }

    uiState.activeYouTubeVideo?.let { activeVideo ->
      if (uiState.isYouTubeEmbedPlaying && uiState.isYouTubeViewportVisible) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MornicaSlateHeader),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MornicaEmerald),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "VISIBLE OFFICIAL YOUTUBE PLAYER · ${activeVideo.title}",
                style = MaterialTheme.typography.labelMedium,
                color = MornicaAmberStart,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(8.dp))
              AndroidView(
                modifier = Modifier.fillMaxWidth().height(220.dp),
                factory = { ctx ->
                  WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webChromeClient = WebChromeClient()
                    webViewClient = WebViewClient()
                    loadUrl(activeVideo.officialEmbedUrl())
                  }
                },
                update = { webView ->
                  if (webView.url != activeVideo.officialEmbedUrl()) {
                    webView.loadUrl(activeVideo.officialEmbedUrl())
                  }
                },
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                TextButton(onClick = { onOpenExternalYouTube(activeVideo.officialWatchUrl()) }) {
                  Text("Watch in Official YouTube App", color = MornicaCyan)
                }
                TextButton(onClick = { router.onYouTubeVisibilityChanged(false) }) {
                  Text("Close Embed", color = MornicaEmerald)
                }
              }
            }
          }
        }
      }
    }

    if (uiState.youtubeVideos.isEmpty()) {
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "No Verified Official YouTube Videos in Local Cache",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MornicaAmberStart,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text =
                "Moronto Mornica never displays fabricated channels or placeholder video IDs. " +
                  "Open YouTube directly to search '${uiState.searchQuery.ifBlank { "Afrobeats" }}'.",
              style = MaterialTheme.typography.bodySmall,
              color = MornicaTextMuted,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { onOpenExternalYouTube(uiState.externalYouTubeSearchUrl) },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
            ) {
              Text("Search on Official YouTube", color = Color(0xFF04130E), fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      items(uiState.youtubeVideos, key = { it.videoId }) { video ->
        YouTubeVideoItemCard(
          video = video,
          onWatchEmbed = { router.startYouTubeEmbed(video, viewportWidthDp = 360, viewportHeightDp = 220) },
          onWatchExternal = { onOpenExternalYouTube(video.officialWatchUrl()) },
        )
      }
    }
  }
}

@Composable
private fun YouTubeVideoItemCard(
  video: YouTubeVideoCard,
  onWatchEmbed: () -> Unit,
  onWatchExternal: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, MornicaSlateBorder),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = video.sourceBadge.label,
        style = MaterialTheme.typography.labelSmall,
        color = MornicaCyan,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = video.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MornicaTextLight,
      )
      Text(
        text = "${video.channelTitle} · ${video.category} · Free Official Stream",
        style = MaterialTheme.typography.bodySmall,
        color = MornicaTextMuted,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = onWatchEmbed,
          colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text("Watch Visible Embed", color = Color(0xFF04130E), fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
          onClick = onWatchExternal,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.dp, MornicaSlateBorder),
        ) {
          Text("Open YouTube", color = MornicaCyan)
        }
      }
    }
  }
}

@Composable
private fun CreatorHubScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  val context = LocalContext.current
  var artistName by remember { mutableStateOf("") }
  var trackTitle by remember { mutableStateOf("") }
  var genre by remember { mutableStateOf("Afrobeats") }
  var contact by remember { mutableStateOf("") }
  var rightsAttested by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(
        text = "Independent Creator Demo Showcase (First-Party Synth Previews)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MornicaCyan,
      )
    }

    items(uiState.creatorProfiles, key = { it.id }) { profile ->
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "${profile.provenanceBadge.label} · ${profile.city}",
            style = MaterialTheme.typography.labelSmall,
            color = MornicaAmberStart,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = profile.artistName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MornicaTextLight,
          )
          Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
          Spacer(modifier = Modifier.height(8.dp))
          val previewTrack =
            uiState.localTracks.firstOrNull { profile.previewTrackIds.contains(it.id) }
          if (previewTrack != null) {
            Button(
              onClick = { router.playLocalTrack(previewTrack) },
              colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
              shape = RoundedCornerShape(10.dp),
            ) {
              Text(
                text = "Play First-Party Demo Preview: ${previewTrack.title}",
                color = Color(0xFF04130E),
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateHeader),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Request Artist Launch Package (Commercial Pilot)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MornicaEmerald,
          )
          Text(
            text =
              "Monetization applies strictly to independent creator promotion & verified profile setup — never YouTube playback.",
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = artistName,
            onValueChange = { artistName = it },
            label = { Text("Artist or Label Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = trackTitle,
            onValueChange = { trackTitle = it },
            label = { Text("Track / EP Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = genre,
            onValueChange = { genre = it },
            label = { Text("Genre (Afrobeats, Amapiano, Street-Pop, Afro-Soul)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text("Contact Email or WhatsApp") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rightsAttested, onCheckedChange = { rightsAttested = it })
            Text(
              text = "I attest that I own or control 100% of the master & publishing rights for this preview.",
              style = MaterialTheme.typography.bodySmall,
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = {
              val ok =
                router.submitArtistLaunchRequest(
                  ArtistLaunchRequest(
                    artistName = artistName,
                    trackTitle = trackTitle,
                    genre = genre,
                    contactEmailOrWhatsApp = contact,
                    rightsAttested = rightsAttested,
                  ),
                )
              if (ok) {
                artistName = ""
                trackTitle = ""
                contact = ""
                rightsAttested = false
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
            shape = RoundedCornerShape(10.dp),
          ) {
            Text(
              text = "Submit Pilot Launch Inquiry",
              color = Color(0xFF04130E),
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MornicaSlateBorder),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Privacy, Data Safety & YouTube Policy Compliance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MornicaAmberStart,
          )
          Text(
            text =
              "1. Local audio files (READ_MEDIA_AUDIO) are scanned strictly on-device and never uploaded. " +
                "2. Official YouTube embeds remain visible, free, and pause automatically when backgrounded. " +
                "3. Zero downloading, ripping, or stream extraction from YouTube is permitted.",
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = {
              val intent =
                Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(com.shadowspark.morontomornica.model.PUBLIC_PRIVACY_POLICY_URL),
                )
              context.startActivity(intent)
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MornicaSlateBorder),
          ) {
            Text(
              text = "Open Public Privacy Policy",
              color = MornicaCyan,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    }
  }
}

