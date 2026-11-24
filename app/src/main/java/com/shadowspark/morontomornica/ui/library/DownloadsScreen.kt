package com.shadowspark.morontomornica.ui.library

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.domain.CatalogDownloadState
import com.shadowspark.morontomornica.domain.LicensedCatalogTrack
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.ui.MornicaAmberStart
import com.shadowspark.morontomornica.ui.MornicaCyan
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaSlateBorder
import com.shadowspark.morontomornica.ui.MornicaSlateCard
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.MornicaTextMuted
import com.shadowspark.morontomornica.ui.components.EmptyStateCard

/**
 * Dedicated offline/downloads shelf. Every action routes through the existing rights-gated
 * manager: downloads only start after [PlaybackRouter.requestCatalogDownload] passes the gate;
 * "Play offline" calls [PlaybackRouter.playDownloadedCatalogTrack], which fail-closes when the
 * Media3 DownloadIndex has no completed bytes.
 */
@Composable
fun DownloadsScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  onBack: () -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    contentPadding = PaddingValues(vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MornicaEmerald)
        }
        Column {
          Text("Downloads", style = MaterialTheme.typography.titleLarge, color = MornicaTextLight, fontWeight = FontWeight.Bold)
          Text("Authorized offline tracks · ready offline plays without data", style = MaterialTheme.typography.labelMedium, color = MornicaTextMuted)
        }
      }
    }

    if (uiState.catalogTracks.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No catalogue tracks yet",
          body = "Licensed downloads appear here after a real catalog backend connects. The demo catalogue proves the offline pipeline only.",
        )
      }
    }

    items(uiState.catalogTracks, key = { it.id }) { track ->
      DownloadRow(
        track = track,
        state = uiState.catalogDownloadStates[track.id] ?: CatalogDownloadState.NONE,
        onDownload = { router.requestCatalogDownload(track) },
        onCancel = { router.cancelCatalogDownload(track.id) },
        onPlayOffline = { router.playDownloadedCatalogTrack(track) },
      )
    }

    item {
      Text(
        text = "Streaming-only tracks are never downloadable. Offline readiness is gated by real downloaded bytes, not a flag.",
        style = MaterialTheme.typography.labelSmall,
        color = MornicaAmberStart,
        modifier = Modifier.padding(top = 4.dp),
      )
    }
  }
}

@Composable
private fun DownloadRow(
  track: LicensedCatalogTrack,
  state: CatalogDownloadState,
  onDownload: () -> Unit,
  onCancel: () -> Unit,
  onPlayOffline: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MornicaTextLight)
      Text("${track.artist} · ${track.sourceLabel}", style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted)
      Spacer(Modifier.height(8.dp))
      Text(
        text = stateLabel(state),
        style = MaterialTheme.typography.labelSmall,
        color = if (state == CatalogDownloadState.COMPLETED) MornicaEmerald else MornicaCyan,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when (state) {
          CatalogDownloadState.COMPLETED ->
            Button(onClick = onPlayOffline, colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald)) {
              Text("Play offline", color = androidx.compose.ui.graphics.Color(0xFF04130E), fontWeight = FontWeight.Bold)
            }
          CatalogDownloadState.QUEUED,
          CatalogDownloadState.DOWNLOADING ->
            OutlinedButton(onClick = onCancel) { Text("Cancel", color = MornicaAmberStart) }
          CatalogDownloadState.FAILED ->
            OutlinedButton(onClick = onDownload) { Text("Retry", color = MornicaCyan) }
          CatalogDownloadState.NONE ->
            if (track.offlineAllowed) {
              Button(onClick = onDownload, colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald)) {
                Text("Download for offline", color = androidx.compose.ui.graphics.Color(0xFF04130E), fontWeight = FontWeight.Bold)
              }
            } else {
              Text("Streaming only · offline not licensed", style = MaterialTheme.typography.labelSmall, color = MornicaAmberStart)
            }
        }
      }
    }
  }
}

private fun stateLabel(state: CatalogDownloadState): String =
  when (state) {
    CatalogDownloadState.NONE -> "Not downloaded"
    CatalogDownloadState.QUEUED -> "Queued for download"
    CatalogDownloadState.DOWNLOADING -> "Downloading…"
    CatalogDownloadState.COMPLETED -> "Ready offline"
    CatalogDownloadState.FAILED -> "Download failed"
  }
