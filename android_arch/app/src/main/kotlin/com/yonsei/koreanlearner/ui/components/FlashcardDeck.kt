package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.ui.theme.extended
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ══════════════════════════════════════════════════════════════════
// 数据模型（@Stable/@Immutable 保证 Compose 编译器稳定性分析）
// ══════════════════════════════════════════════════════════════════

/**
 * @Stable：Compose 编译器会跳过对此类的相等性比较，
 * 假设调用方负责保证稳定性（适用于可能部分更新的数据）。
 *
 * 与 @Immutable 的区别：
 * - @Immutable：所有属性永不改变（val only）
 * - @Stable：属性可能改变，但 equals() 正确实现（data class 默认满足）
 */
@Immutable
data class FlashcardData(
    val id: Int,
    val korean: String,
    val chinese: String,
    val partOfSpeech: String,
    val level: String,
    val irregularType: String? = null,
    val itemType: String = "WORD",        // "WORD" | "GRAMMAR"
    val repetitionLevel: Int = 0,
    val easeFactor: Double = 2.5
)

/** 卡片滑出方向语义 */
enum class SwipeDirection { LEFT, RIGHT, NONE }

/** 手势回调集合（@Stable，函数引用本身稳定） */
@Stable
class FlashcardCallbacks(
    val onSwipeLeft  : (FlashcardData) -> Unit,  // 左划 = 不熟（FUZZY）
    val onSwipeRight : (FlashcardData) -> Unit,  // 右划 = 掌握（GOOD）
    val onDoubleTap  : (FlashcardData) -> Unit,  // 双击 = 熟练秒杀（PERFECT）
    val onFlip       : (FlashcardData) -> Unit   // 单击 = 翻牌
)

// ══════════════════════════════════════════════════════════════════
// FlashcardDeck — 全屏手势滑卡主组件
// ══════════════════════════════════════════════════════════════════

/**
 * Instagram Feed 风格全屏滑卡组件。
 *
 * 手势设计：
 *   单击           → 翻牌（正面/背面切换，3D rotationY 动画）
 *   双击           → 熟练秒杀（PERFECT）+ 心形粒子爆发动画
 *   左划（速度足够）→ FUZZY（模糊/不熟）+ 红色渐隐
 *   右划（速度足够）→ GOOD（掌握）+ 绿色渐隐
 *
 * 性能保障：
 *   - Animatable 驱动所有动画（可中断，自然感强）
 *   - graphicsLayer 在渲染线程执行，零主线程 Measure/Layout
 *   - 最多同时持有 2 张卡片（当前卡 + 下一张预加载）
 *   - @Immutable FlashcardData 防止不必要重组
 *
 * @param cards       卡片数据列表（@Immutable 保证稳定）
 * @param callbacks   手势回调集合
 * @param modifier    外部布局修饰符
 */
