package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import com.yonsei.koreanlearner.ui.theme.extended

// ══════════════════════════════════════════════════════════════════
// 数据模型（@Immutable 防止非必要重组）
// ══════════════════════════════════════════════════════════════════

/**
 * @Immutable：告知 Compose 编译器此类所有属性在其生命周期内不会改变，
 * 从而跳过对持有此类实例的 Composable 的重组检查。
 *
 * 性能关键：StoryProgressRow 的 items 列表如果使用非 @Stable 类型，
 * 每次父级重组都会强制重组所有子节点。
 */
@Immutable
data class LessonStoryItem(
    val id: Int,                       // 课次 ID（作为 LazyRow key）
    val book: Int,                     // 册数
    val lesson: Int,                   // 课次
    val label: String,                 // 显示标签，如 "1-1"
    val masteryPercent: Float,         // 掌握度 0.0~1.0（影响渐变环填充比例）
    val isCompleted: Boolean,          // 是否已完成全部复习
    val isActive: Boolean,             // 是否为当前正在学习的课次
    val streakDays: Int = 0            // 连续打卡天数（>0 显示火焰徽章）
)

// ══════════════════════════════════════════════════════════════════
// StoryProgressRow — Instagram Story 风格课时进度行
// ══════════════════════════════════════════════════════════════════

/**
 * 仿 Instagram Stories 的课时进度展示组件。
 *
 * 视觉特性：
 * - 每个课时显示为圆形头像圈
 * - 已掌握进度以 Instagram 彩虹渐变圆弧表示
 * - 未开始：灰色虚线环
 * - 进行中：渐变实线环（按掌握度填充）
 * - 已完成：完整渐变环 + 白色对勾
 * - Active 课时有缩放脉冲动画
 *
 * 性能保障：
 * - LazyRow + key + contentType：避免重组时重新创建所有节点
 * - Canvas 直接绘制圆弧（无额外 View 对象）
 * - 动画仅在 isActive=true 的节点上运行
 *
 * @param items      课时数据列表（@Immutable，稳定引用）
 * @param onItemClick 点击课时圆圈的回调
 * @param modifier   外部布局修饰符
 */
