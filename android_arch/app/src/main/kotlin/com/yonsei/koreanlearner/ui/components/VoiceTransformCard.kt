package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.yonsei.koreanlearner.algorithm.korean.GrammaticalVoice
import com.yonsei.koreanlearner.algorithm.korean.SentenceVoiceTransformation
import com.yonsei.koreanlearner.algorithm.korean.VoiceTransformationEngine
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语法语态（主动态/被动态/使动态）交互卡片 (VoiceTransformCard)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun VoiceTransformCard(
    transformation : SentenceVoiceTransformation,
    ttsManager     : TtsManager? = null,
    modifier       : Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var currentVoice by remember { mutableStateOf(GrammaticalVoice.ACTIVE) }

    val hasPassive = transformation.passiveSentence != null
    val hasCausative = transformation.causativeSentence != null

    val currentSentence = when (currentVoice) {
        GrammaticalVoice.ACTIVE -> transformation.activeSentence
        GrammaticalVoice.PASSIVE -> transformation.passiveSentence ?: transformation.activeSentence
        GrammaticalVoice.CAUSATIVE -> transformation.causativeSentence ?: transformation.activeSentence
    }

    Card(
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. 顶部语态切换 Tabs ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VoiceTabChip(
                    text = "主动态",
                    isSelected = currentVoice == GrammaticalVoice.ACTIVE,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        currentVoice = GrammaticalVoice.ACTIVE
                    },
                    modifier = Modifier.weight(1f)
                )

                if (hasPassive) {
                    VoiceTabChip(
                        text = "被动态 (피동)",
                        isSelected = currentVoice == GrammaticalVoice.PASSIVE,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentVoice = GrammaticalVoice.PASSIVE
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (hasCausative) {
                    VoiceTabChip(
                        text = "使动态 (사동)",
                        isSelected = currentVoice == GrammaticalVoice.CAUSATIVE,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            currentVoice = GrammaticalVoice.CAUSATIVE
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 2. 语态句子动态展示区 ───────────────────────────
            AnimatedContent(
                targetState = currentSentence,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "voice_sentence"
            ) { sentence ->
                Surface(
                    color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    shape  = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sentence,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            lineHeight = 24.sp
                        )

                        IconButton(
                            onClick = { ttsManager?.speak(sentence) },
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
                        ) {
                            Text("", fontSize = 16.sp)
                        }
                    }
                }
            }

            // ── 3. 语法成分彩色解析胶囊 ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ComponentTag(title = "施动者", value = transformation.agent, color = Color(0xFF3B82F6))
                ComponentTag(title = "受动者", value = transformation.patient, color = Color(0xFF10B981))
                ComponentTag(title = "核心动词", value = transformation.baseVerb, color = Color(0xFFF59E0B))
            }

            // ── 4. 句法转换原理解析 ─────────────────────────────
            Surface(
                color  = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                shape  = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = " 句式转换与助词变格规则：",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = transformation.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceTabChip(
    text       : String,
    isSelected : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.extended.cardBorder),
        modifier = modifier.height(36.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComponentTag(title: String, value: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
