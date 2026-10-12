package com.yonsei.koreanlearner.algorithm

import androidx.compose.ui.geometry.Offset
import com.yonsei.koreanlearner.algorithm.korean.HangulStrokeData
import com.yonsei.koreanlearner.ui.components.buildSmoothedBezierPath
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩文字母标准笔顺骨架与贝塞尔平滑算法单元测试
 */
class HangulStrokeDataTest {

    @Test
    fun testHangulLettersDataIntegrity() {
        val letters = HangulStrokeData.LETTERS
        assertTrue(letters.isNotEmpty())

        // 验证辅音与元音覆盖
        assertTrue(letters.any { it.letterSymbol == "ㄱ" && it.isConsonant && it.totalStrokes == 1 })
        assertTrue(letters.any { it.letterSymbol == "ㄴ" && it.isConsonant && it.totalStrokes == 1 })
        assertTrue(letters.any { it.letterSymbol == "ㄷ" && it.isConsonant && it.totalStrokes == 2 })
        assertTrue(letters.any { it.letterSymbol == "ㄹ" && it.isConsonant && it.totalStrokes == 3 })
        assertTrue(letters.any { it.letterSymbol == "ㅁ" && it.isConsonant && it.totalStrokes == 3 })
        assertTrue(letters.any { it.letterSymbol == "ㅇ" && it.isConsonant && it.totalStrokes == 1 })
        assertTrue(letters.any { it.letterSymbol == "ㅎ" && it.isConsonant && it.totalStrokes == 3 })
        assertTrue(letters.any { it.letterSymbol == "ㅏ" && !it.isConsonant && it.totalStrokes == 2 })

        letters.forEach { l ->
            assertFalse(l.charName.isBlank())
            assertFalse(l.strokeGuide.isBlank())
            assertEquals(l.totalStrokes, l.strokeList.size)
            l.strokeList.forEach { s ->
                assertTrue(s.startPoint.x in 0f..1f)
                assertTrue(s.startPoint.y in 0f..1f)
                assertTrue(s.endPoint.x in 0f..1f)
                assertTrue(s.endPoint.y in 0f..1f)
            }
        }
    }

    @Test
    fun testBezierPathSmoothingAlgorithm() {
        val samplePoints = listOf(
            Offset(100f, 100f),
            Offset(120f, 130f),
            Offset(150f, 180f),
            Offset(200f, 240f)
        )

        val path = buildSmoothedBezierPath(samplePoints)
        assertFalse(path.isEmpty)
    }
}
