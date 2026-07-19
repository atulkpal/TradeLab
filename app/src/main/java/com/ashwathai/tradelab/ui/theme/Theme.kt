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

private val LightColorScheme =
  lightColorScheme(
    primary = BrandViolet,
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
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(
    LocalThemeIsDark provides darkTheme
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
