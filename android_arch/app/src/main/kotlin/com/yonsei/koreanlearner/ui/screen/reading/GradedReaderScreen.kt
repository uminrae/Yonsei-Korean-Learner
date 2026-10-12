package com.yonsei.koreanlearner.ui.screen.reading

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.reading.GradedArticle
import com.yonsei.koreanlearner.data.local.reading.GradedReadingData
import com.yonsei.koreanlearner.data.local.reading.GradedSentence

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世精读短文与双语分级阅读专栏界面 (GradedReaderScreen)
 * 纯白高光 Ins 风格，支持延世 1~6 册分级阅读、点句即读与语法解析
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun GradedReaderScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var selectedArticle by remember { mutableStateOf(GradedReadingData.ARTICLES.first()) }
    var isBilingualMode by remember { mutableStateOf(true) }
    var activePlayingIndex by remember { mutableIntStateOf(-1) }

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
                            text = "延世双语分级精读",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "YONSEI GRADED READER LAB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                    }

                    // 沉浸/中韩切换
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .then(
                                if (isBilingualMode) Modifier.background(Color(0xFF111827))
                                else Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            )
                            .clickable { isBilingualMode = !isBilingualMode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBilingualMode) "中韩对照" else "纯韩沉浸",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBilingualMode) Color.White else Color(0xFF111827)
                        )
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
        ) {
            // ── 1. 册数横向切换 Chip ─────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GradedReadingData.ARTICLES, key = { it.id }) { article ->
                    val isSel = article.id == selectedArticle.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSel) Modifier.background(Color(0xFF111827))
                                else Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                            )
                            .clickable {
                                selectedArticle = article
                                activePlayingIndex = -1
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "第 ${article.bookLevel} 册 · ${article.title.take(6)}...",
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // ── 2. 文章列表与段落解析 ───────────────────────────
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 顶部文章标题卡片
                item {
                    ArticleHeaderCard(
                        article = selectedArticle,
                        onPlayAll = {
                            val fullText = selectedArticle.sentences.joinToString(" ") { it.korean }
                            ttsManager.speak(fullText)
                        }
                    )
                }

                // 核心生词词条速览
                item {
                    CoreVocabCard(vocabList = selectedArticle.coreVocabList)
                }

                // 逐句精读卡片列表
                itemsIndexed(selectedArticle.sentences) { index, sentence ->
                    val isPlaying = index == activePlayingIndex

                    SentenceReadingCard(
                        sentence = sentence,
                        index = index + 1,
                        isPlaying = isPlaying,
                        isBilingual = isBilingualMode,
                        onPlaySentence = {
                            activePlayingIndex = index
                            ttsManager.speak(sentence.korean)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleHeaderCard(
    article: GradedArticle,
    onPlayAll: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF111827))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "延世第 ${article.bookLevel} 册精选",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 全篇朗读
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF3F4F6),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onPlayAll() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "全篇朗读",
                            tint = Color(0xFF111827),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = article.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827),
                lineHeight = 28.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = article.chineseTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = article.summary,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CoreVocabCard(
    vocabList: List<Pair<String, String>>
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "💡 本篇核心词汇与固定搭配",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                vocabList.forEach { (kr, cn) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Column {
                            Text(kr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text(cn, fontSize = 9.sp, color = Color(0xFF6B7280))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentenceReadingCard(
    sentence: GradedSentence,
    index: Int,
    isPlaying: Boolean,
    isBilingual: Boolean,
    onPlaySentence: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPlaying) Color.White else Color(0xFFFAFAFC),
        border = BorderStroke(
            width = if (isPlaying) 1.5.dp else 1.dp,
            color = if (isPlaying) Color(0xFF111827) else Color(0xFFE5E7EB)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlaySentence() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S$index",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) Color(0xFF111827) else Color(0xFF9CA3AF)
                )

                IconButton(onClick = onPlaySentence, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "朗读",
                        tint = if (isPlaying) Color(0xFF111827) else Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = sentence.korean,
                fontSize = 16.sp,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Medium,
                color = Color(0xFF111827),
                lineHeight = 24.sp
            )

            if (isBilingual) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = sentence.chinese,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 18.sp
                )
            }

            if (!sentence.grammarNote.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📌 语法拆解: ${sentence.grammarNote}",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5563)
                    )
                }
            }
        }
    }
}
