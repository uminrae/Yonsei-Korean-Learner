package com.yonsei.koreanlearner.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════
// Instagram 视觉基调调色板
// ══════════════════════════════════════════════════════════════════

// ── 基础黑白灰系 ──────────────────────────────────────────────────
val White           = Color(0xFFFFFFFF)
val Black           = Color(0xFF000000)

// Light Mode 背景层级
val SurfaceL0       = Color(0xFFFFFFFF)   // 最底层背景
val SurfaceL1       = Color(0xFFF9F9F9)   // 卡片背景
val SurfaceL2       = Color(0xFFF2F2F2)   // 输入框/分隔
val BorderSubtle    = Color(0xFFEFEFEF)   // 1dp 描边（极淡）

// Dark Mode 背景层级
val Dark0           = Color(0xFF121212)   // 最底层背景
val Dark1           = Color(0xFF1E1E1E)   // 卡片背景
val Dark2           = Color(0xFF2A2A2A)   // 输入框/分隔
val DarkBorder      = Color(0xFF333333)   // Dark 模式描边

// ── 文字系 ────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFF0A0A0A)   // 主文字（Light）
val TextSecondary   = Color(0xFF666666)   // 次要文字
val TextTertiary    = Color(0xFF999999)   // 辅助/占位文字
val TextPrimaryDark = Color(0xFFF5F5F5)   // 主文字（Dark）
val TextSecDark     = Color(0xFF999999)

// ── Instagram 渐变辅色（点缀色）──────────────────────────────────
// 用于：打卡高亮、连续天数徽章、Story 进度环
val InsGradientPurple = Color(0xFF833AB4)   // 紫
val InsGradientPink   = Color(0xFFE1306C)   // 粉
val InsGradientOrange = Color(0xFFF77737)   // 橙
val InsGradientYellow = Color(0xFFFCCC12)   // 金黄（渐变终点）

// 渐变色列表（按 Ins 彩虹顺序，供 Brush.linearGradient 使用）
val InsGradientColors = listOf(
    InsGradientPurple,
    InsGradientPink,
    InsGradientOrange,
    InsGradientYellow
)

// ── 语义色 ────────────────────────────────────────────────────────
// 评分按钮 — 对应 ReviewRating 四色
val RatingBlackout  = Color(0xFFEF4444)   // 完全遗忘（红）
val RatingFuzzy     = Color(0xFFF59E0B)   // 模糊（琥珀）
val RatingGood      = Color(0xFF10B981)   // 掌握（绿）
val RatingPerfect   = Color(0xFF6366F1)   // 秒杀（靛蓝）

// 记忆保留率语义色
val RetentionHigh   = Color(0xFF10B981)   // ≥80%
val RetentionMid    = Color(0xFFF59E0B)   // 50~80%
val RetentionLow    = Color(0xFFEF4444)   // <50%

// ── Material 3 角色映射辅助色 ─────────────────────────────────────
val PrimaryLight    = Color(0xFF0A0A0A)   // Light Primary（极深近黑）
val OnPrimaryLight  = Color(0xFFFFFFFF)
val SecondaryLight  = Color(0xFF444444)
val TertiaryLight   = InsGradientPink

val PrimaryDark     = Color(0xFFF5F5F5)   // Dark Primary（近白）
val OnPrimaryDark   = Color(0xFF121212)
val SecondaryDark   = Color(0xFFAAAAAA)
val TertiaryDark    = InsGradientOrange
