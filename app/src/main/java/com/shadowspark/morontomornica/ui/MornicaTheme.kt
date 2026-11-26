package com.shadowspark.morontomornica.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class MorontoPalette(
  val midnightBg: Color,
  val slateHeader: Color,
  val slateCard: Color,
  val slateElevated: Color,
  val slateBorder: Color,
  val emerald: Color,
  val emeraldDark: Color,
  val cyan: Color,
  val amberStart: Color,
  val amberEnd: Color,
  val dangerRed: Color,
  val textLight: Color,
  val textMuted: Color,
)

private val EmeraldPalette = MorontoPalette(
  midnightBg = Color(0xFF071321),
  slateHeader = Color(0xFF0B1727),
  slateCard = Color(0xFF14243A),
  slateElevated = Color(0xFF1B2F4D),
  slateBorder = Color(0xFF24344D),
  emerald = Color(0xFF12D9A1),
  emeraldDark = Color(0xFF062E22),
  cyan = Color(0xFF20B6EB),
  amberStart = Color(0xFFFFBE55),
  amberEnd = Color(0xFFEA580C),
  dangerRed = Color(0xFFFF6479),
  textLight = Color(0xFFF4FAFE),
  textMuted = Color(0xFFA5B6CA),
)

private val SunsetBrassPalette = MorontoPalette(
  midnightBg = Color(0xFF130E0B),
  slateHeader = Color(0xFF1C140F),
  slateCard = Color(0xFF261B14),
  slateElevated = Color(0xFF33241A),
  slateBorder = Color(0xFF473325),
  emerald = Color(0xFFFFBE55),
  emeraldDark = Color(0xFF3F2B0D),
  cyan = Color(0xFFF59E0B),
  amberStart = Color(0xFFFB923C),
  amberEnd = Color(0xFFEA580C),
  dangerRed = Color(0xFFFB7185),
  textLight = Color(0xFFFEF7F0),
  textMuted = Color(0xFFD1BBA8),
)

private val LagosIndigoPalette = MorontoPalette(
  midnightBg = Color(0xFF090C1E),
  slateHeader = Color(0xFF0E1330),
  slateCard = Color(0xFF171E45),
  slateElevated = Color(0xFF212B5C),
  slateBorder = Color(0xFF2E3C7A),
  emerald = Color(0xFF38BDF8),
  emeraldDark = Color(0xFF0D2D4D),
  cyan = Color(0xFF818CF8),
  amberStart = Color(0xFFFACC15),
  amberEnd = Color(0xFFF97316),
  dangerRed = Color(0xFFFB7185),
  textLight = Color(0xFFF4F7FF),
  textMuted = Color(0xFFA9B4DE),
)

private val ObsidianRosePalette = MorontoPalette(
  midnightBg = Color(0xFF120B11),
  slateHeader = Color(0xFF1B101A),
  slateCard = Color(0xFF261724),
  slateElevated = Color(0xFF332031),
  slateBorder = Color(0xFF472D44),
  emerald = Color(0xFFFB7185),
  emeraldDark = Color(0xFF421521),
  cyan = Color(0xFFF472B6),
  amberStart = Color(0xFFFBBF24),
  amberEnd = Color(0xFFEA580C),
  dangerRed = Color(0xFFFF6479),
  textLight = Color(0xFFFFF5F8),
  textMuted = Color(0xFFD2B6C8),
)

fun paletteForTheme(themeId: String): MorontoPalette =
  when (themeId) {
    "sunset-brass" -> SunsetBrassPalette
    "lagos-indigo" -> LagosIndigoPalette
    "obsidian-rose" -> ObsidianRosePalette
    else -> EmeraldPalette
  }

val LocalMorontoPalette = staticCompositionLocalOf { EmeraldPalette }

val MornicaMidnightBg: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.midnightBg
val MornicaSlateHeader: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.slateHeader
val MornicaSlateCard: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.slateCard
val MornicaSlateElevated: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.slateElevated
val MornicaSlateBorder: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.slateBorder
val MornicaEmerald: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.emerald
val MornicaEmeraldDark: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.emeraldDark
val MornicaCyan: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.cyan
val MornicaAmberStart: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.amberStart
val MornicaAmberEnd: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.amberEnd
val MornicaDangerRed: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.dangerRed
val MornicaTextLight: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.textLight
val MornicaTextMuted: Color
  @Composable @ReadOnlyComposable get() = LocalMorontoPalette.current.textMuted

@Composable
fun MornicaTheme(
  themeId: String = "emerald-console",
  content: @Composable () -> Unit,
) {
  val palette = paletteForTheme(themeId)
  val colorScheme = darkColorScheme(
    primary = palette.emerald,
    onPrimary = Color(0xFF04130E),
    secondary = palette.cyan,
    onSecondary = palette.midnightBg,
    tertiary = palette.amberStart,
    background = palette.midnightBg,
    onBackground = palette.textLight,
    surface = palette.slateHeader,
    onSurface = palette.textLight,
    surfaceVariant = palette.slateCard,
    onSurfaceVariant = palette.textMuted,
    outline = palette.slateBorder,
    error = palette.dangerRed,
  )
  val typography = Typography(
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = palette.textLight),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.textLight),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textLight),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = palette.textMuted),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = palette.textMuted),
  )

  CompositionLocalProvider(LocalMorontoPalette provides palette) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typography,
      content = content,
    )
  }
}
