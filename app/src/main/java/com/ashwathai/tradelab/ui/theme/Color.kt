package com.ashwathai.tradelab.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SERIOUS, VIBRANT, LIGHT, TERMINAL, ARCADE }

// Local Composition for Theme Mode
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SERIOUS }
val LocalThemeIsDark = staticCompositionLocalOf { true } // Deprecated, use LocalThemeMode

// Core Brand Colors
val BrandIndigo = Color(0xFF6366F1) // Neon Indigo
val BrandViolet = Color(0xFF8B5CF6) // Neon Violet
val BrandVioletDark = Color(0xFF2E1065) // Dark Purple
val BrandVioletMedium = Color(0xFF7C3AED) // Deep Purple

// Vibrant "GenZ" Colors
val VibrantCyan = Color(0xFF00F2FF)
val VibrantMagenta = Color(0xFFFF00E5)
val VibrantLime = Color(0xFFADFF2F)
val VibrantOrange = Color(0xFFFF8C00)
val VibrantPurple = Color(0xFFBF40BF)

// RetroSynth Palette
val SynthPink = Color(0xFFFF2D55)
val SynthBlue = Color(0xFF007AFF)
val SynthCyan = Color(0xFF5AC8FA)
val SynthPurple = Color(0xFF5856D6)
val SynthBlack = Color(0xFF0D0D12)

// Terminal Palette
val TermGreen = Color(0xFF00FF00)
val TermDarkGreen = Color(0xFF003300)
val TermAmber = Color(0xFFFFB000)
val TermBlack = Color(0xFF000000)

// Profit and Loss Colors
val AccentGreen = Color(0xFF10B981) // Emerald Green for gains
val AccentGreenDark = Color(0xFF064E3B)
val AccentGreenMedium = Color(0xFF34D399)

val AccentRose = Color(0xFFFB7185) // Rose Red for losses
val AccentRoseDark = Color(0xFF4C0519)
val AccentRoseMedium = Color(0xFFE11D48)

val AccentYellow = Color(0xFFFACC15)

// Functional/Centralized Colors
val GoogleBlue = Color(0xFF00A2FF)
val GoogleGreen = Color(0xFF34A853)
val GoogleDarkGray = Color(0xFF1E1E24)
val GoogleTextGray = Color(0xFF9AA0A6)
val WarningRed = Color(0xFFFF5252)

// Dynamic theme resolution
val DarkBg: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF0D0D0D)
        ThemeMode.VIBRANT -> Color(0xFF0A0A1F)
        ThemeMode.LIGHT -> Color(0xFFF3F4F6)
        ThemeMode.TERMINAL -> TermBlack
        ThemeMode.ARCADE -> SynthBlack
    }

val DarkSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF161616)
        ThemeMode.VIBRANT -> Color(0xFF12122A)
        ThemeMode.LIGHT -> Color(0xFFFFFFFF)
        ThemeMode.TERMINAL -> Color(0xFF0A0A0A)
        ThemeMode.ARCADE -> Color(0xFF161622)
    }

val DarkSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF1A1A1A)
        ThemeMode.VIBRANT -> Color(0xFF181835)
        ThemeMode.LIGHT -> Color(0xFFE5E7EB)
        ThemeMode.TERMINAL -> Color(0xFF111111)
        ThemeMode.ARCADE -> Color(0xFF1C1C2D)
    }

val DarkBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF222222)
        ThemeMode.VIBRANT -> Color(0xFF1F1F45)
        ThemeMode.LIGHT -> Color(0xFFD1D5DB)
        ThemeMode.TERMINAL -> TermDarkGreen
        ThemeMode.ARCADE -> SynthPurple.copy(alpha = 0.3f)
    }

val DarkBorderElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF262626)
        ThemeMode.VIBRANT -> Color(0xFF2D2D5A)
        ThemeMode.LIGHT -> Color(0xFF9CA3AF)
        ThemeMode.TERMINAL -> TermGreen.copy(alpha = 0.2f)
        ThemeMode.ARCADE -> SynthPink.copy(alpha = 0.3f)
    }

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFFFFFFFF)
        ThemeMode.VIBRANT -> Color(0xFFFFFFFF)
        ThemeMode.LIGHT -> Color(0xFF111827)
        ThemeMode.TERMINAL -> TermGreen
        ThemeMode.ARCADE -> Color.White
    }

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFFE8E8E8)
        ThemeMode.VIBRANT -> Color(0xFFE0E0FF)
        ThemeMode.LIGHT -> Color(0xFF374151)
        ThemeMode.TERMINAL -> TermGreen.copy(alpha = 0.8f)
        ThemeMode.ARCADE -> Color(0xFFE8E8FF)
    }

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFFA1A1A1)
        ThemeMode.VIBRANT -> Color(0xFF9DA2FF)
        ThemeMode.LIGHT -> Color(0xFF4B5563)
        ThemeMode.TERMINAL -> TermGreen.copy(alpha = 0.5f)
        ThemeMode.ARCADE -> SynthCyan
    }

val TextSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF717171)
        ThemeMode.VIBRANT -> Color(0xFF6D72C3)
        ThemeMode.LIGHT -> Color(0xFF374151)
        ThemeMode.TERMINAL -> TermGreen.copy(alpha = 0.3f)
        ThemeMode.ARCADE -> SynthPurple
    }

// Theme-aware Brand Highlights
val DynamicPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> BrandViolet
        ThemeMode.VIBRANT -> VibrantMagenta
        ThemeMode.LIGHT -> BrandVioletMedium
        ThemeMode.TERMINAL -> TermGreen
        ThemeMode.ARCADE -> SynthPink
    }

val DynamicSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> BrandIndigo
        ThemeMode.VIBRANT -> VibrantCyan
        ThemeMode.LIGHT -> BrandIndigo
        ThemeMode.TERMINAL -> TermAmber
        ThemeMode.ARCADE -> SynthBlue
    }

val DeepProfit: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF0A1F16)
        ThemeMode.VIBRANT -> Color(0xFF051C1C)
        ThemeMode.LIGHT -> Color(0xFFE8F5E9)
        ThemeMode.TERMINAL -> Color(0xFF001A00)
        ThemeMode.ARCADE -> Color(0xFF001A1A)
    }

val DeepLoss: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF220A10)
        ThemeMode.VIBRANT -> Color(0xFF1F0514)
        ThemeMode.LIGHT -> Color(0xFFFFEBEE)
        ThemeMode.TERMINAL -> Color(0xFF1A0000)
        ThemeMode.ARCADE -> Color(0xFF1A0014)
    }

val SurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color(0xFF1A1A1A)
        ThemeMode.VIBRANT -> Color(0xFF181835)
        ThemeMode.LIGHT -> Color(0xFFF1F5F9) // Slate-50/100 feel
        ThemeMode.TERMINAL -> Color(0xFF080808)
        ThemeMode.ARCADE -> Color(0xFF161626)
    }

val TextOnAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = when (LocalThemeMode.current) {
        ThemeMode.SERIOUS -> Color.White
        ThemeMode.VIBRANT -> Color.White
        ThemeMode.LIGHT -> Color.White
        ThemeMode.TERMINAL -> TermBlack
        ThemeMode.ARCADE -> Color.Black
    }


