package com.yonsei.koreanlearner.algorithm

import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating
import kotlin.math.roundToInt

/**
 * 记忆排程算法引擎（MemoryScheduler）
 *
 * ═══════════════════════════════════════════════════════════
 * 算法原理：改良版 SM-2 + 艾宾浩斯遗忘曲线双重融合
 * ═══════════════════════════════════════════════════════════
 *
 * ## SM-2 核心公式
 * 新难度系数：
 *   EF' = EF + (0.1 - (3-q) × (0.08 + (3-q) × 0.02))
 *   其中 q = 用户评分（0~3），EF 范围 [1.3, 4.0]
 *
 * 复习间隔扩展（从第3轮起）：
 *   I(n) = I(n-1) × EF   [q=2, 掌握]
 *   I(n) = I(n-1) × EF × 1.15  [q=3, 熟练秒杀，艾宾浩斯加速修正]
 *
 * ## 延世背诵本 7 轮基础锚点
 * 将 SM-2 动态间隔锚定到延世背诵节奏：
 *   轮次 0→1: 当天    轮次 1→2: 1天
 *   轮次 2→3: 3天     轮次 3→4: 7天
 *   轮次 4→5: 15天    轮次 5→6: 30天
 *   轮次 6→7: 60天（毕业）
 *
 * ## 抖动机制
 * 为防止复习队列在同一时间大量到期（扎堆效应），
 * 每次计算的下次复习时间增加 ±10% 随机偏移。
 *
 * ═══════════════════════════════════════════════════════════
 */
object MemoryScheduler {

    // ── 常量配置 ──────────────────────────────────────────
    private const val MIN_EASE_FACTOR     = 1.3
    private const val MAX_EASE_FACTOR     = 4.0
    const val         DEFAULT_EASE_FACTOR = 2.5

    /** 熟练秒杀时的间隔加速因子（艾宾浩斯修正） */
    private const val PERFECT_BOOST       = 1.15

    /** 错题集触发阈值（累计出错次数） */
    const val ERROR_ITEM_THRESHOLD        = 2

    /** 错题集解除阈值（连续答对次数） */
    const val RECOVER_SUCCESS_THRESHOLD   = 3

    /** 最高复习轮次（达到7轮=背诵本完成=长期记忆毕业） */
    const val MAX_REPETITION_LEVEL        = 7

    /**
     * 延世背诵本 7 轮对应的基础最小间隔天数锚点。
     * 索引 i = 进入第 i 轮后的最小间隔天数。
     * 索引 7 = MAX_VALUE（毕业，不再加入复习队列）
     */
    private val YONSEI_BASE_INTERVALS = intArrayOf(
        /* 0→新词 */ 0,
        /* 1轮 */    1,
        /* 2轮 */    3,
        /* 3轮 */    7,
        /* 4轮 */    15,
        /* 5轮 */    30,
        /* 6轮 */    60,
        /* 7轮/毕业 */ Int.MAX_VALUE
    )

    private const val DAY_MS = 24L * 60L * 60L * 1000L

    // ────────────────────────────────────────────────────────
    // 公开 API
    // ────────────────────────────────────────────────────────

