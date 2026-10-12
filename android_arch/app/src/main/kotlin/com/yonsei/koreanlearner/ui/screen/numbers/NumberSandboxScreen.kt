package com.yonsei.koreanlearner.ui.screen.numbers

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.korean.KoreanNumberClassifierEngine
import com.yonsei.koreanlearner.algorithm.korean.KoreanNumberClassifierEngine.Classifier
import com.yonsei.koreanlearner.algorithm.korean.KoreanNumberClassifierEngine.NumberSystemType
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语数词与量词交互沙盒与测验界面（NumberSandboxScreen）
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun NumberSandboxScreen(
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: 实时沙盒, 1: 随堂测验

    Scaffold(
        topBar = {
            NumberTopBar(
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
                0 -> NumberSandboxContent(ttsManager = ttsManager)
                1 -> ClassifierQuizContent(ttsManager = ttsManager)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏与模式 Tab
// ══════════════════════════════════════════════════════════════════

@Composable
private fun NumberTopBar(
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
                text = "韩语数词与量词双轨系统",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.width(36.dp))
        }

        Spacer(Modifier.height(10.dp))

        // 顶层 2 档 Tab 切换
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text(" 实时转换沙盒", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text(" 量词随堂测验", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Tab 0: 实时转换沙盒
// ══════════════════════════════════════════════════════════════════

enum class SandboxScenario(val displayName: String, val icon: String) {
    TIME("时间钟点", ""),
    QUANTITY("数量量词", ""),
    AGE("年龄岁数", ""),
    PRICE("韩元金额", ""),
    DATE("年月日", "")
}

@Composable
private fun NumberSandboxContent(ttsManager: TtsManager?) {
    var scenario by remember { mutableStateOf(SandboxScenario.TIME) }

    // 状态量
    var hour by remember { mutableIntStateOf(14) }
    var minute by remember { mutableIntStateOf(35) }

    var selectedClassifier by remember { mutableStateOf(Classifier.GWON) }
    var quantityAmount by remember { mutableIntStateOf(3) }

    var ageAmount by remember { mutableIntStateOf(21) }
    var priceAmount by remember { mutableIntStateOf(45000) }

    var year by remember { mutableIntStateOf(2026) }
    var month by remember { mutableIntStateOf(6) }
    var day by remember { mutableIntStateOf(15) }

    // 实时计算当前场景的转换结果
    val conversionResult = remember(
        scenario, hour, minute, selectedClassifier, quantityAmount,
        ageAmount, priceAmount, year, month, day
    ) {
        when (scenario) {
            SandboxScenario.TIME ->
                KoreanNumberClassifierEngine.convertTime(hour, minute)

            SandboxScenario.QUANTITY ->
                KoreanNumberClassifierEngine.convertQuantity(quantityAmount, selectedClassifier)

            SandboxScenario.AGE ->
                KoreanNumberClassifierEngine.convertQuantity(ageAmount, Classifier.SAL)

            SandboxScenario.PRICE ->
                KoreanNumberClassifierEngine.convertQuantity(priceAmount, Classifier.WON)

            SandboxScenario.DATE ->
                KoreanNumberClassifierEngine.convertDate(year, month, day)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 场景切换 FilterChips ──────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SandboxScenario.entries.forEach { sc ->
                val isSelected = sc == scenario
                FilterChip(
                    selected = isSelected,
                    onClick = { scenario = sc },
                    label = { Text("${sc.icon} ${sc.displayName}") },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // ── 实时结果展示大卡片 ─────────────────────────────────
        ResultDisplayCard(result = conversionResult, ttsManager = ttsManager)

        // ── 参数调节器（根据场景动态展示） ─────────────────────
        Surface(
            color  = MaterialTheme.colorScheme.surface,
            shape  = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "⚙️ 参数调节控制台",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                when (scenario) {
                    SandboxScenario.TIME -> {
                        SliderRow(label = "小时 ($hour 时)", value = hour.toFloat(), range = 0f..23f) { hour = it.toInt() }
                        SliderRow(label = "分钟 ($minute 分)", value = minute.toFloat(), range = 0f..59f) { minute = it.toInt() }
                    }

                    SandboxScenario.QUANTITY -> {
                        Text("选择量词：", style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Classifier.GWON, Classifier.GAE, Classifier.JAN,
                                Classifier.BYEONG, Classifier.JANG, Classifier.MARI,
                                Classifier.MYEONG, Classifier.BEOL, Classifier.GEUREUT
                            ).forEach { cl ->
                                FilterChip(
                                    selected = cl == selectedClassifier,
                                    onClick = { selectedClassifier = cl },
                                    label = { Text("${cl.korean} (${cl.chineseMeaning.take(1)})") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                        SliderRow(label = "数量 ($quantityAmount)", value = quantityAmount.toFloat(), range = 1f..30f) { quantityAmount = it.toInt() }
                    }

                    SandboxScenario.AGE -> {
                        SliderRow(label = "年龄 ($ageAmount 岁)", value = ageAmount.toFloat(), range = 1f..99f) { ageAmount = it.toInt() }
                    }

                    SandboxScenario.PRICE -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1000, 4500, 10000, 45000, 120000, 500000).forEach { p ->
                                FilterChip(
                                    selected = p == priceAmount,
                                    onClick = { priceAmount = p },
                                    label = { Text("₩$p") },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    SandboxScenario.DATE -> {
                        SliderRow(label = "月份 ($month 月)", value = month.toFloat(), range = 1f..12f) { month = it.toInt() }
                        SliderRow(label = "日期 ($day 日)", value = day.toFloat(), range = 1f..31f) { day = it.toInt() }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultDisplayCard(
    result: KoreanNumberClassifierEngine.KoreanNumberResult,
    ttsManager: TtsManager?
) {
    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 系统标签
                Surface(
                    color = when (result.systemType) {
                        NumberSystemType.NATIVE -> MaterialTheme.colorScheme.primaryContainer
                        NumberSystemType.SINO -> MaterialTheme.colorScheme.tertiaryContainer
                        NumberSystemType.HYBRID -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result.systemType.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (ttsManager != null) {
                    IconButton(
                        onClick = { ttsManager.speak(result.koreanFullText) },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("", fontSize = 16.sp)
                    }
                }
            }

            // 韩文大字展示
            Text(
                text       = result.koreanFullText,
                fontSize   = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onSurface
            )

            // 发音与输入
            Text(
                text = "发音音标: ${result.pronunciation}  (输入: ${result.rawInput})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            // 规则解析说明
            Text(
                text = " 语言学解析: ${result.ruleExplanation}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = (range.endInclusive - range.start).toInt() - 1
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// Tab 1: 量词随堂测验挑战
// ══════════════════════════════════════════════════════════════════

data class NumberQuizQuestion(
    val id: Int,
    val promptQuestion: String,
    val correctKorean: String,
    val options: List<String>,
    val explanation: String
)

private val QUIZ_QUESTIONS = listOf(
    NumberQuizQuestion(
        id = 1,
        promptQuestion = "请问“3 杯咖啡”的正确韩语表达是？",
        correctKorean = "세 잔",
        options = listOf("세 잔", "삼 잔", "셋 잔", "세 개"),
        explanation = "量词 '잔(杯)' 必须接固有词，且 3(셋) 在量词前缩略为 '세'。"
    ),
    NumberQuizQuestion(
        id = 2,
        promptQuestion = "请问“10 月 15 日”的正确韩语读法是？",
        correctKorean = "시월 십오 일",
        options = listOf("시월 십오 일", "십월 십오 일", "열월 십오 일", "시월 열다섯 일"),
        explanation = "年月日使用汉字词，且 10 月发生音变收音脱落读作 '시월'。"
    ),
    NumberQuizQuestion(
        id = 3,
        promptQuestion = "请问“20 岁”的正确韩语表达是？",
        correctKorean = "스무 살",
        options = listOf("스무 살", "이십 살", "스물 살", "스물한 살"),
        explanation = "年龄量词 '살' 接固有词，且 20(스물) 遇量词变形为 '스무'。"
    ),
    NumberQuizQuestion(
        id = 4,
        promptQuestion = "请问“下午 2:30”的正确韩语读法是？",
        correctKorean = "오후 두 시 삼십 분",
        options = listOf("오후 두 시 삼십 분", "오후 이 시 삼십 분", "오후 두 시 서른 분", "오후 둘 시 삼십 분"),
        explanation = "时间表达中，小时接固有词 (2->두)，分钟接汉字词 (30->삼십)。"
    ),
    NumberQuizQuestion(
        id = 5,
        promptQuestion = "请问“14 本书”的正确韩语表达是？",
        correctKorean = "열네 권",
        options = listOf("열네 권", "십사 권", "열넷 권", "십네 권"),
        explanation = "量词 '권(本)' 接固有词，14(열넷) 末尾 4 缩略为 '열네'。"
    )
)

@Composable
private fun ClassifierQuizContent(ttsManager: TtsManager?) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    val currentQuestion = QUIZ_QUESTIONS[currentIndex]

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
            Text(
                text = "第 ${currentIndex + 1} / ${QUIZ_QUESTIONS.size} 题",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "当前得分: $score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 题目卡片
        Card(
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = currentQuestion.promptQuestion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 选项列表
        currentQuestion.options.forEach { option ->
            val isSelected = selectedOption == option
            val isCorrect = option == currentQuestion.correctKorean

            val borderColor = when {
                !isSubmitted && isSelected -> MaterialTheme.colorScheme.primary
                isSubmitted && isCorrect -> Color(0xFF10B981) // 绿色
                isSubmitted && isSelected && !isCorrect -> Color(0xFFEF4444) // 红色
                else -> MaterialTheme.extended.cardBorder
            }

            Surface(
                color  = MaterialTheme.colorScheme.surface,
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isSubmitted) { selectedOption = option }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )

                    if (isSubmitted) {
                        if (isCorrect) Text("✓ 正确", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        else if (isSelected) Text("✗ 错误", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 提交或下一题按钮
        if (!isSubmitted) {
            Button(
                onClick = {
                    if (selectedOption != null) {
                        isSubmitted = true
                        if (selectedOption == currentQuestion.correctKorean) score += 20
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
                    Text(" 解析：", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(currentQuestion.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Button(
                onClick = {
                    if (currentIndex + 1 < QUIZ_QUESTIONS.size) {
                        currentIndex++
                        selectedOption = null
                        isSubmitted = false
                    } else {
                        // 重置
                        currentIndex = 0
                        selectedOption = null
                        isSubmitted = false
                        score = 0
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (currentIndex + 1 < QUIZ_QUESTIONS.size) "下一题 →" else "重新测试 ", fontWeight = FontWeight.Bold)
            }
        }
    }
}
