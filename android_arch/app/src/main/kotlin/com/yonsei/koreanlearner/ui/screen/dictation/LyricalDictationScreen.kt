package com.yonsei.koreanlearner.ui.screen.dictation

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.core.audio.LrcParser
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended
import kotlinx.coroutines.flow.collectLatest

/**
 * ══════════════════════════════════════════════════════════════════
 * K-Pop 歌词逐句精析与填空听写沙盒界面 (LyricalDictationScreen)
 * 纯白高光 Ins 风格，支持 (G)I-DLE/雨琦/薇娟/小娟、TXT、BTS 经典名曲
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun LyricalDictationScreen(
    viewModel: LyricalDictationViewModel = hiltViewModel(),
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val ttsManager = remember { TtsManager(context) }

    // ── Effect 监听与平滑自动滚动 ────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LyricalDictationEffect.AutoScrollToLine -> {
                    lazyListState.animateScrollToItem(
                        index = effect.lineIndex.coerceAtLeast(0),
                        scrollOffset = -150
                    )
                }
                LyricalDictationEffect.PlayHapticSuccess -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                LyricalDictationEffect.PlayHapticError -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                is LyricalDictationEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            DictationTopBar(
                title = state.title,
                isDictationMode = state.isDictationMode,
                onToggleDictationMode = { viewModel.onIntent(LyricalDictationIntent.ToggleDictationMode) },
                onBack = onBack
            )
        },
        bottomBar = {
            DictationPlaybackBottomBar(
                state = state,
                onTogglePlayPause = {
                    viewModel.onIntent(LyricalDictationIntent.TogglePlayPause)
                    val activeLine = state.lines.getOrNull(state.currentLineIndex)
                    if (activeLine != null && !state.isPlaying) {
                        ttsManager.speak(activeLine.koreanText)
                    }
                },
                onToggleLoop = { viewModel.onIntent(LyricalDictationIntent.ToggleSingleLineLoop) },
                onChangeSpeed = { viewModel.onIntent(LyricalDictationIntent.ChangeSpeed(it)) }
            )
        },
        containerColor = Color(0xFFF7F8FA),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 1. 热门曲目横向选择 Chip ────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.availableSongs, key = { it.id }) { song ->
                    val isSelected = song.id == state.selectedSongId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSelected) Modifier.background(Color(0xFF111827))
                                else Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                            )
                            .clickable { viewModel.onIntent(LyricalDictationIntent.SelectSong(song.id)) }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = song.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // ── 2. 当前歌曲背景与核心语法卡片 ───────────────────
            if (state.keyGrammarNotes.isNotEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.artist,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = state.albumCoverTag,
                                    fontSize = 10.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            state.keyGrammarNotes.forEach { note ->
                                Text(
                                    text = note,
                                    fontSize = 11.sp,
                                    color = Color(0xFF4B5563),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. 音频律动波形条 ───────────────────────────────
            AudioWaveformVisualizer(
                isPlaying = state.isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .padding(horizontal = 24.dp, vertical = 4.dp)
            )

            // ── 4. 随时间戳平滑滚动的歌词列表 ───────────────────
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(state.lines, key = { _, line -> "${state.selectedSongId}_${line.index}" }) { index, line ->
                    val isCurrent = index == state.currentLineIndex

                    LyricalLineCard(
                        line = line,
                        isCurrent = isCurrent,
                        isDictationMode = state.isDictationMode,
                        userInput = state.userInputs[index] ?: "",
                        isVerified = state.verificationResults[index],
                        phoneticHint = state.phoneticHints[index],
                        onLineClick = {
                            viewModel.onIntent(LyricalDictationIntent.SelectLine(index))
                            ttsManager.speak(line.koreanText)
                        },
                        onInput = { viewModel.onIntent(LyricalDictationIntent.InputAnswer(index, it)) },
                        onVerify = { viewModel.onIntent(LyricalDictationIntent.VerifyAnswer(index)) },
                        onReveal = { viewModel.onIntent(LyricalDictationIntent.RevealAnswer(index)) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 1. 顶部音频波形律动效果
// ══════════════════════════════════════════════════════════════════

@Composable
private fun AudioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_phase"
    )

    Canvas(modifier = modifier) {
        val barCount = 28
        val spacing = size.width / barCount
        val barWidth = spacing * 0.45f
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val progress = (i.toFloat() / barCount + phase) % 1f
            val waveHeight = if (isPlaying) {
                maxHeight * (0.25f + 0.75f * kotlin.math.sin(progress * Math.PI.toFloat()).coerceAtLeast(0f))
            } else {
                maxHeight * 0.2f
            }

            drawRoundRect(
                color = if (isPlaying) Color(0xFF111827) else Color(0xFFE5E7EB),
                topLeft = Offset(i * spacing + (spacing - barWidth) / 2f, (maxHeight - waveHeight) / 2f),
                size = Size(barWidth, waveHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 2. 单行歌词/台词交互卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun LyricalLineCard(
    line: LrcParser.LrcLine,
    isCurrent: Boolean,
    isDictationMode: Boolean,
    userInput: String,
    isVerified: Boolean?,
    phoneticHint: String?,
    onLineClick: () -> Unit,
    onInput: (String) -> Unit,
    onVerify: () -> Unit,
    onReveal: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLineClick() },
        color = if (isCurrent) Color.White else Color(0xFFFAFAFC),
        border = BorderStroke(
            width = if (isCurrent) 1.5.dp else 1.dp,
            color = if (isCurrent) Color(0xFF111827) else Color(0xFFE5E7EB)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 时间戳与发音按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", (line.startTimeMs / 1000) / 60, (line.startTimeMs / 1000) % 60),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Color(0xFF111827) else Color(0xFF9CA3AF)
                )

                IconButton(onClick = onLineClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "朗读此句",
                        tint = if (isCurrent) Color(0xFF111827) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // 韩文字幕展示
            if (!isDictationMode || isVerified == true) {
                Text(
                    text = line.koreanText,
                    fontSize = 17.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent) Color(0xFF111827) else Color(0xFF4B5563),
                    lineHeight = 24.sp
                )
            } else {
                Text(
                    text = line.blankQuestion?.maskedKorean ?: line.koreanText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    lineHeight = 24.sp
                )
            }

            // 中文翻译
            if (!line.chineseText.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = line.chineseText,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }

            // 听写模式输入区
            if (isDictationMode && isVerified != true) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = onInput,
                        placeholder = { Text("输入挖空部分词汇...", fontSize = 12.sp, color = Color(0xFF9CA3AF)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF111827),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )

                    Button(
                        onClick = onVerify,
                        enabled = userInput.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("校验", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }

                if (isVerified == false) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phoneticHint ?: "拼写不一致，请仔细分辨收音",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp
                        )
                        TextButton(onClick = onReveal) {
                            Text("看答案", fontSize = 11.sp, color = Color(0xFF111827))
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 3. 顶栏与控制底栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DictationTopBar(
    title: String,
    isDictationMode: Boolean,
    onToggleDictationMode: () -> Unit,
    onBack: (() -> Unit)?
) {
    Surface(
        color = Color(0xFFF7F8FA),
        modifier = Modifier.fillMaxWidth()
    ) {
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
                    text = "K-Pop 歌词精读听写",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "LYRICS LAB & DICTATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isDictationMode) Modifier.background(Color(0xFF111827))
                        else Modifier
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                    )
                    .clickable { onToggleDictationMode() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isDictationMode) "听写模式" else "精读模式",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDictationMode) Color.White else Color(0xFF111827)
                )
            }
        }
    }
}

@Composable
private fun DictationPlaybackBottomBar(
    state: LyricalDictationUiState,
    onTogglePlayPause: () -> Unit,
    onToggleLoop: () -> Unit,
    onChangeSpeed: (Float) -> Unit
) {
    Surface(
        color  = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape  = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 倍速切换 (0.8x / 1.0x / 1.2x)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.8f, 1.0f, 1.2f).forEach { spd ->
                    val isSel = state.speed == spd
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (isSel) Modifier.background(Color(0xFF111827))
                                else Modifier.background(Color(0xFFF3F4F6))
                            )
                            .clickable { onChangeSpeed(spd) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${spd}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // 单句循环控制
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .then(
                        if (state.isLoopingSingleLine) Modifier.background(Color(0xFF111827))
                        else Modifier.background(Color(0xFFF3F4F6))
                    )
                    .clickable { onToggleLoop() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (state.isLoopingSingleLine) "单句循环" else "顺序播放",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isLoopingSingleLine) Color.White else Color(0xFF6B7280)
                )
            }

            // 播放/暂停大圆钮
            Surface(
                shape = CircleShape,
                color = Color(0xFF111827),
                modifier = Modifier.size(46.dp)
            ) {
                IconButton(onClick = onTogglePlayPause) {
                    Text(
                        text = if (state.isPlaying) "⏸" else "▶",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
