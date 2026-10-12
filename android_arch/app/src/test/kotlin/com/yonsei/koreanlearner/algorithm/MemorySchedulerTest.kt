package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating
import org.junit.Assert.*
import org.junit.Test

/**
 * MemoryScheduler 纯函数单元测试
 *
 * 覆盖以下场景：
 * 1. 初始记录创建
 * 2. 四种评分等级的轮次晋升/降级逻辑
 * 3. SM-2 EF 边界值（不低于 1.3）
 * 4. 7轮毕业边界条件
 * 5. 错题集进入与离开条件
 * 6. 连续答对重置/累计逻辑
 */
class MemorySchedulerTest {

    // 固定时间戳（毫秒），方便断言
    private val NOW_MS = 1_700_000_000_000L

    private fun baseRecord(
        level: Int = 0,
        easeFactor: Double = 2.5,
        intervalDays: Int = 0,
        consecutiveSuccess: Int = 0,
        errorCount: Int = 0,
        isErrorItem: Boolean = false
    ) = ReviewRecordEntity(
        itemId                  = 1,
        itemType                = "WORD",
        repetitionLevel         = level,
        easeFactor              = easeFactor,
        intervalDays            = intervalDays,
        nextReviewTimestamp     = NOW_MS,
        consecutiveSuccessCount = consecutiveSuccess,
        errorCount              = errorCount,
        isErrorItem             = isErrorItem,
        firstLearnTimestamp     = NOW_MS,
        lastReviewTimestamp     = NOW_MS,
        totalReviewCount        = 0
    )

    // ── 测试1：初始记录创建 ─────────────────────────────────────

    @Test
    fun `createInitialRecord should have level 0 and default easeFactor`() {
        val record = MemoryScheduler.createInitialRecord(1, "WORD", NOW_MS)
        assertEquals(0, record.repetitionLevel)
        assertEquals(MemoryScheduler.DEFAULT_EASE_FACTOR, record.easeFactor, 0.001)
        assertEquals(0, record.intervalDays)
        assertEquals(NOW_MS, record.nextReviewTimestamp)
        assertEquals(0, record.errorCount)
        assertFalse(record.isErrorItem)
    }

    // ── 测试2：BLACKOUT → 重置到第0轮 ──────────────────────────

    @Test
    fun `BLACKOUT rating should reset level to 0 and schedule immediately`() {
        val record = baseRecord(level = 3, intervalDays = 7)
        val updated = MemoryScheduler.calculateNextReview(record, ReviewRating.BLACKOUT, NOW_MS)

        assertEquals(0, updated.repetitionLevel)
        assertEquals(0, updated.intervalDays)
        // 当天复习，timestamp应等于NOW_MS（不加间隔）
        assertEquals(NOW_MS, updated.nextReviewTimestamp)
        assertEquals(0, updated.consecutiveSuccessCount)
        assertEquals(1, updated.errorCount)
        assertEquals(1, updated.totalReviewCount)
    }

    // ── 测试3：BLACKOUT 累计2次 → 进入错题集 ──────────────────

    @Test
    fun `2 BLACKOUT ratings should mark item as error item`() {
        var record = baseRecord()
        record = MemoryScheduler.calculateNextReview(record, ReviewRating.BLACKOUT, NOW_MS)
        assertFalse("第1次答错不应进入错题集", record.isErrorItem)

        record = MemoryScheduler.calculateNextReview(record, ReviewRating.BLACKOUT, NOW_MS)
        assertTrue("第2次答错应进入错题集", record.isErrorItem)
        assertEquals(2, record.errorCount)
    }

    // ── 测试4：FUZZY → 降一轮，缩短间隔 ──────────────────────

    @Test
    fun `FUZZY rating should decrease level by 1 and shorten interval`() {
        val record = baseRecord(level = 3, intervalDays = 7)
        val updated = MemoryScheduler.calculateNextReview(record, ReviewRating.FUZZY, NOW_MS)

        assertEquals(2, updated.repetitionLevel)    // 3→2
        assertTrue("模糊后间隔应缩短", updated.intervalDays < 7)
        // 连续成功不变（模糊不清零也不增加）
        assertEquals(0, updated.consecutiveSuccessCount)
    }

    // ── 测试5：GOOD → 升一轮，正常扩展 ───────────────────────

    @Test
    fun `GOOD rating should increase level by 1`() {
        val record = baseRecord(level = 0)
        val updated = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)

