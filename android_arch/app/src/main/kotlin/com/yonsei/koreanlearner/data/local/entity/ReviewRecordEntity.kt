package com.yonsei.koreanlearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 记忆追踪核心 Entity（ReviewRecordEntity）
 *
 * 功能职责：
 * 1. 追踪每个词汇/语法项目的延世背诵本 7 轮复习进度（repetitionLevel 0~7）
 * 2. 存储 SM-2 算法状态参数（easeFactor / intervalDays / nextReviewTimestamp）
 * 3. 记录学习质量指标（consecutiveSuccessCount / errorCount / isErrorItem）
 *
 * 索引设计说明：
 * - (item_type, item_id) UNIQUE：防止重复记录，且支持O(1)查找单项记录
 * - next_review_timestamp：核心到期复习队列查询的主索引
 * - is_error_item：错题集过滤查询索引
 * - repetition_level：按阶段统计/过滤查询索引
 */
@Entity(
    tableName = "review_records",
    indices = [
        Index(value = ["item_type", "item_id"], unique = true),   // 主要查找索引
        Index(value = ["next_review_timestamp"]),                  // 到期复习队列核心索引
        Index(value = ["is_error_item"]),                          // 错题集过滤
        Index(value = ["repetition_level"]),                       // 按阶段查询
        Index(value = ["last_review_timestamp"])                   // 今日学习统计
    ]
)
data class ReviewRecordEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    // ─────────────────────────────────────────────
    // 关联标识符
    // ─────────────────────────────────────────────

    /** 对应 WordEntity.id 或 GrammarEntity.id */
    @ColumnInfo(name = "item_id")
    val itemId: Int,

    /** 记录类型："WORD" | "GRAMMAR" */
    @ColumnInfo(name = "item_type")
    val itemType: String,

    // ─────────────────────────────────────────────
    // 延世背诵本 7 轮复习阶段
    // ─────────────────────────────────────────────

    /**
     * 复习阶段 0~7，对应延世背诵本 7 轮打勾进度：
     *   0 = 新词（未开始）
     *   1~6 = 复习中（第1~6轮）
     *   7 = 毕业（长期记忆巩固完成）
     */
    @ColumnInfo(name = "repetition_level")
    val repetitionLevel: Int = 0,

    // ─────────────────────────────────────────────
    // SM-2 核心算法参数
    // ─────────────────────────────────────────────

    /**
     * 难度系数（Ease Factor）
     * 默认值 2.5，范围 1.3~4.0。
     * 值越低表示越难记忆，复习间隔扩展越慢。
     */
    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double = 2.5,

    /**
     * 当前复习间隔天数。
     *   0 = 新词/当天
     *   1 = 1天后
     *   3 = 3天后
     *   依此类推，按SM-2公式指数增长
     */
    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 0,

    /**
     * 下次复习到期时间戳（毫秒）。
     * 该字段有专属索引，支持高效范围查询（WHERE next_review_timestamp <= ?）。
     */
    @ColumnInfo(name = "next_review_timestamp")
    val nextReviewTimestamp: Long,

    // ─────────────────────────────────────────────
    // 学习质量追踪指标
    // ─────────────────────────────────────────────

    /**
     * 连续答对次数。
     * ≥ 3 次连续答对（rating >= GOOD）视为稳固掌握。
     * 任意答错（rating == BLACKOUT）则重置为 0。
     */
    @ColumnInfo(name = "consecutive_success_count")
    val consecutiveSuccessCount: Int = 0,

    /**
     * 累计出错次数（rating == BLACKOUT 时 +1）。
     * 当 errorCount >= 2 时，isErrorItem 标记为 true，进入错题集。
     */
    @ColumnInfo(name = "error_count")
    val errorCount: Int = 0,

    // ─────────────────────────────────────────────
    // 错题集标记
    // ─────────────────────────────────────────────

    /**
     * 是否在错题集中。
     * 触发条件：errorCount >= 2。
     * 从错题集移出条件：consecutiveSuccessCount >= 3。
     */
    @ColumnInfo(name = "is_error_item")
    val isErrorItem: Boolean = false,

    // ─────────────────────────────────────────────
    // 时间追踪字段
    // ─────────────────────────────────────────────

    /** 首次学习时间戳（毫秒），记录后不再更新 */
    @ColumnInfo(name = "first_learn_timestamp")
    val firstLearnTimestamp: Long = System.currentTimeMillis(),

    /** 最近一次复习时间戳（毫秒），用于今日学习统计 */
    @ColumnInfo(name = "last_review_timestamp")
    val lastReviewTimestamp: Long? = null,

    /** 累计总复习次数 */
    @ColumnInfo(name = "total_review_count")
    val totalReviewCount: Int = 0,

    /**
     * 每轮打卡历史记录（以 JSON 存储 List<RoundReviewLog>），
     * 完整记录 1~7 轮每一次打卡的时间戳、掌握评级（0~3 分）与难度系数。
     */
    @ColumnInfo(name = "review_history")
    val reviewHistory: String = "[]"
)

/**
 * 单轮复习打卡日志（记录每轮打卡时间与掌握度）
 */
data class RoundReviewLog(
    val round       : Int,     // 对应轮次 (1~7)
    val timestamp   : Long,    // 打卡完成时间戳（毫秒）
    val ratingScore : Int,     // 掌握评级 0=遗忘, 1=模糊, 2=掌握, 3=秒杀
    val easeFactor  : Double   // 当次结算后的难度系数
)

