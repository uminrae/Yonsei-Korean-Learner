package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.ui.components.StorySnapshotGenerator
import org.junit.Assert.*
import org.junit.Test

/**
 * Instagram Story 快照数据模型与尺寸规范测试
 */
class StorySnapshotGeneratorTest {

    @Test
    fun testStoryDimensionConstants() {
        // 验证 Instagram Stories 官方标准分辨率 1080x1920 (9:16)
        assertEquals(1080, StorySnapshotGenerator.STORY_WIDTH)
        assertEquals(1920, StorySnapshotGenerator.STORY_HEIGHT)
        assertEquals(9.0 / 16.0, 1080.0 / 1920.0, 0.001)
    }

    @Test
    fun testSnapshotDataCreation() {
        val data = StorySnapshotGenerator.StorySnapshotData(
            korean          = "안녕하십니까?",
            chinese         = "您好",
            partOfSpeech    = "常用语",
            level           = "TOPIK 1",
            irregularType   = null,
            romanization    = "an-nyeong-ha-sim-ni-kka",
            exampleKorean   = "선생님, 안녕하십니까?",
            exampleChinese  = "老师，您好！",
            book            = 1,
            lesson          = 1,
            repetitionLevel = 3,
            streakDays      = 12,
            easeFactor      = 2.6
        )

        assertEquals("안녕하십니까?", data.korean)
        assertEquals("您好", data.chinese)
        assertEquals(12, data.streakDays)
        assertEquals(3, data.repetitionLevel)
    }
}
