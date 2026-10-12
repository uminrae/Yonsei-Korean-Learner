package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.yonsei.koreanlearner.algorithm.korean.HonorificCorrectionEngine
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语敬语/谦语找茬纠错与训练交互卡片 (HonorificSandboxCard)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun HonorificSandboxCard(
    sampleSentence : String = "할아버지가 밥을 먹고 집에서 자요.",
    ttsManager     : TtsManager? = null,
    modifier       : Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // 计算自动诊断报告
    val report = remember(sampleSentence) {
        HonorificCorrectionEngine.correctToHonorific(sampleSentence)
    }

    // 找茬纠错模式状态：记录用户已正确纠正的索引集合
    val correctedIndices = remember { mutableStateListOf<Int>() }
    var selectedDiffIndex by remember { mutableStateOf<Int?>(null) }
    var isAllCorrect by remember { mutableStateOf(false) }

    val isCompleted = correctedIndices.size == report.diffList.size

    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 顶部标头与徽章 ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "敬语找茬纠错挑战",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isCompleted) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = " 敬语全通关",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = "待纠错: ${report.diffList.size - correctedIndices.size} 处",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ── 交互式句子词块排版 ──────────────────────────────
            Surface(
                color  = MaterialTheme.colorScheme.background,
                shape  = RoundedCornerShape(14.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = " 点击句子中不合礼节的非敬语单词，并选出正确的尊称：",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    // 词块排版
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        report.diffList.forEachIndexed { index, diff ->
                            val isCorrected = correctedIndices.contains(index)
                            val isSelected = selectedDiffIndex == index

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isCorrected -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        }
                                    )
                                    .clickable(enabled = !isCorrected) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedDiffIndex = index
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isCorrected) diff.correctedToken else diff.originalToken,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrected) Color(0xFF059669) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ── 选项选择弹框区（当用户点击了某个待纠正词块时） ───
            selectedDiffIndex?.let { diffIndex ->
                val currentDiff = report.diffList[diffIndex]
                val options = remember(diffIndex) {
                    listOf(
                        currentDiff.correctedToken,
                        currentDiff.originalToken,
                        currentDiff.originalToken + "요"
                    ).shuffled()
                }

                Surface(
                    color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "请为 '${currentDiff.originalToken}' 选择最高敬语尊称：",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            options.forEach { opt ->
                                OutlinedButton(
                                    onClick = {
                                        if (opt == currentDiff.correctedToken) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            correctedIndices.add(diffIndex)
                                            selectedDiffIndex = null
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(opt, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ── 通关后展示的最高敬语完整规范句 ───────────────────
            AnimatedVisibility(visible = isCompleted) {
                Surface(
                    color  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape  = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = " 最高敬语规范句",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (ttsManager != null) {
                                IconButton(
                                    onClick = { ttsManager.speak(report.correctedSentence) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Text("", fontSize = 14.sp)
                                }
                            }
                        }

                        Text(
                            text = report.correctedSentence,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), thickness = 0.5.dp)

                        Text(
                            text = report.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 16.sp,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
