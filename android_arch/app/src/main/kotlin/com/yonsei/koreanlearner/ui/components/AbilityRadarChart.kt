package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.korean.TopikAbilityScores
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * ══════════════════════════════════════════════════════════════════
 * 五维韩语能力雷达图组件 (AbilityRadarChart)
 * ══════════════════════════════════════════════════════════════════
 *
 * 五大核心维度：
 * 1. 词汇量 (Vocabulary / 어휘력)
 * 2. 语法活用 (Grammar / 문법활용)
 * 3. 敬语得体度 (Honorifics / 경어법)
 * 4. 音变听辨 (Phonology / 음운발음)
 * 5. 阅读理解 (Reading / 독해추론)
 */
@Composable
fun AbilityRadarChart(
    scores: TopikAbilityScores,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(scores) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val polygonFillColor = Color(0xFF833AB4).copy(alpha = 0.22f)
    val polygonStrokeColor = Color(0xFF833AB4)

    val dimensions = remember {
        listOf(
            "词汇量 (어휘)",
            "语法活用 (문법)",
            "敬语得体度 (경어)",
            "音变听辨 (음운)",
            "阅读理解 (독해)"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(32.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.width.coerceAtMost(size.height) / 2f) * 0.85f

            val vertexCount = 5
            val angleStep = (2 * Math.PI / vertexCount).toFloat()
            val startAngle = -Math.PI.toFloat() / 2f // 顶点朝正上方

            // ── 1. 绘制 4 层同心正五边形背景网格 (25%, 50%, 75%, 100%) ──
            val levels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
            levels.forEach { level ->
                val r = maxRadius * level
                val gridPath = Path()
                for (i in 0 until vertexCount) {
                    val angle = startAngle + i * angleStep
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                }
                gridPath.close()
                drawPath(gridPath, color = gridColor, style = Stroke(width = 1.5f))
            }

            // ── 2. 绘制 5 条中心向外的辐射轴线 ───────────────────
            for (i in 0 until vertexCount) {
                val angle = startAngle + i * angleStep
                val x = center.x + maxRadius * cos(angle)
                val y = center.y + maxRadius * sin(angle)
                drawLine(
                    color = axisColor,
                    start = center,
                    end = Offset(x, y),
                    strokeWidth = 1.5f
                )
            }

            // ── 3. 绘制用户能力五边形多边形（带动画展开） ─────────
            val rawScores = listOf(
                scores.vocabularyScore,
                scores.grammarScore,
                scores.honorificScore,
                scores.phonologyScore,
                scores.readingScore
            )

            val dataPath = Path()
            val pointList = mutableListOf<Offset>()

            for (i in 0 until vertexCount) {
                val normalizedScore = (rawScores[i] / 100f).coerceIn(0.15f, 1.0f)
                val currentRadius = maxRadius * normalizedScore * animProgress.value
                val angle = startAngle + i * angleStep
                val x = center.x + currentRadius * cos(angle)
                val y = center.y + currentRadius * sin(angle)
                val point = Offset(x, y)
                pointList.add(point)

                if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            // 填充渐变多边形
            drawPath(
                path = dataPath,
                color = polygonFillColor,
                style = Fill
            )

            // 描边多边形
            drawPath(
                path = dataPath,
                color = polygonStrokeColor,
                style = Stroke(width = 3.dp.toPx())
            )

            // 绘制 5 个顶点实心圆球
            pointList.forEach { p ->
                drawCircle(
                    color = Color.White,
                    radius = 4.5.dp.toPx(),
                    center = p
                )
                drawCircle(
                    color = polygonStrokeColor,
                    radius = 3.dp.toPx(),
                    center = p
                )
            }
        }

        // ── 4. 五个维度文字标签排版 ───────────────────────────
        // 使用相对布局固定标注在正五边形 5 个顶点外侧
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部顶点：词汇量
            RadarLabelChip("어휘 ${scores.vocabularyScore.toInt()}分")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RadarLabelChip("독해 ${scores.readingScore.toInt()}分")
                RadarLabelChip("문법 ${scores.grammarScore.toInt()}分")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RadarLabelChip("음운 ${scores.phonologyScore.toInt()}分")
                RadarLabelChip("경어 ${scores.honorificScore.toInt()}分")
            }
        }
    }
}

@Composable
private fun RadarLabelChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
