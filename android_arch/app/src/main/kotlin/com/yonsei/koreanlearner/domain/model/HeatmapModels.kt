package com.yonsei.koreanlearner.domain.model

import androidx.compose.runtime.Immutable
import com.yonsei.koreanlearner.data.local.entity.RoundReviewLog

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世 7 轮复习进度热力图领域模型
 * ══════════════════════════════════════════════════════════════════
 */

/**
 * 单个单词的热力图数据项（@Immutable 确保 Compose 120fps 稳定性）
 */
@Immutable
data class WordHeatItem(
    val wordId          : Int,
    val book            : Int,
    val lesson          : Int,
    val cumulativeIndex : Int,
    val korean          : String,
    val chinese         : String,
    val partOfSpeech    : String,
    val irregularType   : String?,
    /** 当前复习进度轮次：0=未学，1~6=复习中，7=已毕业长期巩固 */
    val repetitionLevel : Int,
    /** 热力深度等级：0~7（对应 8 级紫粉渐变色深度） */
    val heatDepth       : Int,
    /** 是否已完成全部 7 轮复习（毕业） */
    val isGraduated     : Boolean,
    /** 难度系数 (Ease Factor) */
    val easeFactor      : Double,
    /** 连续正确次数 */
    val consecutiveSuccess: Int,
    /** 是否属于错题集 */
    val isErrorItem     : Boolean,
    /** 1~7 轮打卡明细历史日志 */
    val history         : List<RoundReviewLog> = emptyList()
) {
    /** 判断指定轮次 (1~7) 是否已打卡点亮 */
    fun isRoundCompleted(round: Int): Boolean = repetitionLevel >= round

    /** 获取指定轮次的评级评分 (0~3)，若无则为 null */
    fun getRoundRating(round: Int): Int? =
        history.find { it.round == round }?.ratingScore
}

/**
 * 单个课时的热力图聚合数据
 */
@Immutable
data class LessonHeatmapItem(
    val book               : Int,
    val lesson             : Int,
    val totalWords         : Int,
    val learnedWords       : Int,
    val graduatedWords     : Int,
    val avgRepetitionLevel : Float,
    /** 该课综合热力点亮比例 (0.0f ~ 1.0f) */
    val completionRatio    : Float,
    /** 该课内所有单词的热力项列表 */
    val words              : List<WordHeatItem> = emptyList()
) {
    /** 8 级综合热力级别 (0~7) */
    val heatLevel: Int get() = (avgRepetitionLevel).toInt().coerceIn(0, 7)
}
