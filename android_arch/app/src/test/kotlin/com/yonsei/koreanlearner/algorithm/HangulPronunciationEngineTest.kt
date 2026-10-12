package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.HangulPronunciationEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语音变规则计算引擎单元测试 (HangulPronunciationEngineTest)
 * ══════════════════════════════════════════════════════════════════
 */
class HangulPronunciationEngineTest {

    // ── 1. 连音化 (Liaison) ─────────────────────────────────────
    @Test
    fun testLiaisonRules() {
        // 单收音连音
        val r1 = HangulPronunciationEngine.getActualPronunciation("한국어")
        assertEquals("[한구거]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.LIAISON))

        val r2 = HangulPronunciationEngine.getActualPronunciation("음악")
        assertEquals("[으막]", r2.pronunciationText)

        val r3 = HangulPronunciationEngine.getActualPronunciation("먹어")
        assertEquals("[머거]", r3.pronunciationText)

        // 双收音连音
        val r4 = HangulPronunciationEngine.getActualPronunciation("앉아")
        assertEquals("[안자]", r4.pronunciationText)

        val r5 = HangulPronunciationEngine.getActualPronunciation("읽어")
        assertEquals("[일거]", r5.pronunciationText)
    }

    // ── 2. 鼻音化 (Nasalization) ────────────────────────────────
    @Test
    fun testNasalizationRules() {
        // ㄱ -> ㅇ
        val r1 = HangulPronunciationEngine.getActualPronunciation("국물")
        assertEquals("[궁물]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.NASALIZATION))

        val r2 = HangulPronunciationEngine.getActualPronunciation("한국말")
        assertEquals("[한궁말]", r2.pronunciationText)

        // ㄷ -> ㄴ
        val r3 = HangulPronunciationEngine.getActualPronunciation("받는")
        assertEquals("[반는]", r3.pronunciationText)

        // ㅂ -> ㅁ
        val r4 = HangulPronunciationEngine.getActualPronunciation("밥맛")
        assertEquals("[밤맏]", r4.pronunciationText)

        val r5 = HangulPronunciationEngine.getActualPronunciation("십년")
        assertEquals("[심년]", r5.pronunciationText)
    }

    // ── 3. 流音化 (Lateralization) ──────────────────────────────
    @Test
    fun testLateralizationRules() {
        // 顺行流音化：ㄴ + ㄹ -> ㄹ + ㄹ
        val r1 = HangulPronunciationEngine.getActualPronunciation("신라")
        assertEquals("[실라]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.LATERALIZATION))

        val r2 = HangulPronunciationEngine.getActualPronunciation("연락")
        assertEquals("[열락]", r2.pronunciationText)

        // 逆行流音化：ㄹ + ㄴ -> ㄹ + ㄹ
        val r3 = HangulPronunciationEngine.getActualPronunciation("칼날")
        assertEquals("[칼랄]", r3.pronunciationText)

        val r4 = HangulPronunciationEngine.getActualPronunciation("설날")
        assertEquals("[설랄]", r4.pronunciationText)
    }

    // ── 4. 口盖音化 / 腭音化 (Palatalization) ────────────────────
    @Test
    fun testPalatalizationRules() {
        // ㄷ + 이 -> 지
        val r1 = HangulPronunciationEngine.getActualPronunciation("굳이")
        assertEquals("[구지]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.PALATALIZATION))

        // ㅌ + 이 -> 치
        val r2 = HangulPronunciationEngine.getActualPronunciation("같이")
        assertEquals("[가치]", r2.pronunciationText)

        val r3 = HangulPronunciationEngine.getActualPronunciation("붙이다")
        assertEquals("[부치다]", r3.pronunciationText)
    }

    // ── 5. 紧音化 / 硬音化 (Tensification) ───────────────────────
    @Test
    fun testTensificationRules() {
        val r1 = HangulPronunciationEngine.getActualPronunciation("입고")
        assertEquals("[입꼬]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.TENSIFICATION))

        val r2 = HangulPronunciationEngine.getActualPronunciation("학교")
        assertEquals("[학꾜]", r2.pronunciationText)

        val r3 = HangulPronunciationEngine.getActualPronunciation("식당")
        assertEquals("[식땅]", r3.pronunciationText)

        val r4 = HangulPronunciationEngine.getActualPronunciation("듣고")
        assertEquals("[듣꼬]", r4.pronunciationText)

        val r5 = HangulPronunciationEngine.getActualPronunciation("잡지")
        assertEquals("[잡찌]", r5.pronunciationText)
    }

    // ── 6. 激音化 / 送气化 (Aspiration) ─────────────────────────
    @Test
    fun testAspirationRules() {
        // 顺行激音化
        val r1 = HangulPronunciationEngine.getActualPronunciation("좋다")
        assertEquals("[조타]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.ASPIRATION))

        val r2 = HangulPronunciationEngine.getActualPronunciation("많다")
        assertEquals("[만타]", r2.pronunciationText)

        // 逆行激音化
        val r3 = HangulPronunciationEngine.getActualPronunciation("축하")
        assertEquals("[추카]", r3.pronunciationText)

        val r4 = HangulPronunciationEngine.getActualPronunciation("입학")
        assertEquals("[이팍]", r4.pronunciationText)

        val r5 = HangulPronunciationEngine.getActualPronunciation("맞히다")
        assertEquals("[마치다]", r5.pronunciationText)
    }

    // ── 7. 复合连续音变 (Compound Shifts) ────────────────────────
    @Test
    fun testCompoundPhonologicalShifts() {
        // 독립 -> 독닙 (流音鼻音化) -> 동닙 (辅音同化鼻音化)
        val r1 = HangulPronunciationEngine.getActualPronunciation("독립")
        assertEquals("[동닙]", r1.pronunciationText)
        assertTrue(r1.appliedRules.contains(HangulPronunciationEngine.PhonologicalRule.NASALIZATION))
    }
}
