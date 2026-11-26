package com.shadowspark.morontomornica.ui.playlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
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
import com.shadowspark.morontomornica.ui.MornicaCyan
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.MornicaTextMuted
import com.shadowspark.morontomornica.ui.components.EmptyStateCard

/**
 * Playlist index over [PlaylistStore]. Create here, open a playlist for reorder/remove.
 * No network; names and track IDs persist through the store's JSON implementation.
 */
@Composable
fun PlaylistListScreen(
  store: PlaylistStore,
  onOpenPlaylist: (String) -> Unit,
  onBack: () -> Unit,
) {
  var version by remember { mutableIntStateOf(0) }
  val names = remember(version) { store.names() }
  var showCreate by remember { mutableStateOf(false) }
  var newName by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MornicaEmerald)
        }
        Column(modifier = Modifier.weight(1f)) {
          Text("Playlists", style = MaterialTheme.typography.titleLarge, color = MornicaTextLight, fontWeight = FontWeight.Bold)
          Text("Created and kept on this device", style = MaterialTheme.typography.labelMedium, color = MornicaTextMuted)
        }
        OutlinedButton(onClick = { showCreate = true }) {
          Icon(Icons.Default.Add, contentDescription = null, tint = MornicaEmerald)
          Text("New", color = MornicaEmerald, modifier = Modifier.padding(start = 6.dp))
        }
      }
    }

    if (names.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No playlists yet",
          body = "Create a playlist, then add tracks from the Now Playing screen. Playlists stay on this device.",
          actionLabel = "New playlist",
          onAction = { showCreate = true },
        )
      }
    }

    items(names, key = { it }) { name ->
      val count = store.tracks(name).size
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(name, style = MaterialTheme.typography.titleSmall, color = MornicaTextLight, fontWeight = FontWeight.Bold)
          Text("$count tracks", style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted)
        }
        IconButton(onClick = { onOpenPlaylist(name) }) {
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Open playlist", tint = MornicaCyan)
        }
      }
    }

    item { Spacer(Modifier.height(72.dp)) }
  }

  if (showCreate) {
    AlertDialog(
      onDismissRequest = { showCreate = false },
      title = { Text("New playlist") },
      text = {
        OutlinedTextField(
          value = newName,
          onValueChange = { newName = it },
          singleLine = true,
          placeholder = { Text("Playlist name") },
        )
      },
      confirmButton = {
        TextButton(onClick = {
          if (store.create(newName.trim())) {
            val created = newName.trim()
            newName = ""
            showCreate = false
            onOpenPlaylist(created)
          }
        }) { Text("Create") }
      },
      dismissButton = {
        TextButton(onClick = { showCreate = false }) { Text("Cancel") }
      },
    )
  }
}
