package com.yonsei.koreanlearner.ui.screen.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.media.KMediaCorpusData
import com.yonsei.koreanlearner.data.local.media.KMediaQuote

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩剧影视台词与地道口语虚词精析 (KMediaCorpusScreen)
 * 纯白极简 Ins 风格，原声语料与地道虚词用法精剖
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun KMediaCorpusScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            KMediaTopBar(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "header_intro") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    Text(
                        text = "经典韩剧真实语料",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "告别生硬课本语法，掌握母语者在实际影视中高频使用的情绪虚词与句尾语气。",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 17.sp
                    )
                }
            }

            items(KMediaCorpusData.SPOKEN_ENDINGS_LIST, key = { it.dramaTitle }) { quote ->
                MediaQuoteCard(
                    quote = quote,
                    onPlayAudio = { ttsManager.speak(quote.lineKorean) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun KMediaTopBar(onBack: () -> Unit) {
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
                    text = "韩剧台词与口语",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "K-DRAMA CORPUS & SPOKEN NUANCE",
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
// 台词卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun MediaQuoteCard(
    quote: KMediaQuote,
    onPlayAudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quote.dramaTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280)
                )

                IconButton(onClick = onPlayAudio) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "播放台词原声", tint = Color(0xFF111827))
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "“ ${quote.lineKorean} ”",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                lineHeight = 25.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = quote.lineChinese,
                fontSize = 13.sp,
                color = Color(0xFF4B5563),
                lineHeight = 19.sp
            )

            Spacer(Modifier.height(14.dp))

            // 虚词核心剖析底卡
            Surface(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF111827))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "核心句尾：${quote.keySpokenEnding}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = quote.endingGrammarExplanation,
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
