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
 * Loop 4: "Licensed & Downloaded" — Nigerian-friendly catalogue with clear DEMO labels, Stream /
 * Download / Retry / Cancel / offline actions, a Wi-Fi-only download preference with mobile-data
 * messaging, and empty/loading/network-style status copy. All actions route through the gate-enforced
 * engine. Verified via Robolectric Compose interaction/semantics tests, NOT a screenshot (rendered
 * pixels are unverified in this environment).
 */
@Composable
fun LicensedCatalogSection(
  uiState: MornicaUiState,
  router: PlaybackRouter,
) {
  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
    Text(
      text = "Licensed & Downloaded",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MornicaEmerald,
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = "Fresh Naija sounds, one place. No data? No wahala — your saved music still dey.",
      style = MaterialTheme.typography.bodyMedium,
      color = MornicaTextMuted,
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Demo previews are labeled DEMO. Only tracks that pass the rights gate can stream or save offline.",
      style = MaterialTheme.typography.bodySmall,
      color = MornicaTextMuted,
    )
    Spacer(modifier = Modifier.height(10.dp))

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
          text = "Wi-Fi only for downloads",
          style = MaterialTheme.typography.bodyMedium,
          color = MornicaTextLight,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text =
            if (uiState.preferWifiOnlyDownloads) {
              "Downloads stop on mobile data. Your MB no dey waste."
            } else {
              "Downloads can use mobile data. Use Wi-Fi-only to save data."
            },
          style = MaterialTheme.typography.bodySmall,
          color = MornicaTextMuted,
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))

    val downloadedCount = uiState.catalogDownloadStates.count { it.value == CatalogDownloadState.COMPLETED }
    if (downloadedCount > 0) {
      Text(
        text = "$downloadedCount downloaded · ready offline",
        style = MaterialTheme.typography.labelMedium,
        color = MornicaCyan,
      )
      Spacer(modifier = Modifier.height(6.dp))
    }

    if (uiState.catalogTracks.isEmpty()) {
      Text(
        text = "No licensed tracks available yet. Check back soon.",
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
          Text(if (isOfflineReady || downloadState == CatalogDownloadState.COMPLETED) "Play" else "Stream",
            color = Color(0xFF04130E), fontWeight = FontWeight.Bold)
        }
        when {
          isOfflineReady || downloadState == CatalogDownloadState.COMPLETED -> TextButton(onClick = onStream) {
            Text("Offline ready", color = MornicaCyan, fontWeight = FontWeight.Bold)
          }
          downloadState == CatalogDownloadState.QUEUED ||
            downloadState == CatalogDownloadState.DOWNLOADING -> {
            Text("Downloading…", style = MaterialTheme.typography.labelMedium, color = MornicaAmberStart)
            TextButton(onClick = onCancel) { Text("Cancel", color = MornicaTextMuted) }
          }
          downloadState == CatalogDownloadState.FAILED -> TextButton(onClick = onDownload) {
            Text("Retry download", color = MornicaAmberStart, fontWeight = FontWeight.Bold)
          }
          else -> TextButton(onClick = onDownload) {
            Text("Download for later", color = MornicaEmerald, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
