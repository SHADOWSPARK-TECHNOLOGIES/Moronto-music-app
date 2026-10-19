package com.shadowspark.morontomornica.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
      topBar = {
        Surface(
          color = MornicaSurfacePurple,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
              text = "MORONTO MORNICA",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.ExtraBold,
              color = MornicaNeonMagenta,
            )
            Text(
              text = "Music for Real People · YouTube Discovery × Musicolet Offline Player",
              style = MaterialTheme.typography.bodySmall,
              color = MornicaCyberBlue,
            )
            uiState.statusBannerMessage?.let { msg ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = msg,
                style = MaterialTheme.typography.labelSmall,
                color = MornicaGoldAccent,
              )
            }
          }
        }
      },
      bottomBar = {
        Column {
          if (uiState.activeLocalTrack != null) {
            OfflineMiniPlayerBar(uiState = uiState, router = router)
          }
          NavigationBar(containerColor = MornicaSurfacePurple) {
            PrimaryScreen.entries.forEach { screen ->
              NavigationBarItem(
                selected = uiState.currentScreen == screen,
                onClick = { router.selectScreen(screen) },
                icon = {
                  Text(
                    text =
                      when (screen) {
                        PrimaryScreen.DISCOVER_WATCH -> "YT"
                        PrimaryScreen.MY_MUSIC -> "MP3"
                        PrimaryScreen.CREATOR_HUB -> "PRO"
                      },
                    fontWeight = FontWeight.Bold,
                  )
                },
                label = { Text(screen.title) },
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
            .background(MornicaDeepPurple)
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
        colors = CardDefaults.cardColors(containerColor = MornicaCardPurple),
        shape = RoundedCornerShape(12.dp),
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Discover & Watch · Official YouTube Layer (Free & Unpaywalled)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MornicaCyberBlue,
          )
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
        )
        Button(onClick = { router.updateSearchQuery(searchText, uiState.risingCreatorsOnly) }) {
          Text("Search")
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
            colors = CardDefaults.cardColors(containerColor = MornicaSurfacePurple),
            shape = RoundedCornerShape(14.dp),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "VISIBLE OFFICIAL YOUTUBE PLAYER · ${activeVideo.title}",
                style = MaterialTheme.typography.labelMedium,
                color = MornicaGoldAccent,
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
                  Text("Watch in Official YouTube App", color = MornicaCyberBlue)
                }
                TextButton(onClick = { router.onYouTubeVisibilityChanged(false) }) {
                  Text("Close Embed", color = MornicaNeonMagenta)
                }
              }
            }
          }
        }
      }
    }

    items(uiState.youtubeVideos, key = { it.videoId }) { video ->
      YouTubeVideoItemCard(
        video = video,
        onWatchEmbed = { router.startYouTubeEmbed(video, viewportWidthDp = 360, viewportHeightDp = 220) },
        onWatchExternal = { onOpenExternalYouTube(video.officialWatchUrl()) },
      )
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
    colors = CardDefaults.cardColors(containerColor = MornicaCardPurple),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = video.sourceBadge.label,
        style = MaterialTheme.typography.labelSmall,
        color = MornicaCyberBlue,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = video.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
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
          colors = ButtonDefaults.buttonColors(containerColor = MornicaNeonMagenta),
        ) {
          Text("Watch Visible Embed")
        }
        OutlinedButton(onClick = onWatchExternal) {
          Text("Open YouTube", color = MornicaCyberBlue)
        }
      }
    }
  }
}

