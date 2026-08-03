package com.ashwathai.tradelab.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BrandViolet,
    secondary = BrandIndigo,
    tertiary = AccentYellow,
    background = Color(0xFF0D0D0D),
    surface = Color(0xFF161616),
    onPrimary = Color(0xFF0D0D0D),
    onSecondary = Color(0xFF0D0D0D),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    outline = Color(0xFF222222)
  )

private val VibrantColorScheme =
  darkColorScheme(
    primary = VibrantMagenta,
    secondary = VibrantCyan,
    tertiary = VibrantOrange,
    background = Color(0xFF0A0A1F),
    surface = Color(0xFF12122A),
    onPrimary = Color(0xFF0A0A1F),
    onSecondary = Color(0xFF0A0A1F),
    onBackground = Color(0xFFE0E0FF),
    onSurface = Color(0xFFE0E0FF),
    outline = Color(0xFF1F1F45)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BrandVioletMedium,
    secondary = BrandIndigo,
    tertiary = AccentYellow,
    background = Color(0xFFF3F4F6),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    outline = Color(0xFFD1D5DB)
  )

@Composable
fun MyApplicationTheme(
  themeMode: ThemeMode = ThemeMode.SERIOUS,
  isStealthMode: Boolean = false,
  isZenMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when (themeMode) {
      ThemeMode.SERIOUS -> DarkColorScheme
      ThemeMode.VIBRANT -> VibrantColorScheme
      ThemeMode.LIGHT -> LightColorScheme
      ThemeMode.TERMINAL -> DarkColorScheme.copy(primary = TermGreen, secondary = TermAmber)
      ThemeMode.ARCADE -> VibrantColorScheme.copy(primary = SynthPink, secondary = SynthBlue)
  }

  CompositionLocalProvider(
    LocalThemeMode provides themeMode,
    LocalThemeIsDark provides (themeMode != ThemeMode.LIGHT),
    LocalStealthMode provides isStealthMode,
    LocalZenMode provides isZenMode
  ) {
    val typography = if (themeMode == ThemeMode.TERMINAL) MonospaceTypography else Typography
    MaterialTheme(
      colorScheme = colorScheme,
      typography = typography,
      content = content
    )
  }
}
