package com.yonsei.koreanlearner.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════
 * Android 14+ 预测性返回过渡动画容器 (PredictiveBackTransition)
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心动效：
 * 1. 拦截边缘侧滑返回手势，捕获手势进度 progress (0.0 -> 1.0)。
 * 2. 容器从全屏平滑缩放到卡片态（Scale: 1.0 -> 0.90f，CornerRadius: 0dp -> 24dp）。
 * 3. 伴随手指滑动的微位移与高斯阴影层级浮现。
 * 4. 手势取消时弹性回弹全屏，手势确认完成时顺畅退出。
 *
 * @param onBack 确认返回时的回调
 */
@Composable
fun PredictiveBackTransition(
    enabled  : Boolean = true,
    onBack   : () -> Unit,
    modifier : Modifier = Modifier,
    content  : @Composable () -> Unit
) {
    val scaleAnim = remember { Animatable(1.0f) }
    val cornerRadiusAnim = remember { Animatable(0f) }
    val translationXAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1.0f) }

    val coroutineScope = rememberCoroutineScope()

    PredictiveBackHandler(enabled = enabled) { progressFlow: Flow<BackEventCompat> ->
        try {
            progressFlow.collect { backEvent ->
                val progress = backEvent.progress // 0.0 ~ 1.0
                val edge = backEvent.swipeEdge   // LEFT or RIGHT

                // 物理缩放计算 (1.0 -> 0.90)
                val targetScale = 1.0f - (progress * 0.10f)
                // 圆角过渡 (0dp -> 24dp)
                val targetRadius = progress * 24f
                // X 轴偏移（跟随滑动方向轻微移动）
                val sign = if (edge == BackEventCompat.EDGE_LEFT) 1f else -1f
                val targetTranslationX = sign * (progress * 40f)
                // 透明度轻微变化
                val targetAlpha = 1.0f - (progress * 0.15f)

                coroutineScope.launch {
                    scaleAnim.snapTo(targetScale)
                    cornerRadiusAnim.snapTo(targetRadius)
                    translationXAnim.snapTo(targetTranslationX)
                    alphaAnim.snapTo(targetAlpha)
                }
            }

            // 侧滑确认完成 -> 快速淡出退出并执行返回
            coroutineScope.launch {
                alphaAnim.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                onBack()
            }
        } catch (e: Exception) {
            // 用户中途取消侧滑返回 -> 物理弹性回弹归位
            coroutineScope.launch {
                launch { scaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                launch { cornerRadiusAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                launch { translationXAnim.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                launch { alphaAnim.animateTo(1.0f, tween(120)) }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = translationXAnim.value
                    alpha = alphaAnim.value
                    clip = true
                    shape = RoundedCornerShape(cornerRadiusAnim.value.dp)
                }
                .shadow(
                    elevation = if (cornerRadiusAnim.value > 0f) 12.dp else 0.dp,
                    shape = RoundedCornerShape(cornerRadiusAnim.value.dp)
                )
                .clip(RoundedCornerShape(cornerRadiusAnim.value.dp))
        ) {
            content()
        }
    }
}
