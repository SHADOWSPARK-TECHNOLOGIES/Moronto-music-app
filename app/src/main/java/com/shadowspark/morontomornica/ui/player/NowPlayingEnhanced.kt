package com.shadowspark.morontomornica.ui.player

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.core.state.SleepTimerController
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.RepeatMode
import com.shadowspark.morontomornica.ui.MornicaAmberGradient
import com.shadowspark.morontomornica.ui.MornicaCyan
import com.shadowspark.morontomornica.ui.MornicaDangerRed
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaEmeraldDark
import com.shadowspark.morontomornica.ui.MornicaMidnightBg
import com.shadowspark.morontomornica.ui.MornicaSlateBorder
import com.shadowspark.morontomornica.ui.MornicaSlateCard
import com.shadowspark.morontomornica.ui.MornicaSlateCardElevated
import com.shadowspark.morontomornica.ui.MornicaSlateHeader
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.MornicaTextMuted
import com.shadowspark.morontomornica.ui.MornicaTokens
import com.shadowspark.morontomornica.ui.components.TrackRowCompact

/**
 * Enhanced Now Playing sheet: artwork slot, transport, sleep timer, share, and the
 * active queue with remove. Playback always flows through [router]; [sleepTimer] only
 * requests a pause through [PlaybackRouter.toggleLocalPlayPause] — it never controls a player.
 *
 * [onRemoveFromQueue] is supplied by the Phase-C router patch (see patches/existing-file-patches.md);
 * until that patch lands, pass a no-op lambda and keep the remove action hidden behind that gate.
 */
