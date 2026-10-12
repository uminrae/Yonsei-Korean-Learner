package com.yonsei.koreanlearner.ui.screen.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.domain.usecase.GetTodayNewWordsUseCase
import com.yonsei.koreanlearner.domain.usecase.GetTodayNewGrammarsUseCase
import com.yonsei.koreanlearner.domain.usecase.InitializeReviewRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 今日新词学习界面 ViewModel（MVI）
 *
 * 功能：
 * 1. 按当前 book/lesson 加载未学新词和新语法
 * 2. 用户标记"已学"时调用 InitializeReviewRecordUseCase 写入初始记忆记录
 * 3. 全部新词完成后通过 Effect 引导跳转复习界面
 */
@HiltViewModel
class LearnViewModel @Inject constructor(
    private val getTodayNewWords: GetTodayNewWordsUseCase,
    private val getTodayNewGrammars: GetTodayNewGrammarsUseCase,
    private val initializeRecord: InitializeReviewRecordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LearnUiState())
    val state: StateFlow<LearnUiState> = _state.asStateFlow()

    private val _effect = Channel<LearnEffect>(Channel.BUFFERED)
    val effect: Flow<LearnEffect> = _effect.receiveAsFlow()

    // 用于取消旧订阅的 Job
    private var wordsJob: kotlinx.coroutines.Job? = null
    private var grammarsJob: kotlinx.coroutines.Job? = null

    init {
        loadLessonContent(book = 1, lesson = 1)
    }

    // ── Intent 分发 ───────────────────────────────────────────

    fun onIntent(intent: LearnIntent) {
        when (intent) {
            is LearnIntent.MarkWordLearned -> handleMarkLearned()
            is LearnIntent.SkipWord        -> handleSkipWord()
            is LearnIntent.SelectLesson    -> loadLessonContent(intent.book, intent.lesson)
            is LearnIntent.DismissError    -> _state.update { it.copy(errorMessage = null) }
        }
    }

    // ── 加载课次内容 ──────────────────────────────────────────

    private fun loadLessonContent(book: Int, lesson: Int) {
        // 取消上一课次的订阅
        wordsJob?.cancel()
        grammarsJob?.cancel()

        _state.update { it.copy(book = book, lesson = lesson, isLoading = true, wordIndex = 0) }

        // 新词 Flow 订阅
        wordsJob = viewModelScope.launch {
            try {
                getTodayNewWords(book, lesson, limit = 10)
                    .collect { words ->
                        _state.update { it.copy(newWords = words, isLoading = false) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "加载新词失败：${e.message}") }
            }
        }

        // 新语法 Flow 订阅
        grammarsJob = viewModelScope.launch {
            try {
                getTodayNewGrammars(book, lesson, limit = 4)
                    .collect { grammars ->
                        _state.update { it.copy(newGrammars = grammars) }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "加载新语法失败：${e.message}") }
            }
        }
    }

    // ── 标记已学 ──────────────────────────────────────────────

    private fun handleMarkLearned() {
        val currentState = _state.value
        val currentWord  = currentState.currentWord ?: return

        viewModelScope.launch {
            try {
                // 初始化该单词的记忆记录（写入 review_records 表）
                initializeRecord(itemId = currentWord.id, itemType = "WORD")

                // 移动到下一个
                advanceToNext()
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "记录失败：${e.message}") }
                _effect.send(LearnEffect.ShowSnackbar("记录失败，请重试"))
            }
        }
    }

    // ── 跳过当前单词 ──────────────────────────────────────────

    private fun handleSkipWord() {
        // 跳过不写入记忆记录，仅在队列中前进
        advanceToNext()
    }

    // ── 推进到下一张卡片 ──────────────────────────────────────

    private fun advanceToNext() {
        val currentState = _state.value
        val nextIndex = currentState.wordIndex + 1

        if (nextIndex >= currentState.newWords.size) {
            // 全部新词学完
            _state.update { it.copy(wordIndex = nextIndex, isLessonComplete = true) }
            viewModelScope.launch {
                _effect.send(LearnEffect.NavigateToReview)
            }
        } else {
            _state.update { it.copy(wordIndex = nextIndex) }
        }
    }
}