@Composable
fun StoryProgressRow(
    items: List<LessonStoryItem>,
    onItemClick: (LessonStoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier            = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding      = PaddingValues(horizontal = 16.dp)
    ) {
        items(
            items       = items,
            key         = { it.id },                     // 稳定 key，防止 item 位移引起重组
            contentType = { "story_item" }               // 统一 contentType，复用 Composition
        ) { item ->
            StoryItemNode(
                item    = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 单个 Story 节点
// ══════════════════════════════════════════════════════════════════

@Composable
private fun StoryItemNode(
    item: LessonStoryItem,
    onClick: () -> Unit
) {
    // ── Active 节点脉冲动画 ──────────────────────────────────────
    // 仅 isActive=true 时运行，避免列表中所有节点同时执行动画
    val pulseScale by if (item.isActive) {
        val transition = rememberInfiniteTransition(label = "story_pulse_${item.id}")
        transition.animateFloat(
            initialValue   = 1.0f,
            targetValue    = 1.06f,
            animationSpec  = infiniteRepeatable(
                animation  = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .semantics { contentDescription = "第${item.book}册第${item.lesson}课，掌握度${(item.masteryPercent * 100).toInt()}%" }
    ) {
        // ── 圆形头像圈（Canvas 绘制渐变环）──────────────────────
        StoryRing(
            item      = item,
            ringSize  = 64.dp,
            ringWidth = 3.dp,
            scale     = pulseScale,
            modifier  = Modifier.clickable(onClick = onClick)
        )

        Spacer(Modifier.height(6.dp))

        // ── 课次标签 ──────────────────────────────────────────
        Text(
            text      = item.label,
            style     = MaterialTheme.typography.labelSmall,
            color     = if (item.isActive) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// Story 渐变环（Canvas 绘制，在渲染线程执行，不触发 Layout）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun StoryRing(
    item: LessonStoryItem,
    ringSize: Dp,
    ringWidth: Dp,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.extended
    val density  = LocalDensity.current
    val gradientCache = com.yonsei.koreanlearner.core.render.rememberCachedSweepGradient()

    // 渐变色列表（稳定引用，CompositionLocal 读取）
    val gradientColors = remember(extended) {
        listOf(
            extended.gradientStart,
            extended.gradientMid1,
            extended.gradientMid2,
            extended.gradientEnd
        )
    }
    val trackColor    = MaterialTheme.colorScheme.outlineVariant
    val bgColor       = MaterialTheme.colorScheme.surface
    val onBgColor     = MaterialTheme.colorScheme.onBackground
    val ringWidthPx   = with(density) { ringWidth.toPx() }

    Box(
        modifier = modifier
            .size(ringSize)
            // graphicsLayer 在渲染线程执行 scale，不触发主线程 Layout/Measure
            .graphicsLayer { scaleX = scale; scaleY = scale },
        contentAlignment = Alignment.Center
    ) {
        // ── Canvas：绘制进度环 ───────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            val arcRect    = Size(size.minDimension - ringWidthPx, size.minDimension - ringWidthPx)
            val arcOffset  = Offset(ringWidthPx / 2f, ringWidthPx / 2f)
            val center     = Offset(arcOffset.x + arcRect.width / 2f, arcOffset.y + arcRect.height / 2f)
            val brush      = gradientCache.getOrCreate(center, gradientColors)

            when {
                // ── 已完成：完整渐变环 ────────────────────────
                item.isCompleted -> {
                    drawArc(
                        brush       = brush,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = arcOffset,
                        size        = arcRect,
                        style       = Stroke(width = ringWidthPx, cap = StrokeCap.Round)
                    )
                }

                // ── 进行中：按掌握度填充渐变弧 + 灰色轨道 ────
                item.masteryPercent > 0f -> {
                    // 灰色背景轨道
                    drawArc(
                        color       = trackColor,
                        startAngle  = -90f,
                        sweepAngle  = 360f,
                        useCenter   = false,
                        topLeft     = arcOffset,
                        size        = arcRect,
                        style       = Stroke(width = ringWidthPx, cap = StrokeCap.Round)
                    )
                    // 渐变进度弧
                    drawArc(
                        brush       = brush,
                        startAngle  = -90f,
                        sweepAngle  = 360f * item.masteryPercent,
                        useCenter   = false,
                        topLeft     = arcOffset,
                        size        = arcRect,
                        style       = Stroke(width = ringWidthPx, cap = StrokeCap.Round)
                    )
                }

                // ── 未开始：灰色虚线环 ─────────────────────────
                else -> {
                    val dashIntervals = floatArrayOf(10f, 8f)
                    drawArc(
                        color      = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = arcOffset,
                        size       = arcRect,
                        style      = Stroke(
                            width       = ringWidthPx * 0.7f,
                            cap         = StrokeCap.Round,
                            pathEffect  = PathEffect.dashPathEffect(dashIntervals, 0f)
                        )
                    )
                }
            }
        }

        // ── 内部圆形背景（gap 分隔环与内容）────────────────────
        Box(
            modifier = Modifier
                .size(ringSize - ringWidth * 2 - 6.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 已完成：对勾
                item.isCompleted -> Text("✓", fontSize = 22.sp, color = extended.gradientMid1, fontWeight = FontWeight.Bold)
                // 连续打卡：火焰徽章
                item.streakDays > 0 && item.isActive -> Text("", fontSize = 18.sp)
                // 进行中/未开始：课次数字
                else -> Text(
                    text      = item.lesson.toString(),
                    fontSize  = 16.sp,
                    fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Normal,
                    color     = onBgColor
                )
            }
        }
    }
}
