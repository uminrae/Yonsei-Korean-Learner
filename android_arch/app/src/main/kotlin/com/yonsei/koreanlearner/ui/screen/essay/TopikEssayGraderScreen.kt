package com.yonsei.koreanlearner.ui.screen.essay

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.essay.TopikEssayEvaluator
import com.yonsei.koreanlearner.algorithm.essay.TopikPrompt

/**
 * ══════════════════════════════════════════════════════════════════
 * TOPIK 54 大作文智能诊断与批改 (TopikEssayGraderScreen)
 * 纯白极简 Ins 风格，实时字数/文体基本阶/篇章结构全自动诊断
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun TopikEssayGraderScreen(
    onBack: () -> Unit = {}
) {
    var selectedPromptIndex by remember { mutableIntStateOf(0) }
    val currentPrompt = TopikEssayEvaluator.PRESET_PROMPTS[selectedPromptIndex]
    var essayText by remember { mutableStateOf(currentPrompt.modelEssay) }
    var showModelEssayDialog by remember { mutableStateOf(false) }

    val report = remember(essayText) {
        TopikEssayEvaluator.evaluate(essayText)
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            TopikEssayTopBar(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. 真题切换 Chip ────────────────────────────────
            item(key = "prompt_selector") {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "经典真题题目",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(6.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TopikEssayEvaluator.PRESET_PROMPTS.indices.toList(), key = { it }) { index ->
                            val isSelected = selectedPromptIndex == index
                            val item = TopikEssayEvaluator.PRESET_PROMPTS[index]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (isSelected) Modifier.background(Color(0xFF111827))
                                        else Modifier
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                                    )
                                    .clickable {
                                        selectedPromptIndex = index
                                        essayText = item.modelEssay
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (index == 0) "真题 1：成功的意义" else "真题 2：青少年阅读",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. 题目要求大卡片 ─────────────────────────────
            item(key = "prompt_card") {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    PromptDetailCard(
                        prompt = currentPrompt,
                        onLoadModelEssay = { essayText = currentPrompt.modelEssay },
                        onClearText = { essayText = "" }
                    )
                }
            }

            // ── 3. 智能诊断多维得分面板 ─────────────────────────
            item(key = "diagnostic_dashboard") {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    DiagnosticReportCard(report = report)
                }
            }

            // ── 4. 实时作文编辑器 ─────────────────────────────
            item(key = "editor_box") {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "作答区（基本阶 -ㄴ/는다）",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "${report.charCount} / 700 字",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (report.charCount in 550..750) Color(0xFF10B981) else Color(0xFF6B7280)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = essayText,
                        onValueChange = { essayText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        placeholder = { Text("在此输入或粘贴你的韩语大作文...", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF111827),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            }

            // ── 5. 详细诊断反馈清单 ───────────────────────────
            item(key = "feedback_list") {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "逐项诊断建议",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )

                    report.feedbackComments.forEach { comment ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = comment,
                                fontSize = 13.sp,
                                color = Color(0xFF374151),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun TopikEssayTopBar(onBack: () -> Unit) {
    Surface(
        color = Color(0xFFF7F8FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF111827))
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(
                    text = "TOPIK 作文诊断",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "TOPIK 54 ESSAY GRADER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 题目要求卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun PromptDetailCard(
    prompt: TopikPrompt,
    onLoadModelEssay: () -> Unit,
    onClearText: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = prompt.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = prompt.topicDescription,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
            Spacer(Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                prompt.writingPoints.forEachIndexed { i, pt ->
                    Text(
                        text = "${i + 1}. $pt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { onLoadModelEssay() }
                ) {
                    Text("填入满分范文", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }

                Surface(
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.clickable { onClearText() }
                ) {
                    Text("清空自己写", fontSize = 11.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 诊断得分面板
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DiagnosticReportCard(report: com.yonsei.koreanlearner.algorithm.essay.EssayDiagnosticReport) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF111827)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("预估标准得分", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                    Spacer(Modifier.height(2.dp))
                    Text("${report.estimatedScore} / 50 分", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "等级：${report.scoreGrade}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 核心统计指标
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "字符数", value = "${report.charCount} 字")
                MetricItem(label = "词汇数", value = "${report.wordCount} 词")
                MetricItem(label = "自然段", value = "${report.paragraphCount} 段")
                MetricItem(
                    label = "文体规范",
                    value = if (report.hasCasualEndings) "含口语(扣分)" else "基本阶(合格)"
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF9CA3AF))
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
