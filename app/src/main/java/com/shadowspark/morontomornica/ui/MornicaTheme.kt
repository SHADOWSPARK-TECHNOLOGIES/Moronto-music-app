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

// Mornica brand system — original Nigeria-first palette (docs/brand/MORNICA_BRAND_SYSTEM.md)
// Ink/Canvas/Surface for depth; Jade for affirmative/now-playing; Azure for links/secondary;
// Amber for demo/rights notices; Coral for errors. NOT a copy of any competing music app.
val MornicaMidnightBg = Color(0xFF071321)      // ink — deepest background
val MornicaSlateHeader = Color(0xFF0B1727)      // canvas — app-level surface
val MornicaSlateCard = Color(0xFF14243A)        // surface — elevated cards/sheets
val MornicaSlateCardElevated = Color(0xFF1B2F4D) // surface+1 — hover / pressed cards
val MornicaSlateBorder = Color(0xFF24344D)       // border — subtle separation

val MornicaEmerald = Color(0xFF12D9A1)          // jade — now playing, affirmative
val MornicaEmeraldDark = Color(0xFF062E22)       // jade container
val MornicaCyan = Color(0xFF20B6EB)             // azure — links, secondary accent
val MornicaInfoBlue = Color(0xFF20B6EB)          // azure alias
val MornicaAmberStart = Color(0xFFFFBE55)        // amber — demo/rights/warning start
val MornicaAmberEnd = Color(0xFFEA580C)          // amber gradient end
val MornicaGoldSparkle = Color(0xFFFFBE55)       // amber alias for sparkle icon
val MornicaDangerRed = Color(0xFFFF6479)         // coral — errors, caution

val MornicaTextLight = Color(0xFFF4FAFE)         // foreground text
val MornicaTextMuted = Color(0xFFA5B6CA)         // secondary/muted text

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
    onPrimary = Color(0xFF071321),
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
