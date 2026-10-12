package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.core.audio.LrcParser
import org.junit.Assert.*
import org.junit.Test

/**
 * LRC 歌词字幕解析与智能挖空单元测试
 */
class LrcParserTest {

    @Test
    fun testLrcTimestampAndBilingualParsing() {
        val lrc = """
            [00:00.00]안녕하세요, 여러분! | 大家好，各位！
            [00:03.50]눈이 오기 때문에 길이 미끄럽습니다. | 因为下雪，所以路很滑。
            [00:08.200]학교에서 친구를 만났어요. // 在学校见到了朋友。
        """.trimIndent()

        val lines = LrcParser.parseLrc(lrc)
        assertEquals(3, lines.size)

        // 验证行 0
        assertEquals(0L, lines[0].startTimeMs)
        assertEquals(3500L, lines[0].endTimeMs)
        assertEquals("안녕하세요, 여러분!", lines[0].koreanText)
        assertEquals("大家好，各位！", lines[0].chineseText)

        // 验证行 1
        assertEquals(3500L, lines[1].startTimeMs)
        assertEquals(8200L, lines[1].endTimeMs)
        assertEquals("눈이 오기 때문에 길이 미끄럽습니다.", lines[1].koreanText)
        assertEquals("因为下雪，所以路很滑。", lines[1].chineseText)

        // 验证行 2
        assertEquals(8200L, lines[2].startTimeMs)
        assertEquals("학교에서 친구를 만났어요.", lines[2].koreanText)
        assertEquals("在学校见到了朋友。", lines[2].chineseText)
    }

    @Test
    fun testSmartBlankGeneration() {
        val question1 = LrcParser.generateSmartBlank("눈이 오기 때문에 길이 미끄럽습니다.")
        assertNotNull(question1)
        assertTrue(question1!!.maskedKorean.contains("_____"))
        assertNotNull(question1.targetAnswer)

        val question2 = LrcParser.generateSmartBlank("학교에서 친구를 만났어요.")
        assertNotNull(question2)
        assertTrue(question2!!.targetAnswer.contains("학교에서") || question2.targetAnswer.contains("친구를") || question2.targetAnswer.contains("만났어요"))
    }
}
