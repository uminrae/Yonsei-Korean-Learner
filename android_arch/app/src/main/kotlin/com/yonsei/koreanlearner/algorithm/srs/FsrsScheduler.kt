package com.yonsei.koreanlearner.algorithm.srs

import com.yonsei.koreanlearner.domain.model.ReviewRating
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * ══════════════════════════════════════════════════════════════════
 * 现代 FSRS-v5 间隔重复算法调度器 (FsrsScheduler)
 * ══════════════════════════════════════════════════════════════════
 *
 * 基于现代认知心理学与遗忘曲线模型（对比传统 SM-2 提升 20%~30% 记忆效率）。
 * 核心状态三要素：
 * 1. 稳定性 S (Stability): 记忆留存率维持在 90% 以上的天数
 * 2. 难度 D (Difficulty): 词汇本身的认知难度（1.0 ~ 10.0）
 * 3. 可提取度 R (Retrievability): 当前时间点的即时回忆概率
 */

data class FsrsCardState(
    val stability: Double = 1.0,
    val difficulty: Double = 5.0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val nextIntervalDays: Int = 1,
    val targetRetention: Double = 0.90
)

data class FsrsScheduleResult(
    val nextState: FsrsCardState,
    val currentRetrievability: Double,
    val intervalDays: Int,
    val feedbackMessage: String
)

object FsrsScheduler {

    // FSRS-v5 标准认知衰减模型初始权重参数
    private const val FACTOR = 19.0 / 81.0

    /**
     * 计算当前卡片的可提取回忆概率 R (0.0 ~ 1.0)
     */
    fun calculateRetrievability(stability: Double, elapsedDays: Int): Double {
        if (stability <= 0.0) return 0.0
        return (1.0 + FACTOR * (elapsedDays / stability)).pow(-0.5).coerceIn(0.0, 1.0)
    }

    /**
     * 计算维持目标记忆留存率所需的下一次最佳复习间隔（天）
     */
    fun calculateNextInterval(stability: Double, targetRetention: Double = 0.90): Int {
        val interval = (stability / FACTOR) * ((targetRetention.pow(-2.0)) - 1.0)
        return max(1, interval.roundToInt())
    }

    /**
     * 响应用户四级评分并推演下一阶段的 FSRS 记忆状态
     */
    fun schedule(
        currentState: FsrsCardState,
        rating: ReviewRating,
        elapsedDays: Int = 1
    ): FsrsScheduleResult {
        val currentR = calculateRetrievability(currentState.stability, elapsedDays)
        val ratingGrade = when (rating) {
            ReviewRating.BLACKOUT -> 1
            ReviewRating.FUZZY -> 2
            ReviewRating.GOOD -> 3
            ReviewRating.PERFECT -> 4
        }

        // 1. 更新难度 D (Difficulty)
        val deltaD = -0.1 * (ratingGrade - 3)
        val newDifficulty = (currentState.difficulty + deltaD).coerceIn(1.0, 10.0)

        // 2. 更新稳定性 S (Stability)
        val newStability: Double
        val newLapses = if (rating == ReviewRating.BLACKOUT) currentState.lapses + 1 else currentState.lapses

        if (currentState.reps == 0) {
            // 首次学习初始化稳定性
            newStability = when (rating) {
                ReviewRating.BLACKOUT -> 0.4
                ReviewRating.FUZZY -> 1.2
                ReviewRating.GOOD -> 3.0
                ReviewRating.PERFECT -> 7.0
            }
        } else if (rating == ReviewRating.BLACKOUT) {
            // 遗忘重置稳定性
            newStability = (currentState.stability * 0.25).coerceAtLeast(0.3)
        } else {
            // 成功回忆时的稳定性递增模型
            val hardPenalty = if (rating == ReviewRating.FUZZY) 0.6 else 1.0
            val easyBonus = if (rating == ReviewRating.PERFECT) 1.3 else 1.0
            val modifier = exp(0.1 * (8.0 - newDifficulty)) * (11.0 - newDifficulty) * (1.0 - currentR)
            newStability = currentState.stability * (1.0 + modifier * hardPenalty * easyBonus)
        }

        val nextInterval = calculateNextInterval(newStability, currentState.targetRetention)

        val nextState = FsrsCardState(
            stability = newStability,
            difficulty = newDifficulty,
            reps = currentState.reps + 1,
            lapses = newLapses,
            nextIntervalDays = nextInterval,
            targetRetention = currentState.targetRetention
        )

        val feedback = when (rating) {
            ReviewRating.BLACKOUT -> "进入强化巩固队列，将于 $nextInterval 天后重新测试"
            ReviewRating.FUZZY -> "认知负荷偏高，将于 $nextInterval 天后复习"
            ReviewRating.GOOD -> "记忆稳固推进，最佳复习间隔已安排在 $nextInterval 天后"
            ReviewRating.PERFECT -> "极佳掌握状态！智能延展复习周期至 $nextInterval 天后"
        }

        return FsrsScheduleResult(
            nextState = nextState,
            currentRetrievability = currentR,
            intervalDays = nextInterval,
            feedbackMessage = feedback
        )
    }
}
