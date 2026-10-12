package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.DialogueStateMachine
import com.yonsei.koreanlearner.algorithm.korean.ResponseRating
import org.junit.Assert.*
import org.junit.Test

/**
 * 生活情境会话沙盒状态机单元测试
 */
class DialogueStateMachineTest {

    @Test
    fun testAllScenariosIntegrity() {
        val scenarios = DialogueStateMachine.SCENARIOS
        assertEquals(5, scenarios.size)

        // 验证 5 大场景
        assertTrue(scenarios.any { it.id == "store" })
        assertTrue(scenarios.any { it.id == "taxi" })
        assertTrue(scenarios.any { it.id == "hospital" })
        assertTrue(scenarios.any { it.id == "realty" })
        assertTrue(scenarios.any { it.id == "interview" })

        scenarios.forEach { sc ->
            assertTrue(sc.steps.containsKey(sc.initialStepId))
            sc.steps.values.forEach { step ->
                assertEquals(3, step.userOptions.size)
                // 必须包含 1个最佳、1个瑕疵、1个非敬语
                assertTrue(step.userOptions.any { it.rating == ResponseRating.OPTIMAL })
                assertTrue(step.userOptions.any { it.rating == ResponseRating.FLAWED })
                assertTrue(step.userOptions.any { it.rating == ResponseRating.INAPPROPRIATE })
            }
        }
    }
}
