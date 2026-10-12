package com.yonsei.koreanlearner.ui.screen.idiom

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.database.IdiomSeedData
import com.yonsei.koreanlearner.data.local.entity.IdiomEntity
import com.yonsei.koreanlearner.data.local.entity.IdiomType
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 俗语/四字成语/拟声拟态词专项学习模块界面 (IdiomModuleScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun IdiomModuleScreen(
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 词库画廊, 1: 情境测验
    var selectedFilterType by remember { mutableStateOf<IdiomType?>(null) } // null: 全部

    Scaffold(
        topBar = {
            IdiomTopBar(
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
                0 -> IdiomGalleryContent(
                    selectedFilter = selectedFilterType,
                    onFilterChanged = { selectedFilterType = it },
                    ttsManager = ttsManager
                )
                1 -> IdiomQuizContent(ttsManager = ttsManager)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 1. 词库画廊列表 Tab
// ══════════════════════════════════════════════════════════════════

@Composable
private fun IdiomGalleryContent(
    selectedFilter  : IdiomType?,
    onFilterChanged : (IdiomType?) -> Unit,
    ttsManager      : TtsManager?
) {
    val allIdioms = remember { IdiomSeedData.PRESET_IDIOMS }
    val displayList = remember(selectedFilter) {
        if (selectedFilter == null) allIdioms
        else allIdioms.filter { it.type == selectedFilter }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── 分类 FilterChips ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick  = { onFilterChanged(null) },
                label    = { Text("全部 (${allIdioms.size})") },
                shape    = RoundedCornerShape(10.dp)
            )

            IdiomType.entries.forEach { t ->
                val isSelected = selectedFilter == t
                val count = allIdioms.count { it.type == t }
                FilterChip(
                    selected = isSelected,
                    onClick  = { onFilterChanged(t) },
                    label    = { Text("${t.displayName} ($count)") },
                    shape    = RoundedCornerShape(10.dp)
                )
            }
        }

        // ── 瀑布流卡片列表 ─────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(displayList, key = { it.id }) { idiom ->
                IdiomStoryCard(
                    idiom = idiom,
                    ttsManager = ttsManager
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 2. 情境对号入座选择题测验 Tab
// ══════════════════════════════════════════════════════════════════

data class IdiomQuizItem(
    val id             : Int,
    val situationStory : String,            // 情境对话
    val correctAnswer  : String,            // 正确俗语/拟态词
    val options        : List<String>,      // 4个备选选项
    val explanation    : String             // 语法与文化解析
)

private val IDIOM_QUIZ_LIST = listOf(
    IdiomQuizItem(
        id = 1,
        situationStory = "A: 每天坚持背10个单词真的有用吗？\nB: 当然有用，韩语俗话说‘______’，日积月累就能掌握整套词库！",
        correctAnswer = "티끌 모아 태산",
        options = listOf("티끌 모아 태산", "쇠귀에 경 읽기", "갈수록 태산", "누워서 떡 먹기"),
        explanation = "‘티끌 모아 태산’（尘土汇聚成泰山）比喻积少成多，非常贴合每天坚持积累单词的语境。"
    ),
    IdiomQuizItem(
        id = 2,
        situationStory = "A: 刚刚面试结束，你感觉怎么样？\nB: 轮到我进去的时候，紧张得心脏‘______’狂跳个不停。",
        correctAnswer = "두근두근",
        options = listOf("두근두근", "차곡차곡", "다닥다닥", "따르릉"),
        explanation = "‘두근두근’是拟态词，专门生动形容心跳加速、紧张激动的生理心理状态。"
    ),
    IdiomQuizItem(
        id = 3,
        situationStory = "A: 无论我怎么好言相劝，他就是不改掉熬夜的坏习惯。\nB: 唉，对他这种固执的人说话真是‘______’。",
        correctAnswer = "쇠귀에 경 읽기",
        options = listOf("쇠귀에 경 읽기", "고진감래", "일석이조", "십시일반"),
        explanation = "‘쇠귀에 경 읽기’（给牛读经）相当于汉语的‘对牛弹琴’，形容劝说顽固之人毫无效果。"
    ),
    IdiomQuizItem(
        id = 4,
        situationStory = "A: 考研备考这一年真的太艰辛痛苦了。\nB: 别放弃，俗话说‘______’，熬过最苦的日子一定会迎来好成绩！",
        correctAnswer = "고진감래",
        options = listOf("고진감래", "갈수록 태산", "일석이조", "다닥다닥"),
        explanation = "‘고진감래’（苦尽甘来）是四字成语，用于勉励熬过艰难困苦迎接美好成果。"
    )
)

@Composable
private fun IdiomQuizContent(ttsManager: TtsManager?) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val currentQuiz = IDIOM_QUIZ_LIST[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度标头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "第 ${currentIndex + 1} / ${IDIOM_QUIZ_LIST.size} 题",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "得分: $score",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // 情境故事卡片
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = " 对话情境：",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentQuiz.situationStory,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 4个备选选项
        currentQuiz.options.forEach { opt ->
            val isSelected = selectedOption == opt
            val isCorrect = opt == currentQuiz.correctAnswer

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
                    .clickable(enabled = !isSubmitted) { selectedOption = opt }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = opt,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )

                    if (isSubmitted) {
                        if (isCorrect) Text("✓ 正确", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        else if (isSelected) Text("✗ 错误", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 提交 / 下一题
        if (!isSubmitted) {
            Button(
                onClick = {
                    if (selectedOption != null) {
                        isSubmitted = true
                        if (selectedOption == currentQuiz.correctAnswer) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            score += 25
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                },
                enabled = selectedOption != null,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("提交答案", fontWeight = FontWeight.Bold)
            }
        } else {
            // 解析卡片
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(" 文化与语境解析：", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(currentQuiz.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    if (currentIndex + 1 < IDIOM_QUIZ_LIST.size) {
                        currentIndex++
                        selectedOption = null
                        isSubmitted = false
                    } else {
                        currentIndex = 0
                        selectedOption = null
                        isSubmitted = false
                        score = 0
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (currentIndex + 1 < IDIOM_QUIZ_LIST.size) "下一题 →" else "重新测试 ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 3. 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun IdiomTopBar(
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
                text = "俗语 · 成语 · 拟声拟态词",
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
                text = { Text(" 词库画廊", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text(" 情境对号入座", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}
