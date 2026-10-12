package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.AffixCategory
import com.yonsei.koreanlearner.algorithm.korean.VoiceTransformationEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩语法语态（Voice）与句法重构算法单元测试
 */
class VoiceTransformationEngineTest {

    @Test
    fun testVoiceAffixDictionary() {
        val dict = VoiceTransformationEngine.VOICE_DICTIONARY
        assertTrue(dict.isNotEmpty())

        // 1. 이 词缀
        val v1 = VoiceTransformationEngine.findVoiceEntry("보다")
        assertNotNull(v1)
        assertEquals("보이다", v1?.passiveVerb)
        assertEquals(AffixCategory.I, v1?.affix)

        val v2 = VoiceTransformationEngine.findVoiceEntry("줄다")
        assertEquals("줄이다", v2?.causativeVerb)

        // 2. 히 词缀
        val v3 = VoiceTransformationEngine.findVoiceEntry("닫다")
        assertEquals("닫히다", v3?.passiveVerb)

        val v4 = VoiceTransformationEngine.findVoiceEntry("입다")
        assertEquals("입히다", v4?.causativeVerb)

        // 3. 리 词缀
        val v5 = VoiceTransformationEngine.findVoiceEntry("열다")
        assertEquals("열리다", v5?.passiveVerb)

        val v6 = VoiceTransformationEngine.findVoiceEntry("살다")
        assertEquals("살리다", v6?.causativeVerb)

        // 4. 기 词缀
        val v7 = VoiceTransformationEngine.findVoiceEntry("안다")
        assertEquals("안기다", v7?.passiveVerb)

        val v8 = VoiceTransformationEngine.findVoiceEntry("웃다")
        assertEquals("웃기다", v8?.causativeVerb)

        // 5. 우/추 词缀
        val v9 = VoiceTransformationEngine.findVoiceEntry("자다")
        assertEquals("재우다", v9?.causativeVerb)

        val v10 = VoiceTransformationEngine.findVoiceEntry("낮다")
        assertEquals("낮추다", v10?.causativeVerb)
    }

    @Test
    fun testSampleTransformationsCoverage() {
        val samples = VoiceTransformationEngine.SAMPLE_TRANSFORMATIONS
        assertTrue(samples.isNotEmpty())

        // 验证涵盖被动句与使动句
        assertTrue(samples.any { it.passiveSentence != null })
        assertTrue(samples.any { it.causativeSentence != null })

        samples.forEach { s ->
            assertFalse(s.activeSentence.isBlank())
            assertFalse(s.agent.isBlank())
            assertFalse(s.patient.isBlank())
            assertFalse(s.explanation.isBlank())
        }
    }
}
