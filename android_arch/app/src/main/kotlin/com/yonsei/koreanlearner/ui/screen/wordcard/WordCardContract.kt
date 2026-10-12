package com.yonsei.koreanlearner.ui.screen.wordcard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.yonsei.koreanlearner.domain.model.ReviewRating

// ══════════════════════════════════════════════════════════════════
// 领域模型（UI 层专用，脱离 Entity 依赖）
// ══════════════════════════════════════════════════════════════════

/**
 * 单词卡片展示模型。
 *
 * 包含从 WordEntity + ReviewRecordEntity 聚合的全量展示字段。
 * @Immutable：所有属性 val，Compose 编译器可安全跳过重组检查。
 */
@Immutable
data class WordCard(
    val wordId          : Int,
    val book            : Int,
    val lesson          : Int,
    val cumulativeIndex : Int,

    // ── 展示字段 ─────────────────────────────────────────────────
    val korean          : String,
    val chinese         : String,
    val partOfSpeech    : String,
    val level           : String,
    val irregularType   : String? = null,

    // ── 发音 ─────────────────────────────────────────────────────
    /** 罗马字拼音（可选，用于辅助发音显示） */
    val romanization    : String? = null,

    // ── 例句（从语法库或内置例句库获取） ─────────────────────────
    val exampleSentences: List<ExampleSentence> = emptyList(),

    // ── 记忆状态 ─────────────────────────────────────────────────
    val repetitionLevel : Int    = 0,
    val easeFactor      : Double = 2.5,
    val intervalDays    : Int    = 0,
    val isErrorItem     : Boolean = false,

    // ── 状态标记 ─────────────────────────────────────────────────
    /** true = 已有 ReviewRecord（已学），false = 全新单词 */
    val isNewWord       : Boolean = true
)

@Immutable
data class ExampleSentence(
    val korean  : String,
    val chinese : String,
    /** 高亮目标词汇在例句中的起始索引（可选，用于加粗目标词） */
    val highlightRange: IntRange? = null
)

// ══════════════════════════════════════════════════════════════════
// 当前课时信息
// ══════════════════════════════════════════════════════════════════

@Immutable
data class LessonProgress(
    val book          : Int,
    val lesson        : Int,
    val totalWords    : Int,
    val learnedWords  : Int,
    val newWordsToday : Int
) {
    val progressFraction: Float
        get() = if (totalWords == 0) 0f else learnedWords.toFloat() / totalWords
    val isComplete: Boolean
        get() = learnedWords >= totalWords
}

// ══════════════════════════════════════════════════════════════════
// MVI — UiState
// ══════════════════════════════════════════════════════════════════

/**
 * WordCardScreen 的完整 UI 状态快照。
 * 任何 UI 变化都通过替换此对象触发，绝不使用局部 mutableState。
 */
@Immutable
data class WordCardUiState(

    // ── 课时上下文 ────────────────────────────────────────────────
    val currentBook   : Int = 1,
    val currentLesson : Int = 1,

    // ── 卡片队列 ──────────────────────────────────────────────────
    /** 本课新词队列（未学，按 cumulative_index 排列） */
    val newWordQueue   : List<WordCard> = emptyList(),

    /** 当前展示的卡片在队列中的索引 */
    val currentIndex   : Int = 0,

    // ── 卡片交互状态 ──────────────────────────────────────────────
    /** 卡片是否已翻面（false=正面韩文，true=背面中文+例句+按钮） */
    val isCardFlipped  : Boolean = false,

    /** 是否正在提交评分（防止重复点击） */
    val isSubmitting   : Boolean = false,

    // ── 课时进度 ──────────────────────────────────────────────────
    val lessonProgress : LessonProgress? = null,

    // ── 课时解锁状态 ──────────────────────────────────────────────
    /** 当前用户已解锁的最高课（book * 100 + lesson） */
    val unlockedUpTo   : Int = 101,   // 默认解锁 Book1-Lesson1

    // ── 加载 / 错误 ───────────────────────────────────────────────
    val isLoading      : Boolean = true,
    val errorMessage   : String? = null,

    // ── 本次 Session 统计 ─────────────────────────────────────────
    val sessionCorrect : Int = 0,     // 本次答对（rating >= GOOD）
    val sessionTotal   : Int = 0,     // 本次总评分次数

    // ── 音频播放状态 ──────────────────────────────────────────────
    val isPlayingAudio : Boolean = false
) {
    val currentCard: WordCard? get() = newWordQueue.getOrNull(currentIndex)
    val isQueueEmpty: Boolean  get() = !isLoading && newWordQueue.isEmpty()
    val isSessionDone: Boolean get() = !isLoading && newWordQueue.isNotEmpty() && currentIndex >= newWordQueue.size
    val accuracy: Float        get() = if (sessionTotal == 0) 0f else sessionCorrect.toFloat() / sessionTotal
}

// ══════════════════════════════════════════════════════════════════
// MVI — Intent（用户可触发的意图）
// ══════════════════════════════════════════════════════════════════

sealed class WordCardIntent {
    /** 选择册数+课次，重新加载队列 */
    data class SelectLesson(val book: Int, val lesson: Int) : WordCardIntent()

    /** 单击卡片正面 → 翻转到背面 */
    object FlipCard : WordCardIntent()

    /** 用户选择四级评分并提交 */
    data class SubmitRating(val rating: ReviewRating) : WordCardIntent()

    /** 点击发音按钮，播放 TTS / 音频 */
    object PlayPronunciation : WordCardIntent()

    /** 跳过当前卡片（不写入 ReviewRecord） */
    object SkipCard : WordCardIntent()

    /** 学习完成后，重新开始当前课次 */
    object RestartLesson : WordCardIntent()

    /** 进入下一课时 */
    object GoToNextLesson : WordCardIntent()

    /** 消除错误提示 */
    object DismissError : WordCardIntent()
}

// ══════════════════════════════════════════════════════════════════
// MVI — Effect（一次性 UI 副作用）
// ══════════════════════════════════════════════════════════════════

sealed class WordCardEffect {
    /** 触发卡片 3D 翻转动画（通知 UI 层启动 Animatable） */
    object TriggerCardFlip : WordCardEffect()

    /** 播放评分反馈触感 */
    data class PlayHaptic(val isSuccess: Boolean) : WordCardEffect()

    /** 触发当前卡片滑出动画（left=遗忘/吃力，right=认识/掌握） */
    data class AnimateCardOut(val toRight: Boolean) : WordCardEffect()

    /** 触发 TTS 发音 */
    data class SpeakKorean(val text: String) : WordCardEffect()

    /** 导航到下一课时 */
    data class NavigateToLesson(val book: Int, val lesson: Int) : WordCardEffect()

    /** 显示全局 Snackbar */
    data class ShowSnackbar(val message: String) : WordCardEffect()

    /** 课时全部完成，展示完成动画 */
    object ShowLessonComplete : WordCardEffect()
}
