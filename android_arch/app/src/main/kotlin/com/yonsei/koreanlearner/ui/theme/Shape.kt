package com.yonsei.koreanlearner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ══════════════════════════════════════════════════════════════════
// Shape 圆角体系（Instagram 美学：统一、一致、克制）
// ══════════════════════════════════════════════════════════════════

val KoreanLearnerShapes = Shapes(

    // ── ExtraSmall：极小圆角（标签 Chip、徽章） ───────────────────
    // 视觉：几乎方形，用于精简信息标签
    extraSmall = RoundedCornerShape(6.dp),

    // ── Small：小圆角（输入框、小型卡片、评分按钮） ───────────────
    small = RoundedCornerShape(10.dp),

    // ── Medium：中圆角（主要卡片、对话框、底部栏） ───────────────
    // 对应设计规范的 16dp 卡片圆角
    medium = RoundedCornerShape(16.dp),

    // ── Large：大圆角（翻卡主卡片、全屏卡片） ────────────────────
    large = RoundedCornerShape(24.dp),

    // ── ExtraLarge：超大圆角（底部 Sheet、头像环） ───────────────
    extraLarge = RoundedCornerShape(32.dp)
)

// ── 常用圆角扩展（直接引用，避免重复声明） ────────────────────────

/** Story 进度环圆形（50% → CircleShape） */
val CircleShape = RoundedCornerShape(50)

/** 底部导航图标的选中背景 */
val NavIndicatorShape = RoundedCornerShape(12.dp)

/** 评分按钮圆角 */
val RatingButtonShape = RoundedCornerShape(14.dp)

/** 全屏卡片（翻卡页）圆角 — 仅上方圆角 */
val TopRoundedShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
