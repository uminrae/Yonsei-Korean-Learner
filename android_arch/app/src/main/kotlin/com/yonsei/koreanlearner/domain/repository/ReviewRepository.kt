package com.yonsei.koreanlearner.domain.repository

import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating
import kotlinx.coroutines.flow.Flow

/**
 * 复习记录与学习队列 Repository 接口（领域层）。
 *
 * 实现类：[com.yonsei.koreanlearner.data.repository.ReviewRepositoryImpl]
 *
 * 采用 Flow 返回，使 ViewModel 可响应式地观察数据库变更，
 * 无需手动轮询或刷新。
 */
interface ReviewRepository {

    // ── 今日待学新内容 ────────────────────────────────────────

    /**
     * 获取今日待学新词（指定册数+课次，无历史记录）。
     * @param book   册数 1~6
     * @param lesson 课次 1~10
     * @param limit  每次最多返回条数（推荐 10）
     */
    fun getTodayNewWords(book: Int, lesson: Int, limit: Int = 10): Flow<List<WordEntity>>

    /**
     * 获取今日待学新语法（指定册数+课次，无历史记录）。
     * @param book   册数 1~4
     * @param lesson 课次 1~10
     * @param limit  每次最多返回条数（推荐 4）
     */
    fun getTodayNewGrammars(book: Int, lesson: Int, limit: Int = 4): Flow<List<GrammarEntity>>

    // ── 今日到期复习队列 ──────────────────────────────────────

    /**
     * 获取今日全部到期复习记录（词+语法混合）。
     * 结果按到期时间升序排列（最紧急的优先）。
     */
    fun getTodayDueReviewQueue(limit: Int = 200): Flow<List<ReviewRecordEntity>>

    /**
     * 获取今日到期复习单词列表（附带完整记忆记录）。
     */
    fun getDueWordsForReview(): Flow<List<WordEntity>>

    /**
     * 获取今日到期复习语法列表。
     */
    fun getDueGrammarsForReview(): Flow<List<GrammarEntity>>

    // ── 错题集 ────────────────────────────────────────────────

    /** 获取错题集单词列表 */
    fun getErrorWords(): Flow<List<WordEntity>>

    /** 获取错题集语法列表 */
    fun getErrorGrammars(): Flow<List<GrammarEntity>>

    // ── 写操作 ────────────────────────────────────────────────

    /**
     * 提交一次复习评分，触发记忆算法更新下次复习时间。
     *
     * 内部流程：
     * 1. 从数据库读取当前记录（或创建初始记录）
     * 2. 调用 MemoryScheduler.calculateNextReview()
     * 3. 将更新后的记录写回数据库
     *
     * @param itemId   单词或语法的 Entity ID
     * @param itemType "WORD" 或 "GRAMMAR"
     * @param rating   用户评分 [ReviewRating]
     */
    suspend fun submitReview(itemId: Int, itemType: String, rating: ReviewRating)

    /**
     * 为新词/新语法创建初始记忆记录（首次学习时调用）。
     * 若记录已存在则跳过（幂等操作）。
     */
    suspend fun initializeRecord(itemId: Int, itemType: String)

    // ── 统计数据 ──────────────────────────────────────────────

    /** 今日到期复习总数（用于首页 Badge） */
    fun getDueReviewCount(): Flow<Int>

    /** 今日已完成复习数 */
    fun getTodayReviewedCount(): Flow<Int>

    /** 错题集总数 */
    fun getErrorItemCount(): Flow<Int>

    /**
     * 指定册数学习进度（已毕业词数 / 总词数）。
     * 返回 Pair(graduated, total)。
     */
    fun getBookProgress(book: Int): Flow<Pair<Int, Int>>
}
