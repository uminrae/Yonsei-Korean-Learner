package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.algorithm.korean.HanjaNetworkEngine
import org.junit.Assert.*
import org.junit.Test

/**
 * 汉字词词根关联知识图谱单元测试
 */
class HanjaNetworkEngineTest {

    @Test
    fun testRootNetworkGeneration() {
        // 词根 '학(學)'
        val network = HanjaNetworkEngine.getNetworkByRoot('학')
        assertEquals('학', network.centerRoot.koreanChar)
        assertEquals("學", network.centerRoot.hanjaChar)
        assertTrue(network.derivedWords.isNotEmpty())

        // 验证派生词是否全部包含 '학'
        assertTrue(network.derivedWords.any { it.korean == "학생" })
        assertTrue(network.derivedWords.any { it.korean == "학교" })
        assertTrue(network.derivedWords.any { it.korean == "대학교" })
        assertTrue(network.derivedWords.any { it.korean == "학원" })
    }

    @Test
    fun testWordDecomposition() {
        // 分解 "대학교" -> 应包含 '학', '교' 等词根
        val decomp = HanjaNetworkEngine.decomposeHanjaWord("대학교")
        assertTrue(decomp.isNotEmpty())
        assertTrue(decomp.any { it.centerRoot.koreanChar == '학' })
        assertTrue(decomp.any { it.centerRoot.koreanChar == '교' })
    }

    @Test
    fun testRootSearch() {
        val search1 = HanjaNetworkEngine.searchHanja("学习")
        assertTrue(search1.any { it.koreanChar == '학' })

        val search2 = HanjaNetworkEngine.searchHanja("院")
        assertTrue(search2.any { it.koreanChar == '원' })
    }
}
