package com.yonsei.koreanlearner.ui.screen.topik

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.algorithm.korean.TopikAbilityScores
import com.yonsei.koreanlearner.algorithm.korean.TopikQuestion
import com.yonsei.koreanlearner.ui.components.AbilityRadarChart
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * TOPIK 考点自适应自测与能力雷达评估界面 (TopikExamScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun TopikExamScreen(
    viewModel: TopikExamViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopikExamTopBar(
                isSubmitted = state.isSubmitted,
                currentIndex = state.currentQuestionIndex,
                totalCount = state.questions.size,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!state.isSubmitted) {
                // ── 1. 答题进行中卡片 ─────────────────────────────
                val currentQ = state.questions[state.currentQuestionIndex]
                val selectedOption = state.userAnswers[currentQ.id]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 题型标签
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentQ.type.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // 题干卡片
                    Card(
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                        modifier  = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = currentQ.questionStem,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 26.sp
                            )
                        }
                    }

                    // 4 个备选项
                    currentQ.options.forEachIndexed { optIndex, optText ->
                        val isSelected = selectedOption == optIndex
                        Surface(
                            shape  = RoundedCornerShape(12.dp),
                            color  = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.extended.cardBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.selectOption(currentQ.id, optIndex)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A'.code + optIndex).toChar()}.",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = optText,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // 底部导航/交卷按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (state.currentQuestionIndex > 0) {
                            OutlinedButton(
                                onClick = { viewModel.previousQuestion() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("上一题")
                            }
                        }

                        if (state.currentQuestionIndex + 1 < state.questions.size) {
                            Button(
                                onClick = { viewModel.nextQuestion() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("下一题 →")
                            }
                        } else {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.submitExam()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text("交卷评估 ", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // ── 2. 交卷后：能力雷达图与预估等级评估报告 ─────────
                TopikEvaluationReportContent(
                    scores = state.abilityScores,
                    questions = state.questions,
                    userAnswers = state.userAnswers,
                    onRestart = { viewModel.restartExam() }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 能力雷达图评估报告卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun TopikEvaluationReportContent(
    scores      : TopikAbilityScores,
    questions   : List<TopikQuestion>,
    userAnswers : Map<Int, Int>,
    onRestart   : () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 预估等级勋章
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(" TOPIK 综合预估等级", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = scores.estimatedLevel,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 五维能力雷达图
        Card(
            shape     = RoundedCornerShape(18.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(" 五维语言能力雷达图", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                AbilityRadarChart(scores = scores)
            }
        }

        // 错题考点深度解析折叠
        Text(" 试题深度考点解析：", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

        questions.forEachIndexed { index, q ->
            val userAns = userAnswers[q.id]
            val isCorrect = userAns == q.correctIndex

            Card(
                shape     = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border    = BorderStroke(1.dp, if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("第 ${index + 1} 题 · ${q.type.displayName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (isCorrect) "✓ 正确" else "✗ 错误",
                            color = if (isCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = q.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 重新自测按钮
        Button(
            onClick = onRestart,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("重新生成自测自适应卷 ", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TopikExamTopBar(
    isSubmitted  : Boolean,
    currentIndex : Int,
    totalCount   : Int,
    onBack       : (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TOPIK 考点自适应自测", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            if (!isSubmitted) {
                Text("第 ${currentIndex + 1} / $totalCount 题", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.width(36.dp))
    }
}
