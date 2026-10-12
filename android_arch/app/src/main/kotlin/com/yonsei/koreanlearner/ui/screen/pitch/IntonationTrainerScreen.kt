package com.yonsei.koreanlearner.ui.screen.pitch

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 终结句尾语调音高曲线训练沙盒界面 (IntonationTrainerScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun IntonationTrainerScreen(
    viewModel: IntonationTrainerViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val currentTemplate = state.templates[state.currentTemplateIndex]

    Scaffold(
        topBar = {
            IntonationTopBar(onBack = onBack)
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. 终结词尾选择 FilterChips ─────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.templates.forEachIndexed { index, t ->
                    val isSelected = state.currentTemplateIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectTemplate(index)
                        },
                        label    = { Text("${t.endingPattern} (${t.patternType.symbol})") },
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // ── 2. 范例例句卡片 ─────────────────────────────────
            Card(
                shape     = RoundedCornerShape(18.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = currentTemplate.sampleSentence,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentTemplate.chineseMeaning,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { ttsManager?.speak(currentTemplate.sampleSentence) },
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("", fontSize = 20.sp)
                    }
                }
            }

            // ── 3. Canvas 双轨语调音高曲线绘制区 ─────────────────
            IntonationPitchVisualizer(
                template = currentTemplate,
                userPitchTrack = state.userPitchTrack
            )

            // ── 4. 语调契合度评分卡片 ─────────────────────────────
            state.intonationScore?.let { score ->
                Surface(
                    color  = MaterialTheme.colorScheme.surface,
                    shape  = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (score >= 80) Color(0xFF10B981) else Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(" 语调契合度评分 (DTW)：", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$score 分", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = if (score >= 80) Color(0xFF10B981) else Color(0xFFF59E0B))
                        }

                        Divider(color = MaterialTheme.extended.cardBorder, thickness = 0.5.dp)

                        Text(" 语调发音要领指导：", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(currentTemplate.feedbackGuide, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── 5. 录音与模拟操作区 ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.injectSimulatedUserPitch()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("模拟跟读 ", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleRecording()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRecording) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.5f).height(50.dp)
                ) {
                    Text(
                        text = if (state.isRecording) "⏹ 结束录音评估" else " 按下实时跟读",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun IntonationTopBar(onBack: (() -> Unit)?) {
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

        Text(
            text = "终结句尾语调音高曲线",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.width(36.dp))
    }
}
