package com.shadowspark.morontomornica.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.model.LocalMediaTrack
import com.shadowspark.morontomornica.ui.MornicaAmberStart
import com.shadowspark.morontomornica.ui.MornicaCyan
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaSlateBorder
import com.shadowspark.morontomornica.ui.MornicaSlateCard
import com.shadowspark.morontomornica.ui.MornicaSlateHeader
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.MornicaTextMuted
import com.shadowspark.morontomornica.ui.MornicaTokens

@Composable
fun SectionHeader(
  title: String,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      color = MornicaCyan,
      fontWeight = FontWeight.Bold,
    )
    if (actionLabel != null && onAction != null) {
      Text(
        text = actionLabel,
        style = MaterialTheme.typography.labelMedium,
        color = MornicaEmerald,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable(onClick = onAction).padding(4.dp),
      )
    }
  }
}

@Composable
fun GenreChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(MornicaTokens.SortPillRadius),
    color = if (selected) MornicaEmerald else MornicaSlateCard,
    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
    modifier = modifier.clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = if (selected) androidx.compose.ui.graphics.Color(0xFF04130E) else MornicaTextMuted,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
    )
  }
}

@Composable
fun TrackRowCompact(
  track: LocalMediaTrack,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  trailing: @Composable (() -> Unit)? = null,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius))
        .clickable(onClick = onClick)
        .padding(vertical = 8.dp, horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(
      modifier =
        Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius))
          .background(MornicaSlateHeader)
          .border(1.dp, MornicaSlateBorder, RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(Icons.Default.MusicNote, contentDescription = null, tint = MornicaAmberStart, modifier = Modifier.size(20.dp))
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = track.title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MornicaTextLight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MornicaTextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    trailing?.invoke()
  }
}

@Composable
fun ShelfRow(
  title: String,
  tracks: List<LocalMediaTrack>,
  onPlay: (LocalMediaTrack) -> Unit,
  modifier: Modifier = Modifier,
  onOpenAll: (() -> Unit)? = null,
) {
  if (tracks.isEmpty()) return
  Column(modifier = modifier) {
    SectionHeader(title = title, actionLabel = if (onOpenAll != null) "See all" else null, onAction = onOpenAll)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      items(tracks, key = { it.id }) { track ->
        Card(
          colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
          shape = RoundedCornerShape(MornicaTokens.CardOuterRadius),
          border = androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
          modifier = Modifier.width(160.dp).clickable { onPlay(track) },
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .height(96.dp)
                  .clip(RoundedCornerShape(MornicaTokens.CardInnerEmblemRadius))
                  .background(MornicaSlateHeader),
              contentAlignment = Alignment.Center,
            ) {
              Icon(Icons.Default.MusicNote, contentDescription = null, tint = MornicaEmerald, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(track.title, style = MaterialTheme.typography.titleSmall, color = MornicaTextLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
        }
      }
    }
  }
}

@Composable
fun EmptyStateCard(
  title: String,
  body: String,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
    shape = RoundedCornerShape(MornicaTokens.CardOuterRadius),
    border = androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
    modifier = modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(MornicaTokens.CardInnerPadding)) {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MornicaEmerald)
      Spacer(Modifier.height(4.dp))
      Text(body, style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted)
      if (actionLabel != null && onAction != null) {
        Spacer(Modifier.height(10.dp))
        Surface(
          shape = CircleShape,
          color = MornicaEmerald,
          modifier = Modifier.clickable(onClick = onAction),
        ) {
          Text(
            actionLabel,
            color = androidx.compose.ui.graphics.Color(0xFF04130E),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
          )
        }
      }
    }
  }
}
