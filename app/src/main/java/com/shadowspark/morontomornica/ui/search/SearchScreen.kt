package com.shadowspark.morontomornica.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaTextMuted
import com.shadowspark.morontomornica.ui.components.EmptyStateCard
import com.shadowspark.morontomornica.ui.components.GenreChip
import com.shadowspark.morontomornica.ui.components.TrackRowCompact

/**
 * Local library search over title/artist/album. Results are filtered on-device;
 * the query is also mirrored to the router so the library tab stays consistent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  onBack: () -> Unit,
) {
  var text by remember { mutableStateOf(uiState.localSearchQuery) }

  val results =
    remember(text, uiState.localTracks) {
      val q = text.trim().lowercase()
      if (q.isEmpty()) {
        uiState.localTracks.filter { it.isPlayableOffline() }
      } else {
        uiState.localTracks.filter {
          it.isPlayableOffline() &&
            (it.title.lowercase().contains(q) ||
              it.artist.lowercase().contains(q) ||
              it.album.lowercase().contains(q))
        }
      }
    }

  val artistChips =
    remember(uiState.localTracks) {
      uiState.localTracks.filter { it.isPlayableOffline() }
        .groupBy { it.artist }
        .entries.sortedByDescending { it.value.size }
        .take(6)
        .map { it.key }
    }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MornicaEmerald)
      }
      OutlinedTextField(
        value = text,
        onValueChange = {
          text = it
          router.updateLocalSearchQuery(it)
        },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search songs, artists, albums") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MornicaTextMuted) },
        singleLine = true,
      )
    }

    Row(
      modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      artistChips.forEach { artist ->
        GenreChip(
          label = artist,
          selected = text.equals(artist, ignoreCase = true),
          onClick = {
            text = artist
            router.updateLocalSearchQuery(artist)
          },
        )
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      if (results.isEmpty()) {
        item {
          EmptyStateCard(
            title = "Nothing found",
            body = "Try a song title or artist name. Only playable on-device tracks are searched here.",
          )
        }
      }
      items(results, key = LocalMediaTrack::id) { track ->
        TrackRowCompact(
          track = track,
          subtitle = "${track.artist} · ${track.album}",
          onClick = { router.playLocalTrack(track) },
        )
      }
    }
  }
}
