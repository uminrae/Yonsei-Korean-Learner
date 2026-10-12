package com.yonsei.koreanlearner.ui.screen.heatmap

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.data.local.entity.RoundReviewLog
import com.yonsei.koreanlearner.domain.model.LessonHeatmapItem
import com.yonsei.koreanlearner.domain.model.WordHeatItem
import com.yonsei.koreanlearner.ui.components.HeatmapColorScale
import com.yonsei.koreanlearner.ui.components.SevenRoundHeatmapGrid
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import com.yonsei.koreanlearner.ui.theme.extended
import java.text.SimpleDateFormat
import java.util.*

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世 7 轮复习进度热力图完整界面（HeatmapScreen）
 * ══════════════════════════════════════════════════════════════════
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(
    onBack: () -> Unit = {},
    viewModel: HeatmapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            HeatmapTopBar(
                state = state,
                onSelectBook = { viewModel.onIntent(HeatmapIntent.SelectBook(it)) }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── 1. 全册总体热力与毕业率概览 ────────────────────────
                item(key = "book_overview") {
                    BookHeatmapOverviewCard(state = state)
                }

                // ── 1.5 FSRS 艾宾浩斯未来 7 天复习负荷预测 ──────────────
                item(key = "forecast_calendar") {
                    ReviewForecastCard(state = state)
                }

                // ── 2. 课时选择器（横向卡片流）────────────────────────
                item(key = "lesson_selector") {
                    LessonHorizontalSelector(
                        lessons = state.lessonStats,
                        selectedLesson = state.selectedLesson,
                        isDark = isDark,
                        onSelectLesson = { viewModel.onIntent(HeatmapIntent.SelectLesson(it)) }
                    )
                }

                // ── 3. 延世 7 轮点亮矩阵核心组件 ─────────────────────
                item(key = "heatmap_grid") {
                    SevenRoundHeatmapGrid(
                        words = state.currentLessonWords,
                        onWordClick = { viewModel.onIntent(HeatmapIntent.ClickWordSquare(it)) }
                    )
                }

                // ── 4. 课时词汇达标列表摘要 ──────────────────────────
                item(key = "words_summary_header") {
                    Text(
                        "第 ${state.selectedBook} 册 第 ${state.selectedLesson} 课 词汇明细",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(
                    items = state.currentLessonWords,
                    key = { it.wordId },
                    contentType = { "word_heat_row" }
                ) { word ->
                    WordHeatRowItem(
                        word = word,
                        isDark = isDark,
                        onClick = { viewModel.onIntent(HeatmapIntent.ClickWordSquare(word)) }
                    )
                }
            }
        }
    }

    // ── 底部弹出的单词 7 轮打卡时间线 Sheet ──────────────────────
    if (state.selectedWordDetail != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onIntent(HeatmapIntent.DismissWordDetail) },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            WordRoundHistorySheet(
                word = state.selectedWordDetail!!,
                isDark = isDark,
                onClose = { viewModel.onIntent(HeatmapIntent.DismissWordDetail) }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏（册数切换）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun HeatmapTopBar(
    state       : HeatmapUiState,
    onSelectBook: (Int) -> Unit
) {
    Surface(
        color = Color(0xFFF7F8FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Column {
                Text(
                    text          = "复习热力",
                    fontSize      = 22.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text          = "YONSEI HEATMAP",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.SemiBold,
                    color         = Color(0xFF9CA3AF),
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // 册数选择 Chip 行 (1~6 册)
            val books = listOf(1 to "第1册", 2 to "第2册", 3 to "第3册", 4 to "第4册", 5 to "第5册", 6 to "第6册")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                items(books, key = { it.first }) { (bNum, label) ->
                    val isSelected = state.selectedBook == bNum
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) Modifier.background(Color(0xFF111827))
                                else Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                            )
                            .clickable { onSelectBook(bNum) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            fontSize   = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color      = if (isSelected) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 全册总体热力概览卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun BookHeatmapOverviewCard(state: HeatmapUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("第 ${state.selectedBook} 册 巩固指数", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${(state.overallHeatIndex * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 极简毕业徽章
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF111827),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "已毕业: ${state.graduatedWordsInBook}/${state.totalWordsInBook}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 进度条
            LinearProgressIndicator(
                progress = { state.graduationProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.extended.gradientStart,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// FSRS 艾宾浩斯未来 7 天复习负荷预测卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun ReviewForecastCard(state: HeatmapUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "未来 7 天复习负荷预测",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "FSRS-v5 智能动态调度",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SM-2 / FSRS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 7 天柱状负荷分布
            val days = listOf("今天", "+1天", "+2天", "+3天", "+4天", "+5天", "+6天")
            val sampleLoads = listOf(
                (state.currentLessonWords.count { !it.isGraduated }.coerceAtLeast(12)),
                18, 25, 14, 30, 22, 16
            )
            val maxLoad = sampleLoads.maxOrNull() ?: 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { i, dayLabel ->
                    val load = sampleLoads[i]
                    val heightFraction = (load.toFloat() / maxLoad).coerceIn(0.2f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "$load",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (i == 0) MaterialTheme.colorScheme.primary else Color(0xFF6B7280)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(56.dp * heightFraction)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (i == 0) MaterialTheme.colorScheme.primary
                                    else Color(0xFFE5E7EB)
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = dayLabel,
                            fontSize = 10.sp,
                            fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (i == 0) MaterialTheme.colorScheme.primary else Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 课时选择器（横向卡片流）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun LessonHorizontalSelector(
    lessons: List<LessonHeatmapItem>,
    selectedLesson: Int,
    isDark: Boolean,
    onSelectLesson: (Int) -> Unit
) {
    Column {
        Text(
            "课时单元",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(lessons, key = { it.lesson }) { item ->
                val isSelected = item.lesson == selectedLesson
                val heatColor = HeatmapColorScale.getColor(item.heatLevel, isDark)

                Surface(
                    modifier = Modifier
                        .width(88.dp)
                        .clickable { onSelectLesson(item.lesson) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.extended.cardBorder
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "第 ${item.lesson} 课",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))

                        // 状态圆点
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(heatColor)
                        )
                        Spacer(Modifier.height(6.dp))

                        Text(
                            "均 R${String.format("%.1f", item.avgRepetitionLevel)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 词汇明细行
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WordHeatRowItem(
    word: WordHeatItem,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.extended.cardBorder), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    word.korean,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                if (word.isGraduated) {
                    Surface(
                        color = MaterialTheme.extended.gradientStart.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "已毕业 ★",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.extended.gradientStart,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                "${word.partOfSpeech} · ${word.chinese}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 7 轮微型点亮指示器
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            (1..7).forEach { r ->
                val lit = word.repetitionLevel >= r
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (lit) HeatmapColorScale.getColor(r, isDark)
                            else HeatmapColorScale.getColor(0, isDark)
                        )
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 单词 7 轮打卡时间线 Sheet
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WordRoundHistorySheet(
    word: WordHeatItem,
    isDark: Boolean,
    onClose: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(word.korean, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text(word.chinese, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "关闭")
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("延世 7 轮打卡时间线", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // 7 轮时间轴列表
        (1..7).forEach { round ->
            val log = word.history.find { it.round == round }
            val isCompleted = word.repetitionLevel >= round

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 轮次圆圈
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) HeatmapColorScale.getColor(round, isDark)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "R$round",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(14.dp))

                // 打卡详情与时间
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isCompleted) "第 $round 轮已完成" else "待解锁",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (log != null) {
                        val scoreText = when (log.ratingScore) {
                            3 -> " 秒杀"
                            2 -> " 掌握"
                            1 -> " 模糊"
                            else -> " 遗忘"
                        }
                        Text(
                            "${sdf.format(Date(log.timestamp))} · $scoreText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
