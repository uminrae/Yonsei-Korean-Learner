package com.yonsei.koreanlearner.ui.screen.lens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.yonsei.koreanlearner.algorithm.korean.DissectedToken
import com.yonsei.koreanlearner.algorithm.korean.KoreanMorphLensEngine
import com.yonsei.koreanlearner.algorithm.korean.LensDissectionResult
import com.yonsei.koreanlearner.core.audio.TtsManager

/**
 * ══════════════════════════════════════════════════════════════════
 * 万能韩文长句语素与台词拆解镜界面 (KoreanLensScreen)
 * 纯白高光 Ins 风格，纯净输入驱动，实时剖析词形还原与附着成分
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun KoreanLensScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val ttsManager = remember { TtsManager(context) }
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    var inputText by remember { mutableStateOf("") }
    var dissectionResult by remember { mutableStateOf<LensDissectionResult?>(null) }
    val savedTokens = remember { mutableStateListOf<String>() }

    fun runDissect(text: String) {
        inputText = text
        dissectionResult = if (text.isNotBlank()) KoreanMorphLensEngine.dissectSentence(text) else null
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
                            text = "韩文长句语素拆解镜",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "VISUAL MORPHOLOGICAL LENS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .clickable {
                                val clip = clipboardManager.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pasteText = clip.getItemAt(0).text?.toString() ?: ""
                                    if (pasteText.isNotBlank()) {
                                        runDissect(pasteText)
                                        Toast.makeText(context, "已读取剪贴板内容", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("粘贴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. 核心长句输入框 ─────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { runDissect(it) },
                        placeholder = {
                            Text(
                                text = "在此输入或粘贴任何韩文长难句、台词或文本...",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                lineHeight = 20.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    val result = dissectionResult
                    if (result != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF111827))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(result.speechLevel, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(result.sentenceType, fontSize = 11.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                                }
                            }

                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    ttsManager.speak(inputText)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF111827))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "朗读整句",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. 语素级逐词剖析流（输入有内容时才呈现） ─────────────
            val result = dissectionResult
            if (result != null && result.tokens.isNotEmpty()) {
                Text(
                    text = "语素级逐词剖析流",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    result.tokens.forEach { token ->
                        MorphTokenCard(
                            token = token,
                            isSaved = savedTokens.contains(token.rawText),
                            onPlayAudio = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                ttsManager.speak(token.rawText)
                            },
                            onSave = {
                                if (!savedTokens.contains(token.rawText)) {
                                    savedTokens.add(token.rawText)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "已加入延世 7 轮复习库", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            } else if (inputText.isBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "输入或粘贴韩语句子",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "系统将自动逆向拆解词典原形、词性、阶称与附着助词/活用词尾",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MorphTokenCard(
    token: DissectedToken,
    isSaved: Boolean,
    onPlayAudio: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = token.rawText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "朗读",
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (isSaved) Modifier.background(Color(0xFFF3F4F6))
                            else Modifier
                                .background(Color.White)
                                .border(1.dp, Color(0xFF111827), RoundedCornerShape(10.dp))
                        )
                        .clickable(enabled = !isSaved) { onSave() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (isSaved) "已加入复习" else "+ 加入复习",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSaved) Color(0xFF9CA3AF) else Color(0xFF111827)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9FAFB),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("词典原形", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(token.rootWord, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE5E7EB))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(token.partOfSpeech, fontSize = 9.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!token.particleOrEnding.isNullOrBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("附着成分", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(token.particleOrEnding, fontSize = 13.sp, color = Color(0xFF4B5563), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "释义: ${token.chineseMeaning}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}
