package com.shadowspark.morontomornica.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.core.reco.OnDeviceRecommender
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.ui.MornicaCyan
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.components.EmptyStateCard
import com.shadowspark.morontomornica.ui.components.ShelfRow

/**
 * Home feed: personalized shelves built on-device by [OnDeviceRecommender].
 * Read-only against [MornicaUiState]; playback goes through [router.playLocalTrack].
 */
@Composable
fun HomeFeedScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  recommender: OnDeviceRecommender,
  onOpenSearch: () -> Unit,
  onOpenDownloads: () -> Unit,
) {
  val historyVersion by recommender.historyVersion.collectAsState()
  val feed =
    remember(uiState.localTracks, uiState.favoriteTrackIds, historyVersion) {
      HomeFeedModel.build(uiState.localTracks, uiState.favoriteTrackIds, recommender)
    }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Column {
        Text(
          text = "Your music, your way",
          style = MaterialTheme.typography.headlineSmall,
          color = MornicaTextLight,
          fontWeight = FontWeight.ExtraBold,
        )
        Text(
          text = "${feed.totalTracks} playable tracks on this device · offline ready",
          style = MaterialTheme.typography.labelMedium,
          color = MornicaCyan,
        )
      }
    }

    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onOpenSearch) {
          Icon(Icons.Default.Search, contentDescription = null, tint = MornicaEmerald)
          Text("Search", color = MornicaEmerald, modifier = Modifier.padding(start = 6.dp))
        }
        OutlinedButton(onClick = onOpenDownloads) {
          Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = MornicaEmerald)
          Text("Downloads", color = MornicaEmerald, modifier = Modifier.padding(start = 6.dp))
        }
      }
    }

    if (feed.shelves.isEmpty() && feed.artistShelves.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No shelves yet",
          body = "Play a few tracks and your Recent, Liked and Most played shelves build themselves — on this device only.",
          actionLabel = "Search my music",
          onAction = onOpenSearch,
        )
      }
    }

    items(feed.shelves, key = { it.key }) { shelf ->
      ShelfRow(
        title = shelf.title,
        tracks = shelf.tracks,
        onPlay = { track -> router.playLocalTrack(track) },
      )
    }

    items(feed.artistShelves, key = { it.artist }) { artistShelf ->
      ShelfRow(
        title = artistShelf.artist,
        tracks = artistShelf.tracks,
        onPlay = { track -> router.playLocalTrack(track) },
      )
    }

    item { Spacer(Modifier.height(72.dp)) }
  }
}
