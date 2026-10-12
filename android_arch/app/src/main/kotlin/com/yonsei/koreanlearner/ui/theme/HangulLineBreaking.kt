package com.yonsei.koreanlearner.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文专属排版断行优化配置 (HangulLineBreaking)
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心原理：
 * 1. 采用 Android 14+ (API 34+) 的 LineBreak.WordBreak.Phrase 与 LineBreak.Strategy.HighQuality。
 * 2. 强制韩文长句按照自然词块（어절 / Eojeol）进行优雅折行，彻底杜绝字内截断。
 * 3. 开启 includeFontPadding = false 消除韩文字体上下的不均匀冗余内边距。
 */
object HangulLineBreaking {

    /**
     * 韩文专属词块折行配置（Phrase-based Line Breaking）
     * 保证长句换行时完整保留词汇或助词短语。
     */
    val PhraseBreak = LineBreak(
        strategy = LineBreak.Strategy.HighQuality,
        strictness = LineBreak.Strictness.Strict,
        wordBreak = LineBreak.WordBreak.Phrase
    )

    /**
     * 韩文段落级排版配置
     */
    val ParagraphBreak = LineBreak(
        strategy = LineBreak.Strategy.Balanced,
        strictness = LineBreak.Strictness.Normal,
        wordBreak = LineBreak.WordBreak.Phrase
    )

    /**
     * 韩文大字标题/重点词汇排版配置
     */
    val HeadingBreak = LineBreak(
        strategy = LineBreak.Strategy.HighQuality,
        strictness = LineBreak.Strictness.Strict,
        wordBreak = LineBreak.WordBreak.Default
    )

    /**
     * 禁用系统不自然连字符
     */
    val NoHyphens = Hyphens.None

    /**
     * 消除韩文字体自带的上下不对称内边距（IncludeFontPadding = false）
     */
    val CleanPlatformStyle = PlatformTextStyle(
        includeFontPadding = false
    )
}

// ══════════════════════════════════════════════════════════════════
// TextStyle 扩展函数
// ══════════════════════════════════════════════════════════════════

/**
 * 为任意 TextStyle 注入韩文专属词块优雅断行配置
 */
fun TextStyle.withHangulLineBreak(
    lineBreak: LineBreak = HangulLineBreaking.PhraseBreak
): TextStyle = this.copy(
    lineBreak = lineBreak,
    hyphens = HangulLineBreaking.NoHyphens,
    platformStyle = HangulLineBreaking.CleanPlatformStyle
)

/**
 * 韩文标题/大字原词专用样式（极简、无字内截断、字间距微调）
 */
fun TextStyle.asHangulTitle(fontSize: Int = 28): TextStyle = this.copy(
    fontSize = fontSize.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.02).sp,
    lineBreak = HangulLineBreaking.HeadingBreak,
    hyphens = HangulLineBreaking.NoHyphens,
    platformStyle = HangulLineBreaking.CleanPlatformStyle
)

/**
 * 韩文例句/长文本专用样式（自然词块折行、优化行高）
 */
fun TextStyle.asHangulSentence(fontSize: Int = 16): TextStyle = this.copy(
    fontSize = fontSize.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = (fontSize * 1.5).sp,
    letterSpacing = 0.sp,
    lineBreak = HangulLineBreaking.PhraseBreak,
    hyphens = HangulLineBreaking.NoHyphens,
    platformStyle = HangulLineBreaking.CleanPlatformStyle
)
