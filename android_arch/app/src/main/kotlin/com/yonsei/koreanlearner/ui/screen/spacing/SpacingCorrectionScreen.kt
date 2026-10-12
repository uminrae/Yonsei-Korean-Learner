package com.yonsei.koreanlearner.ui.screen.spacing

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
 * 韩文规范空格（띄어쓰기）交互练习界面 (SpacingCorrectionScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SpacingCorrectionScreen(
    viewModel: SpacingCorrectionViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    val currentDrill = state.drillCases[state.currentCaseIndex]
    val chars = currentDrill.rawUnspaced.toCharArray()

    // 实时构造用户当前空格文本
    val userPreview = remember(state.insertedSpaces, currentDrill) {
        val sb = StringBuilder()
        chars.forEachIndexed { idx, c ->
            sb.append(c)
            if (state.insertedSpaces.contains(idx)) sb.append(" ")
        }
        sb.toString()
    }

    Scaffold(
        topBar = {
            SpacingTopBar(
                currentIndex = state.currentCaseIndex,
                totalCount = state.drillCases.size,
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
            // ── 1. 实时预览卡片 ─────────────────────────────────
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(" 当前分词断句预览：", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(
                        text = userPreview.ifBlank { "(请在下方点击字符之间插入空格)" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 26.sp
                    )
                }
            }

            // ── 2. 手势点击插入空格操作区 ────────────────────────
            Text(" 点击字符之间的圆点插槽 [ · ] 插入或撤回空格：", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

            // 字符插槽流式布局
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape  = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                OptInLayoutCharsWithSlots(
                    chars = chars,
                    insertedSpaces = state.insertedSpaces,
                    isSubmitted = state.isSubmitted,
                    onToggle = { idx ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleSpaceAt(idx)
                    }
                )
            }

            // ── 3. 校验诊断结果展开 ─────────────────────────────
            if (state.isSubmitted && state.checkResult != null) {
                val res = state.checkResult!!
                val isAllCorrect = userPreview.replace(".", "").trim() == currentDrill.standardSpaced.replace(".", "").trim()

                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border    = BorderStroke(1.5.dp, if (isAllCorrect) Color(0xFF10B981) else Color(0xFFEF4444)),
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isAllCorrect) " 恭喜！隔写完全符合国立国语院规范！" else "⚠️ 存在隔写瑕疵，请看解析：",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isAllCorrect) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 14.sp
                            )

                            IconButton(
                                onClick = { ttsManager?.speak(currentDrill.standardSpaced) },
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("", fontSize = 12.sp)
                            }
                        }

                        Text("标准规范写法: ${currentDrill.standardSpaced}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                        Divider(color = MaterialTheme.extended.cardBorder, thickness = 0.5.dp)

                        Text(" 隔写规则深度原理解析：", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(currentDrill.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── 4. 底部控制按钮 ─────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!state.isSubmitted) {
                    OutlinedButton(
                        onClick = { viewModel.resetDrill() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("清空空格 ↺")
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.submitCheck()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).height(48.dp)
                    ) {
                        Text("校验隔写 ", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextCase() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("下一题练习 →", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInLayoutCharsWithSlots(
    chars          : CharArray,
    insertedSpaces : Set<Int>,
    isSubmitted    : Boolean,
    onToggle       : (Int) -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chars.forEachIndexed { idx, char ->
            // 单个韩文字符块
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = char.toString(),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 字符之间的空格插槽（最后一个字符后不显示）
            if (idx < chars.size - 1) {
                val hasSpace = insertedSpaces.contains(idx)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (hasSpace) MaterialTheme.colorScheme.primary else Color.Transparent,
                    border = BorderStroke(1.dp, if (hasSpace) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clickable(enabled = !isSubmitted) { onToggle(idx) }
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (hasSpace) "␣" else "·",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasSpace) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpacingTopBar(
    currentIndex: Int,
    totalCount: Int,
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
            Text("韩文规范隔写（띄어쓰기）校对", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("练习 ${currentIndex + 1} / $totalCount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.width(36.dp))
    }
}
