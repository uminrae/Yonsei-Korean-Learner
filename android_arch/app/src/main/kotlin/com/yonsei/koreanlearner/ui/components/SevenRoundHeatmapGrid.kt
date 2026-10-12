package com.yonsei.koreanlearner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.domain.model.WordHeatItem
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 8 级 Ins 高级纯白灰黑阶（从 Level 0 极浅灰 到 Level 7 深邃纯黑）
 * ══════════════════════════════════════════════════════════════════
 */
object HeatmapColorScale {
    val LightScale = listOf(
        Color(0xFFF3F4F6), // Level 0: 未开始
        Color(0xFFE5E7EB), // Level 1
        Color(0xFFD1D5DB), // Level 2
        Color(0xFF9CA3AF), // Level 3
        Color(0xFF6B7280), // Level 4
        Color(0xFF4B5563), // Level 5
        Color(0xFF374151), // Level 6
        Color(0xFF111827)  // Level 7: 毕业（极简纯黑高对比）
    )

    fun getColor(level: Int, isDark: Boolean = false): Color {
        return LightScale.getOrElse(level.coerceIn(0, 7)) { LightScale[0] }
    }
}

/**
 * ══════════════════════════════════════════════════════════════════
 * SevenRoundHeatmapGrid — 延世 7 轮复习进度热力矩阵 (120fps 满血高性能版)
 * 移除无限动画，纯静态 GPU 批量绘制，零滚动重绘开销
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SevenRoundHeatmapGrid(
    words: List<WordHeatItem>,
    onWordClick: (WordHeatItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (words.isEmpty()) {
        EmptyHeatmapPlaceholder(modifier)
        return
    }

    val scrollState = rememberScrollState()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ── 标题与图例行 ─────────────────────────────────────
            HeatmapHeader()

            Spacer(Modifier.height(16.dp))

            // ── 7 轮方块矩阵（左侧轮次标签 + 右侧横向平滑滚动网格） ─
            Row(modifier = Modifier.fillMaxWidth()) {
                // 左侧轮次标签列 (第 1~7 轮)
                RoundLabelsColumn()

                Spacer(Modifier.width(10.dp))

                // 右侧矩阵网格（支持横向惯性平滑滑动）
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        words.forEach { wordItem ->
                            key(wordItem.wordId) {
                                WordColumnTrack(
                                    word = wordItem,
                                    onWordClick = { onWordClick(wordItem) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 底部色阶图例 ─────────────────────────────────────
            HeatmapLegend()
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 矩阵单列（对应 1 个单词的 7 轮方块垂直堆叠，零动画极速渲染）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WordColumnTrack(
    word: WordHeatItem,
    onWordClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onWordClick)
    ) {
        // 7 行方块：从第 1 轮 到 第 7 轮
        (1..7).forEach { round ->
            val isCurrentOrPassedRound = word.repetitionLevel >= round
            val isGraduatedRound = word.isGraduated && round == 7

            val squareColor = when {
                isGraduatedRound -> Color(0xFF111827)
                isCurrentOrPassedRound -> HeatmapColorScale.getColor(word.repetitionLevel)
                else -> Color(0xFFF3F4F6)
            }

            RoundSquareItem(
                color = squareColor,
                isGraduatedRound = isGraduatedRound
            )
        }
    }
}

@Composable
private fun RoundSquareItem(
    color: Color,
    isGraduatedRound: Boolean
) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .then(
                if (isGraduatedRound) Modifier.border(1.dp, Color(0xFF111827), RoundedCornerShape(4.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isGraduatedRound) {
            Text("★", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 左侧轮次标签 (第1轮 ~ 第7轮)
// ══════════════════════════════════════════════════════════════════

@Composable
private fun RoundLabelsColumn() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        (1..7).forEach { round ->
            Box(
                modifier = Modifier.height(20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (round == 7) "第7轮" else "第${round}轮",
                    fontSize = 10.sp,
                    color = if (round == 7) Color(0xFF111827) else Color(0xFF9CA3AF),
                    fontWeight = if (round == 7) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 头部与图例
// ══════════════════════════════════════════════════════════════════

@Composable
private fun HeatmapHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "延世 7 轮点亮矩阵",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "横向滑动查看各单词打卡深度",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF)
            )
        }

        // 毕业徽章
        Surface(
            color = Color(0xFFF3F4F6),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("第7轮 永久记忆", fontSize = 11.sp, color = Color(0xFF111827), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("未开始", fontSize = 10.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.width(6.dp))

        (0..7).forEach { lvl ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HeatmapColorScale.getColor(lvl))
            )
            Spacer(Modifier.width(3.dp))
        }

        Spacer(Modifier.width(3.dp))
        Text("第7轮 毕业", fontSize = 10.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyHeatmapPlaceholder(modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "暂无当前单元复习数据",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
        }
    }
}