@Composable
fun FlashcardDeck(
    cards: List<FlashcardData>,
    callbacks: FlashcardCallbacks,
    modifier: Modifier = Modifier
) {
    if (cards.isEmpty()) {
        DeckEmptyState(modifier)
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentCard  = cards.getOrNull(currentIndex) ?: return
    val nextCard     = cards.getOrNull(currentIndex + 1)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ── 下一张卡片（预加载，静止在底部，稍微缩小）──────────
        nextCard?.let { next ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .fillMaxHeight(0.64f)
                    .zIndex(0f)
                    .graphicsLayer {
                        scaleX = 0.95f
                        scaleY = 0.95f
                        translationY = 20.dp.toPx()
                    }
            ) {
                CardFaceContainer(
                    card    = next,
                    isBack  = false,
                    flipRot = 0f,
                    overlay = Color.Transparent
                )
            }
        }

        // ── 当前卡片（可交互，层级在上）─────────────────────────
        SwipeableFlashcard(
            card      = currentCard,
            onSwiped  = { direction ->
                when (direction) {
                    SwipeDirection.LEFT  -> callbacks.onSwipeLeft(currentCard)
                    SwipeDirection.RIGHT -> callbacks.onSwipeRight(currentCard)
                    SwipeDirection.NONE  -> {}
                }
                // 无论哪个方向，滑出后前进到下一张
                if (direction != SwipeDirection.NONE) {
                    currentIndex = (currentIndex + 1).coerceAtMost(cards.size)
                }
            },
            onDoubleTap = { callbacks.onDoubleTap(currentCard) },
            onFlip      = { callbacks.onFlip(currentCard) },
            modifier    = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.68f)
                .zIndex(1f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// 可手势交互的单张卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SwipeableFlashcard(
    card       : FlashcardData,
    onSwiped   : (SwipeDirection) -> Unit,
    onDoubleTap: () -> Unit,
    onFlip     : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic         = LocalHapticFeedback.current

    // ── 动画状态 ──────────────────────────────────────────────────
    val offsetX    = remember { Animatable(0f) }       // 水平拖拽偏移
    val swipeAlpha = remember { Animatable(1f) }       // 整体透明度（滑出时渐隐）
    val flipRot    = remember { Animatable(0f) }       // Y轴旋转（翻牌）
    val doubleTapScale = remember { Animatable(1f) }   // 双击缩放

    // 当前是否显示背面
    var isBack by remember { mutableStateOf(false) }

    // 覆盖色（左划=红，右划=绿）
    val overlayColor by remember {
        derivedStateOf {
            val swipeProgress = offsetX.value / 400f
            when {
                swipeProgress > 0.05f -> Color(0xFF10B981).copy(alpha = swipeProgress.coerceIn(0f, 0.35f))
                swipeProgress < -0.05f -> Color(0xFFEF4444).copy(alpha = (-swipeProgress).coerceIn(0f, 0.35f))
                else -> Color.Transparent
            }
        }
    }

    // 速度追踪器（判断手势是否够快以触发滑出）
    val velocityTracker = remember { VelocityTracker() }

    Box(
        modifier = modifier
            .graphicsLayer {
                // graphicsLayer：在渲染线程执行，不触发主线程 Measure/Layout
                translationX = offsetX.value
                alpha        = swipeAlpha.value
                scaleX       = doubleTapScale.value
                scaleY       = doubleTapScale.value
                // 拖拽时微旋转（每100px偏移旋转约3°，增加物理感）
                rotationZ    = (offsetX.value / 100f) * 3f
            }
            // ── 双击手势（PERFECT 评分）────────────────────────
            .pointerInput(card.id) {
                detectTapGestures(
                    onDoubleTap = { _ ->
                        coroutineScope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // 双击缩放动画：快速放大后恢复
                            launch {
                                doubleTapScale.animateTo(
                                    targetValue   = 1.08f,
                                    animationSpec = spring(stiffness = Spring.StiffnessHigh)
                                )
                                doubleTapScale.animateTo(
                                    targetValue   = 1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                )
                            }
                            onDoubleTap()
                        }
                    },
                    onTap = { _ ->
                        // 单击翻牌
                        coroutineScope.launch {
                            // 翻转到90°（消失）
                            flipRot.animateTo(
                                targetValue   = 90f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            )
                            isBack = !isBack
                            // 从-90°翻转到0°（出现）
                            flipRot.snapTo(-90f)
                            flipRot.animateTo(
                                targetValue   = 0f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            )
                            onFlip()
                        }
                    }
                )
            }
            // ── 水平拖拽手势（左划/右划）──────────────────────
            .pointerInput(card.id) {
                detectHorizontalDragGestures(
                    onDragStart = { velocityTracker.resetTracking() },
                    onDragEnd   = {
                        coroutineScope.launch {
                            val velocity = velocityTracker.calculateVelocity().x
                            val offset   = offsetX.value
                            val swipeThresholdVelocity = 800f   // dp/s
                            val swipeThresholdOffset   = 120f   // px

                            val direction = when {
                                offset > swipeThresholdOffset || velocity > swipeThresholdVelocity
                                    -> SwipeDirection.RIGHT
                                offset < -swipeThresholdOffset || velocity < -swipeThresholdVelocity
                                    -> SwipeDirection.LEFT
                                else -> SwipeDirection.NONE
                            }

                            if (direction != SwipeDirection.NONE) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // 滑出动画
                                val targetX = if (direction == SwipeDirection.RIGHT) 1200f else -1200f
                                launch { offsetX.animateTo(targetX, tween(300, easing = FastOutLinearInEasing)) }
                                launch { swipeAlpha.animateTo(0f, tween(250)) }
                                kotlinx.coroutines.delay(280)
                                onSwiped(direction)
                                // 重置状态（为下一张卡片复用此 Composable）
                                offsetX.snapTo(0f)
                                swipeAlpha.snapTo(1f)
                                isBack = false
                            } else {
                                // 回弹到中心（弹簧动画）
                                launch { offsetX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) }
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        coroutineScope.launch {
                            // 阻尼系数：拖拽越远越难拖
                            val damping = 1f - (abs(offsetX.value) / 1200f).coerceIn(0f, 0.6f)
                            offsetX.snapTo(offsetX.value + dragAmount * damping)
                        }
                    }
                )
            }
    ) {
        // 卡片内容（正面或背面）
        CardFaceContainer(
            card    = card,
            isBack  = isBack,
            flipRot = flipRot.value,
            overlay = overlayColor
        )

        // 双击爆心特效（Canvas 绘制的心形粒子）
        DoubleTapHeartOverlay(
            scale   = doubleTapScale.value,
            visible = doubleTapScale.value > 1.02f
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// 卡片内容容器（正面 / 背面，含翻转动画层）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun CardFaceContainer(
    card    : FlashcardData,
    isBack  : Boolean,
    flipRot : Float,
    overlay : Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = flipRot; cameraDistance = 14f * density }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape    = RoundedCornerShape(24.dp),
            colors   = CardDefaults.cardColors(
                containerColor = if (!isBack) MaterialTheme.colorScheme.surface
                                 else MaterialTheme.colorScheme.surfaceVariant
            ),
            border   = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.extended.cardBorder
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (!isBack) CardFront(card) else CardBack(card)
        }

        // 滑动覆盖色层（渐变染色提示）
        if (overlay != Color.Transparent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(overlay)
            )
        }
    }
}

