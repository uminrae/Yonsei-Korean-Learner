package com.yonsei.koreanlearner.domain.usecase

import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// ════════════════════════════════════════════════════════════════
// 1. 今日新词 UseCase
// ════════════════════════════════════════════════════════════════

/**
 * 获取今日待学新词（按册数+课次节拍解锁）。
 *
 * 调用方：LearnViewModel。
 * 每次调用返回指定册数/课次中尚未初始化记忆记录的词汇。
 */
class GetTodayNewWordsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    operator fun invoke(book: Int, lesson: Int, limit: Int = 10): Flow<List<WordEntity>> =
        repository.getTodayNewWords(book, lesson, limit)
}

// ════════════════════════════════════════════════════════════════
// 2. 今日新语法 UseCase
// ════════════════════════════════════════════════════════════════

/**
 * 获取今日待学新语法（按册数+课次节拍解锁）。
 *
 * 调用方：LearnViewModel。
 */
class GetTodayNewGrammarsUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    operator fun invoke(book: Int, lesson: Int, limit: Int = 4): Flow<List<GrammarEntity>> =
        repository.getTodayNewGrammars(book, lesson, limit)
}

// ════════════════════════════════════════════════════════════════
// 3. 今日到期复习队列 UseCase
// ════════════════════════════════════════════════════════════════

/**
 * 获取今日全部到期复习记录（词+语法混合），按到期时间升序排列。
 *
 * 调用方：ReviewViewModel（主复习流程驱动器）。
 *
 * 典型使用场景：
 * ```kotlin
 * val dueQueue by getDueReviewQueueUseCase().collectAsState(initial = emptyList())
 * ```
 */
class GetDueReviewQueueUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    operator fun invoke(limit: Int = 200): Flow<List<ReviewRecordEntity>> =
        repository.getTodayDueReviewQueue(limit)
}

// ════════════════════════════════════════════════════════════════
// 4. 提交复习评分 UseCase
// ════════════════════════════════════════════════════════════════

/**
 * 用户翻卡评分后提交复习结果，触发 SM-2 记忆算法更新。
 *
 * 调用方：ReviewViewModel（用户点击评分按钮时）。
 *
 * 内部流程：
 * 1. 读取当前 ReviewRecordEntity（或创建初始记录）
 * 2. 调用 MemoryScheduler.calculateNextReview()
 * 3. 持久化更新后的记录到 Room
 *
 * 为 suspend 函数，需在协程作用域内调用：
 * ```kotlin
 * viewModelScope.launch { submitReviewUseCase(itemId, "WORD", ReviewRating.GOOD) }
 * ```
 */
class SubmitReviewUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(
        itemId: Int,
        itemType: String,
        rating: ReviewRating
    ) = repository.submitReview(itemId, itemType, rating)
}

// ════════════════════════════════════════════════════════════════
// 5. 初始化记忆记录 UseCase
// ════════════════════════════════════════════════════════════════

/**
 * 首次学习某个词/语法时创建初始 ReviewRecord（幂等）。
 *
 * 调用时机：用户在新词学习卡片上滑动"已学会"或完成首次浏览时。
 * 若该 item 已有记录则跳过，不产生重复写入。
 */
class InitializeReviewRecordUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(itemId: Int, itemType: String) =
        repository.initializeRecord(itemId, itemType)
}
