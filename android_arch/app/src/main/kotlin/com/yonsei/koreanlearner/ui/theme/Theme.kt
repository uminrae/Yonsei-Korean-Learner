package com.yonsei.koreanlearner.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════════════
// 「未来的韩语」— 统一极简 Ins 纯白 / Pure OLED Studio 纯黑双轨调色板
// ══════════════════════════════════════════════════════════════════

// ── 1. Ins 纯白高光配色 ───────────────────────────────────────────
private val InsWhiteBackdrop = Color(0xFFF7F8FA) // 柔和高级纯白背景
private val InsCardWhite     = Color(0xFFFFFFFF) // 纯白卡片
private val InsCardBorder    = Color(0xFFE9ECEF) // 极细高光描边
private val InsTextPrimary   = Color(0xFF111827) // 深黑主标题
private val InsTextSecondary = Color(0xFF6B7280) // 优雅副文本
private val InsTextMuted     = Color(0xFF9CA3AF) // 浅灰占位

// ── 2. Pure OLED Studio 纯黑深色配色 ─────────────────────────────
private val OledBlackBackdrop = Color(0xFF000000) // 纯黑 OLED 像素级全黑背景
private val OledCardSurface   = Color(0xFF121214) // 深邃磨砂暗黑卡片
private val OledCardBorder    = Color(0xFF26262B) // 暗光质感描边
private val OledTextPrimary   = Color(0xFFF9FAFB) // 纯白高对比文字
private val OledTextSecondary = Color(0xFF9CA3AF) // 浅灰文字
private val OledTextMuted     = Color(0xFF6B7280) // 极简暗调

private val UnifiedLightColorScheme = lightColorScheme(
    primary              = InsTextPrimary,
    onPrimary            = Color.White,
    primaryContainer     = Color(0xFFF3F4F6),
    onPrimaryContainer   = InsTextPrimary,

    secondary            = Color(0xFF374151),
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFF9FAFB),
    onSecondaryContainer = InsTextPrimary,

    tertiary             = Color(0xFF4B5563),
    onTertiary           = Color.White,
    tertiaryContainer    = Color(0xFFF3F4F6),
    onTertiaryContainer  = InsTextPrimary,

    background           = InsWhiteBackdrop,
    onBackground         = InsTextPrimary,
    surface              = InsCardWhite,
    onSurface            = InsTextPrimary,
    surfaceVariant       = Color(0xFFF3F4F6),
    onSurfaceVariant     = InsTextSecondary,

    outline              = InsCardBorder,
    outlineVariant       = Color(0xFFE5E7EB),

    error                = Color(0xFFEF4444),
    onError              = Color.White,
    errorContainer       = Color(0xFFFEE2E2),
    onErrorContainer     = Color(0xFF991B1B),

    surfaceTint          = InsTextPrimary,
    scrim                = Color(0x33000000)
)

private val UnifiedDarkColorScheme = darkColorScheme(
    primary              = Color.White,
    onPrimary            = Color(0xFF111827),
    primaryContainer     = Color(0xFF1F2937),
    onPrimaryContainer   = Color.White,

    secondary            = Color(0xFFE5E7EB),
    onSecondary          = Color(0xFF111827),
    secondaryContainer   = Color(0xFF1E1E22),
    onSecondaryContainer = Color.White,

    tertiary             = Color(0xFF9CA3AF),
    onTertiary           = Color(0xFF111827),
    tertiaryContainer    = Color(0xFF1F2937),
    onTertiaryContainer  = Color.White,

    background           = OledBlackBackdrop,
    onBackground         = OledTextPrimary,
    surface              = OledCardSurface,
    onSurface            = OledTextPrimary,
    surfaceVariant       = Color(0xFF1E1E22),
    onSurfaceVariant     = OledTextSecondary,

    outline              = OledCardBorder,
    outlineVariant       = Color(0xFF374151),

    error                = Color(0xFFF87171),
    onError              = Color(0xFF450A0A),
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Color(0xFFFEE2E2),

    surfaceTint          = Color.White,
    scrim                = Color(0x66000000)
)

@Immutable
data class ExtendedColors(
    val gradientStart  : Color = Color(0xFF111827),
    val gradientMid1   : Color = Color(0xFF374151),
    val gradientMid2   : Color = Color(0xFF6B7280),
    val gradientEnd    : Color = Color(0xFF9CA3AF),
    val ratingBlackout : Color = Color(0xFFEF4444),
    val ratingFuzzy    : Color = Color(0xFFF59E0B),
    val ratingGood     : Color = Color(0xFF10B981),
    val ratingPerfect  : Color = Color(0xFF3B82F6),
    val retentionHigh  : Color = Color(0xFF10B981),
    val retentionMid   : Color = Color(0xFFF59E0B),
    val retentionLow   : Color = Color(0xFFEF4444),
    val borderSubtle   : Color = InsCardBorder,
    val cardBorder     : Color = InsCardBorder,
    val surfaceL1      : Color = InsCardWhite,
    val surfaceL2      : Color = Color(0xFFF3F4F6),
    val streakFire     : Color = InsTextPrimary
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

val MaterialTheme.extended: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current

@Composable
fun KoreanLearnerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) UnifiedDarkColorScheme else UnifiedLightColorScheme
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            gradientStart  = Color(0xFFF9FAFB),
            gradientMid1   = Color(0xFFE5E7EB),
            gradientMid2   = Color(0xFF9CA3AF),
            gradientEnd    = Color(0xFF6B7280),
            borderSubtle   = OledCardBorder,
            cardBorder     = OledCardBorder,
            surfaceL1      = OledCardSurface,
            surfaceL2      = Color(0xFF1E1E22),
            streakFire     = Color.White
        )
    } else {
        ExtendedColors()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = if (darkTheme) Color.Black.toArgb() else InsCardWhite.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = KoreanLearnerTypography,
            shapes      = KoreanLearnerShapes,
            content     = content
        )
    }
}
