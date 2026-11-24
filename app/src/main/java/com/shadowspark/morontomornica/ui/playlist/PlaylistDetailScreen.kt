package com.shadowspark.morontomornica.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.data.local.PlaylistStore
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.MornicaTextMuted
import com.shadowspark.morontomornica.ui.components.TrackRowCompact

/**
 * Playlist detail backed by [PlaylistStore]. Reorder/remove actions bump [version] so the
 * snapshot recomposes; when Room replaces the store, observe its Flow instead of this counter.
 */
@Composable
fun PlaylistDetailScreen(
  name: String,
  store: PlaylistStore,
  uiState: MornicaUiState,
  router: PlaybackRouter,
  onBack: () -> Unit,
) {
  var version by remember { mutableIntStateOf(0) }
  val ids = remember(version, name) { store.tracks(name) }
  val tracks = remember(version, ids, uiState.localTracks) {
    uiState.localTracks.filter { it.id in ids }
  }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MornicaEmerald)
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(name, style = MaterialTheme.typography.titleLarge, color = MornicaTextLight, fontWeight = FontWeight.Bold)
        Text("${tracks.size} tracks", style = MaterialTheme.typography.labelMedium, color = MornicaTextMuted)
      }
      IconButton(onClick = { tracks.firstOrNull()?.let { router.playLocalTrack(it) } }) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Play playlist", tint = MornicaEmerald)
      }
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
        TrackRowCompact(
          track = track,
          subtitle = track.artist,
          onClick = { router.playLocalTrack(track) },
          trailing = {
            IconButton(onClick = {
              if (index > 0) {
                store.move(name, index, index - 1)
                version++
              }
            }) {
              Icon(Icons.Default.ArrowUpward, contentDescription = "Move up", tint = MornicaTextMuted)
            }
            IconButton(onClick = {
              if (index < tracks.lastIndex) {
                store.move(name, index, index + 1)
                version++
              }
            }) {
              Icon(Icons.Default.ArrowDownward, contentDescription = "Move down", tint = MornicaTextMuted)
            }
            IconButton(onClick = {
              store.removeTrack(name, track.id)
              version++
            }) {
              Icon(Icons.Default.Close, contentDescription = "Remove from playlist", tint = MornicaTextMuted)
            }
          },
        )
      }
    }
  }
}
