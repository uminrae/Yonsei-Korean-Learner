package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.data.local.database.IdiomSeedData
import com.yonsei.koreanlearner.data.local.entity.IdiomType
import org.junit.Assert.*
import org.junit.Test

/**
 * 俗语、四字成语与拟声拟态词数据模型与种子数据集单元测试
 */
class IdiomModuleTest {

    @Test
    fun testSeedDataIntegrity() {
        val list = IdiomSeedData.PRESET_IDIOMS
        assertTrue(list.isNotEmpty())

        // 验证分类覆盖
        val idioms = list.filter { it.type == IdiomType.IDIOM }
        val fourChars = list.filter { it.type == IdiomType.FOUR_CHAR }
        val onomatopoeia = list.filter { it.type == IdiomType.ONOMATOPOEIA }

        assertTrue(idioms.isNotEmpty())
        assertTrue(fourChars.isNotEmpty())
        assertTrue(onomatopoeia.isNotEmpty())

        // 验证核心条目存在
        assertTrue(idioms.any { it.korean == "쇠귀에 경 읽기" })
        assertTrue(idioms.any { it.korean == "갈수록 태산" })
        assertTrue(fourChars.any { it.korean == "고진감래" && it.hanja == "苦盡甘來" })
        assertTrue(onomatopoeia.any { it.korean == "따르릉" })
        assertTrue(onomatopoeia.any { it.korean == "두근두근" })
    }

    @Test
    fun testCulturalBackgroundAndDialogues() {
        IdiomSeedData.PRESET_IDIOMS.forEach { idiom ->
            assertFalse(idiom.literalMeaning.isBlank())
            assertFalse(idiom.metaphorMeaning.isBlank())
            assertFalse(idiom.culturalBackground.isBlank())
            assertFalse(idiom.dialoguesJson.isBlank())
        }
    }
}
