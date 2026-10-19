package com.shadowspark.morontomornica.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MornicaDeepPurple = Color(0xFF0D0714)
val MornicaSurfacePurple = Color(0xFF180E29)
val MornicaCardPurple = Color(0xFF23143B)
val MornicaNeonMagenta = Color(0xFFFF2E93)
val MornicaCyberBlue = Color(0xFF00E5FF)
val MornicaGoldAccent = Color(0xFFFFB800)
val MornicaTextLight = Color(0xFFF6F2FF)
val MornicaTextMuted = Color(0xFFBBAFD0)

private val MornicaDarkColorScheme =
  darkColorScheme(
    primary = MornicaNeonMagenta,
    onPrimary = Color.White,
    secondary = MornicaCyberBlue,
    onSecondary = MornicaDeepPurple,
    tertiary = MornicaGoldAccent,
    background = MornicaDeepPurple,
    onBackground = MornicaTextLight,
    surface = MornicaSurfacePurple,
    onSurface = MornicaTextLight,
    surfaceVariant = MornicaCardPurple,
    onSurfaceVariant = MornicaTextMuted,
  )

@Composable
fun MornicaTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = MornicaDarkColorScheme,
    content = content,
  )
}
