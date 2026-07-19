package com.ashwathai.tradelab.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Local Composition for Theme Mode
val LocalThemeIsDark = staticCompositionLocalOf { true }

// Core Neon Theme Brand Colors (Used instead of AccentGreen for general highlights)
val BrandIndigo = Color(0xFF6366F1) // Neon Indigo
val BrandViolet = Color(0xFF8B5CF6) // Neon Violet
val BrandVioletDark = Color(0xFF2E1065) // Dark Purple
val BrandVioletMedium = Color(0xFF7C3AED) // Deep Purple

// Profit and Loss Colors (Exclusively reserved for trade/portfolio gains and losses)
val AccentGreen = Color(0xFF10B981) // Emerald Green for gains
val AccentGreenDark = Color(0xFF064E3B)
val AccentGreenMedium = Color(0xFF34D399)

val AccentRose = Color(0xFFFB7185) // Rose Red for losses
val AccentRoseDark = Color(0xFF4C0519)
val AccentRoseMedium = Color(0xFFE11D48)

val AccentYellow = Color(0xFFFACC15)

// Dynamic theme resolution for backward compatibility with hardcoded styles
val DarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFF0D0D0D) else Color(0xFFF3F4F6)

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFF161616) else Color(0xFFFFFFFF)

val DarkSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFF1A1A1A) else Color(0xFFE5E7EB)

val DarkBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFF222222) else Color(0xFFD1D5DB)

val DarkBorderElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFF262626) else Color(0xFF9CA3AF)

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFFFFFFFF) else Color(0xFF111827)

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFFE8E8E8) else Color(0xFF374151)

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFFA1A1A1) else Color(0xFF6B7280)

val TextSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalThemeIsDark.current) Color(0xFF717171) else Color(0xFF9CA3AF)
