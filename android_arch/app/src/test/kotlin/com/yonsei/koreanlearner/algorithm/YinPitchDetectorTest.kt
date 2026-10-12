package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.IntonationTemplateDatabase
import com.yonsei.koreanlearner.algorithm.korean.PitchPatternType
import com.yonsei.koreanlearner.core.audio.YinPitchDetector
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

/**
 * YIN 音高提取与 DTW 语调相似度评分算法单元测试
 */
class YinPitchDetectorTest {

    @Test
    fun testYinSineWavePitchDetection() {
        val sampleRate = 44100
        val targetFreq = 220f // 220Hz (A3 标准人声音高)
        val bufferSize = 1024
        val buffer = FloatArray(bufferSize)

        for (i in 0 until bufferSize) {
            buffer[i] = sin(2.0 * PI * targetFreq * i / sampleRate).toFloat()
        }

        val detectedF0 = YinPitchDetector.getPitch(buffer, sampleRate)
        assertTrue(detectedF0 > 0f)
        // 允许 ±5Hz 误差
        assertEquals(targetFreq, detectedF0, 5.0f)
    }

    @Test
    fun testDtwSimilarityScoring() {
        val targetTemplate = listOf(0.3f, 0.4f, 0.5f, 0.7f, 0.9f)
        val identicalUserTrack = listOf(0.3f, 0.4f, 0.5f, 0.7f, 0.9f)
        val slightlyDeviatedTrack = listOf(0.32f, 0.41f, 0.53f, 0.68f, 0.88f)
        val inverseTrack = listOf(0.9f, 0.7f, 0.5f, 0.4f, 0.3f)

        val perfectScore = YinPitchDetector.calculateIntonationScore(identicalUserTrack, targetTemplate)
        val goodScore = YinPitchDetector.calculateIntonationScore(slightlyDeviatedTrack, targetTemplate)
        val poorScore = YinPitchDetector.calculateIntonationScore(inverseTrack, targetTemplate)

        assertTrue("完美重合得分应 >= 90", perfectScore >= 90)
        assertTrue("轻微偏差得分应 >= 80", goodScore >= 80)
        assertTrue("完全反转得分应显著低于正常", poorScore < goodScore)
    }

    @Test
    fun testIntonationTemplateDatabaseCoverage() {
        val templates = IntonationTemplateDatabase.TEMPLATES
        assertEquals(4, templates.size)

        assertTrue(templates.any { it.patternType == PitchPatternType.RISING_TAIL })
        assertTrue(templates.any { it.patternType == PitchPatternType.FALL_RISE_FALL })
        assertTrue(templates.any { it.patternType == PitchPatternType.FALLING_TAIL })
        assertTrue(templates.any { it.patternType == PitchPatternType.EXCLAMATION_PEAK })

        templates.forEach { t ->
            assertEquals(20, t.pitchCurve.size)
            assertFalse(t.sampleSentence.isBlank())
            assertFalse(t.feedbackGuide.isBlank())
        }
    }
}
