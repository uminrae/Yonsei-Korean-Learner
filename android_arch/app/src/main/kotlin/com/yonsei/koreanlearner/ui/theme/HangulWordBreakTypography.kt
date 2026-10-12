package com.yonsei.koreanlearner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文专属整词折行与全局排版配置 (HangulWordBreakTypography)
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心原理：
 * 1. 注入 LineBreak.WordBreak.Phrase 与 LineBreak.Strategy.HighQuality。
 * 2. 彻底杜绝韩文字符在单词/助词中间被硬性截断折行的问题。
 * 3. 开启 includeFontPadding = false 消除韩文字体内边距抖动。
 */
object HangulWordBreakTypography {

    private val KoreanLineBreak = LineBreak(
        strategy = LineBreak.Strategy.HighQuality,
        strictness = LineBreak.Strictness.Strict,
        wordBreak = LineBreak.WordBreak.Phrase
    )

    private val CleanPlatformStyle = PlatformTextStyle(
        includeFontPadding = false
    )

    private fun TextStyle.toHangulOptimized(): TextStyle = this.copy(
        lineBreak = KoreanLineBreak,
        hyphens = Hyphens.None,
        platformStyle = CleanPlatformStyle
    )

    /**
     * 生成全套针对韩语优化的 Material 3 Typography
     */
    fun createTypography(baseTypography: Typography = Typography()): Typography {
        return Typography(
            displayLarge   = baseTypography.displayLarge.toHangulOptimized(),
            displayMedium  = baseTypography.displayMedium.toHangulOptimized(),
            displaySmall   = baseTypography.displaySmall.toHangulOptimized(),
            headlineLarge  = baseTypography.headlineLarge.toHangulOptimized(),
            headlineMedium = baseTypography.headlineMedium.toHangulOptimized(),
            headlineSmall  = baseTypography.headlineSmall.toHangulOptimized(),
            titleLarge     = baseTypography.titleLarge.toHangulOptimized(),
            titleMedium    = baseTypography.titleMedium.toHangulOptimized(),
            titleSmall     = baseTypography.titleSmall.toHangulOptimized(),
            bodyLarge      = baseTypography.bodyLarge.toHangulOptimized(),
            bodyMedium     = baseTypography.bodyMedium.toHangulOptimized(),
            bodySmall      = baseTypography.bodySmall.toHangulOptimized(),
            labelLarge     = baseTypography.labelLarge.toHangulOptimized(),
            labelMedium    = baseTypography.labelMedium.toHangulOptimized(),
            labelSmall     = baseTypography.labelSmall.toHangulOptimized()
        )
    }
}
