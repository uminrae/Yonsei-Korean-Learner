package com.yonsei.koreanlearner.ui.screen.discourse

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.algorithm.korean.DiscourseCategory
import com.yonsei.koreanlearner.algorithm.korean.DiscourseMarker
import com.yonsei.koreanlearner.algorithm.korean.DiscourseMarkerDatabase
import com.yonsei.koreanlearner.algorithm.korean.DiscourseRegister
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 篇章逻辑接续词与转折矩阵界面 (DiscourseMatrixScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun DiscourseMatrixScreen(
    viewModel: DiscourseMatrixViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 逻辑矩阵画廊, 1: 完形填空测验

    Scaffold(
        topBar = {
            DiscourseTopBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> DiscourseGalleryView(
                    markers = state.markers,
                    selectedCategory = state.selectedCategory,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    ttsManager = ttsManager
                )
                1 -> DiscourseClozeQuizView()
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 1. 逻辑矩阵画廊 Tab
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DiscourseGalleryView(
    markers          : List<DiscourseMarker>,
    selectedCategory : DiscourseCategory?,
    onSelectCategory : (DiscourseCategory?) -> Unit,
    ttsManager       : TtsManager?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 分类 FilterChips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick  = { onSelectCategory(null) },
                label    = { Text("全部") },
                shape    = RoundedCornerShape(10.dp)
            )

            DiscourseCategory.entries.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick  = { onSelectCategory(cat) },
                    label    = { Text(cat.displayName.substringBefore(" ")) },
                    shape    = RoundedCornerShape(10.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(markers, key = { it.id }) { marker ->
                DiscourseMarkerCard(
                    marker = marker,
                    onSpeak = { ttsManager?.speak(marker.korean) }
                )
            }
        }
    }
}

@Composable
private fun DiscourseMarkerCard(
    marker  : DiscourseMarker,
    onSpeak : () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 顶栏：接续词大字 + 语体 Badge + 发音
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = marker.korean,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Text("", fontSize = 12.sp)
                    }
                }

                Surface(
                    color = when (marker.register) {
                        DiscourseRegister.WRITTEN -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                        DiscourseRegister.SPOKEN -> Color(0xFF10B981).copy(alpha = 0.15f)
                        DiscourseRegister.NEUTRAL -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = marker.register.displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (marker.register) {
                            DiscourseRegister.WRITTEN -> Color(0xFF2563EB)
                            DiscourseRegister.SPOKEN -> Color(0xFF059669)
                            DiscourseRegister.NEUTRAL -> Color(0xFF7C3AED)
                        }
                    )
                }
            }

            Text(
                text = "含义: ${marker.chinese}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // 语感强弱条 (Intensity Bar)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("转折/连接语感强度：", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(marker.intensityLevel * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(marker.intensityLevel)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }

            // 语法限制条件胶囊
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape  = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("⚠️ 句式约束与语用：", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(marker.constraints.joinToString("; "), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                }
            }

            // 典型例句
            marker.examples.firstOrNull()?.let { ex ->
                Text(" 例: ${ex.koreanSentence}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text("    ${ex.chineseSentence}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 2. 上下文逻辑完形填空测验 Tab
// ══════════════════════════════════════════════════════════════════

data class DiscourseQuizItem(
    val id           : Int,
    val priorClause  : String,              // 前文句子
    val postClause   : String,              // 后文句子
    val correctMarker: String,              // 正确接续词
    val options      : List<String>,        // 4个选项
    val explanation  : String               // 语用辨析
)

private val DISCOURSE_QUIZ_LIST = listOf(
    DiscourseQuizItem(
        id = 1,
        priorClause = "밖은 날씨가 매우 춥습니다.",
        postClause = "옷을 따뜻하게 입고 나가세요.",
        correctMarker = "그러니까",
        options = listOf("그러니까", "따라서", "그래서", "그러나"),
        explanation = "后句 '나가세요' 是命令句型，客观因果连词 '따라서' 与 '그래서' 严禁后接命令句，必须使用包含主观要求与劝诱的 '그러니까'！"
    ),
    DiscourseQuizItem(
        id = 2,
        priorClause = "비가 많이 내렸습니다.",
        postClause = "바람까지 심하게 불어 우산이 뒤집혔습니다.",
        correctMarker = "게다가",
        options = listOf("게다가", "그러나", "반면에", "오히려"),
        explanation = "前句下大雨，后句追加刮大风导致雨伞翻了，属于消极情况的雪上加霜递进强化，应选用 '게다가'。"
    ),
    DiscourseQuizItem(
        id = 3,
        priorClause = "감기약을 먹었습니다.",
        postClause = "열이 내리지 않고 더 심해졌습니다.",
        correctMarker = "오히려",
        options = listOf("오히려", "그리고", "따라서", "한편"),
        explanation = "吃药后的常理预期是退烧，实际结果却截然相反加重了，属于出乎意料的戏剧性反转，首选 '오히려'。"
    )
)

@Composable
private fun DiscourseClozeQuizView() {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedMarker by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val currentQuiz = DISCOURSE_QUIZ_LIST[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度与得分
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("第 ${currentIndex + 1} / ${DISCOURSE_QUIZ_LIST.size} 题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("得分: $score", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        // 上下文语境卡片
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(" 请为上下文语境选择最得体的篇章接续词：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                Text(
                    text = "${currentQuiz.priorClause}\n[  ${selectedMarker ?: " ? "}  ]  ${currentQuiz.postClause}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
            }
        }

        // 4 个备选接续词
        currentQuiz.options.forEach { opt ->
            val isSelected = selectedMarker == opt
            val isCorrect = opt == currentQuiz.correctMarker

            val borderColor = when {
                !isSubmitted && isSelected -> MaterialTheme.colorScheme.primary
                isSubmitted && isCorrect -> Color(0xFF10B981)
                isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444)
                else -> MaterialTheme.extended.cardBorder
            }

            Surface(
                color  = MaterialTheme.colorScheme.surface,
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSubmitted) { selectedMarker = opt }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(opt, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)

                    if (isSubmitted) {
                        if (isCorrect) Text("✓ 正确", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        else if (isSelected) Text("✗ 错误", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 提交与解析
        if (!isSubmitted) {
            Button(
                onClick = {
                    if (selectedMarker != null) {
                        isSubmitted = true
                        if (selectedMarker == currentQuiz.correctMarker) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            score += 33
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                },
                enabled = selectedMarker != null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("提交答案", fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(" 句式约束与语气差异解析：", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(currentQuiz.explanation, style = MaterialTheme.typography.bodyMedium, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            Button(
                onClick = {
                    if (currentIndex + 1 < DISCOURSE_QUIZ_LIST.size) {
                        currentIndex++
                        selectedMarker = null
                        isSubmitted = false
                    } else {
                        currentIndex = 0
                        selectedMarker = null
                        isSubmitted = false
                        score = 0
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (currentIndex + 1 < DISCOURSE_QUIZ_LIST.size) "下一题 →" else "重新测试 ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DiscourseTopBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onBack: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Text("←", fontSize = 20.sp)
                }
            } else {
                Spacer(Modifier.width(36.dp))
            }

            Text(
                text = "篇章逻辑接续词与转折矩阵",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.width(36.dp))
        }

        Spacer(Modifier.height(8.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text(" 逻辑矩阵画廊", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text(" 完形填空测验", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}
