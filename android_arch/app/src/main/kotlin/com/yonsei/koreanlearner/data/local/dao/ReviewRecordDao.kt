package com.yonsei.koreanlearner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import kotlinx.coroutines.flow.Flow

/** 记忆追踪核心记录 ReviewRecordEntity 的 DAO 接口 */
@Dao
interface ReviewRecordDao {

    // ──────────────────────────────────────────────────────────
    // 写操作
    // ──────────────────────────────────────────────────────────

    /**
     * 插入或更新一条记忆记录（Upsert）。
     *
     * 使用 REPLACE 策略：若 (item_type, item_id) 已存在则整行替换，
     * 等效于 Upsert（依赖数据库唯一索引约束触发 REPLACE）。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: ReviewRecordEntity): Long

    /** 更新已存在的记忆记录（通过主键 id 定位） */
    @Update
    suspend fun update(record: ReviewRecordEntity)

    // ──────────────────────────────────────────────────────────
    // 到期复习队列查询（核心高频查询，有专属索引支持）
    // ──────────────────────────────────────────────────────────

    /**
     * 获取**全部类型**（WORD + GRAMMAR）今日到期复习记录。
     *
     * 核心查询：利用 next_review_timestamp 列索引进行范围扫描。
     * 时间复杂度接近 O(log N + K)，K = 到期记录数。
     *
     * @param nowMs 当前时间戳（毫秒），默认 System.currentTimeMillis()
     * @param limit 单次最大返回条数，防止 UI 瞬间加载过多
     */
    @Query("""
        SELECT * FROM review_records
        WHERE next_review_timestamp <= :nowMs
          AND repetition_level < 7
        ORDER BY next_review_timestamp ASC
        LIMIT :limit
    """)
    fun getDueReviewQueue(
        nowMs: Long,
        limit: Int = 200
    ): Flow<List<ReviewRecordEntity>>

    /**
     * 按类型获取到期复习队列（仅 WORD 或仅 GRAMMAR）。
     * 供"单词专练"或"语法专练"模式使用。
     *
     * @param type "WORD" 或 "GRAMMAR"
     * @param nowMs 当前时间戳（毫秒）
     */
    @Query("""
        SELECT * FROM review_records
        WHERE item_type = :type
          AND next_review_timestamp <= :nowMs
          AND repetition_level < 7
        ORDER BY next_review_timestamp ASC
    """)
    fun getDueReviewQueueByType(
        type: String,
        nowMs: Long
    ): Flow<List<ReviewRecordEntity>>

    // ──────────────────────────────────────────────────────────
    // 单条记录查询
    // ──────────────────────────────────────────────────────────

    /**
     * 查询单个词汇/语法的记忆记录。
     * 利用 (item_type, item_id) 唯一索引，O(1) 查找。
     *
     * @return 存在时返回记录，首次学习前返回 null
     */
    @Query("""
        SELECT * FROM review_records
        WHERE item_id   = :itemId
          AND item_type = :type
        LIMIT 1
    """)
    suspend fun getRecord(itemId: Int, type: String): ReviewRecordEntity?

    /**
     * 以 Flow 形式持续监听单条记录变化。
     * 供"单词详情"界面实时刷新使用。
     */
    @Query("""
        SELECT * FROM review_records
        WHERE item_id   = :itemId
          AND item_type = :type
        LIMIT 1
    """)
    fun observeRecord(itemId: Int, type: String): Flow<ReviewRecordEntity?>

    // ──────────────────────────────────────────────────────────
    // 统计查询（Badge / 今日进度）
    // ──────────────────────────────────────────────────────────

    /**
     * 今日到期复习总数（用于首页 Badge 数字显示）。
     * 利用索引极速返回，不加载完整记录数据。
     */
    @Query("""
        SELECT COUNT(*) FROM review_records
        WHERE next_review_timestamp <= :nowMs
          AND repetition_level < 7
    """)
    fun getDueReviewCount(nowMs: Long): Flow<Int>

    /**
     * 今日实际已复习条数（用于首页学习统计）。
     *
     * @param todayStartMs 今日零点时间戳（毫秒）
     * @param todayEndMs   今日23:59:59时间戳（毫秒）
     */
    @Query("""
        SELECT COUNT(*) FROM review_records
        WHERE last_review_timestamp >= :todayStartMs
          AND last_review_timestamp <= :todayEndMs
    """)
    fun getTodayReviewedCount(todayStartMs: Long, todayEndMs: Long): Flow<Int>

    // ──────────────────────────────────────────────────────────
    // 错题集
    // ──────────────────────────────────────────────────────────

    /** 获取所有错题集记录（is_error_item = 1），按出错次数降序 */
    @Query("""
        SELECT * FROM review_records
        WHERE is_error_item = 1
        ORDER BY error_count DESC, last_review_timestamp DESC
    """)
    fun getErrorItems(): Flow<List<ReviewRecordEntity>>

    /** 错题集总数（用于首页"错题集"入口 Badge） */
    @Query("SELECT COUNT(*) FROM review_records WHERE is_error_item = 1")
    fun getErrorItemCount(): Flow<Int>

    // ──────────────────────────────────────────────────────────
    // 学习进度 / 分布统计
    // ──────────────────────────────────────────────────────────

    /**
     * 按复习轮次分布统计（用于"学习进度"雷达图/环形图）。
     *
     * 返回格式：List<LevelCount>，其中 level = repetition_level (0~7)，
     * count = 该轮次的记录数。
     */
    @Query("""
        SELECT repetition_level AS level, COUNT(*) AS count
        FROM review_records
        WHERE item_type = :type
        GROUP BY repetition_level
        ORDER BY repetition_level ASC
    """)
    fun getRepetitionLevelDistribution(type: String): Flow<List<LevelCount>>

    /**
     * 按课时聚合统计复习热力数据（针对指定册数）。
     */
    @Query("""
        SELECT w.lesson AS lesson,
               COUNT(w.id) AS totalWords,
               COUNT(CASE WHEN rr.repetition_level >= 1 THEN 1 END) AS learnedWords,
               COUNT(CASE WHEN rr.repetition_level >= 7 THEN 1 END) AS graduatedWords,
               COALESCE(AVG(CASE WHEN rr.repetition_level IS NOT NULL THEN rr.repetition_level ELSE 0.0 END), 0.0) AS avgRepetitionLevel
        FROM words w
        LEFT JOIN review_records rr ON rr.item_id = w.id AND rr.item_type = 'WORD'
        WHERE w.book = :book
        GROUP BY w.lesson
        ORDER BY w.lesson ASC
    """)
    fun getLessonHeatmapAggregates(book: Int): Flow<List<LessonHeatmapAggregate>>
}

/** 轮次分布统计结果数据类 */
data class LevelCount(
    val level: Int,
    val count: Int
)

/** 课时复习热力聚合统计数据类 */
data class LessonHeatmapAggregate(
    val lesson             : Int,
    val totalWords         : Int,
    val learnedWords       : Int,
    val graduatedWords     : Int,
    val avgRepetitionLevel : Double
)
