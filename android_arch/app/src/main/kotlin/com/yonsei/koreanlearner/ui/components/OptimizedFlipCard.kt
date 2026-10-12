package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yonsei.koreanlearner.core.render.backfaceCulling

/**
 * ══════════════════════════════════════════════════════════════════
 * 120fps 满帧 3D 翻转卡片容器（硬件加速、完美贴合圆角无方块暗影）
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun OptimizedFlipCard(
    isFlipped: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    // 旋转角度 Animatable（0°~180°）
    val rotationAnim = remember { Animatable(if (isFlipped) 180f else 0f) }

    LaunchedEffect(isFlipped) {
        val target = if (isFlipped) 180f else 0f
        if (rotationAnim.value != target) {
            rotationAnim.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 360, easing = FastOutSlowInEasing)
            )
        }
    }

    val currentRotation = rotationAnim.value

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCardClick
            )
    ) {
        // ── 正面层 ────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxSize()
                .backfaceCulling(
                    rotationY = currentRotation,
                    isBackFace = false,
                    cameraDistance = 16f
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            frontContent()
        }

        // ── 背面层 ────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxSize()
                .backfaceCulling(
                    rotationY = currentRotation,
                    isBackFace = true,
                    cameraDistance = 16f
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            backContent()
        }
    }
}
