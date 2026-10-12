package com.yonsei.koreanlearner.domain.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yonsei.koreanlearner.data.local.entity.RoundReviewLog
import com.yonsei.koreanlearner.ui.components.HeatmapColorScale
import org.junit.Assert.*
import org.junit.Test

/**
 * 延世 7 轮复习热力图业务逻辑与色阶单元测试
 */
class HeatmapCalculationTest {

    private val gson = Gson()
    private val logType = object : TypeToken<List<RoundReviewLog>>() {}.type

    @Test
    fun testWordHeatItemRoundState() {
        val history = listOf(
            RoundReviewLog(round = 1, timestamp = 1000L, ratingScore = 2, easeFactor = 2.5),
            RoundReviewLog(round = 2, timestamp = 2000L, ratingScore = 3, easeFactor = 2.65),
            RoundReviewLog(round = 3, timestamp = 3000L, ratingScore = 3, easeFactor = 2.8)
        )

        val item = WordHeatItem(
            wordId = 10001,
            book = 1,
            lesson = 1,
            cumulativeIndex = 1,
            korean = "안녕하세요",
            chinese = "你好",
            partOfSpeech = "常用语",
            irregularType = null,
            repetitionLevel = 3,
            heatDepth = 3,
            isGraduated = false,
            easeFactor = 2.8,
            consecutiveSuccess = 3,
            isErrorItem = false,
            history = history
        )

        // 验证各轮次点亮状态
        assertTrue(item.isRoundCompleted(1))
        assertTrue(item.isRoundCompleted(2))
        assertTrue(item.isRoundCompleted(3))
        assertFalse(item.isRoundCompleted(4))
        assertFalse(item.isRoundCompleted(7))

        // 验证打卡历史评分查询
        assertEquals(2, item.getRoundRating(1))
        assertEquals(3, item.getRoundRating(2))
        assertEquals(3, item.getRoundRating(3))
        assertNull(item.getRoundRating(4))
    }

    @Test
    fun testColorScaleBoundary() {
        // 测试 8 级渐变色阶边界（0~7 级）
        val lightLvl0 = HeatmapColorScale.getColor(0, isDark = false)
        val lightLvl7 = HeatmapColorScale.getColor(7, isDark = false)
        val darkLvl0  = HeatmapColorScale.getColor(0, isDark = true)
        val darkLvl7  = HeatmapColorScale.getColor(7, isDark = true)

        assertNotNull(lightLvl0)
        assertNotNull(lightLvl7)
        assertNotNull(darkLvl0)
        assertNotNull(darkLvl7)

        // 溢出值自动截断
        assertEquals(lightLvl7, HeatmapColorScale.getColor(99, isDark = false))
        assertEquals(lightLvl0, HeatmapColorScale.getColor(-5, isDark = false))
    }

    @Test
    fun testRoundReviewLogJsonSerialization() {
        val logs = listOf(
            RoundReviewLog(round = 1, timestamp = 1700000000000L, ratingScore = 2, easeFactor = 2.5),
            RoundReviewLog(round = 7, timestamp = 1710000000000L, ratingScore = 3, easeFactor = 3.2)
        )
        val json = gson.toJson(logs)
        val deserialized: List<RoundReviewLog> = gson.fromJson(json, logType)

        assertEquals(2, deserialized.size)
        assertEquals(7, deserialized[1].round)
        assertEquals(3, deserialized[1].ratingScore)
    }
}
