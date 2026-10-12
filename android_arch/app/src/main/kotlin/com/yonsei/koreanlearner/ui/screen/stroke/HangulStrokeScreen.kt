package com.yonsei.koreanlearner.ui.screen.stroke

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
import com.yonsei.koreanlearner.algorithm.korean.HangulLetterStroke
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.components.HangulStrokeCanvas
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文字母笔顺拆解与原笔迹临摹画布界面 (HangulStrokeScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun HangulStrokeScreen(
    viewModel: HangulStrokeViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val currentLetter = state.letters[state.currentLetterIndex]

    Scaffold(
        topBar = {
            StrokeTopBar(
                currentLetter = currentLetter,
                onBack = onBack
            )
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
            // ── 1. 字母选择 FilterChips ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.letters.forEachIndexed { index, letter ->
                    val isSelected = state.currentLetterIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectLetter(index)
                        },
                        label    = { Text(letter.letterSymbol, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // ── 2. 原笔迹平滑临摹画布 ───────────────────────────
            HangulStrokeCanvas(
                letter = currentLetter,
                userStrokes = state.userStrokes,
                isDemonstrating = state.isDemonstrating,
                onAddStroke = { stroke ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.addStroke(stroke)
                }
            )

            // ── 3. 控制工具栏 (Demonstrate / Undo / Clear / TTS) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 示范
                    FilledTonalButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.playDemonstration()
                        },
                        enabled = !state.isDemonstrating,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(" 笔顺示范")
                    }

                    // 撤销一笔
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.undoStroke()
                        },
                        enabled = state.userStrokes.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("↺ 撤销")
                    }

                    // 清空重写
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.clearCanvas()
                        },
                        enabled = state.userStrokes.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(" 清空")
                    }
                }

                // 发音
                IconButton(
                    onClick = { ttsManager?.speak(currentLetter.letterSymbol) },
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text("", fontSize = 18.sp)
                }
            }

            // ── 4. 评估字迹按钮与评分结果 ───────────────────────
            if (state.userStrokes.isNotEmpty()) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.evaluateHandwriting()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(" 评估笔迹贴合度", fontWeight = FontWeight.Bold)
                }
            }

            state.evaluationScore?.let { score ->
                Surface(
                    color  = MaterialTheme.colorScheme.surface,
                    shape  = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (score >= 90) Color(0xFF10B981) else Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(" 临摹贴合度评分", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(if (score >= 90) "笔顺标准，笔触流畅！" else "笔画数或形态稍有偏差，请多示范练习。", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("$score 分", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if (score >= 90) Color(0xFF10B981) else Color(0xFFF59E0B))
                    }
                }
            }

            // ── 5. 字母书写要领指南卡片 ─────────────────────────
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(" ${currentLetter.charName} 书写规范：", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    Text("总笔画数: ${currentLetter.totalStrokes} 笔", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(currentLetter.strokeGuide, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun StrokeTopBar(
    currentLetter: HangulLetterStroke,
    onBack: (() -> Unit)?
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
            Text("韩文字母笔顺与临摹画布", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(currentLetter.charName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.width(36.dp))
    }
}
