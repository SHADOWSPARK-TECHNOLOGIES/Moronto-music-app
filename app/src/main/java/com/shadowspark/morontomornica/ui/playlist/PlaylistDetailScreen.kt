package com.shadowspark.morontomornica.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
 * Playlist detail backed by [PlaylistStore]: play, add the current track, rename, delete,
 * remove tracks, and reorder up/down. Mutations bump [version] so the snapshot recomposes;
 * when Room replaces the store, observe its Flow instead of this counter.
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
    // Keep the store's order: the up/down indices below refer to store positions,
    // so sorting by library order would move the wrong track.
    val byId = uiState.localTracks.associateBy { it.id }
    ids.mapNotNull { byId[it] }
  }
  var showRename by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }
  var renameValue by remember(name) { mutableStateOf(name) }

  Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MornicaEmerald)
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(name, style = MaterialTheme.typography.titleLarge, color = MornicaTextLight, fontWeight = FontWeight.Bold)
        Text("${tracks.size} tracks", style = MaterialTheme.typography.labelMedium, color = MornicaTextMuted)
      }
      IconButton(onClick = { showRename = true }) {
        Icon(Icons.Default.Edit, contentDescription = "Rename playlist", tint = MornicaTextMuted)
      }
      IconButton(onClick = { confirmDelete = true }) {
        Icon(Icons.Default.Delete, contentDescription = "Delete playlist", tint = MornicaTextMuted)
      }
      IconButton(onClick = { tracks.firstOrNull()?.let { router.playLocalTrack(it) } }) {
        Icon(Icons.Default.PlayArrow, contentDescription = "Play first track in playlist", tint = MornicaEmerald)
      }
    }

    val active = uiState.activeLocalTrack
    if (active != null && active.id !in ids) {
      OutlinedButton(onClick = {
        store.add(name, active.id)
        version++
      }) {
        Icon(Icons.Default.Add, contentDescription = null, tint = MornicaEmerald)
        Text("Add current track", color = MornicaEmerald, modifier = Modifier.padding(start = 6.dp))
      }
    }

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      items(tracks, key = { it.id }) { track ->
        TrackRowCompact(
          track = track,
          subtitle = track.artist,
          onClick = { router.playLocalTrack(track) },
          trailing = {
            IconButton(onClick = {
              // Use the store's own index, not the displayed row index: a track removed from the
              // device can be absent from `tracks` while still occupying a store slot.
              val from = ids.indexOf(track.id)
              if (from > 0) {
                store.move(name, from, from - 1)
                version++
              }
            }) {
              Icon(Icons.Default.ArrowUpward, contentDescription = "Move up", tint = MornicaTextMuted)
            }
            IconButton(onClick = {
              val from = ids.indexOf(track.id)
              if (from >= 0 && from < ids.lastIndex) {
                store.move(name, from, from + 1)
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

  if (showRename) {
    AlertDialog(
      onDismissRequest = { showRename = false },
      title = { Text("Rename playlist") },
      text = {
        OutlinedTextField(
          value = renameValue,
          onValueChange = { renameValue = it },
          singleLine = true,
          placeholder = { Text("Playlist name") },
        )
      },
      confirmButton = {
        TextButton(onClick = {
          val newName = renameValue.trim()
          if (newName.isNotBlank() && store.rename(name, newName)) {
            showRename = false
            onBack()
          }
        }) { Text("Rename") }
      },
      dismissButton = {
        TextButton(onClick = { showRename = false }) { Text("Cancel") }
      },
    )
  }

  if (confirmDelete) {
    AlertDialog(
      onDismissRequest = { confirmDelete = false },
      title = { Text("Delete playlist?") },
      text = { Text("\"$name\" and its track order are removed from this device. Tracks in your library are not deleted.") },
      confirmButton = {
        TextButton(onClick = {
          store.delete(name)
          confirmDelete = false
          onBack()
        }) { Text("Delete") }
      },
      dismissButton = {
        TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
      },
    )
  }
}
