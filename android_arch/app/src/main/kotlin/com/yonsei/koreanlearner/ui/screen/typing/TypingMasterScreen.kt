package com.yonsei.koreanlearner.ui.screen.typing

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.typing.Hangul2SetJamoMapper
import com.yonsei.koreanlearner.algorithm.typing.KeyboardKeyInfo

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语 2-Set 标准双拼键盘“盲打打字机” (TypingMasterScreen)
 * 纯白高光 Ins 风格，配备机械键盘触感反馈与实时 WPM 测速仪表
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun TypingMasterScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var selectedSample by remember { mutableStateOf(Hangul2SetJamoMapper.PRESET_TYPING_SAMPLES.first()) }
    var typedText by remember { mutableStateOf("") }
    var isShiftActive by remember { mutableStateOf(false) }
    var startTimeMs by remember { mutableLongStateOf(0L) }
    var isCompleted by remember { mutableStateOf(false) }

    val target = selectedSample.targetKorean
    val accuracy = remember(typedText, target) {
        if (typedText.isEmpty()) 100
        else {
            var correctCount = 0
            for (i in typedText.indices) {
                if (i < target.length && typedText[i] == target[i]) {
                    correctCount++
                }
            }
            ((correctCount.toFloat() / typedText.length) * 100).toInt().coerceIn(0, 100)
        }
    }

    val wpm = remember(typedText, startTimeMs) {
        if (startTimeMs == 0L || typedText.isEmpty()) 0
        else {
            val elapsedMinutes = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(1000L) / 60000.0
            val wordsTyped = typedText.length / 3.0
            (wordsTyped / elapsedMinutes).toInt().coerceIn(0, 500)
        }
    }

    fun onKeyPress(char: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (startTimeMs == 0L) startTimeMs = System.currentTimeMillis()

        if (typedText.length < target.length) {
            val newTyped = typedText + char
            typedText = newTyped
            if (newTyped == target) {
                isCompleted = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    fun onBackspace() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (typedText.isNotEmpty()) {
            typedText = typedText.dropLast(1)
            isCompleted = false
        }
    }

    fun onReset() {
        typedText = ""
        startTimeMs = 0L
        isCompleted = false
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFFF7F8FA), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF111827))
                        }
                    } else {
                        Spacer(Modifier.width(36.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "韩语盲打打字机",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "2-SET HANGUL TYPING MASTER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                            .clickable { onReset() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("重置", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                }
            }
        },
        containerColor = Color(0xFFF7F8FA),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. 模式/练习句选择 ───────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(Hangul2SetJamoMapper.PRESET_TYPING_SAMPLES, key = { it.id }) { sample ->
                    val isSel = sample.id == selectedSample.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSel) Modifier.background(Color(0xFF111827))
                                else Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                            )
                            .clickable {
                                selectedSample = sample
                                onReset()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sample.levelName,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // ── 2. 打字速度与准确率实时仪表盘 ─────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(title = "WPM 打字速度", value = "$wpm", unit = "字/分", modifier = Modifier.weight(1f))
                StatCard(title = "准确率", value = "$accuracy%", unit = "ACC", modifier = Modifier.weight(1f))
                StatCard(title = "已击键", value = "${typedText.length}/${target.length}", unit = "JAMO", modifier = Modifier.weight(1f))
            }

            // ── 3. 打字主舞台卡片 ─────────────────────────────────
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, if (isCompleted) Color(0xFF10B981) else Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = selectedSample.chineseMeaning,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(10.dp))

                    // 目标字字符逐一高亮对比
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        target.forEachIndexed { i, char ->
                            val userChar = typedText.getOrNull(i)
                            val isCorrect = userChar == char
                            val isCurrent = i == typedText.length

                            Text(
                                text = char.toString(),
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    userChar == null -> if (isCurrent) Color(0xFF111827) else Color(0xFFD1D5DB)
                                    isCorrect -> Color(0xFF10B981)
                                    else -> Color(0xFFEF4444)
                                },
                                modifier = if (isCurrent) Modifier
                                    .background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 2.dp)
                                else Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }

                    if (isCompleted) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "🎉 完美盲打完成！速度: $wpm WPM | 准确率: $accuracy%",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── 4. 2-Set 标准双拼模拟机械键盘 ─────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFFFFF),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 第一排
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Hangul2SetJamoMapper.ROW_1.forEach { key ->
                            KeyButton(
                                key = key,
                                isShift = isShiftActive,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(if (isShiftActive && key.shiftHangulChar != null) key.shiftHangulChar else key.hangulChar) }
                            )
                        }
                    }

                    // 第二排
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Hangul2SetJamoMapper.ROW_2.forEach { key ->
                            KeyButton(
                                key = key,
                                isShift = isShiftActive,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(key.hangulChar) }
                            )
                        }
                    }

                    // 第三排 (包含 Shift & Backspace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shift 键
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isShiftActive) Color(0xFF111827) else Color(0xFFF3F4F6),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    isShiftActive = !isShiftActive
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⇧",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isShiftActive) Color.White else Color(0xFF374151)
                                )
                            }
                        }

                        Hangul2SetJamoMapper.ROW_3.forEach { key ->
                            KeyButton(
                                key = key,
                                isShift = isShiftActive,
                                modifier = Modifier.weight(1f),
                                onClick = { onKeyPress(key.hangulChar) }
                            )
                        }

                        // 退格键
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3F4F6),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBackspace() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⌫",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                            }
                        }
                    }

                    // 左右手键位提示色标
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF3B82F6)))
                        Spacer(Modifier.width(4.dp))
                        Text("左手区(辅音자음)", fontSize = 10.sp, color = Color(0xFF6B7280))

                        Spacer(Modifier.width(16.dp))

                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Spacer(Modifier.width(4.dp))
                        Text("右手区(元音모음)", fontSize = 10.sp, color = Color(0xFF6B7280))
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    key: KeyboardKeyInfo,
    isShift: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val displayHangul = if (isShift && key.shiftHangulChar != null) key.shiftHangulChar else key.hangulChar

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (key.isConsonant) Color(0xFFDBEAFE) else Color(0xFFFEF3C7)),
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayHangul,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (key.isConsonant) Color(0xFF1D4ED8) else Color(0xFFD97706)
            )
            Text(
                text = key.qwertyChar.uppercase(),
                fontSize = 8.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF111827))
                Spacer(Modifier.width(2.dp))
                Text(unit, fontSize = 8.sp, color = Color(0xFF6B7280))
            }
        }
    }
}
