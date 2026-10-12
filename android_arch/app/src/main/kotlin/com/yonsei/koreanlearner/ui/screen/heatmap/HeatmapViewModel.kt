package com.yonsei.koreanlearner.ui.screen.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.local.dao.WordWithRecord
import com.yonsei.koreanlearner.data.local.entity.RoundReviewLog
import com.yonsei.koreanlearner.domain.model.LessonHeatmapItem
import com.yonsei.koreanlearner.domain.model.WordHeatItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世 7 轮复习进度热力图 ViewModel（Clean Architecture + MVI）
 * ══════════════════════════════════════════════════════════════════
 */
@HiltViewModel
class HeatmapViewModel @Inject constructor(
    private val wordDao         : WordDao,
    private val reviewRecordDao : ReviewRecordDao
) : ViewModel() {

    private val gson = Gson()
    private val logListType = object : TypeToken<List<RoundReviewLog>>() {}.type

    private val _state = MutableStateFlow(HeatmapUiState())
    val state: StateFlow<HeatmapUiState> = _state.asStateFlow()

    private val _effect = Channel<HeatmapEffect>(Channel.BUFFERED)
    val effect: Flow<HeatmapEffect> = _effect.receiveAsFlow()

    private var observeJob: Job? = null

    init {
        loadBookHeatmap(book = 1, lesson = 1)
    }

    fun onIntent(intent: HeatmapIntent) {
        when (intent) {
            is HeatmapIntent.SelectBook       -> loadBookHeatmap(book = intent.book, lesson = 1)
            is HeatmapIntent.SelectLesson     -> selectLesson(intent.lesson)
            is HeatmapIntent.ClickWordSquare  -> _state.update { it.copy(selectedWordDetail = intent.word) }
            is HeatmapIntent.DismissWordDetail -> _state.update { it.copy(selectedWordDetail = null) }
        }
    }

    private fun selectLesson(lesson: Int) {
        val currentStats = _state.value.lessonStats
        val targetLessonWords = currentStats.find { it.lesson == lesson }?.words ?: emptyList()
        _state.update {
            it.copy(
                selectedLesson     = lesson,
                currentLessonWords = targetLessonWords
            )
        }
    }

    private fun loadBookHeatmap(book: Int, lesson: Int) {
        observeJob?.cancel()
        _state.update { it.copy(selectedBook = book, selectedLesson = lesson, isLoading = true) }

        observeJob = viewModelScope.launch {
            // 响应式监听指定册数的所有单词与记忆记录
            wordDao.getWordsWithRecordsByBook(book)
                .catch { e ->
                    _state.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { list ->
                    val allWordHeatItems = list.map { it.toWordHeatItem() }

                    // 按课时 (1~10) 分组聚合
                    val groupedByLesson = allWordHeatItems.groupBy { it.lesson }
                    val lessonStats = (1..10).map { lNum ->
                        val wordsInLesson = groupedByLesson[lNum] ?: emptyList()
                        val totalWords    = wordsInLesson.size
                        val learnedWords  = wordsInLesson.count { it.repetitionLevel >= 1 }
                        val graduated     = wordsInLesson.count { it.repetitionLevel >= 7 }
                        val avgLevel      = if (totalWords == 0) 0f else wordsInLesson.map { it.repetitionLevel }.average().toFloat()
                        val ratio         = if (totalWords == 0) 0f else (graduated.toFloat() / totalWords.toFloat())

                        LessonHeatmapItem(
                            book               = book,
                            lesson             = lNum,
                            totalWords         = totalWords,
                            learnedWords       = learnedWords,
                            graduatedWords     = graduated,
                            avgRepetitionLevel = avgLevel,
                            completionRatio    = ratio,
                            words              = wordsInLesson
                        )
                    }

                    val totalWordsInBook     = allWordHeatItems.size
                    val graduatedWordsInBook = allWordHeatItems.count { it.isGraduated }
                    val currentLessonWords   = lessonStats.find { it.lesson == _state.value.selectedLesson }?.words
                        ?: lessonStats.firstOrNull()?.words ?: emptyList()

                    val overallHeat = if (totalWordsInBook == 0) 0f
                    else allWordHeatItems.map { it.repetitionLevel }.sum().toFloat() / (totalWordsInBook * 7f)

                    _state.update {
                        it.copy(
                            selectedBook         = book,
                            lessonStats          = lessonStats,
                            currentLessonWords   = currentLessonWords,
                            totalWordsInBook     = totalWordsInBook,
                            graduatedWordsInBook = graduatedWordsInBook,
                            overallHeatIndex     = overallHeat,
                            isLoading            = false
                        )
                    }
                }
        }
    }

    private fun WordWithRecord.toWordHeatItem(): WordHeatItem {
        val w = this.word
        val r = this.record
        val historyList: List<RoundReviewLog> = if (r != null && r.reviewHistory.isNotBlank()) {
            try {
                gson.fromJson(r.reviewHistory, logListType) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val repLevel = r?.repetitionLevel ?: 0
        return WordHeatItem(
            wordId             = w.id,
            book               = w.book,
            lesson             = w.lesson,
            cumulativeIndex    = w.cumulativeIndex,
            korean             = w.korean,
            chinese            = w.chinese,
            partOfSpeech       = w.partOfSpeech,
            irregularType      = w.irregularType,
            repetitionLevel    = repLevel,
            heatDepth          = repLevel.coerceIn(0, 7),
            isGraduated        = repLevel >= 7,
            easeFactor         = r?.easeFactor ?: 2.5,
            consecutiveSuccess = r?.consecutiveSuccessCount ?: 0,
            isErrorItem        = r?.isErrorItem ?: false,
            history            = historyList
        )
    }
}