@Composable
private fun MyMusicOfflineScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  localRepo: LocalMediaRepository,
  onRequestAudioPermission: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaCardPurple),
        shape = RoundedCornerShape(12.dp),
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "My Music · Musicolet-Inspired Offline Local Audio Engine",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MornicaGoldAccent,
          )
          Text(
            text =
              if (uiState.hasAudioPermission) {
                "Device MediaStore audio permission granted. Showing user-owned device files + cleared DEMO previews."
              } else {
                "Device audio permission not granted yet. Playing bundled rights-cleared DEMO previews. Tap below to scan device MP3/FLAC/WAV folders."
              },
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
          if (!uiState.hasAudioPermission) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRequestAudioPermission) {
              Text("Scan Device Audio Files (READ_MEDIA_AUDIO)")
            }
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        LibraryTab.entries.forEach { tab ->
          FilterChip(
            selected = uiState.selectedLibraryTab == tab,
            onClick = { router.selectLibraryTab(tab) },
            label = { Text(tab.label) },
          )
        }
      }
    }

    when (uiState.selectedLibraryTab) {
      LibraryTab.SONGS -> {
        items(uiState.localTracks, key = { it.id }) { track ->
          LocalTrackItemCard(track = track, uiState = uiState, router = router)
        }
      }
      LibraryTab.FOLDERS -> {
        val grouped = localRepo.groupByFolder(uiState.localTracks)
        grouped.forEach { (folder, tracks) ->
          item {
            Text(
              text = "Folder: $folder (${tracks.size} tracks)",
              style = MaterialTheme.typography.titleSmall,
              color = MornicaCyberBlue,
              fontWeight = FontWeight.Bold,
            )
          }
          items(tracks, key = { "folder-${it.id}" }) { track ->
            LocalTrackItemCard(track = track, uiState = uiState, router = router)
          }
        }
      }
      LibraryTab.ARTISTS -> {
        val grouped = localRepo.groupByArtist(uiState.localTracks)
        grouped.forEach { (artist, tracks) ->
          item {
            Text(
              text = "Artist: $artist (${tracks.size} tracks)",
              style = MaterialTheme.typography.titleSmall,
              color = MornicaCyberBlue,
              fontWeight = FontWeight.Bold,
            )
          }
          items(tracks, key = { "artist-${it.id}" }) { track ->
            LocalTrackItemCard(track = track, uiState = uiState, router = router)
          }
        }
      }
      LibraryTab.ALBUMS -> {
        val grouped = localRepo.groupByAlbum(uiState.localTracks)
        grouped.forEach { (album, tracks) ->
          item {
            Text(
              text = "Album: $album (${tracks.size} tracks)",
              style = MaterialTheme.typography.titleSmall,
              color = MornicaCyberBlue,
              fontWeight = FontWeight.Bold,
            )
          }
          items(tracks, key = { "album-${it.id}" }) { track ->
            LocalTrackItemCard(track = track, uiState = uiState, router = router)
          }
        }
      }
      LibraryTab.PLAYLISTS -> {
        uiState.playlists.forEach { (playlistName, trackIds) ->
          item {
            Text(
              text = "Playlist: $playlistName (${trackIds.size} tracks)",
              style = MaterialTheme.typography.titleSmall,
              color = MornicaGoldAccent,
              fontWeight = FontWeight.Bold,
            )
          }
          val tracks = uiState.localTracks.filter { trackIds.contains(it.id) }
          items(tracks, key = { "pl-$playlistName-${it.id}" }) { track ->
            LocalTrackItemCard(track = track, uiState = uiState, router = router)
          }
        }
      }
    }
  }
}

@Composable
private fun LocalTrackItemCard(
  track: LocalMediaTrack,
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  val isSaved = uiState.favoriteTrackIds.contains(track.id)
  Card(
    colors = CardDefaults.cardColors(containerColor = MornicaCardPurple),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = track.provenance.label,
        style = MaterialTheme.typography.labelSmall,
        color =
          if (track.provenance == ProvenanceStatus.UNAVAILABLE) {
            MornicaNeonMagenta
          } else {
            MornicaGoldAccent
          },
      )
      Text(
        text = track.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "${track.artist} · ${track.album} · Folder: ${track.folderName}",
        style = MaterialTheme.typography.bodySmall,
        color = MornicaTextMuted,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { router.playLocalTrack(track) },
          enabled = track.isPlayableOffline(),
        ) {
          Text(if (track.isPlayableOffline()) "Play Offline" else "Awaiting Clearance")
        }
        OutlinedButton(onClick = { router.toggleFavorite(track.id) }) {
          Text(if (isSaved) "Saved ★" else "Save ☆")
        }
      }
    }
  }
}

@Composable
private fun OfflineMiniPlayerBar(
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  val active = uiState.activeLocalTrack ?: return
  Surface(
    color = MornicaCardPurple,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
      Text(
        text = "NOW PLAYING OFFLINE · ${active.title} — ${active.artist}",
        style = MaterialTheme.typography.labelMedium,
        color = MornicaGoldAccent,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(onClick = { router.skipPrevious() }) { Text("Prev") }
        Button(onClick = { router.toggleLocalPlayPause() }) {
          Text(if (uiState.isLocalPlaying) "Pause" else "Play")
        }
        TextButton(onClick = { router.skipNext() }) { Text("Next") }
        TextButton(onClick = { router.toggleShuffle() }) {
          Text(if (uiState.shuffleEnabled) "Shuffle: ON" else "Shuffle: OFF")
        }
        TextButton(onClick = { router.cycleRepeatMode() }) {
          Text("Repeat: ${uiState.repeatMode.name}")
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
        text = "Verified Independent Creator Spotlight",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MornicaCyberBlue,
      )
    }

    items(uiState.creatorProfiles, key = { it.id }) { profile ->
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaCardPurple),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "${profile.provenanceBadge.label} · ${profile.city}",
            style = MaterialTheme.typography.labelSmall,
            color = MornicaGoldAccent,
          )
          Text(
            text = profile.artistName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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
            Button(onClick = { router.playLocalTrack(previewTrack) }) {
              Text("Play Verified 30s Demo: ${previewTrack.title}")
            }
          }
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSurfacePurple),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Request Artist Launch Package (Commercial Pilot)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MornicaNeonMagenta,
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
          ) {
            Text("Submit Pilot Launch Inquiry")
          }
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaCardPurple),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "Privacy, Data Safety & YouTube Policy Compliance",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MornicaGoldAccent,
          )
          Text(
            text =
              "1. Local audio files (READ_MEDIA_AUDIO) are scanned strictly on-device and never uploaded. " +
                "2. Official YouTube embeds remain visible, free, and pause automatically when backgrounded. " +
                "3. Zero downloading, ripping, or stream extraction from YouTube is permitted.",
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
        }
      }
    }
  }
}
