package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.ui.components.KoreanPhoneticTranscriber
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩语实际发音形态与音变分析引擎单元测试
 */
class KoreanPhoneticTranscriberTest {

    @Test
    fun testPalatalization() {
        // 腭音化：같이 -> [가치], 굳이 -> [구지]
        val p1 = KoreanPhoneticTranscriber.transcribe("같이")
        assertEquals("[가치]", p1.standardPhonetic)
        assertEquals("腭音化", p1.soundShiftType)

        val p2 = KoreanPhoneticTranscriber.transcribe("굳이")
        assertEquals("[구지]", p2.standardPhonetic)
    }

    @Test
    fun testNasalization() {
        // 鼻音化：국물 -> [궁물], 독립 -> [동닙], 감사합니다 -> [감사함니다]
        val n1 = KoreanPhoneticTranscriber.transcribe("국물")
        assertEquals("[궁물]", n1.standardPhonetic)
        assertTrue(n1.soundShiftType!!.contains("鼻音化"))

        val n2 = KoreanPhoneticTranscriber.transcribe("독립")
        assertEquals("[동닙]", n2.standardPhonetic)

        val n3 = KoreanPhoneticTranscriber.transcribe("감사합니다")
        assertEquals("[감사함니다]", n3.standardPhonetic)
    }

    @Test
    fun testLateralization() {
        // 流音化：신라 -> [실라], 연락 -> [열락]
        val l1 = KoreanPhoneticTranscriber.transcribe("신라")
        assertEquals("[실라]", l1.standardPhonetic)
        assertEquals("流音化", l1.soundShiftType)

        val l2 = KoreanPhoneticTranscriber.transcribe("연락")
        assertEquals("[열락]", l2.standardPhonetic)
    }

    @Test
    fun testTensification() {
        // 硬音化 / 紧音化：학교 -> [학꾜], 식당 -> [식땅], 듣고 -> [듣꼬]
        val t1 = KoreanPhoneticTranscriber.transcribe("학교")
        assertEquals("[학꾜]", t1.standardPhonetic)
        assertTrue(t1.soundShiftType!!.contains("硬音化"))

        val t2 = KoreanPhoneticTranscriber.transcribe("식당")
        assertEquals("[식땅]", t2.standardPhonetic)
    }

    @Test
    fun testLiaison() {
        // 连音化：한국어 -> [한구거], 음악 -> [으막], 먹어 -> [머거]
        val li1 = KoreanPhoneticTranscriber.transcribe("한국어")
        assertEquals("[한구거]", li1.standardPhonetic)
        assertEquals("连音化", li1.soundShiftType)

        val li2 = KoreanPhoneticTranscriber.transcribe("먹어")
        assertEquals("[머거]", li2.standardPhonetic)
    }
}
