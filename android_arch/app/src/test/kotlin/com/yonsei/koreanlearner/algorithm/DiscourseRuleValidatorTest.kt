package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.DiscourseCategory
import com.yonsei.koreanlearner.algorithm.korean.DiscourseMarkerDatabase
import org.junit.Assert.*
import org.junit.Test

/**
 * 篇章逻辑接续词与转折矩阵规则校验单元测试
 */
class DiscourseRuleValidatorTest {

    @Test
    fun testMarkerDatabaseCoverage() {
        val markers = DiscourseMarkerDatabase.MARKERS
        assertTrue(markers.isNotEmpty())

        // 验证四大维度全覆盖
        assertTrue(markers.any { it.category == DiscourseCategory.ADDITIVE })
        assertTrue(markers.any { it.category == DiscourseCategory.ADVERSATIVE })
        assertTrue(markers.any { it.category == DiscourseCategory.CAUSAL })
        assertTrue(markers.any { it.category == DiscourseCategory.COMPARATIVE })

        // 验证核心接续词存在
        assertTrue(markers.any { it.korean == "그리고" })
        assertTrue(markers.any { it.korean == "그러나" })
        assertTrue(markers.any { it.korean == "그러니까" })
        assertTrue(markers.any { it.korean == "따라서" })
        assertTrue(markers.any { it.korean == "오히려" })
        assertTrue(markers.any { it.korean == "반면에" })
    }

    @Test
    fun testImperativeConstraintValidation() {
        // 后句为命令句：따뜻하게 입으세요.
        // 选用 따라서 -> 应报错并建议 그러니까
        val res1 = DiscourseMarkerDatabase.validateUsage("따라서", "날씨가 춥습니다.", "옷을 따뜻하게 입으세요.")
        assertFalse(res1.isValid)
        assertEquals("그러니까", res1.suggestedMarker)

        // 选用 그래서 -> 应报错
        val res2 = DiscourseMarkerDatabase.validateUsage("그래서", "날씨가 춥습니다.", "옷을 따뜻하게 입으세요.")
        assertFalse(res2.isValid)

        // 选用 그러니까 -> 应通过
        val res3 = DiscourseMarkerDatabase.validateUsage("그러니까", "날씨가 춥습니다.", "옷을 따뜻하게 입으세요.")
        assertTrue(res3.isValid)
    }
}
