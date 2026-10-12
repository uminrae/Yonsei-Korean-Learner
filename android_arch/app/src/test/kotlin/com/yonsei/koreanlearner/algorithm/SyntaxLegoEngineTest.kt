package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.SentenceConstraintEvaluator
import com.yonsei.koreanlearner.algorithm.korean.SyntaxLegoEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * 语法乐高拼装与造句语境约束校验单元测试
 */
class SyntaxLegoEngineTest {

    @Test
    fun testSyntaxLegoAssemblyEvaluation() {
        val puzzle = SyntaxLegoEngine.PRESET_PUZZLES[0] // 눈이 오기 때문에 길이 미끄럽습니다.

        // 正确顺序
        val successRes = SyntaxLegoEngine.evaluateAssembly(puzzle, listOf("B1", "B2", "B3"))
        assertTrue(successRes.isCompleted)
        assertTrue(successRes.isCorrect)

        // 错误顺序
        val failRes = SyntaxLegoEngine.evaluateAssembly(puzzle, listOf("B2", "B1", "B3"))
        assertTrue(failRes.isCompleted)
        assertFalse(failRes.isCorrect)
    }

    @Test
    fun testSentenceConstraintEvaluator() {
        // 测试 -는 바람에 触发命令句禁忌
        val res1 = SentenceConstraintEvaluator.evaluateSentence("-는 바람에", "비가 오는 바람에 우산을 쓰세요.")
        assertEquals(SentenceConstraintEvaluator.EvaluationStatus.ERROR, res1.status)
        assertTrue(res1.suggestions.any { it.contains("命令句") })

        // 测试 -는 바람에 过去时冲突
        val res2 = SentenceConstraintEvaluator.evaluateSentence("-는 바람에", "비가 왔는 바람에 늦었어요.")
        assertEquals(SentenceConstraintEvaluator.EvaluationStatus.ERROR, res2.status)
        assertTrue(res2.suggestions.any { it.contains("过去时") })

        // 测试 -는 바람에 积极语境警告
        val res3 = SentenceConstraintEvaluator.evaluateSentence("-는 바람에", "열심히 공부하는 바람에 시험에 합격했어요.")
        assertEquals(SentenceConstraintEvaluator.EvaluationStatus.WARNING, res3.status)

        // 测试 -는 바람에 完美通过
        val res4 = SentenceConstraintEvaluator.evaluateSentence("-는 바람에", "차가 막히는 바람에 약속에 늦었습니다.")
        assertEquals(SentenceConstraintEvaluator.EvaluationStatus.PASS, res4.status)
        assertEquals(100, res4.score)
    }
}
