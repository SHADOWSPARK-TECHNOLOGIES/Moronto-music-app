package com.shadowspark.morontomornica.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shadowspark.morontomornica.domain.MornicaUiState
import com.shadowspark.morontomornica.domain.PlaybackRouter
import com.shadowspark.morontomornica.model.PUBLIC_PRIVACY_POLICY_URL
import com.shadowspark.morontomornica.ui.MornicaCyan
import com.shadowspark.morontomornica.ui.MornicaEmerald
import com.shadowspark.morontomornica.ui.MornicaSlateBorder
import com.shadowspark.morontomornica.ui.MornicaSlateCard
import com.shadowspark.morontomornica.ui.MornicaTextLight
import com.shadowspark.morontomornica.ui.MornicaTextMuted

/**
 * Settings surface. The only functional toggle (Wi-Fi-only downloads) delegates to the router;
 * other rows are informational and open the system browser for the public policy URL.
 */
@Composable
fun SettingsScreen(
  uiState: MornicaUiState,
  router: PlaybackRouter,
  onBack: () -> Unit,
) {
  val context = LocalContext.current

  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MornicaEmerald)
        }
        Text("Settings", style = MaterialTheme.typography.titleLarge, color = MornicaTextLight, fontWeight = FontWeight.Bold)
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text("Download on Wi-Fi only", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MornicaTextLight)
            Text("Prevents mobile-data downloads of authorized tracks", style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted)
          }
          Switch(
            checked = uiState.preferWifiOnlyDownloads,
            onCheckedChange = { router.setWifiOnlyDownloads(it) },
            colors = SwitchDefaults.colors(checkedTrackColor = MornicaEmerald),
          )
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Privacy & rights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MornicaTextLight)
          Spacer(Modifier.height(4.dp))
          Text(
            "Your music library stays on-device. No advertising or analytics SDKs. YouTube links open externally and are never downloaded.",
            style = MaterialTheme.typography.bodySmall,
            color = MornicaTextMuted,
          )
          Spacer(Modifier.height(10.dp))
          OutlinedButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PUBLIC_PRIVACY_POLICY_URL)))
          }) {
            Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = MornicaCyan)
            Text("Public privacy policy", color = MornicaCyan, modifier = Modifier.padding(start = 6.dp))
          }
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MornicaSlateCard),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MornicaSlateBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("About Mornica", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MornicaTextLight)
          Spacer(Modifier.height(4.dp))
          Text("Music for real people · African music at the center.", style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted)
          Text("Built by ShadowSpark Technologies. Demo catalogue tracks are labeled DEMO and use sample audio; licensed tracks require a proven rights agreement.", style = MaterialTheme.typography.bodySmall, color = MornicaTextMuted)
        }
      }
    }
  }
}
