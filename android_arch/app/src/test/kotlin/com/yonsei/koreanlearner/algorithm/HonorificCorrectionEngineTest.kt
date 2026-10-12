package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.HonorificCorrectionEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * 韩语敬语/谦语/助词纠错引擎单元测试
 */
class HonorificCorrectionEngineTest {

    @Test
    fun testFullSentenceCorrection() {
        // 输入平语句："할아버지가 밥을 먹고 집에서 자요."
        // 期望敬语句："할아버지께서 진지를 드시고 댁에서 주무세요."
        val report = HonorificCorrectionEngine.correctToHonorific("할아버지가 밥을 먹고 집에서 자요.")

        assertEquals("할아버지께서 진지를 드시고 댁에서 주무세요.", report.correctedSentence)
        assertEquals(4, report.totalReplacements)
        assertTrue(report.diffList.any { it.originalToken == "할아버지가" && it.correctedToken == "할아버지께서" })
        assertTrue(report.diffList.any { it.originalToken == "밥을" && it.correctedToken == "진지를" })
        assertTrue(report.diffList.any { it.originalToken == "집에서" && it.correctedToken == "댁에서" })
        assertTrue(report.diffList.any { it.originalToken == "먹고" && it.correctedToken == "드시고" })
    }

    @Test
    fun testHumbleVerbCorrection() {
        // 输入："선생님한테 선물을 줘요." -> "선생님께 선물을 드려요."
        val report = HonorificCorrectionEngine.correctToHonorific("선생님한테 선물을 줘요.")

        assertTrue(report.correctedSentence.contains("선생님께"))
        assertTrue(report.correctedSentence.contains("드려요"))
    }
}
