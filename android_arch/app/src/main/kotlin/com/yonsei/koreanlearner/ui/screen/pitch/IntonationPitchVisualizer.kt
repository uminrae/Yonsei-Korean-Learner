package com.yonsei.koreanlearner.ui.screen.pitch

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.korean.IntonationTemplate

/**
 * ══════════════════════════════════════════════════════════════════
 * 终结句尾语调音高双轨曲线可视化组件 (IntonationPitchVisualizer)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun IntonationPitchVisualizer(
    template       : IntonationTemplate,
    userPitchTrack : List<Float>,           // 用户采集到的归一化音高轨迹 (0.0f ~ 1.0f)
    modifier       : Modifier = Modifier
) {
    val templatePoints = template.pitchCurve
    val syllables = template.syllables

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141414))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 顶栏：图例标注
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                // 虚线图例
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp, 2.dp).background(Color(0xFF888888)))
                    Spacer(Modifier.width(4.dp))
                    Text("标准母语者", fontSize = 11.sp, color = Color(0xFF888888))
                }

                // 实线图例
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(16.dp, 3.dp).background(Color(0xFFE1306C)))
                    Spacer(Modifier.width(4.dp))
                    Text("你的语调音高", fontSize = 11.sp, color = Color(0xFFE1306C), fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "${template.patternType.displayName} ${template.patternType.symbol}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
        }

        // Canvas 音高曲线绘制区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // ── 1. 绘制 3 条音高参考线 (High / Mid / Low) ─────
                val linePaint = Color(0xFF262626)
                drawLine(linePaint, Offset(0f, h * 0.2f), Offset(w, h * 0.2f), strokeWidth = 1f)
                drawLine(linePaint, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), strokeWidth = 1f)
                drawLine(linePaint, Offset(0f, h * 0.8f), Offset(w, h * 0.8f), strokeWidth = 1f)

                // ── 2. 绘制标准母语者虚线模板 (Target Template) ──
                if (templatePoints.isNotEmpty()) {
                    val targetPath = Path()
                    val stepX = w / (templatePoints.size - 1)

                    templatePoints.forEachIndexed { i, normPitch ->
                        val x = i * stepX
                        val y = h - (normPitch * h * 0.75f + h * 0.12f)
                        if (i == 0) {
                            targetPath.moveTo(x, y)
                        } else {
                            val prevX = (i - 1) * stepX
                            val prevY = h - (templatePoints[i - 1] * h * 0.75f + h * 0.12f)
                            val midX = (prevX + x) / 2f
                            targetPath.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }

                    drawPath(
                        path = targetPath,
                        color = Color(0xFF6B7280),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }

                // ── 3. 绘制用户跟读的平滑贝塞尔音高流线 (User Track) ──
                if (userPitchTrack.size >= 2) {
                    val userPath = Path()
                    val userStepX = w / (userPitchTrack.size - 1).coerceAtLeast(1)

                    userPitchTrack.forEachIndexed { i, uPitch ->
                        val x = i * userStepX
                        val y = h - (uPitch * h * 0.75f + h * 0.12f)
                        if (i == 0) {
                            userPath.moveTo(x, y)
                        } else {
                            val prevX = (i - 1) * userStepX
                            val prevY = h - (userPitchTrack[i - 1] * h * 0.75f + h * 0.12f)
                            val midX = (prevX + x) / 2f
                            userPath.cubicTo(midX, prevY, midX, y, x, y)
                        }
                    }

                    // 渐变实线
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF833AB4), Color(0xFFE1306C), Color(0xFFF59E0B)),
                        startX = 0f,
                        endX = w
                    )

                    drawPath(
                        path = userPath,
                        brush = gradient,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // 绘制终点发光圆球
                    val lastX = (userPitchTrack.size - 1) * userStepX
                    val lastY = h - (userPitchTrack.last() * h * 0.75f + h * 0.12f)
                    drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(lastX, lastY))
                    drawCircle(Color(0xFFE1306C), radius = 3.dp.toPx(), center = Offset(lastX, lastY))
                }
            }
        }

        // ── 4. 底部时间轴音节对齐 ──────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            syllables.forEach { syl ->
                Text(
                    text = syl,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (syl.contains("?") || syl.contains("!") || syl.contains(".")) Color(0xFFF59E0B) else Color(0xFFE5E7EB)
                )
            }
        }
    }
}
