package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.KoreanTokenizer
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩语分词与助词剥离器单元测试
 */
class KoreanTokenizerTest {

    @Test
    fun testSentenceTokenization() {
        val sentence = "눈이 오기 때문에 길이 미끄럽습니다."
        val tokens = KoreanTokenizer.tokenize(sentence)

        assertEquals(5, tokens.size)
        assertEquals("눈이", tokens[0].rawText)
        assertEquals("오기", tokens[1].rawText)
        assertEquals("때문에", tokens[2].rawText)
        assertEquals("길이", tokens[3].rawText)
        assertEquals("미끄럽습니다.", tokens[4].rawText)

        // 验证标点去除与纯净词
        assertEquals("미끄럽습니다", tokens[4].cleanWord)
    }

    @Test
    fun testJosaStrippingCandidates() {
        // "눈이" -> ["눈이", "눈", "눈다"]
        val candidates1 = KoreanTokenizer.generateCandidateStems("눈이")
        assertTrue(candidates1.contains("눈"))

        // "학교에서" -> 剥离 "에서" -> "학교"
        val candidates2 = KoreanTokenizer.generateCandidateStems("학교에서")
        assertTrue(candidates2.contains("학교"))

        // "친구하고" -> 剥离 "하고" -> "친구"
        val candidates3 = KoreanTokenizer.generateCandidateStems("친구하고")
        assertTrue(candidates3.contains("친구"))
    }

    @Test
    fun testVerbEndingCandidates() {
        // "미끄럽습니다" -> 候选包含 "미끄럽다"
        val candidates1 = KoreanTokenizer.generateCandidateStems("미끄럽습니다")
        assertTrue(candidates1.contains("미끄럽다"))

        // "걸어요" -> 候选包含 "걷다"
        val candidates2 = KoreanTokenizer.generateCandidateStems("걸어요")
        assertTrue(candidates2.contains("걷다"))

        // "추워요" -> 候选包含 "춥다"
        val candidates3 = KoreanTokenizer.generateCandidateStems("추워요")
        assertTrue(candidates3.contains("춥다"))

        // "몰라요" -> 候选包含 "모르다"
        val candidates4 = KoreanTokenizer.generateCandidateStems("몰라요")
        assertTrue(candidates4.contains("모르다"))
    }
}