        assertEquals(1, updated.repetitionLevel)
        assertEquals(1, updated.intervalDays)       // 第1轮基础锚点 = 1天
        assertEquals(1, updated.consecutiveSuccessCount)
    }

    // ── 测试6：PERFECT → 升一轮 + 加速 ───────────────────────

    @Test
    fun `PERFECT rating should advance level and apply boost multiplier`() {
        // 从第3轮开始，SM-2动态扩展 + 艾宾浩斯加速
        val record = baseRecord(level = 3, intervalDays = 7, easeFactor = 2.5)
        val updated = MemoryScheduler.calculateNextReview(record, ReviewRating.PERFECT, NOW_MS)

        assertEquals(4, updated.repetitionLevel)
        // 预期间隔 ≥ 7 * 2.5 * 1.15 ≈ 20天（含抖动，至少 > 基础锚点15天）
        assertTrue("PERFECT后间隔应大于GOOD后间隔", updated.intervalDays >= 15)
    }

    @Test
    fun `PERFECT interval should be larger than GOOD interval for same record`() {
        val record = baseRecord(level = 3, intervalDays = 7, easeFactor = 2.5)
        val good    = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)
        val perfect = MemoryScheduler.calculateNextReview(record, ReviewRating.PERFECT, NOW_MS)

        assertTrue(
            "PERFECT(${perfect.intervalDays}d) 应 >= GOOD(${good.intervalDays}d)",
            perfect.intervalDays >= good.intervalDays
        )
    }

    // ── 测试7：EF 下限约束 ─────────────────────────────────────

    @Test
    fun `easeFactor should never drop below 1_3`() {
        var record = baseRecord(easeFactor = 1.4)
        // 多次 BLACKOUT，EF 应被钳制在 1.3
        repeat(10) {
            record = MemoryScheduler.calculateNextReview(record, ReviewRating.BLACKOUT, NOW_MS)
        }
        assertTrue("EF不应低于1.3", record.easeFactor >= 1.3 - 0.001)
    }

    // ── 测试8：7轮毕业边界 ────────────────────────────────────

    @Test
    fun `reaching level 7 should mark as graduated`() {
        var record = baseRecord(level = 6, intervalDays = 30)
        record = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)

        assertEquals(7, record.repetitionLevel)
        assertTrue(MemoryScheduler.isGraduated(record))
        assertEquals(0, MemoryScheduler.levelsUntilGraduation(record))
    }

    @Test
    fun `level should not exceed 7`() {
        var record = baseRecord(level = 7, intervalDays = 60)
        record = MemoryScheduler.calculateNextReview(record, ReviewRating.PERFECT, NOW_MS)
        assertEquals(7, record.repetitionLevel)
    }

    // ── 测试9：连续答对3次 → 离开错题集 ─────────────────────

    @Test
    fun `3 consecutive GOOD ratings should remove error item flag`() {
        var record = baseRecord(errorCount = 3, isErrorItem = true, consecutiveSuccess = 0)

        record = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)
        assertTrue("1次答对不应移出错题集", record.isErrorItem)

        record = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)
        assertTrue("2次答对不应移出错题集", record.isErrorItem)

        record = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)
        assertFalse("3次连续答对应移出错题集", record.isErrorItem)
    }

    // ── 测试10：totalReviewCount 递增 ─────────────────────────

    @Test
    fun `totalReviewCount increments on every review`() {
        var record = baseRecord()
        repeat(5) { i ->
            record = MemoryScheduler.calculateNextReview(record, ReviewRating.GOOD, NOW_MS)
            assertEquals(i + 1, record.totalReviewCount)
        }
    }

    // ── 测试11：记忆保留率估算 ───────────────────────────────

    @Test
    fun `retention rate should be 1_0 immediately after review`() {
        val record = baseRecord(intervalDays = 7)
        val rate = MemoryScheduler.estimateRetentionRate(record, NOW_MS)
        assertEquals(1.0, rate, 0.001)
    }

    @Test
    fun `retention rate should decrease over time`() {
        val record = baseRecord(intervalDays = 7)
        val sevenDaysLater = NOW_MS + 7L * 24 * 60 * 60 * 1000
        val rate = MemoryScheduler.estimateRetentionRate(record, sevenDaysLater)
        // e^(-7/7) = e^(-1) ≈ 0.368
        assertEquals(Math.exp(-1.0), rate, 0.01)
    }
}
