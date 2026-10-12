package com.yonsei.koreanlearner.domain.model

/**
 * 用户复习评分等级（0~3），对应卡片翻面后的自我评估。
 *
 * UI 建议：
 *   - 卡片底部四个色块按钮（红/橙/绿/蓝）
 *   - 每次评分后立即触发 MemoryScheduler.calculateNextReview()
 */
enum class ReviewRating(
    val score: Int,
    val label: String,
    val emoji: String,
    val colorHex: String
) {
    /** 0 - 完全遗忘：重置回第0轮，EF下降，当天可立即再次复习 */
    BLACKOUT(0, "完全遗忘", "", "#EF4444"),

    /** 1 - 模糊/耗时：想起但很慢或不确定，降一级，缩短间隔 */
    FUZZY(1, "模糊/耗时长", "", "#F59E0B"),

    /** 2 - 掌握：顺畅作答，正常升级并按EF扩展间隔 */
    GOOD(2, "掌握", "", "#10B981"),

    /** 3 - 熟练秒杀：瞬间反应，升级并额外加速（EF × 1.15） */
    PERFECT(3, "熟练秒杀", "", "#3B82F6");

    companion object {
        fun fromScore(score: Int): ReviewRating =
            entries.find { it.score == score } ?: BLACKOUT
    }
}
