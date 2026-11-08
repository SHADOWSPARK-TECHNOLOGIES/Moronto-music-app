package com.shadowspark.morontomornica.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.domain.CatalogDownloadState
import com.shadowspark.morontomornica.domain.LicensedCatalogTrack
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter

/**
 * Licensed streaming + offline catalogue section. Nigerian-friendly copy with clear DEMO labels.
 * Stream / Download / Retry / Cancel / offline actions route through the gate-enforced engine.
 * Renders as a plain Column (not nested LazyColumn) — safe for the single-scrollable accessibility
 * contract. Switch to LazyColumn if a real catalogue exceeds ~10 rows.
 */
@Composable
fun LicensedCatalogSection(
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)) {
    Text(
      text = "Ready to play",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MornicaEmerald,
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = "Stream now or save offline · DEMO tracks clearly labelled",
      style = MaterialTheme.typography.bodySmall,
      color = MornicaTextMuted,
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Wi-Fi-only download preference + mobile-data messaging.
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Switch(
        checked = uiState.preferWifiOnlyDownloads,
        onCheckedChange = { router.setWifiOnlyDownloads(it) },
        colors = SwitchDefaults.colors(checkedTrackColor = MornicaEmerald),
      )
      Column {
        Text(
          text = "Wi-Fi downloads",
          style = MaterialTheme.typography.bodyMedium,
          color = MornicaTextLight,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text =
            if (uiState.preferWifiOnlyDownloads) {
              "Saves your mobile data"
            } else {
              "May use mobile data"
            },
          style = MaterialTheme.typography.bodySmall,
          color = MornicaTextMuted,
        )
      }
    }
    Spacer(modifier = Modifier.height(10.dp))

    val downloadedCount = uiState.catalogDownloadStates.count { it.value == CatalogDownloadState.COMPLETED }
    if (downloadedCount > 0) {
      Text(
        text = if (downloadedCount == 1) "1 track saved · ready offline" else "$downloadedCount tracks saved · ready offline",
        style = MaterialTheme.typography.labelMedium,
        color = MornicaCyan,
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    if (uiState.catalogTracks.isEmpty()) {
      Text(
        text = "No tracks available yet — check back soon",
        style = MaterialTheme.typography.bodyMedium,
        color = MornicaTextMuted,
      )
      return@Column
    }

    // Demo catalogue is small, so render as a plain Column (NOT a nested LazyColumn) to avoid a
    // second scrollable competing with the main My Music list (a real catalogue > ~10 rows should
    // switch to a LazyColumn and the accessibility test scoped accordingly).
    uiState.catalogTracks.forEach { track ->
      LicensedCatalogTrackCard(
        track = track,
        downloadState = uiState.catalogDownloadStates[track.id] ?: CatalogDownloadState.NONE,
        isOfflineReady = router.offlinePlaybackAllowed(track),
        offlineAllowed = track.offlineAllowed,
        onStream = { router.playCatalogTrack(track) },
        onDownload = { router.requestCatalogDownload(track) },
        onCancel = { router.cancelCatalogDownload(track.id) },
      )
    }
  }
}

@Composable
private fun LicensedCatalogTrackCard(
  track: LicensedCatalogTrack,
  downloadState: CatalogDownloadState,
  isOfflineReady: Boolean,
  offlineAllowed: Boolean,
  onStream: () -> Unit,
  onDownload: () -> Unit,
  onCancel: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = track.title,
        style = MaterialTheme.typography.titleMedium,
        color = MornicaTextLight,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = "${track.artist} · ${track.sourceLabel}",
        style = MaterialTheme.typography.bodySmall,
        color = if (track.sourceLabel.contains("DEMO", ignoreCase = true)) MornicaAmberStart else MornicaTextMuted,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Button(
          onClick = onStream,
          colors = ButtonDefaults.buttonColors(containerColor = MornicaEmerald),
          shape = RoundedCornerShape(10.dp),
        ) {
          Text(if (isOfflineReady) "Play" else "Stream",
            color = Color(0xFF071321), fontWeight = FontWeight.Bold)
        }
        when {
          // Streaming-only tracks (no offline entitlement) NEVER show a download or cancel
          // control: the download would only be refused by the rights gate, so it is made
          // visibly unavailable instead of a dead tap.
          !offlineAllowed -> Text(
            text = "Streaming only",
            style = MaterialTheme.typography.labelMedium,
            color = MornicaTextMuted,
            maxLines = 1,
          )
          isOfflineReady -> TextButton(onClick = onStream) {
            Text("Ready offline", color = MornicaCyan, fontWeight = FontWeight.Bold)
          }
          downloadState == CatalogDownloadState.QUEUED ||
            downloadState == CatalogDownloadState.DOWNLOADING -> {
            Text("Downloading…", style = MaterialTheme.typography.labelMedium, color = MornicaAmberStart)
            TextButton(onClick = onCancel) { Text("Cancel", color = MornicaTextMuted) }
          }
          downloadState == CatalogDownloadState.FAILED -> TextButton(onClick = onDownload) {
            Text("Retry", color = MornicaAmberStart, fontWeight = FontWeight.Bold)
          }
          else -> TextButton(onClick = onDownload) {
            Text("Save offline", color = MornicaEmerald, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
