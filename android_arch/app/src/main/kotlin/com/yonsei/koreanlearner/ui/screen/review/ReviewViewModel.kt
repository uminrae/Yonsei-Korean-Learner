package com.yonsei.koreanlearner.ui.screen.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.domain.repository.ReviewRepository
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.domain.usecase.GetDueReviewQueueUseCase
import com.yonsei.koreanlearner.domain.usecase.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 复习界面 ViewModel（MVI 架构）
 *
 * 数据流：
 *   User Action → [onIntent] → 业务逻辑 → 更新 [_state] → Compose 重组
 *                                       ↘ 发送 [_effect] → LaunchedEffect 消费
 *
 * 队列构建策略：
 *   1. 从 ReviewRecordDao 获取今日全部到期记录（按 next_review_timestamp 排序）
 *   2. 根据 item_type 分别从 WordDao / GrammarDao 加载实体详情
 *   3. 合并为 List<ReviewQueueItem>，注入 UI State
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: ReviewRepository,
    private val getDueReviewQueueUseCase: GetDueReviewQueueUseCase,
    private val submitReviewUseCase: SubmitReviewUseCase,
    private val wordDao: com.yonsei.koreanlearner.data.local.dao.WordDao,
    private val grammarDao: com.yonsei.koreanlearner.data.local.dao.GrammarDao
) : ViewModel() {

    // ── MVI State（StateFlow：始终保持最新快照） ──────────────
    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    // ── MVI Effect（Channel：每个 Effect 只被消费一次） ───────
    private val _effect = Channel<ReviewEffect>(Channel.BUFFERED)
    val effect: Flow<ReviewEffect> = _effect.receiveAsFlow()

    // ── 词汇和语法实体缓存（避免重复查询）────────────────────
    // key = "WORD_${id}" 或 "GRAMMAR_${id}"
    private val entityCache = mutableMapOf<String, Any>()

    private val gson = Gson()

    init {
        loadDueReviewQueue()
    }

    // ── 公开入口：处理所有 Intent ─────────────────────────────

    fun onIntent(intent: ReviewIntent) {
        when (intent) {
            is ReviewIntent.FlipCard      -> handleFlipCard()
            is ReviewIntent.SubmitRating  -> handleSubmitRating(intent.rating)
            is ReviewIntent.RestartSession -> restartSession()
            is ReviewIntent.DismissError  -> _state.update { it.copy(errorMessage = null) }
        }
    }

    // ── 队列加载 ──────────────────────────────────────────────

    private fun loadDueReviewQueue() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // 获取今日全部到期复习记录（词+语法混合）
                getDueReviewQueueUseCase(limit = 200)
                    .collect { records ->
                        val items = buildQueueItems(records)
                        _state.update { current ->
                            current.copy(
                                queue          = items,
                                totalDueCount  = items.size,
                                currentIndex   = 0,
                                completedCount = 0,
                                isFlipped      = false,
                                isLoading      = false,
                                isSessionComplete = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = "加载复习队列失败：${e.message}")
                }
                _effect.send(ReviewEffect.ShowSnackbar("加载失败，请重试"))
            }
        }
    }

    /**
     * 将记忆记录列表构建为 ReviewQueueItem 列表。
     *
     * 通过 entityCache 避免对同一 item 重复查询。
     * 这里使用 suspend 函数同步从 Repository 获取实体。
     */
    private suspend fun buildQueueItems(
        records: List<ReviewRecordEntity>
    ): List<ReviewQueueItem> {
        return records.mapNotNull { record ->
            when (record.itemType) {
                "WORD" -> {
                    val word = getWordEntity(record.itemId) ?: return@mapNotNull null
                    ReviewQueueItem.WordItem(word, record)
                }
                "GRAMMAR" -> {
                    val grammar = getGrammarEntity(record.itemId) ?: return@mapNotNull null
                    ReviewQueueItem.GrammarItem(grammar, record)
                }
                else -> null
            }
        }
    }

    private suspend fun getWordEntity(id: Int): WordEntity? {
        val cacheKey = "WORD_$id"
        return (entityCache[cacheKey] as? WordEntity) ?: run {
            val entity = wordDao.getWordById(id)
            entity?.also { entityCache[cacheKey] = it }
        }
    }

    private suspend fun getGrammarEntity(id: Int): GrammarEntity? {
        val cacheKey = "GRAMMAR_$id"
        return (entityCache[cacheKey] as? GrammarEntity) ?: run {
            val entity = grammarDao.getGrammarById(id)
            entity?.also { entityCache[cacheKey] = it }
        }
    }

    // ── 翻牌逻辑 ─────────────────────────────────────────────

    private fun handleFlipCard() {
        val currentFlipped = _state.value.isFlipped
        if (currentFlipped) return  // 已翻面，等待评分，不响应

        _state.update { it.copy(isFlipped = true) }
        viewModelScope.launch {
            _effect.send(ReviewEffect.TriggerFlipAnimation)
        }
    }

    // ── 评分提交逻辑 ──────────────────────────────────────────

    private fun handleSubmitRating(rating: ReviewRating) {
        val currentState = _state.value
        val currentItem  = currentState.currentItem ?: return
        if (!currentState.isFlipped) return  // 未翻面不接受评分

        viewModelScope.launch {
            try {
                // 1. 提交评分到 MemoryScheduler + 写入数据库
                submitReviewUseCase(
                    itemId   = currentItem.record.itemId,
                    itemType = currentItem.record.itemType,
                    rating   = rating
                )

                // 2. 发送卡片滑出动画 Effect
                _effect.send(ReviewEffect.ShowNextCard(rating))

                // 3. 更新 State：移动到下一张
                val nextIndex = currentState.currentIndex + 1
                val isComplete = nextIndex >= currentState.queue.size

                _state.update { it.copy(
                    currentIndex   = nextIndex,
                    isFlipped      = false,
                    completedCount = it.completedCount + 1,
                    isSessionComplete = isComplete
                )}

                if (isComplete) {
                    _effect.send(ReviewEffect.NavigateToCompletion)
                }

            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "评分提交失败：${e.message}") }
                _effect.send(ReviewEffect.ShowSnackbar("提交失败，请重试"))
            }
        }
    }

    // ── 重新开始 ──────────────────────────────────────────────

    private fun restartSession() {
        _state.update { ReviewUiState() }
        loadDueReviewQueue()
    }
}
