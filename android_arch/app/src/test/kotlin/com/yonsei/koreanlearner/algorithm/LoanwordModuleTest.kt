package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.data.local.database.LoanwordSeedData
import org.junit.Assert.*
import org.junit.Test

/**
 * 外来语与传统词汇双轨对照专栏单元测试
 */
class LoanwordModuleTest {

    @Test
    fun testLoanwordPresetIntegrity() {
        val list = LoanwordSeedData.PRESET_LOANWORDS
        assertTrue(list.isNotEmpty())

        // 验证高频外来语
        assertTrue(list.any { it.loanword == "아르바이트" && it.foreignOrigin.contains("Arbeit") })
        assertTrue(list.any { it.loanword == "스마트폰" && it.foreignOrigin.contains("Smartphone") })
        assertTrue(list.any { it.loanword == "다이어트" && it.foreignOrigin.contains("Diet") })
        assertTrue(list.any { it.loanword == "서비스" })
        assertTrue(list.any { it.loanword == "원룸" })

        list.forEach { lw ->
            assertFalse(lw.loanword.isBlank())
            assertFalse(lw.foreignOrigin.isBlank())
            assertFalse(lw.nativeKoreanEquivalent.isBlank())
            assertFalse(lw.chineseMeaning.isBlank())
            assertFalse(lw.exampleKorean.isBlank())
        }
    }
}
