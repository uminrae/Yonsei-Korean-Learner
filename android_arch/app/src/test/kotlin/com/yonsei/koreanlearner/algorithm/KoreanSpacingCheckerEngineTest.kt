package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.KoreanSpacingCheckerEngine
import com.yonsei.koreanlearner.algorithm.korean.SpacingRuleCategory
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩文规范空格（띄어쓰기）校验器单元测试
 */
class KoreanSpacingCheckerEngineTest {

    @Test
    fun testBoundNounAndUnitSpacingCorrection() {
        val raw = "한국어를공부한지세달이되었어요"
        val result = KoreanSpacingCheckerEngine.correctSpacing(raw)

        assertEquals("한국어를 공부한 지 세 달이 되었어요.", result.correctedText)
        assertTrue(result.appliedRules.contains(SpacingRuleCategory.BOUND_NOUN_SPACING))
        assertTrue(result.appliedRules.contains(SpacingRuleCategory.UNIT_NOUN_SPACING))
    }

    @Test
    fun testAuxiliaryVerbAndParticleSpacing() {
        val raw = "내일은도서관에서책을읽을수있어요"
        val result = KoreanSpacingCheckerEngine.correctSpacing(raw)

        assertEquals("내일은 도서관에서 책을 읽을 수 있어요.", result.correctedText)
        assertTrue(result.appliedRules.contains(SpacingRuleCategory.BOUND_NOUN_SPACING))
    }

    @Test
    fun testPresetDrillCasesIntegrity() {
        val cases = KoreanSpacingCheckerEngine.PRESET_DRILL_CASES
        assertEquals(4, cases.size)

        cases.forEach { c ->
            assertFalse(c.rawUnspaced.isBlank())
            assertFalse(c.standardSpaced.isBlank())
            assertFalse(c.explanation.isBlank())
        }
    }
}
