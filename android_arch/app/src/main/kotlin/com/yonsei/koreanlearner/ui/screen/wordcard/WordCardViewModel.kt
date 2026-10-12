package com.yonsei.koreanlearner.ui.screen.wordcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.algorithm.MemoryScheduler
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.core.state.AppStateManager
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.domain.repository.ReviewRepository
import com.yonsei.koreanlearner.domain.usecase.GetTodayNewWordsUseCase
import com.yonsei.koreanlearner.domain.usecase.InitializeReviewRecordUseCase
import com.yonsei.koreanlearner.domain.usecase.SubmitReviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WordCardScreen ViewModel（MVI 架构）
 */
@HiltViewModel
class WordCardViewModel @Inject constructor(
    private val wordDao                  : WordDao,
    private val reviewRecordDao          : ReviewRecordDao,
    private val getTodayNewWordsUseCase  : GetTodayNewWordsUseCase,
    private val submitReviewUseCase      : SubmitReviewUseCase,
    private val initializeRecordUseCase  : InitializeReviewRecordUseCase,
    private val reviewRepository         : ReviewRepository,
    val ttsManager                       : TtsManager,
    private val appStateManager          : AppStateManager
) : ViewModel() {

    // ── MVI 核心流 ────────────────────────────────────────────────
    private val _state  = MutableStateFlow(
        WordCardUiState(
            currentBook = appStateManager.getLastWordBook(),
            currentLesson = appStateManager.getLastWordLesson()
        )
    )
    val state: StateFlow<WordCardUiState> = _state.asStateFlow()

    // 细粒度音频播放状态（单独暴露，避免播放/停止导致卡片整体 Recomposition）
    val ttsState = ttsManager.playbackState

    private val _effect = Channel<WordCardEffect>(Channel.BUFFERED)
    val effect: Flow<WordCardEffect> = _effect.receiveAsFlow()

    // 当前加载课次的 Job（切换课次时取消旧订阅）
    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadLesson(
            book = appStateManager.getLastWordBook(),
            lesson = appStateManager.getLastWordLesson()
        )
    }

    // ════════════════════════════════════════════════════════════
    // Intent 分发
    // ════════════════════════════════════════════════════════════

    fun onIntent(intent: WordCardIntent) {
        when (intent) {
            is WordCardIntent.SelectLesson      -> loadLesson(intent.book, intent.lesson)
            is WordCardIntent.FlipCard          -> handleFlipCard()
            is WordCardIntent.SubmitRating      -> handleSubmitRating(intent.rating)
            is WordCardIntent.PlayPronunciation -> handlePlayPronunciation()
            is WordCardIntent.SkipCard          -> handleSkipCard()
            is WordCardIntent.RestartLesson     -> restartLesson()
            is WordCardIntent.GoToNextLesson    -> goToNextLesson()
            is WordCardIntent.DismissError      -> _state.update { it.copy(errorMessage = null) }
        }
    }

    // ════════════════════════════════════════════════════════════
    // 课次加载
    // ════════════════════════════════════════════════════════════

    private fun loadLesson(book: Int, lesson: Int) {
        appStateManager.setLastWordBook(book)
        appStateManager.setLastWordLesson(lesson)
        loadJob?.cancel()
        _state.update {
            it.copy(
                currentBook   = book,
                currentLesson = lesson,
                isLoading     = true,
                currentIndex  = 0,
                isCardFlipped = false,
                errorMessage  = null,
                sessionCorrect = 0,
                sessionTotal   = 0
            )
        }

        loadJob = viewModelScope.launch {
            try {
                // 同时启动新词流 + 课时进度统计
                launch { observeNewWords(book, lesson) }
                launch { observeLessonProgress(book, lesson) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "加载失败：${e.message}") }
            }
        }
    }

    /**
     * 响应式监听新词队列（Room Flow → 自动感知数据库变化）。
     * 当某词被 initializeRecord 写入 review_records 后，
     * LEFT JOIN 的 getNewWordsByBookAndLesson 查询结果自动排除该词。
     */
    private suspend fun observeNewWords(book: Int, lesson: Int) {
        wordDao.getWordsByBookAndLesson(book, lesson)
            .catch { e ->
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
            .collect { entities ->
                val cards = entities.map { it.toWordCard() }
                val savedIndex = appStateManager.getLastWordIndex().coerceIn(0, if (cards.isEmpty()) 0 else cards.size - 1)
                _state.update { current ->
                    current.copy(
                        newWordQueue = cards,
                        currentIndex = if (current.currentIndex == 0 && savedIndex > 0) savedIndex else current.currentIndex.coerceAtMost(if (cards.isEmpty()) 0 else cards.size - 1),
                        isLoading    = false
                    )
                }
            }
    }

    private suspend fun observeLessonProgress(book: Int, lesson: Int) {
        combine(
            wordDao.getLearnedWordCount(book),
            wordDao.getTotalWordCount(book)
        ) { learned, total ->
            LessonProgress(
                book          = book,
                lesson        = lesson,
                totalWords    = total,
                learnedWords  = learned,
                newWordsToday = _state.value.newWordQueue.size
            )
        }.collect { progress ->
            _state.update { it.copy(lessonProgress = progress) }
        }
    }

    // ════════════════════════════════════════════════════════════
    // 翻卡
    // ════════════════════════════════════════════════════════════

    private fun handleFlipCard() {
        val nextFlipped = !_state.value.isCardFlipped
        _state.update { it.copy(isCardFlipped = nextFlipped) }
        viewModelScope.launch {
            _effect.send(WordCardEffect.TriggerCardFlip)
        }
    }

    // ════════════════════════════════════════════════════════════
    // 评分提交（核心业务）
    // ════════════════════════════════════════════════════════════

    private fun handleSubmitRating(rating: ReviewRating) {
        val state = _state.value
        if (!state.isCardFlipped || state.isSubmitting) return
        val card = state.currentCard ?: return

        _state.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                // ── Step 1: 初始化或获取已有记忆记录 ─────────────
                initializeRecordUseCase(card.wordId, "WORD")

                // ── Step 2: 提交评分，触发 SM-2 计算 ─────────────
                submitReviewUseCase(card.wordId, "WORD", rating)

                // ── Step 3: 触感反馈 ──────────────────────────────
                val isSuccess = rating.score >= ReviewRating.GOOD.score
                _effect.send(WordCardEffect.PlayHaptic(isSuccess))

                // ── Step 4: 卡片滑出动画 ──────────────────────────
                _effect.send(WordCardEffect.AnimateCardOut(toRight = isSuccess))

                // ── Step 5: 更新 Session 统计 ─────────────────────
                val newCorrect = state.sessionCorrect + if (isSuccess) 1 else 0
                val newTotal   = state.sessionTotal + 1

                // ── Step 6: 前进到下一张（Room Flow 自动更新队列） ─
                val nextIndex  = state.currentIndex + 1
                appStateManager.setLastWordIndex(nextIndex)
                val isDone     = nextIndex >= state.newWordQueue.size

                _state.update {
                    it.copy(
                        currentIndex   = nextIndex,
                        isCardFlipped  = false,
                        isSubmitting   = false,
                        sessionCorrect = newCorrect,
                        sessionTotal   = newTotal
                    )
                }

                // ── Step 7: 课时完成检查 ──────────────────────────
                if (isDone) {
                    _effect.send(WordCardEffect.ShowLessonComplete)
                }

            } catch (e: Exception) {
                _state.update {
                    it.copy(isSubmitting = false, errorMessage = "提交评分失败：${e.message}")
                }
                _effect.send(WordCardEffect.ShowSnackbar("提交失败，请重试"))
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // 发音播放
    // ════════════════════════════════════════════════════════════

    private fun handlePlayPronunciation() {
        val korean = _state.value.currentCard?.korean ?: return
        ttsManager.speak(korean)
    }

    // ════════════════════════════════════════════════════════════
    // 跳过 / 重启 / 下一课
    // ════════════════════════════════════════════════════════════

    private fun handleSkipCard() {
        val state = _state.value
        val nextIndex = state.currentIndex + 1
        _state.update { it.copy(currentIndex = nextIndex, isCardFlipped = false) }
        viewModelScope.launch {
            _effect.send(WordCardEffect.AnimateCardOut(toRight = false))
            if (nextIndex >= state.newWordQueue.size) {
                _effect.send(WordCardEffect.ShowLessonComplete)
            }
        }
    }

    private fun restartLesson() {
        loadLesson(_state.value.currentBook, _state.value.currentLesson)
    }

    private fun goToNextLesson() {
        val current = _state.value.currentLesson
        val next = if (current >= 30) 1 else current + 1
        loadLesson(_state.value.currentBook, next)
    }

    private fun WordEntity.toWordCard() = WordCard(
        wordId          = id,
        book            = book,
        lesson          = lesson,
        cumulativeIndex = cumulativeIndex,
        korean          = korean,
        chinese         = chinese,
        partOfSpeech    = partOfSpeech,
        level           = level,
        irregularType   = irregularType
    )
}
