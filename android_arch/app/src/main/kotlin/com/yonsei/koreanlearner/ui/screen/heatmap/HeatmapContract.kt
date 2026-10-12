package com.yonsei.koreanlearner.ui.screen.heatmap

import androidx.compose.runtime.Immutable
import com.yonsei.koreanlearner.domain.model.LessonHeatmapItem
import com.yonsei.koreanlearner.domain.model.WordHeatItem

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世 7 轮复习进度热力图 MVI 契约
 * ══════════════════════════════════════════════════════════════════
 */

@Immutable
data class HeatmapUiState(
    /** 当前选中的册数（1~6） */
    val selectedBook          : Int = 1,
    /** 当前选中的课时（1~10，0 表示查看全册概览） */
    val selectedLesson        : Int = 1,
    /** 当前册数下所有课时的聚合进度统计 */
    val lessonStats           : List<LessonHeatmapItem> = emptyList(),
    /** 当前选中课时的单词热力列表 */
    val currentLessonWords    : List<WordHeatItem> = emptyList(),
    /** 当前选中的单个单词详情（用于弹出底部词汇记忆卡片） */
    val selectedWordDetail    : WordHeatItem? = null,
    /** 册内统计 */
    val totalWordsInBook      : Int = 0,
    val graduatedWordsInBook  : Int = 0,
    val overallHeatIndex      : Float = 0f,
    /** 是否正在加载 */
    val isLoading             : Boolean = true,
    /** 错误信息 */
    val errorMessage          : String? = null
) {
    /** 毕业进度比例 0.0f ~ 1.0f */
    val graduationProgress: Float get() =
        if (totalWordsInBook == 0) 0f else graduatedWordsInBook.toFloat() / totalWordsInBook.toFloat()
}

sealed class HeatmapIntent {
    /** 切换册数（1~6） */
    data class SelectBook(val book: Int) : HeatmapIntent()
    /** 切换课时（1~10） */
    data class SelectLesson(val lesson: Int) : HeatmapIntent()
    /** 点击热力图中的某个单词方块，查看其 7 轮打卡时间线 */
    data class ClickWordSquare(val word: WordHeatItem) : HeatmapIntent()
    /** 关闭单词详情弹窗 */
    object DismissWordDetail : HeatmapIntent()
}

sealed class HeatmapEffect {
    /** 当某个单元全部达成第 7 轮毕业时触发发光庆祝动效 */
    data class TriggerGraduationCelebration(val lesson: Int) : HeatmapEffect()
    /** 弹出提示 */
    data class ShowSnackbar(val message: String) : HeatmapEffect()
}
