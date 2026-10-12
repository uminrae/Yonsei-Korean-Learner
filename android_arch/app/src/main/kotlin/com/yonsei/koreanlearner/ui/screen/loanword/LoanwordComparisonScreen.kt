package com.yonsei.koreanlearner.ui.screen.loanword

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
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.database.LoanwordSeedData
import com.yonsei.koreanlearner.data.local.entity.LoanwordEntity
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语外来语与传统词汇双轨对照界面 (LoanwordComparisonScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun LoanwordComparisonScreen(
    viewModel: LoanwordViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 双轨画廊, 1: 听力匹配测验

    Scaffold(
        topBar = {
            LoanwordTopBar(
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
                0 -> LoanwordGalleryView(
                    loanwords = state.loanwords,
                    selectedCategory = state.selectedCategory,
                    onSelectCategory = { viewModel.selectCategory(it) },
                    ttsManager = ttsManager
                )
                1 -> LoanwordListeningQuizView(ttsManager = ttsManager)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 1. 双轨对照画廊 Tab
// ══════════════════════════════════════════════════════════════════

@Composable
private fun LoanwordGalleryView(
    loanwords        : List<LoanwordEntity>,
    selectedCategory : String?,
    onSelectCategory : (String?) -> Unit,
    ttsManager       : TtsManager?
) {
    val categories = remember {
        listOf("生活日常", "科技数码", "饮食健康", "韩式英语Konglish")
    }

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

            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick  = { onSelectCategory(cat) },
                    label    = { Text(cat) },
                    shape    = RoundedCornerShape(10.dp)
                )
            }
        }

        // 瀑布流双栏对照卡片
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(loanwords, key = { it.id }) { item ->
                LoanwordDualCard(
                    item = item,
                    onSpeak = { ttsManager?.speak(item.loanword) }
                )
            }
        }
    }
}

@Composable
private fun LoanwordDualCard(
    item    : LoanwordEntity,
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
            // 顶部分类与释义
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = item.chineseMeaning,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // ── 双轨对照分栏 ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 左侧：外来语 (Loanword)
                Surface(
                    color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(" 外来语", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            IconButton(onClick = onSpeak, modifier = Modifier.size(22.dp)) {
                                Text("", fontSize = 10.sp)
                            }
                        }
                        Text(item.loanword, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(item.foreignOrigin, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                    }
                }

                // 右侧：纯正韩语 / 汉字词 (Native / Sino-Korean)
                Surface(
                    color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(" 纯正韩语 / 汉字词", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Text(item.nativeKoreanEquivalent, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("标准规范表达", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }

            // 例句展示
            Text(
                text = " 例: ${item.exampleKorean}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "    ${item.exampleChinese}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 2. 地道外来语听力与匹配测验 Tab
// ══════════════════════════════════════════════════════════════════

@Composable
private fun LoanwordListeningQuizView(ttsManager: TtsManager?) {
    val quizItems = remember { LoanwordSeedData.PRESET_LOANWORDS.shuffled() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableIntStateOf(-1) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val currentTarget = quizItems[currentIndex]

    // 4 个备选选项
    val options = remember(currentIndex) {
        val others = LoanwordSeedData.PRESET_LOANWORDS.filter { it.id != currentTarget.id }.shuffled().take(3)
        (others + currentTarget).shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度与得分
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("第 ${currentIndex + 1} / ${quizItems.size} 题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("得分: $score", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }

        // 发音收听大卡片
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(" 请听韩式外来语发音：", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        ttsManager?.speak(currentTarget.loanword)
                    },
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text("", fontSize = 28.sp)
                }

                Text("点击喇叭播放发音，并选出对应的外语原型与汉字词：", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // 4 个选项
        options.forEach { opt ->
            val isSelected = selectedId == opt.id
            val isCorrect = opt.id == currentTarget.id

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
                    .clickable(enabled = !isSubmitted) { selectedId = opt.id }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${opt.loanword} (${opt.foreignOrigin})", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("对应纯正词: ${opt.nativeKoreanEquivalent} · ${opt.chineseMeaning}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (isSubmitted) {
                        if (isCorrect) Text("✓ 正确", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        else if (isSelected) Text("✗ 错误", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // 提交 / 下一题
        if (!isSubmitted) {
            Button(
                onClick = {
                    if (selectedId != -1) {
                        isSubmitted = true
                        if (selectedId == currentTarget.id) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            score += 20
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                },
                enabled = selectedId != -1,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("提交答案", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = {
                    if (currentIndex + 1 < quizItems.size) {
                        currentIndex++
                        selectedId = -1
                        isSubmitted = false
                    } else {
                        currentIndex = 0
                        selectedId = -1
                        isSubmitted = false
                        score = 0
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (currentIndex + 1 < quizItems.size) "下一题 →" else "重新测试 ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LoanwordTopBar(
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
                text = "外来语与传统词汇双轨对照",
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
                text = { Text(" 双轨画廊", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text(" 听力匹配测验", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}