// ── 卡片正面 ─────────────────────────────────────────────────────

@Composable
private fun CardFront(card: FlashcardData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        // 轮次徽章
        LevelBadge(repetitionLevel = card.repetitionLevel)

        Spacer(Modifier.height(28.dp))

        // 韩文大字
        Text(
            text       = card.korean,
            fontSize   = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            lineHeight = 50.sp,
            color      = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(20.dp))

        // 词性 + 不规则标签行
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniChip(card.partOfSpeech)
            card.irregularType?.let { MiniChip(it, isError = true) }
        }

        Spacer(Modifier.height(32.dp))

        // 提示手势
        Text(
            text  = "单击翻转 · 双击秒杀 ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ── 卡片背面 ─────────────────────────────────────────────────────

@Composable
private fun CardBack(card: FlashcardData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationY = 180f }  // 防止背面文字镜像
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text       = card.chinese,
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(16.dp))

        Divider(
            color     = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )

        Spacer(Modifier.height(16.dp))

        // 记忆状态行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem("EF", String.format("%.1f", card.easeFactor))
            StatItem("级别", card.level)
            StatItem("第${card.repetitionLevel}轮", if (card.repetitionLevel >= 7) "已毕业" else "进行中")
        }

        Spacer(Modifier.height(24.dp))

        // 滑动提示
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("← 模糊", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extended.ratingFuzzy)
            Text("掌握 →", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.extended.ratingGood)
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 双击心形特效（Canvas 绘制，轻量、无第三方依赖）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DoubleTapHeartOverlay(scale: Float, visible: Boolean) {
    if (!visible) return
    val heartCache = com.yonsei.koreanlearner.core.render.rememberCachedHeartPath()
    val alpha = ((scale - 1f) / 0.08f).coerceIn(0f, 1f)
    val heartColor = Color(0xFFE1306C).copy(alpha = alpha)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.38f
        val r  = 24.dp.toPx() * scale
        val path = heartCache.update(cx, cy, r)
        drawPath(path, heartColor)
    }
}

// ══════════════════════════════════════════════════════════════════
// 子组件（复用）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun LevelBadge(repetitionLevel: Int) {
    val colors = MaterialTheme.extended
    val (text, bgColor) = when {
        repetitionLevel >= 7 -> " 已毕业" to colors.gradientMid1.copy(0.15f)
        repetitionLevel >= 4 -> "第${repetitionLevel}轮 " to colors.gradientMid2.copy(0.15f)
        repetitionLevel >= 1 -> "第${repetitionLevel}轮" to MaterialTheme.colorScheme.primaryContainer
        else                 -> "新词" to MaterialTheme.colorScheme.tertiaryContainer
    }
    Surface(color = bgColor, shape = RoundedCornerShape(10.dp)) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun MiniChip(text: String, isError: Boolean = false) {
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = if (isError) MaterialTheme.colorScheme.onErrorContainer
                       else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DeckEmptyState(modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✅", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("今日全部复习完成！", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}
