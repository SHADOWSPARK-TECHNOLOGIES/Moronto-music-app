package com.shadowspark.morontomornica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shadowspark.morontomornica.domain.MusicoletViewModel
import com.shadowspark.morontomornica.ui.MornicaApp
import com.shadowspark.morontomornica.ui.MornicaTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val vm: MusicoletViewModel = viewModel()
      val state by vm.uiState.collectAsState()
      MornicaTheme(themeId = state.themeId) {
        MornicaApp(viewModel = vm)
      }
    }
  }
}
