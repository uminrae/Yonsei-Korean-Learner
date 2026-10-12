package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.ui.theme.extended
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════
 * 物理弹簧手势卡片组件 (SpringSwipeableCard)
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心交互：
 * 1. 右滑（已熟记 / 掌握）、左滑（未掌握 / 遗忘）、下滑（跳过）。
 * 2. 释放时以 SpringMediumBouncy 弹性物理曲线回弹。
 * 3. 双击（Double-Tap）触发 Instagram 风格爱心放大渐隐微动效与触觉反馈。
 * 4. 伴随水平位移的自然微角度倾斜与阻尼缩放。
 *
 * @param onSwipeRight 右滑成功回调（已掌握）
 * @param onSwipeLeft 左滑回调（未掌握）
 * @param onSwipeDown 下滑回调（跳过）
 * @param onDoubleTap 双击回调（如标星收藏/标记已掌握）
 */
@Composable
fun SpringSwipeableCard(
    onSwipeRight : () -> Unit,
    onSwipeLeft  : () -> Unit,
    onSwipeDown  : (() -> Unit)? = null,
    onDoubleTap  : (() -> Unit)? = null,
    modifier     : Modifier = Modifier,
    content      : @Composable BoxScope.() -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val horizontalThreshold = screenWidthPx * 0.35f
    val verticalThreshold = screenHeightPx * 0.25f

    // 物理弹性位移 Animatable
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    // 双击爱心微动效状态
    var showHeartAnim by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }
    val heartAlpha = remember { Animatable(0f) }

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                // 微角度倾斜跟随（最大 15 度）
                rotationZ = (offsetX.value / screenWidthPx) * 15f
            }
            .pointerInput(Unit) {
                // 双击手势检测
                detectTapGestures(
                    onDoubleTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDoubleTap?.invoke()
                        // 触发爱心动效
                        coroutineScope.launch {
                            showHeartAnim = true
                            heartScale.snapTo(0.2f)
                            heartAlpha.snapTo(1f)
                            heartScale.animateTo(1.3f, tween(240, easing = FastOutSlowInEasing))
                            heartScale.animateTo(1.0f, tween(100))
                            heartAlpha.animateTo(0f, tween(200))
                            showHeartAnim = false
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                // 拖拽手势检测
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            when {
                                // 1. 右滑超出阈值 -> 已掌握
                                offsetX.value > horizontalThreshold -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    offsetX.animateTo(screenWidthPx * 1.5f, tween(220))
                                    onSwipeRight()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }

                                // 2. 左滑超出阈值 -> 未掌握
                                offsetX.value < -horizontalThreshold -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    offsetX.animateTo(-screenWidthPx * 1.5f, tween(220))
                                    onSwipeLeft()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }

                                // 3. 下滑超出阈值 -> 跳过
                                offsetY.value > verticalThreshold && onSwipeDown != null -> {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    offsetY.animateTo(screenHeightPx * 1.2f, tween(220))
                                    onSwipeDown.invoke()
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }

                                // 4. 未达阈值 -> 弹性物理回弹
                                else -> {
                                    launch { offsetX.animateTo(0f, springSpec) }
                                    launch { offsetY.animateTo(0f, springSpec) }
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            launch { offsetX.animateTo(0f, springSpec) }
                            launch { offsetY.animateTo(0f, springSpec) }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 卡片内容载体
        Card(
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                content()
            }
        }

        // 双击爱心微动效叠加层
        if (showHeartAnim) {
            Text(
                text = "",
                fontSize = 80.sp,
                modifier = Modifier
                    .scale(heartScale.value)
                    .graphicsLayer { alpha = heartAlpha.value }
            )
        }
    }
}
