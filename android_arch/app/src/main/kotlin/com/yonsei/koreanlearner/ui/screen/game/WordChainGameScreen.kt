package com.yonsei.koreanlearner.ui.screen.game

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.game.WordChainEngine
import com.yonsei.koreanlearner.algorithm.game.WordChainItem
import com.yonsei.koreanlearner.core.audio.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩国国民游戏“词尾接龙”人机对决界面 (WordChainGameScreen)
 * 纯白高光 Ins 风格，支持头音法则、词性溯源与实时连击计分
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun WordChainGameScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val ttsManager = remember { TtsManager(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val history = remember {
        mutableStateListOf(
            WordChainItem("한국어", "韩国语/韩语", "韓國語", 1, isUser = false)
        )
    }
    var userInput by remember { mutableStateOf("") }
    var isAiThinking by remember { mutableStateOf(false) }
    var comboScore by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val lastItem = history.lastOrNull()

    fun submitWord(word: String) {
        val lastWord = lastItem?.word
        val historySet = history.map { it.word }.toSet()
        val (isValid, reason) = WordChainEngine.validateUserWord(word, lastWord, historySet)

        if (!isValid) {
            errorMessage = reason
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            return
        }

        errorMessage = null
        val matchedDictItem = WordChainEngine.DICTIONARY.find { it.word == word }
        val userItem = WordChainItem(
            word = word,
            chineseMeaning = matchedDictItem?.chineseMeaning ?: "玩家输入词汇",
            hanja = matchedDictItem?.hanja,
            bookLevel = matchedDictItem?.bookLevel ?: 1,
            isUser = true
        )

        history.add(userItem)
        userInput = ""
        comboScore++
        ttsManager.speak(word)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        // 滚动到底部
        scope.launch {
            delay(100)
            listState.animateScrollToItem(history.size - 1)
        }

        // AI 思考与回应
        isAiThinking = true
        scope.launch {
            delay(1200) // 模拟 AI 拟真思考
            val aiResponse = WordChainEngine.getAiResponseWord(word, history.map { it.word }.toSet())
            isAiThinking = false

            if (aiResponse != null) {
                history.add(aiResponse.copy(isUser = false))
                comboScore++
                ttsManager.speak(aiResponse.word)
                delay(100)
                listState.animateScrollToItem(history.size - 1)
            } else {
                Toast.makeText(context, "🎉 AI 词穷啦！恭喜您赢得本局接龙对决！", Toast.LENGTH_LONG).show()
            }
        }
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
                            text = "词尾接龙对决",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "KOREAN WORD CHAIN DUEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                    }

                    // 连击得分胶囊
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF111827))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🔥 连击 $comboScore",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    if (errorMessage != null) {
                        Text(
                            text = "⚠️ $errorMessage",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            placeholder = {
                                val lastChar = lastItem?.word?.lastOrNull()
                                val variants = if (lastChar != null) WordChainEngine.getDueumVariants(lastChar) else emptyList()
                                val hint = if (variants.size > 1) "「${variants.joinToString("/")}」开头..." else "以「$lastChar」开头..."
                                Text(hint, fontSize = 13.sp, color = Color(0xFF9CA3AF))
                            },
                            singleLine = true,
                            enabled = !isAiThinking,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedContainerColor = Color(0xFFF9FAFB),
                                focusedBorderColor = Color(0xFF111827),
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )

                        Surface(
                            shape = CircleShape,
                            color = if (userInput.isNotBlank() && !isAiThinking) Color(0xFF111827) else Color(0xFFE5E7EB),
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .clickable(enabled = userInput.isNotBlank() && !isAiThinking) {
                                    submitWord(userInput)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "发送",
                                    tint = if (userInput.isNotBlank() && !isAiThinking) Color.White else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF7F8FA),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            itemsIndexed(history) { index, item ->
                WordChainBubble(
                    item = item,
                    index = index + 1,
                    onPlayAudio = { ttsManager.speak(item.word) }
                )
            }

            if (isAiThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF3F4F6),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Text(
                                text = "🤖 AI 正在快速检索词库中...",
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordChainBubble(
    item: WordChainItem,
    index: Int,
    onPlayAudio: () -> Unit
) {
    val isUser = item.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) Color(0xFF111827) else Color.White,
            border = BorderStroke(1.dp, if (isUser) Color(0xFF111827) else Color(0xFFE5E7EB)),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "第 $index 步 (玩家)" else "第 $index 步 (AI 对手)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )

                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "发音",
                            tint = if (isUser) Color.White else Color(0xFF111827),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // 核心单词大字
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = item.word,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else Color(0xFF111827)
                    )
                    if (item.hanja != null) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "(${item.hanja})",
                            fontSize = 11.sp,
                            color = if (isUser) Color(0xFFD1D5DB) else Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = item.chineseMeaning,
                    fontSize = 12.sp,
                    color = if (isUser) Color(0xFFE5E7EB) else Color(0xFF4B5563)
                )
            }
        }
    }
}
