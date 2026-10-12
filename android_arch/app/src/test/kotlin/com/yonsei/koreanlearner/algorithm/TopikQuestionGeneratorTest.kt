package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.TopikQuestionGenerator
import com.yonsei.koreanlearner.algorithm.korean.TopikQuestionType
import org.junit.Assert.*
import org.junit.Test

/**
 * TOPIK 考点自适应自测与试题生成器单元测试
 */
class TopikQuestionGeneratorTest {

    @Test
    fun testQuestionBankCoverage() {
        val bank = TopikQuestionGenerator.QUESTION_BANK
        assertTrue(bank.isNotEmpty())

        // 验证三大核心题型覆盖
        assertTrue(bank.any { it.type == TopikQuestionType.GRAMMAR_BLANK })
        assertTrue(bank.any { it.type == TopikQuestionType.SYNONYM_REPLACEMENT })
        assertTrue(bank.any { it.type == TopikQuestionType.HONORIFIC_JUDGMENT })
        assertTrue(bank.any { it.type == TopikQuestionType.PHONOLOGY_IDENTIFICATION })
        assertTrue(bank.any { it.type == TopikQuestionType.READING_COMPREHENSION })

        bank.forEach { q ->
            assertFalse(q.questionStem.isBlank())
            assertEquals(4, q.options.size)
            assertTrue(q.correctIndex in 0..3)
            assertFalse(q.explanation.isBlank())
        }
    }

    @Test
    fun testAdaptiveExamGeneration() {
        val exam = TopikQuestionGenerator.generateAdaptiveExam()
        assertEquals(5, exam.size)
    }

    @Test
    fun testAbilityScoresCalculation() {
        val exam = TopikQuestionGenerator.generateAdaptiveExam()
        val allCorrectAnswers = exam.associate { it.id to it.correctIndex }

        val scores = TopikQuestionGenerator.calculateAbilityScores(exam, allCorrectAnswers)
        assertTrue(scores.vocabularyScore >= 80f)
        assertTrue(scores.grammarScore >= 80f)
        assertTrue(scores.estimatedLevel.contains("TOPIK"))
    }
}
