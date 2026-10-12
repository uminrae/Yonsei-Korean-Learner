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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.Beneficiary
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.PartOfSpeech
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.SentenceType
import com.yonsei.koreanlearner.algorithm.korean.IndirectSpeechTransposer.Tense
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 间接引语与口语缩略形学习交互卡片（IndirectSpeechCard）
 * ══════════════════════════════════════════════════════════════════
 *
 * @param baseWord 谓词或名词原形（如 "먹다", "가다", "예쁘다", "학생"）
 * @param partOfSpeech 词性
 * @param speakerContext 说话人语境前缀（如 "민수가 그러는데..." 或 "선생님이..."）
 * @param ttsManager 全局 TTS 引擎
 */
@Composable
fun IndirectSpeechCard(
    baseWord       : String,
    partOfSpeech   : PartOfSpeech,
    speakerContext : String = "친구가 그러는데",
    ttsManager     : TtsManager? = null,
    modifier       : Modifier = Modifier
) {
    var selectedSentenceType by remember { mutableStateOf(SentenceType.DECLARATIVE) }
    var selectedTense by remember { mutableStateOf(Tense.PRESENT) }
    var isContractedView by remember { mutableStateOf(true) } // true: 口语缩略形, false: 完整形
    var isExpandedFormula by remember { mutableStateOf(false) }

    // 实时推导转述结果
    val transposeResult = remember(baseWord, partOfSpeech, selectedSentenceType, selectedTense) {
        IndirectSpeechTransposer.transpose(
            baseWord     = baseWord,
            partOfSpeech = partOfSpeech,
            sentenceType = selectedSentenceType,
            tense        = selectedTense
        )
    }

    val displayTransposedText = if (isContractedView) transposeResult.contractedForm else transposeResult.fullForm
    val fullSentence = "$speakerContext $displayTransposedText"

    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // ── 顶部标头与四大句型选择器 ───────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "间接引语与口语缩略",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // 完整形 vs 口语缩略形 胶囊切换
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isContractedView) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { isContractedView = false }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "标准形",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (!isContractedView) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isContractedView) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { isContractedView = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "口语缩略",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isContractedView) FontWeight.Bold else FontWeight.Normal,
                            color = if (isContractedView) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ── 四大句型切换 Chips ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SentenceType.entries.forEach { st ->
                    val isSelected = st == selectedSentenceType
                    val badgeColor = when (st) {
                        SentenceType.DECLARATIVE -> Color(0xFFE1306C)
                        SentenceType.INTERROGATIVE -> Color(0xFF3B82F6)
                        SentenceType.IMPERATIVE -> Color(0xFFF59E0B)
                        SentenceType.PROPOSITIVE -> Color(0xFF10B981)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick  = { selectedSentenceType = st },
                        label    = {
                            Text(
                                text = "${st.displayName} (${if (isContractedView) st.shortSuffixTag else st.fullSuffixTag})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape    = RoundedCornerShape(10.dp),
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = badgeColor.copy(alpha = 0.15f),
                            selectedLabelColor     = badgeColor
                        )
                    )
                }
            }

            // ── 原句 vs 转述句 对比核心大框 ──────────────────────
            Surface(
                color  = MaterialTheme.colorScheme.background,
                shape  = RoundedCornerShape(14.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 原型信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "原词: $baseWord  (${partOfSpeech.name})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "句型: ${selectedSentenceType.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                    // 转述大字呈现
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fullSentence,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isContractedView) "口语日常最高频转述表达" else "书面语与标准规范间接引语",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (ttsManager != null) {
                            IconButton(
                                onClick = { ttsManager.speak(fullSentence) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // ── 可折叠加法公式与拆解说明 ─────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpandedFormula = !isExpandedFormula }
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = " 公式拆解: ${transposeResult.stepFormula}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(if (isExpandedFormula) "▲" else "▼", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    AnimatedVisibility(visible = isExpandedFormula) {
                        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("标准完整形: ${transposeResult.fullForm}", style = MaterialTheme.typography.bodySmall)
                            Text("口语缩略形: ${transposeResult.contractedForm}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── 避坑指南与语用小贴士 ─────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(" ", fontSize = 12.sp)
                    Text(
                        text = transposeResult.tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
