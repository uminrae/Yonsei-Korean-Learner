package com.yonsei.koreanlearner.ui.screen.review

import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.domain.model.ReviewRating

// ════════════════════════════════════════════════════════════════
// MVI — State / Intent / Effect
// ════════════════════════════════════════════════════════════════

/**
 * 复习界面的全量 UI 状态快照（MVI State）。
 *
 * Compose 从此 State 单向派生全部 UI，无任何隐式本地状态。
 */
data class ReviewUiState(

    /** 当前复习队列（词 + 语法混合，已排序） */
    val queue: List<ReviewQueueItem> = emptyList(),

    /** 当前正在复习的卡片在队列中的索引 */
    val currentIndex: Int = 0,

    /** 卡片是否已翻面（false=正面显示韩文，true=背面显示中文+评分按钮） */
    val isFlipped: Boolean = false,

    /** 今日总复习数量（用于进度条计算） */
    val totalDueCount: Int = 0,

    /** 本次 Session 已完成数量 */
    val completedCount: Int = 0,

    /** 加载状态 */
    val isLoading: Boolean = true,

    /** 错误信息（非 null 时显示 Snackbar） */
    val errorMessage: String? = null,

    /** 当前 Session 是否全部完成 */
    val isSessionComplete: Boolean = false
) {
    /** 当前卡片（安全访问） */
    val currentItem: ReviewQueueItem? get() = queue.getOrNull(currentIndex)

    /** 进度百分比 0.0~1.0 */
    val progress: Float get() =
        if (totalDueCount == 0) 1f
        else completedCount.toFloat() / totalDueCount.toFloat()
}

/**
 * 复习队列单项（联合词/语法数据与记忆记录）。
 */
sealed class ReviewQueueItem {
    abstract val record: ReviewRecordEntity

    data class WordItem(
        val word: WordEntity,
        override val record: ReviewRecordEntity
    ) : ReviewQueueItem()

    data class GrammarItem(
        val grammar: GrammarEntity,
        override val record: ReviewRecordEntity
    ) : ReviewQueueItem()
}

/**
 * 用户可触发的 UI 意图（MVI Intent）。
 */
sealed class ReviewIntent {
    /** 用户点击卡片正面 → 翻转到背面显示答案 */
    object FlipCard : ReviewIntent()

    /** 用户选择评分等级（仅在卡片翻面后有效） */
    data class SubmitRating(val rating: ReviewRating) : ReviewIntent()

    /** 复习完成界面：用户点击"再来一轮"重新加载到期队列 */
    object RestartSession : ReviewIntent()

    /** Snackbar 消失或用户关闭错误提示 */
    object DismissError : ReviewIntent()
}

/**
 * 一次性 UI 副作用（MVI Effect）。
 * 由 ViewModel 通过 SharedFlow 发送，Compose 通过 LaunchedEffect 消费。
 */
sealed class ReviewEffect {
    /** 导航到复习完成庆祝页面 */
    object NavigateToCompletion : ReviewEffect()

    /** 触发卡片翻转动画 */
    object TriggerFlipAnimation : ReviewEffect()

    /** 评分提交后触发下一张卡片动画（滑出） */
    data class ShowNextCard(val rating: ReviewRating) : ReviewEffect()

    /** 显示全局 Snackbar 错误信息 */
    data class ShowSnackbar(val message: String) : ReviewEffect()
}