@Composable
fun NowPlayingEnhancedSheet(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  sleepTimer: SleepTimerController,
  onRemoveFromQueue: (String) -> Unit,
  onCollapse: () -> Unit,
) {
  val active = uiState.activeLocalTrack ?: return
  val context = LocalContext.current
  var showTimer by remember { mutableStateOf(false) }

  val maxDuration = uiState.localDurationMs.coerceAtLeast(1_000L)
  val position = uiState.localPositionMs.coerceIn(0L, maxDuration)
  val queueTracks =
    uiState.queueTrackIds.mapNotNull { id -> uiState.localTracks.firstOrNull { it.id == id } }

  Surface(modifier = Modifier.fillMaxSize(), color = MornicaMidnightBg) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(onClick = onCollapse) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse player", tint = MornicaTextLight)
          }
          Text("NOW PLAYING OFFLINE", style = MaterialTheme.typography.labelSmall, color = MornicaEmerald, fontWeight = FontWeight.Bold)
          IconToggleButton(checked = uiState.favoriteTrackIds.contains(active.id), onCheckedChange = { router.toggleFavorite(active.id) }) {
            Icon(
              if (uiState.favoriteTrackIds.contains(active.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorite track",
              tint = if (uiState.favoriteTrackIds.contains(active.id)) MornicaDangerRed else MornicaTextMuted,
            )
          }
        }
      }

      item {
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(230.dp)
              .clip(RoundedCornerShape(MornicaTokens.NowPlayingArtworkRadius))
              .background(
                Brush.linearGradient(listOf(MornicaSlateCardElevated, MornicaEmeraldDark, MornicaSlateCard)),
              )
              .border(1.dp, MornicaSlateBorder, RoundedCornerShape(MornicaTokens.NowPlayingArtworkRadius)),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(MornicaSlateHeader).border(2.dp, MornicaEmerald, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(Icons.Default.MusicNote, contentDescription = null, tint = MornicaEmerald, modifier = Modifier.size(44.dp))
          }
        }
      }

      item {
        Column {
          Text(active.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MornicaTextLight)
          Text("${active.artist} • ${active.album}", style = MaterialTheme.typography.bodyMedium, color = MornicaCyan, fontWeight = FontWeight.SemiBold)
          uiState.playbackError?.let { err ->
            Spacer(Modifier.height(6.dp))
            Text("Playback error: $err", style = MaterialTheme.typography.labelSmall, color = MornicaDangerRed)
          }
        }
      }

      item {
        Column {
          Slider(
            value = position.toFloat(),
            onValueChange = { router.seekLocalTo(it.toLong()) },
            valueRange = 0f..maxDuration.toFloat(),
          )
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatMs(position), style = MaterialTheme.typography.labelMedium, color = MornicaTextMuted)
            Text(formatMs(maxDuration), style = MaterialTheme.typography.labelMedium, color = MornicaTextMuted)
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconToggleButton(checked = uiState.shuffleEnabled, onCheckedChange = { router.toggleShuffle() }) {
            Icon(Icons.Default.Shuffle, contentDescription = "Toggle shuffle", tint = if (uiState.shuffleEnabled) MornicaEmerald else MornicaTextMuted)
          }
          IconButton(onClick = { router.skipPrevious() }) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = MornicaTextLight)
          }
          Box(
            modifier = Modifier.size(68.dp).clip(CircleShape).background(MornicaAmberGradient).clickable { router.toggleLocalPlayPause() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              if (uiState.isLocalPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (uiState.isLocalPlaying) "Pause" else "Play",
              tint = Color(0xFF0B101B),
              modifier = Modifier.size(34.dp),
            )
          }
          IconButton(onClick = { router.skipNext() }) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = MornicaTextLight)
          }
          IconButton(onClick = { router.cycleRepeatMode() }) {
            Icon(
              if (uiState.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
              contentDescription = "Repeat mode: ${uiState.repeatMode.name}",
              tint = if (uiState.repeatMode != RepeatMode.OFF) MornicaEmerald else MornicaTextMuted,
            )
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(onClick = { showTimer = true }) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = MornicaCyan)
            Text(
              if (sleepTimer.active) "Sleep timer ${sleepTimer.remainingMinutes}m" else "Sleep timer",
              color = MornicaCyan,
              modifier = Modifier.padding(start = 6.dp),
            )
          }
          TextButton(onClick = {
            val share =
              Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${active.title} · ${active.artist} — playing in Mornica")
              }
            context.startActivity(Intent.createChooser(share, "Share track"))
          }) {
            Icon(Icons.Default.Share, contentDescription = null, tint = MornicaCyan)
            Text("Share", color = MornicaCyan, modifier = Modifier.padding(start = 6.dp))
          }
        }
      }

      item {
        Text(
          "QUEUE · ${queueTracks.size}",
          style = MaterialTheme.typography.labelMedium,
          color = MornicaCyan,
          fontWeight = FontWeight.Bold,
        )
      }

      items(queueTracks, key = { it.id }) { q ->
        TrackRowCompact(
          track = q,
          subtitle = q.artist,
          onClick = { router.playLocalTrack(q) },
          trailing = {
            IconButton(onClick = { onRemoveFromQueue(q.id) }) {
              Icon(Icons.Default.Close, contentDescription = "Remove from queue", tint = MornicaTextMuted)
            }
          },
        )
      }
    }
  }

  if (showTimer) {
    AlertDialog(
      onDismissRequest = { showTimer = false },
      title = { Text("Sleep timer") },
      text = {
        Column {
          listOf(15, 30, 45, 60).forEach { minutes ->
            TextButton(onClick = {
              // The pause callback was supplied when SleepTimerController was constructed
              // (MornicaApp patch 5); the dialog only chooses the duration and starts it.
              sleepTimer.start(minutes)
              showTimer = false
            }) {
              Text("$minutes minutes")
            }
          }
          if (sleepTimer.active) {
            TextButton(onClick = { sleepTimer.cancel() }) { Text("Cancel timer") }
          }
        }
      },
      confirmButton = { TextButton(onClick = { showTimer = false }) { Text("Close") } },
    )
  }
}

private fun formatMs(ms: Long): String {
  val totalSeconds = (ms.coerceAtLeast(0L) / 1_000L).toInt()
  return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
