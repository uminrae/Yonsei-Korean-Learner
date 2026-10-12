package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.korean.HangulLetterStroke
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import kotlin.math.sqrt

/** 单条用户手绘笔画数据（包含采样点与时间戳） */
data class UserDrawnStroke(
    val points : List<Offset>,
    val path   : Path
)

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文字母笔顺拆解与原笔迹平滑临摹画布 (HangulStrokeCanvas)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun HangulStrokeCanvas(
    letter            : HangulLetterStroke,
    userStrokes       : List<UserDrawnStroke>,
    isDemonstrating   : Boolean,            // 是否正在播放笔顺示范动画
    onAddStroke       : (UserDrawnStroke) -> Unit,
    modifier          : Modifier = Modifier
) {
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val animProgress = remember { Animatable(0f) }

    // 笔顺示范流光动画
    LaunchedEffect(isDemonstrating, letter) {
        if (isDemonstrating) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF141414))
            .pointerInput(letter, isDemonstrating) {
                if (isDemonstrating) return@pointerInput
                detectDragGestures(
                    onDragStart = { startOffset ->
                        currentPoints = listOf(startOffset)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPoints = currentPoints + change.position
                    },
                    onDragEnd = {
                        if (currentPoints.size >= 2) {
                            val smoothedPath = buildSmoothedBezierPath(currentPoints)
                            onAddStroke(UserDrawnStroke(currentPoints, smoothedPath))
                        }
                        currentPoints = emptyList()
                    },
                    onDragCancel = {
                        currentPoints = emptyList()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // ── 1. 绘制米字格参考辅助线 ───────────────────────────
            val gridColor = Color(0xFF262626)
            drawLine(gridColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 1.5f)
            drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 1.5f)
            drawLine(gridColor, Offset(0f, 0f), Offset(w, h), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
            drawLine(gridColor, Offset(w, 0f), Offset(0f, h), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))

            // ── 2. 绘制标准字母骨架灰色底纹 (Template Skeleton) ─
            letter.strokeList.forEach { skeleton ->
                val start = Offset(skeleton.startPoint.x * w, skeleton.startPoint.y * h)
                val end = Offset(skeleton.endPoint.x * w, skeleton.endPoint.y * h)
                val path = Path().apply {
                    moveTo(start.x, start.y)
                    skeleton.waypoints.forEach { wp ->
                        lineTo(wp.x * w, wp.y * h)
                    }
                    lineTo(end.x, end.y)
                }

                // 浅灰色骨架粗线条
                drawPath(
                    path = path,
                    color = Color(0xFF2E2E2E),
                    style = Stroke(width = 36.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // 笔顺序号引导圆点
                drawCircle(
                    color = Color(0xFF3B82F6),
                    radius = 8.dp.toPx(),
                    center = start
                )
            }

            // ── 3. 笔顺示范流光动画 (Demonstration Mode) ─────────
            if (isDemonstrating) {
                val totalStrokes = letter.strokeList.size
                letter.strokeList.forEachIndexed { idx, skeleton ->
                    val strokeStartProgress = idx.toFloat() / totalStrokes
                    val strokeEndProgress = (idx + 1).toFloat() / totalStrokes

                    if (animProgress.value >= strokeStartProgress) {
                        val localProgress = ((animProgress.value - strokeStartProgress) / (strokeEndProgress - strokeStartProgress)).coerceIn(0f, 1f)

                        val start = Offset(skeleton.startPoint.x * w, skeleton.startPoint.y * h)
                        val end = Offset(skeleton.endPoint.x * w, skeleton.endPoint.y * h)

                        val currentX = start.x + (end.x - start.x) * localProgress
                        val currentY = start.y + (end.y - start.y) * localProgress

                        // 流光路径
                        val demoPath = Path().apply {
                            moveTo(start.x, start.y)
                            if (skeleton.waypoints.isNotEmpty() && localProgress > 0.5f) {
                                val wp = skeleton.waypoints.first()
                                lineTo(wp.x * w, wp.y * h)
                            }
                            lineTo(currentX, currentY)
                        }

                        drawPath(
                            path = demoPath,
                            color = Color(0xFFE1306C),
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // 笔尖发光球
                        drawCircle(Color.White, radius = 9.dp.toPx(), center = Offset(currentX, currentY))
                        drawCircle(Color(0xFFE1306C), radius = 6.dp.toPx(), center = Offset(currentX, currentY))
                    }
                }
            }

            // ── 4. 绘制用户已完成的平滑笔迹 (User Drawn Strokes) ──
            userStrokes.forEach { stroke ->
                drawPath(
                    path = stroke.path,
                    brush = Brush.linearGradient(InsGradientColors),
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // ── 5. 绘制当前正在书写的手指拖拽笔迹 ────────────────
            if (currentPoints.size >= 2) {
                val livePath = buildSmoothedBezierPath(currentPoints)
                drawPath(
                    path = livePath,
                    brush = Brush.linearGradient(InsGradientColors),
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        // 右上角字母标识
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
        ) {
            Text(
                text = letter.letterSymbol,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 二次贝塞尔中点抗锯齿平滑算法 (Midpoint Quadratic Bézier Smoothing)
 */
fun buildSmoothedBezierPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path

    path.moveTo(points.first().x, points.first().y)
    if (points.size < 2) return path

    for (i in 0 until points.size - 1) {
        val p1 = points[i]
        val p2 = points[i + 1]
        val midX = (p1.x + p2.x) / 2f
        val midY = (p1.y + p2.y) / 2f
        path.quadraticBezierTo(p1.x, p1.y, midX, midY)
    }
    path.lineTo(points.last().x, points.last().y)
    return path
}
