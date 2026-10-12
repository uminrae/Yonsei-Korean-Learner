package com.yonsei.koreanlearner.algorithm

import androidx.compose.ui.text.style.LineBreak
import com.yonsei.koreanlearner.ui.theme.HangulLineBreaking
import com.yonsei.koreanlearner.ui.theme.HangulWordBreakTypography
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩文专属整词折行与排版配置单元测试
 */
class HangulTypographyTest {

    @Test
    fun testHangulTypographyProperties() {
        val typography = HangulWordBreakTypography.createTypography()

        // 验证 BodyLarge 是否成功注入 Phrase-based LineBreak
        assertEquals(LineBreak.WordBreak.Phrase, typography.bodyLarge.lineBreak.wordBreak)
        assertEquals(LineBreak.Strategy.HighQuality, typography.bodyLarge.lineBreak.strategy)
        assertEquals(LineBreak.Strictness.Strict, typography.bodyLarge.lineBreak.strictness)

        // 验证 TitleLarge 是否成功注入
        assertEquals(LineBreak.WordBreak.Phrase, typography.titleLarge.lineBreak.wordBreak)

        // 验证平台样式
        assertFalse(typography.bodyLarge.platformStyle!!.paragraphStyle!!.includeFontPadding)
    }
}
