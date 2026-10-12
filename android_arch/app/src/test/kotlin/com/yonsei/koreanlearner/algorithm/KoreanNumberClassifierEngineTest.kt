package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.KoreanNumberClassifierEngine
import com.yonsei.koreanlearner.algorithm.korean.KoreanNumberClassifierEngine.Classifier
import com.yonsei.koreanlearner.algorithm.korean.KoreanNumberClassifierEngine.NumberSystemType
import org.junit.Assert.*
import org.junit.Test

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语双轨数词与量词智能转换引擎单元测试
 * ══════════════════════════════════════════════════════════════════
 */
class KoreanNumberClassifierEngineTest {

    // ── 1. 汉字词系统测试 ───────────────────────────────────────

    @Test
    fun testSinoKoreanConversion() {
        assertEquals("영", KoreanNumberClassifierEngine.toSinoKorean(0))
        assertEquals("일", KoreanNumberClassifierEngine.toSinoKorean(1))
        assertEquals("십", KoreanNumberClassifierEngine.toSinoKorean(10))
        assertEquals("십오", KoreanNumberClassifierEngine.toSinoKorean(15))
        assertEquals("삼십", KoreanNumberClassifierEngine.toSinoKorean(30))
        assertEquals("백", KoreanNumberClassifierEngine.toSinoKorean(100))
        assertEquals("천", KoreanNumberClassifierEngine.toSinoKorean(1000))
        assertEquals("이천이십육", KoreanNumberClassifierEngine.toSinoKorean(2026))
        assertEquals("사만 오천", KoreanNumberClassifierEngine.toSinoKorean(45000))
        assertEquals("오십만", KoreanNumberClassifierEngine.toSinoKorean(500000))
    }

    // ── 2. 固有词系统与量词缩略变形测试 ─────────────────────────

    @Test
    fun testNativeKoreanStandaloneAndAdnominal() {
        // 独立形态 (hasClassifier = false)
        assertEquals("하나", KoreanNumberClassifierEngine.toNativeKorean(1, false))
        assertEquals("둘", KoreanNumberClassifierEngine.toNativeKorean(2, false))
        assertEquals("셋", KoreanNumberClassifierEngine.toNativeKorean(3, false))
        assertEquals("넷", KoreanNumberClassifierEngine.toNativeKorean(4, false))
        assertEquals("스물", KoreanNumberClassifierEngine.toNativeKorean(20, false))

        // 接量词变形形态 (hasClassifier = true)
        assertEquals("한", KoreanNumberClassifierEngine.toNativeKorean(1, true))
        assertEquals("두", KoreanNumberClassifierEngine.toNativeKorean(2, true))
        assertEquals("세", KoreanNumberClassifierEngine.toNativeKorean(3, true))
        assertEquals("네", KoreanNumberClassifierEngine.toNativeKorean(4, true))
        assertEquals("스무", KoreanNumberClassifierEngine.toNativeKorean(20, true))

        // 复合固有数字（如 14, 21, 34, 45）
        assertEquals("열네", KoreanNumberClassifierEngine.toNativeKorean(14, true))
        assertEquals("스물한", KoreanNumberClassifierEngine.toNativeKorean(21, true))
        assertEquals("서른네", KoreanNumberClassifierEngine.toNativeKorean(34, true))
        assertEquals("마흔다섯", KoreanNumberClassifierEngine.toNativeKorean(45, true))
    }

    // ── 3. 时间复合表达测试 ─────────────────────────────────────

    @Test
    fun testHybridTimeConversion() {
        // 09:00 -> 오전 아홉 시
        val t1 = KoreanNumberClassifierEngine.convertTime(9, 0)
        assertEquals("오전 아홉 시", t1.koreanFullText)
        assertEquals(NumberSystemType.HYBRID, t1.systemType)

        // 14:35 -> 오후 두 시 삼십오 분
        val t2 = KoreanNumberClassifierEngine.convertTime(14, 35)
        assertEquals("오후 두 시 삼십오 분", t2.koreanFullText)

        // 20:30 -> 오후 여덟 시 삼십 분 (반)
        val t3 = KoreanNumberClassifierEngine.convertTime(20, 30)
        assertTrue(t3.koreanFullText.contains("여덟 시"))
        assertTrue(t3.koreanFullText.contains("삼십 분"))
    }

    // ── 4. 年月日及 6月/10月音变特例测试 ─────────────────────────

    @Test
    fun testDateAndMonthSoundShift() {
        // 6月特例 -> 유월 (非 육월)
        val d1 = KoreanNumberClassifierEngine.convertDate(2026, 6, 15)
        assertEquals("이천이십육 년 유월 십오 일", d1.koreanFullText)

        // 10月特例 -> 시월 (非 십월)
        val d2 = KoreanNumberClassifierEngine.convertDate(2026, 10, 25)
        assertEquals("이천이십육 년 시월 이십오 일", d2.koreanFullText)

        // 常规月份：5月 -> 오월
        val d3 = KoreanNumberClassifierEngine.convertDate(2026, 5, 1)
        assertEquals("이천이십육 년 오월 일 일", d3.koreanFullText)
    }

    // ── 5. 量词场景智能组合测试 ─────────────────────────────────

    @Test
    fun testQuantityClassifierCombination() {
        // 3本书 -> 세 권 (固有词)
        val q1 = KoreanNumberClassifierEngine.convertQuantity(3, Classifier.GWON)
        assertEquals("세 권", q1.koreanFullText)
        assertEquals(NumberSystemType.NATIVE, q1.systemType)

        // 20岁 -> 스무 살 (固有词 20 变形)
        val q2 = KoreanNumberClassifierEngine.convertQuantity(20, Classifier.SAL)
        assertEquals("스무 살", q2.koreanFullText)

        // 45,000韩元 -> 사만 오천 원 (汉字词)
        val q3 = KoreanNumberClassifierEngine.convertQuantity(45000, Classifier.WON)
        assertEquals("사만 오천 원", q3.koreanFullText)
        assertEquals(NumberSystemType.SINO, q3.systemType)

        // 7楼 -> 칠 층 (汉字词)
        val q4 = KoreanNumberClassifierEngine.convertQuantity(7, Classifier.CHEUNG)
        assertEquals("칠 층", q4.koreanFullText)
    }
}