    /**
     * 根据用户评分计算下一次复习的所有参数。
     *
     * 此函数为**纯函数**（无副作用），仅返回更新后的实体副本，
     * 持久化由上层 Repository / UseCase 负责。
     *
     * @param currentRecord 当前记忆记录（从 Room 读取）
     * @param rating        用户本次自评等级 [ReviewRating]
     * @param nowMs         当前时间戳（毫秒），默认 System.currentTimeMillis()
     * @return              更新后的 [ReviewRecordEntity]（未写入数据库）
     */
    fun calculateNextReview(
        currentRecord: ReviewRecordEntity,
        rating: ReviewRating,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewRecordEntity {

        val score = rating.score

        // ── Step 1: 更新难度系数 EF ──────────────────────
        val newEaseFactor = computeEaseFactor(currentRecord.easeFactor, score)

        // ── Step 2: 更新复习轮次 & 间隔天数 ──────────────
        val (newLevel, newIntervalDays) = computeLevelAndInterval(
            currentRecord   = currentRecord,
            score           = score,
            newEaseFactor   = newEaseFactor
        )

        // ── Step 3: 更新连续答对次数 ──────────────────────
        val newConsecutiveSuccess = when {
            score >= ReviewRating.GOOD.score  -> currentRecord.consecutiveSuccessCount + 1
            score == ReviewRating.BLACKOUT.score -> 0
            else                                -> currentRecord.consecutiveSuccessCount
        }

        // ── Step 4: 更新出错计数 & 错题集标记 ────────────
        val newErrorCount = if (score == ReviewRating.BLACKOUT.score) {
            currentRecord.errorCount + 1
        } else {
            currentRecord.errorCount
        }

        // 进入错题集：出错≥阈值
        // 离开错题集：连续成功≥阈值（优先判断，所以用newConsecutiveSuccess）
        val newIsErrorItem = when {
            newConsecutiveSuccess >= RECOVER_SUCCESS_THRESHOLD -> false
            newErrorCount >= ERROR_ITEM_THRESHOLD             -> true
            else                                              -> currentRecord.isErrorItem
        }

        // ── Step 5: 计算下次复习时间戳（含±10%抖动）────
        val nextReviewTs = computeNextReviewTimestamp(nowMs, newIntervalDays)

        // ── Step 6: 追加每轮打卡历史 ───────────────────
        val gson = com.google.gson.Gson()
        val listType = object : com.google.gson.reflect.TypeToken<List<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog>>() {}.type
        val existingLogs: MutableList<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog> = try {
            gson.fromJson<List<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog>>(currentRecord.reviewHistory, listType)?.toMutableList() ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        existingLogs.add(
            com.yonsei.koreanlearner.data.local.entity.RoundReviewLog(
                round       = newLevel,
                timestamp   = nowMs,
                ratingScore = rating.score,
                easeFactor  = newEaseFactor
            )
        )

        return currentRecord.copy(
            repetitionLevel         = newLevel,
            easeFactor              = newEaseFactor,
            intervalDays            = newIntervalDays,
            nextReviewTimestamp     = nextReviewTs,
            consecutiveSuccessCount = newConsecutiveSuccess,
            errorCount              = newErrorCount,
            isErrorItem             = newIsErrorItem,
            lastReviewTimestamp     = nowMs,
            totalReviewCount        = currentRecord.totalReviewCount + 1,
            reviewHistory           = gson.toJson(existingLogs)
        )
    }

    /**
     * 为新词/新语法创建初始记忆记录（repetitionLevel = 0）。
     *
     * @param itemId   WordEntity.id 或 GrammarEntity.id
     * @param itemType "WORD" 或 "GRAMMAR"
     */
    fun createInitialRecord(
        itemId: Int,
        itemType: String,
        nowMs: Long = System.currentTimeMillis()
    ): ReviewRecordEntity = ReviewRecordEntity(
        itemId                  = itemId,
        itemType                = itemType,
        repetitionLevel         = 0,
        easeFactor              = DEFAULT_EASE_FACTOR,
        intervalDays            = 0,
        nextReviewTimestamp     = nowMs,   // 新词立即可复习
        consecutiveSuccessCount = 0,
        errorCount              = 0,
        isErrorItem             = false,
        firstLearnTimestamp     = nowMs,
        lastReviewTimestamp     = null,
        totalReviewCount        = 0
    )

    // ────────────────────────────────────────────────────────
    // 私有计算函数
    // ────────────────────────────────────────────────────────

    /**
     * SM-2 难度系数更新公式。
     * EF' = EF + (0.1 - (3-q) × (0.08 + (3-q) × 0.02))
     * 结果钳制在 [MIN_EASE_FACTOR, MAX_EASE_FACTOR]。
     */
    private fun computeEaseFactor(currentEF: Double, score: Int): Double {
        val delta = 0.1 - (3 - score) * (0.08 + (3 - score) * 0.02)
        return (currentEF + delta).coerceIn(MIN_EASE_FACTOR, MAX_EASE_FACTOR)
    }

    /**
     * 根据评分决定新的复习轮次和间隔天数。
     *
     * 返回 Pair(newLevel, newIntervalDays)
     */
    private fun computeLevelAndInterval(
        currentRecord: ReviewRecordEntity,
        score: Int,
        newEaseFactor: Double
    ): Pair<Int, Int> {

        return when (score) {

            // ── BLACKOUT (0)：完全遗忘 → 重置回第0轮，当天立即复习 ──
            ReviewRating.BLACKOUT.score -> Pair(0, 0)

            // ── FUZZY (1)：模糊/耗时 → 降一级，间隔缩半 ──
            ReviewRating.FUZZY.score -> {
                val downLevel = (currentRecord.repetitionLevel - 1).coerceAtLeast(0)
                val baseInterval = YONSEI_BASE_INTERVALS[downLevel]
                val shortenedInterval = (baseInterval * 0.5).roundToInt().coerceAtLeast(1)
                Pair(downLevel, shortenedInterval)
            }

            // ── GOOD (2) / PERFECT (3)：掌握/熟练 → 升级 ──
            else -> {
                val nextLevel = (currentRecord.repetitionLevel + 1)
                    .coerceAtMost(MAX_REPETITION_LEVEL)

                // 已到达毕业轮次，不再计算间隔
                if (nextLevel == MAX_REPETITION_LEVEL) {
                    return Pair(MAX_REPETITION_LEVEL, YONSEI_BASE_INTERVALS[MAX_REPETITION_LEVEL - 1])
                }

                val newInterval = when (nextLevel) {
                    // 前两轮使用延世固定锚点，不引入SM-2动态扩展
                    1    -> YONSEI_BASE_INTERVALS[1]  // 1天
                    2    -> YONSEI_BASE_INTERVALS[2]  // 3天

                    // 第3轮起：SM-2动态扩展，下限 = 延世锚点
                    else -> {
                        val prevInterval = currentRecord.intervalDays
                            .takeIf { it > 0 } ?: YONSEI_BASE_INTERVALS[nextLevel - 1]

                        // 熟练秒杀时应用艾宾浩斯加速因子
                        val multiplier = if (score == ReviewRating.PERFECT.score) {
                            newEaseFactor * PERFECT_BOOST
                        } else {
                            newEaseFactor
                        }

                        val computed = (prevInterval * multiplier).roundToInt()
                        // 不低于延世背诵本对应轮次的基础间隔
                        computed.coerceAtLeast(YONSEI_BASE_INTERVALS[nextLevel - 1])
                    }
                }

                Pair(nextLevel, newInterval)
            }
        }
    }

    /**
     * 计算加抖动后的下次复习时间戳。
     *
     * @param nowMs        当前时间戳（毫秒）
     * @param intervalDays 间隔天数（0 = 立即）
     * @return             下次复习时间戳（毫秒）
     */
    private fun computeNextReviewTimestamp(nowMs: Long, intervalDays: Int): Long {
        if (intervalDays == 0) return nowMs

        // 添加 ±10% 随机抖动，防止复习队列扎堆
        val jitterFraction = (Math.random() * 2.0 - 1.0) * 0.10  // [-0.10, +0.10]
        val effectiveDays = intervalDays * (1.0 + jitterFraction)
        val effectiveMs = (effectiveDays * DAY_MS).toLong()

        return nowMs + effectiveMs
    }

    // ────────────────────────────────────────────────────────
    // 辅助工具函数
    // ────────────────────────────────────────────────────────

    /**
     * 计算给定记录距离"毕业"还需几轮。
     */
    fun levelsUntilGraduation(record: ReviewRecordEntity): Int =
        (MAX_REPETITION_LEVEL - record.repetitionLevel).coerceAtLeast(0)

    /**
     * 判断某条记录是否已"毕业"（长期记忆巩固完成）。
     */
    fun isGraduated(record: ReviewRecordEntity): Boolean =
        record.repetitionLevel >= MAX_REPETITION_LEVEL

    /**
     * 估算记忆保留率（基于艾宾浩斯遗忘曲线简化公式）。
     * R = e^(-t/S)，其中 S = intervalDays（稳定性），t = 已过天数。
     *
     * @param record 记忆记录
     * @param nowMs  当前时间戳
     * @return       0.0~1.0 的记忆保留率估算值
     */
    fun estimateRetentionRate(record: ReviewRecordEntity, nowMs: Long): Double {
        val lastReview = record.lastReviewTimestamp ?: return 1.0
        val elapsedDays = (nowMs - lastReview).toDouble() / DAY_MS
        val stability = record.intervalDays.toDouble().takeIf { it > 0 } ?: 1.0
        return Math.exp(-elapsedDays / stability).coerceIn(0.0, 1.0)
    }
}
