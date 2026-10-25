package com.shadowspark.morontomornica.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Original Moronto Music (com.aistudio.morontomusic.xkpqr) Midnight Slate & Emerald/Cyan/Amber Palette
val MornicaMidnightBg = Color(0xFF0B101B)
val MornicaSlateHeader = Color(0xFF0E1525)
val MornicaSlateCard = Color(0xFF131D31)
val MornicaSlateCardElevated = Color(0xFF19263E)
val MornicaSlateBorder = Color(0xFF24344D)

val MornicaEmerald = Color(0xFF10B981)
val MornicaEmeraldDark = Color(0xFF052E22)
val MornicaCyan = Color(0xFF06B6D4)
val MornicaInfoBlue = Color(0xFF0EA5E9)
val MornicaAmberStart = Color(0xFFF59E0B)
val MornicaAmberEnd = Color(0xFFEA580C)
val MornicaGoldSparkle = Color(0xFFFBBF24)
val MornicaDangerRed = Color(0xFFEF4444)

val MornicaTextLight = Color(0xFFF8FAFC)
val MornicaTextMuted = Color(0xFF94A3B8)

val MornicaAmberGradient =
  Brush.horizontalGradient(
    colors = listOf(MornicaAmberStart, MornicaAmberEnd),
  )

/**
 * Design-system tokens extracted via ECC `design-system` and `make-interfaces-feel-better` skills.
 * Concentric radius rule: outerRadius = innerRadius + padding where surfaces nest closely.
 */
object MornicaTokens {
  val CardOuterRadius = 16.dp
  val CardInnerPadding = 12.dp
  val CardInnerEmblemRadius = 10.dp
  val SearchFieldRadius = 14.dp
  val SortPillRadius = 10.dp
  val NowPlayingArtworkRadius = 24.dp
  val MinTouchTarget = 48.dp
  const val TABULAR_NUMS_FEATURE = "tnum"
}

fun TextStyle.withTabularNums(): TextStyle = copy(fontFeatureSettings = MornicaTokens.TABULAR_NUMS_FEATURE)

private val BaseTypography = Typography()

val MornicaTypography =
  Typography(
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = FontFamily.SansSerif, letterSpacing = (-0.3).sp),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = FontFamily.SansSerif, letterSpacing = (-0.2).sp),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = FontFamily.SansSerif),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = FontFamily.SansSerif),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = FontFamily.SansSerif),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = FontFamily.SansSerif),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = FontFamily.SansSerif),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = FontFamily.SansSerif),
  )

private val MornicaDarkColorScheme =
  darkColorScheme(
    primary = MornicaEmerald,
    onPrimary = Color(0xFF04130E),
    secondary = MornicaCyan,
    onSecondary = MornicaMidnightBg,
    tertiary = MornicaAmberStart,
    background = MornicaMidnightBg,
    onBackground = MornicaTextLight,
    surface = MornicaSlateHeader,
    onSurface = MornicaTextLight,
    surfaceVariant = MornicaSlateCard,
    onSurfaceVariant = MornicaTextMuted,
    outline = MornicaSlateBorder,
  )

@Composable
fun MornicaTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = MornicaDarkColorScheme,
    typography = MornicaTypography,
    content = content,
  )
}
