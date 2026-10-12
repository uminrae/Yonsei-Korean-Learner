package com.yonsei.koreanlearner.ui.screen.learn

import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity

// ════════════════════════════════════════════════════════════════
// 今日学习界面 MVI 契约
// ════════════════════════════════════════════════════════════════

data class LearnUiState(
    /** 当前正在学习的册数 */
    val book: Int = 1,
    /** 当前正在学习的课次 */
    val lesson: Int = 1,
    /** 今日新词列表 */
    val newWords: List<WordEntity> = emptyList(),
    /** 今日新语法列表 */
    val newGrammars: List<GrammarEntity> = emptyList(),
    /** 当前在新词列表中的索引 */
    val wordIndex: Int = 0,
    /** 是否正在加载 */
    val isLoading: Boolean = true,
    /** 全部新内容已学习完毕 */
    val isLessonComplete: Boolean = false,
    /** 错误信息 */
    val errorMessage: String? = null
) {
    val currentWord: WordEntity? get() = newWords.getOrNull(wordIndex)
    val progress: Float get() =
        if (newWords.isEmpty()) 1f
        else wordIndex.toFloat() / newWords.size.toFloat()
    val remainingWords: Int get() = (newWords.size - wordIndex).coerceAtLeast(0)
}

sealed class LearnIntent {
    /** 用户记住当前单词 → 初始化记忆记录 + 显示下一个 */
    object MarkWordLearned : LearnIntent()
    /** 用户跳过当前单词（稍后复习） */
    object SkipWord : LearnIntent()
    /** 切换到指定课次 */
    data class SelectLesson(val book: Int, val lesson: Int) : LearnIntent()
    /** 关闭错误提示 */
    object DismissError : LearnIntent()
}

sealed class LearnEffect {
    /** 新词全部学完，引导跳转复习 */
    object NavigateToReview : LearnEffect()
    /** 展示 Snackbar */
    data class ShowSnackbar(val message: String) : LearnEffect()
}
