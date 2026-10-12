package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yonsei.koreanlearner.algorithm.korean.SentenceConstraintEvaluator
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 开放式造句语法润色与语境约束校验卡片 (SentenceGraderCard)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SentenceGraderCard(
    targetGrammar: String = "-는 바람에",
    grammarConstraintHint: String = "必须接消极意外结果，严禁后接命令句/共动句，前句不可使用过去时。",
    modifier: Modifier = Modifier
) {
    var userInput by remember { mutableStateOf("") }
    var evaluationResult by remember { mutableStateOf<SentenceConstraintEvaluator.ConstraintEvaluationResult?>(null) }
    val haptic = LocalHapticFeedback.current

    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 标头与语法约束提示 ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = " 开放式造句润色沙盒",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = targetGrammar,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 语境约束规则胶囊
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape  = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️ 语境约束：", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(grammarConstraintHint, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                }
            }

            // 用户造句输入框
            OutlinedTextField(
                value = userInput,
                onValueChange = {
                    userInput = it
                    evaluationResult = null // 输入变动时重置评估结果
                },
                placeholder = { Text("在此输入使用 $targetGrammar 的完整造句...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // 诊断润色按钮
            Button(
                onClick = {
                    val res = SentenceConstraintEvaluator.evaluateSentence(targetGrammar, userInput)
                    evaluationResult = res
                    if (res.status == SentenceConstraintEvaluator.EvaluationStatus.PASS) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                },
                enabled = userInput.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("智能诊断与语境润色", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            // ── 诊断与润色结果面板 ──────────────────────────────
            AnimatedVisibility(visible = evaluationResult != null) {
                evaluationResult?.let { res ->
                    val statusColor = when (res.status) {
                        SentenceConstraintEvaluator.EvaluationStatus.PASS -> Color(0xFF10B981)
                        SentenceConstraintEvaluator.EvaluationStatus.WARNING -> Color(0xFFF59E0B)
                        SentenceConstraintEvaluator.EvaluationStatus.ERROR -> Color(0xFFEF4444)
                    }

                    Surface(
                        color  = statusColor.copy(alpha = 0.08f),
                        shape  = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )

                                Surface(
                                    color = statusColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${res.score} 分",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                }
                            }

                            // 润色建议清单
                            res.suggestions.forEach { sug ->
                                Text(
                                    text = "• $sug",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )
                            }

                            // 推荐规范例句
                            if (res.polishedSample.isNotBlank() && res.status != SentenceConstraintEvaluator.EvaluationStatus.PASS) {
                                Divider(color = statusColor.copy(alpha = 0.2f), thickness = 0.5.dp)
                                Text(
                                    text = " 推荐规范表达: ${res.polishedSample}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
