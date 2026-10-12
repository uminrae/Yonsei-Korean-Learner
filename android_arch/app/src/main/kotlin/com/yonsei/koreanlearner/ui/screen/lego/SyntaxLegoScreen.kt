package com.yonsei.koreanlearner.ui.screen.lego

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.yonsei.koreanlearner.algorithm.korean.LegoBlock
import com.yonsei.koreanlearner.algorithm.korean.LegoEvaluationResult
import com.yonsei.koreanlearner.algorithm.korean.LegoPuzzle
import com.yonsei.koreanlearner.algorithm.korean.SyntaxLegoEngine
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语语法乐高积木拼装沙盒界面 (SyntaxLegoScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SyntaxLegoScreen(
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var puzzleIndex by remember { mutableIntStateOf(0) }
    val currentPuzzle = SyntaxLegoEngine.PRESET_PUZZLES[puzzleIndex]

    // 拼装槽中的积木 ID 列表
    val assembledBlockIds = remember { mutableStateListOf<String>() }
    var evaluationResult by remember { mutableStateOf<LegoEvaluationResult?>(null) }
    val haptic = LocalHapticFeedback.current

    // 切换关卡时重置拼装状态
    fun resetAssembly() {
        assembledBlockIds.clear()
        evaluationResult = null
    }

    Scaffold(
        topBar = {
            SyntaxLegoTopBar(
                puzzleIndex = puzzleIndex,
                totalPuzzles = SyntaxLegoEngine.PRESET_PUZZLES.size,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. 目标中文含义卡片 ─────────────────────────────
            Card(
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                modifier  = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = " 目标合成语义：",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentPuzzle.targetChinese,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── 2. 乐高积木拼装卡槽区 (Assembly Slot Zone) ─────
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape  = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.extended.cardBorder),
                modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = " 积木拼装卡槽 (点击积木可移回托盘)：",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (assembledBlockIds.isEmpty()) {
                            Text(
                                text = "（请从下方托盘选择积木拼接...）",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        } else {
                            val idToBlock = currentPuzzle.scatteredBlocks.associateBy { it.id }
                            assembledBlockIds.forEach { id ->
                                val block = idToBlock[id] ?: return@forEach
                                LegoBlockItem(
                                    block = block,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        assembledBlockIds.remove(id)
                                        evaluationResult = null
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. 散落积木托盘区 (Scattered Lego Tray) ──────────
            Text(
                text = " 待拼装积木碎片：",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentPuzzle.scatteredBlocks.forEach { block ->
                    val isUsed = assembledBlockIds.contains(block.id)
                    if (!isUsed) {
                        LegoBlockItem(
                            block = block,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                assembledBlockIds.add(block.id)
                                // 若已全部拼装完，自动触发校验
                                if (assembledBlockIds.size == currentPuzzle.scatteredBlocks.size) {
                                    val res = SyntaxLegoEngine.evaluateAssembly(currentPuzzle, assembledBlockIds.toList())
                                    evaluationResult = res
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── 4. 拼装结果与隔写/活用解析面板 ─────────────────
            AnimatedVisibility(visible = evaluationResult != null) {
                evaluationResult?.let { res ->
                    Surface(
                        color  = if (res.isCorrect) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f),
                        shape  = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (res.isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (res.isCorrect) "✓ 拼装成功！" else "✗ 拼装有误",
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isCorrect) Color(0xFF059669) else Color(0xFFEF4444)
                                )

                                if (res.isCorrect && ttsManager != null) {
                                    IconButton(onClick = { ttsManager.speak(res.assembledSentence) }, modifier = Modifier.size(30.dp)) {
                                        Text("", fontSize = 14.sp)
                                    }
                                }
                            }

                            if (res.assembledSentence.isNotBlank()) {
                                Text(
                                    text = res.assembledSentence,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = res.feedback,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 底部操作栏（重置与下一关）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { resetAssembly() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("重置积木 ", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        puzzleIndex = (puzzleIndex + 1) % SyntaxLegoEngine.PRESET_PUZZLES.size
                        resetAssembly()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("下一关 ➡️", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LegoBlockItem(
    block: LegoBlock,
    onClick: () -> Unit
) {
    val blockColor = when (block.category) {
        com.yonsei.koreanlearner.algorithm.korean.LegoCategory.CLAUSE_HEAD -> Color(0xFF833AB4)
        com.yonsei.koreanlearner.algorithm.korean.LegoCategory.GRAMMAR_CONNECTOR -> Color(0xFFE1306C)
        com.yonsei.koreanlearner.algorithm.korean.LegoCategory.CLAUSE_TAIL -> Color(0xFFF59E0B)
        com.yonsei.koreanlearner.algorithm.korean.LegoCategory.OBJECT_PHRASE -> Color(0xFF10B981)
    }

    Surface(
        color  = blockColor.copy(alpha = 0.15f),
        shape  = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, blockColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = block.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = blockColor
            )
            Text(
                text = block.category.displayName,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SyntaxLegoTopBar(
    puzzleIndex: Int,
    totalPuzzles: Int,
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
            Text("语法乐高积木拼装", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("第 ${puzzleIndex + 1} / $totalPuzzles 关", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.width(36.dp))
    }
}
