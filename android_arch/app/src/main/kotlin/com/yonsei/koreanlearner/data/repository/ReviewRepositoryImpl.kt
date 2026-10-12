package com.yonsei.koreanlearner.data.repository

import com.yonsei.koreanlearner.algorithm.MemoryScheduler
import com.yonsei.koreanlearner.data.local.dao.GrammarDao
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.domain.repository.ReviewRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ReviewRepository] 的具体实现（数据层）。
 *
 * 通过 Hilt 注入三个 DAO 和 IO 调度器，
 * 所有 Flow 操作均切换到 [ioDispatcher]（Dispatchers.IO），
 * 所有 suspend 写操作通过 [withContext] 切换线程。
 */
@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
    private val grammarDao: GrammarDao,
    private val reviewRecordDao: ReviewRecordDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ReviewRepository {

    // ── 今日待学新内容 ────────────────────────────────────────

    override fun getTodayNewWords(book: Int, lesson: Int, limit: Int): Flow<List<WordEntity>> =
        wordDao.getNewWordsByBookAndLesson(book, lesson, limit)
            .flowOn(ioDispatcher)

    override fun getTodayNewGrammars(book: Int, lesson: Int, limit: Int): Flow<List<GrammarEntity>> =
        grammarDao.getNewGrammarsByBookAndLesson(book, lesson, limit)
            .flowOn(ioDispatcher)

    // ── 今日到期复习队列 ──────────────────────────────────────

    override fun getTodayDueReviewQueue(limit: Int): Flow<List<ReviewRecordEntity>> =
        reviewRecordDao.getDueReviewQueue(
            nowMs = System.currentTimeMillis(),
            limit = limit
        ).flowOn(ioDispatcher)

    override fun getDueWordsForReview(): Flow<List<WordEntity>> =
        wordDao.getDueWordsForReview(System.currentTimeMillis())
            .map { list -> list.map { it.word } }
            .flowOn(ioDispatcher)

    override fun getDueGrammarsForReview(): Flow<List<GrammarEntity>> =
        grammarDao.getDueGrammarsForReview(System.currentTimeMillis())
            .flowOn(ioDispatcher)

    // ── 错题集 ────────────────────────────────────────────────

    override fun getErrorWords(): Flow<List<WordEntity>> =
        wordDao.getErrorWords().flowOn(ioDispatcher)

    override fun getErrorGrammars(): Flow<List<GrammarEntity>> =
        grammarDao.getErrorGrammars().flowOn(ioDispatcher)

    // ── 写操作 ────────────────────────────────────────────────

    /**
     * 提交复习评分并更新记忆排程。
     *
     * 线程安全：切换到 IO 线程执行数据库读写。
     * 原子性：读-计算-写在同一协程上下文中串行执行。
     */
    override suspend fun submitReview(
        itemId: Int,
        itemType: String,
        rating: ReviewRating
    ) {
        withContext(ioDispatcher) {
            val existing = reviewRecordDao.getRecord(itemId, itemType)
                ?: MemoryScheduler.createInitialRecord(itemId, itemType)

            val updated = MemoryScheduler.calculateNextReview(existing, rating)
            reviewRecordDao.upsert(updated)
        }
    }

    /**
     * 首次学习某个词/语法时初始化其记忆记录（幂等）。
     * 若记录已存在则跳过，不执行任何写入。
     */
    override suspend fun initializeRecord(
        itemId: Int,
        itemType: String
    ) = withContext(ioDispatcher) {
        val existing = reviewRecordDao.getRecord(itemId, itemType)
        if (existing == null) {
            reviewRecordDao.upsert(
                MemoryScheduler.createInitialRecord(itemId, itemType)
            )
        }
    }

    // ── 统计数据 ──────────────────────────────────────────────

    override fun getDueReviewCount(): Flow<Int> =
        reviewRecordDao.getDueReviewCount(System.currentTimeMillis())
            .flowOn(ioDispatcher)

    override fun getTodayReviewedCount(): Flow<Int> {
        val (start, end) = todayRange()
        return reviewRecordDao.getTodayReviewedCount(start, end)
            .flowOn(ioDispatcher)
    }

    override fun getErrorItemCount(): Flow<Int> =
        reviewRecordDao.getErrorItemCount().flowOn(ioDispatcher)

    override fun getBookProgress(book: Int): Flow<Pair<Int, Int>> =
        combine(
            wordDao.getGraduatedWordCount(book),
            wordDao.getTotalWordCount(book)
        ) { graduated, total -> Pair(graduated, total) }
            .flowOn(ioDispatcher)

    // ── 私有工具函数 ──────────────────────────────────────────

    /** 计算今日零点到23:59:59的时间戳范围（毫秒） */
    private fun todayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis

        return Pair(start, end)
    }
}

/** Hilt 限定符：标记 IO 调度器 */
@javax.inject.Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
