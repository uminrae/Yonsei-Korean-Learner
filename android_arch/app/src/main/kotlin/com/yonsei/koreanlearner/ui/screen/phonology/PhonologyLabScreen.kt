package com.yonsei.koreanlearner.ui.screen.phonology

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.phonology.KoreanPhonologyEngine
import com.yonsei.koreanlearner.algorithm.phonology.PhonologyAnalysisResult
import com.yonsei.koreanlearner.algorithm.phonology.PhonologyRuleType
import com.yonsei.koreanlearner.core.audio.TtsManager

/**
 * ══════════════════════════════════════════════════════════════════
 * 音变规律动态实验室 (PhonologyLabScreen)
 * 纯白极简 Ins 风格，动态拆解韩语 7 大经典音变轨迹
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun PhonologyLabScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var inputText by remember { mutableStateOf("국립") }
    var selectedRuleFilter by remember { mutableStateOf<PhonologyRuleType?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current

    val analysisResult = remember(inputText) {
        KoreanPhonologyEngine.analyze(inputText)
    }

    val filteredExamples = remember(selectedRuleFilter) {
        if (selectedRuleFilter == null) {
            KoreanPhonologyEngine.ALL_PRESET_EXAMPLES
        } else {
            KoreanPhonologyEngine.ALL_PRESET_EXAMPLES.filter {
                it.appliedRules.contains(selectedRuleFilter)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            PhonologyTopBar(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. 交互式输入框 ─────────────────────────────────
            item(key = "input_box") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        placeholder = { Text("输入韩语词汇（如：국립, 신라, 같이...）", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (inputText.isNotBlank()) {
                                IconButton(onClick = { ttsManager.speak(inputText) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "发音", tint = Color(0xFF111827))
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF111827),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            }

            // ── 2. 动态音变核心演算大卡片 ─────────────────────────
            item(key = "main_card") {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    MainTransformationCard(
                        result = analysisResult,
                        onPlayAudio = { ttsManager.speak(analysisResult.originalText) }
                    )
                }
            }

            // ── 3. 7 大音变法则筛选流 ────────────────────────────
            item(key = "rules_filter") {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "经典音变法则库",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    Spacer(Modifier.height(8.dp))

                    val rules = listOf(
                        null to "全部法则",
                        PhonologyRuleType.NASALIZATION to "鼻音化",
                        PhonologyRuleType.LATERALIZATION to "流音化",
                        PhonologyRuleType.LIAISON to "连音化",
                        PhonologyRuleType.TENZIFICATION to "紧音化",
                        PhonologyRuleType.ASPIRATION to "激音化",
                        PhonologyRuleType.PALATALIZATION to "腭音化"
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(rules, key = { it.second }) { (rule, label) ->
                            val isSelected = selectedRuleFilter == rule
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(
                                        if (isSelected) Modifier.background(Color(0xFF111827))
                                        else Modifier
                                            .background(Color.White)
                                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                                    )
                                    .clickable { selectedRuleFilter = rule }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. 高频音变预置案例列表 ─────────────────────────
            items(filteredExamples, key = { it.originalText }) { item ->
                PresetExampleCard(
                    item = item,
                    onClick = { inputText = item.originalText },
                    onPlayAudio = { ttsManager.speak(item.originalText) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun PhonologyTopBar(onBack: () -> Unit) {
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
                    text = "音变实验室",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "PHONOLOGY LAB",
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
// 动态演变主卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun MainTransformationCard(
    result: PhonologyAnalysisResult,
    onPlayAudio: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            // 原词与音标比对大标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = result.originalText.ifEmpty { "输入词汇" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "实际读音：${result.phoneticNotation}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF111827),
                    modifier = Modifier.size(46.dp)
                ) {
                    IconButton(onClick = onPlayAudio) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "播放读音", tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // 规则标签 Chip
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                result.appliedRules.forEach { rule ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${rule.titleCn} (${rule.titleKr})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 逐步拆解 Timeline
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                result.steps.forEachIndexed { index, step ->
                    Surface(
                        color = Color(0xFFF9FAFB),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF111827)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = step.stageName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = step.explanation,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 预置案例卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun PresetExampleCard(
    item: PhonologyAnalysisResult,
    onClick: () -> Unit,
    onPlayAudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.originalText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "→  ${item.phoneticNotation}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4B5563)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.appliedRules.joinToString(" · ") { it.titleCn },
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            IconButton(onClick = onPlayAudio) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "播放读音", tint = Color(0xFF9CA3AF))
            }
        }
    }
}
