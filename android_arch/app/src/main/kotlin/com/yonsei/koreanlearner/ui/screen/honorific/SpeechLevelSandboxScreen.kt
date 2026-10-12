package com.yonsei.koreanlearner.ui.screen.honorific

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
import com.yonsei.koreanlearner.algorithm.honorific.SpeechLevel
import com.yonsei.koreanlearner.algorithm.honorific.SpeechLevelConverter
import com.yonsei.koreanlearner.algorithm.honorific.SpeechLevelResult
import com.yonsei.koreanlearner.core.audio.TtsManager

/**
 * ══════════════════════════════════════════════════════════════════
 * 敬语与 6 大阶称沙盒 (SpeechLevelSandboxScreen)
 * 纯白高光 Ins 风格，一键转换对比 6 大语气阶称与尊谦词汇
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SpeechLevelSandboxScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var inputSentence by remember { mutableStateOf("밥을 먹다") }
    var currentTab by remember { mutableIntStateOf(0) } // 0: 阶称转换, 1: 尊谦词表
    val keyboard = LocalSoftwareKeyboardController.current

    val results = remember(inputSentence) {
        SpeechLevelConverter.convertAllLevels(inputSentence)
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            SpeechLevelTopBar(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Tab 切换（6 大阶称 ↔ 尊谦词典）────────────────
            item(key = "tab_selector") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("6 大语气阶称对照", "常用尊谦词汇表").forEachIndexed { index, label ->
                        val isSelected = currentTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSelected) Modifier.background(Color(0xFF111827))
                                    else Modifier
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                                )
                                .clickable { currentTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            if (currentTab == 0) {
                // ── 2. 交互式输入框 ─────────────────────────────
                item(key = "input_box") {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        OutlinedTextField(
                            value = inputSentence,
                            onValueChange = { inputSentence = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            placeholder = { Text("输入句子原型（如：밥을 먹다, 집에 가다...）", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp)) },
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

                        Spacer(Modifier.height(10.dp))

                        // 预置例句快捷 Chip
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(SpeechLevelConverter.SAMPLE_PRESETS) { sample ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                    modifier = Modifier.clickable { inputSentence = sample }
                                ) {
                                    Text(
                                        text = sample,
                                        fontSize = 11.sp,
                                        color = Color(0xFF4B5563),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── 3. 6 大阶称结果卡片列表 ────────────────────────
                items(results, key = { it.level.code }) { item ->
                    SpeechLevelCard(
                        result = item,
                        onPlayAudio = { ttsManager.speak(item.sentenceText) }
                    )
                }
            } else {
                // ── 4. 尊谦助词卡片 ─────────────────────────────
                item(key = "particles_card") {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "尊称助词对照",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(8.dp))
                        SpeechParticlesCard()
                    }
                }

                // ── 5. 尊谦核心词汇表 ───────────────────────────
                item(key = "vocab_header") {
                    Text(
                        text = "核心尊谦动词与名词",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                items(SpeechLevelConverter.HONORIFIC_VOCABULARY, key = { it.plain }) { pair ->
                    HonorificVocabCard(
                        pair = pair,
                        onPlayAudio = { ttsManager.speak(pair.honorific) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SpeechLevelTopBar(onBack: () -> Unit) {
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
                    text = "敬语与阶称沙盒",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "SPEECH LEVEL SANDBOX",
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
// 阶称卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SpeechLevelCard(
    result: SpeechLevelResult,
    onPlayAudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${result.level.titleCn} (${result.level.titleKr})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "礼貌指数 " + "★".repeat(result.politenessScore),
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                IconButton(onClick = onPlayAudio) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "播放", tint = Color(0xFF111827))
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = result.sentenceText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "适用场景：${result.level.usageContext}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 助词卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SpeechParticlesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SpeechLevelConverter.HONORIFIC_PARTICLES.forEach { (plain, honorific, example) ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = plain, fontSize = 13.sp, color = Color(0xFF6B7280))
                        Text(text = "→  $honorific", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(text = "例：$example", fontSize = 12.sp, color = Color(0xFF9CA3AF))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 尊谦词汇卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun HonorificVocabCard(
    pair: com.yonsei.koreanlearner.algorithm.honorific.HonorificWordPair,
    onPlayAudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
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
                        text = pair.plain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "→  ${pair.honorific}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(pair.category, fontSize = 10.sp, color = Color(0xFF6B7280))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pair.chineseMeaning,
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            IconButton(onClick = onPlayAudio) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "发音", tint = Color(0xFF9CA3AF))
            }
        }
    }
}
