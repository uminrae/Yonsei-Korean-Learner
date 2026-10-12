package com.yonsei.koreanlearner.ui.screen.search

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.yonsei.koreanlearner.algorithm.search.SearchResultItem
import com.yonsei.koreanlearner.algorithm.search.UniversalSearchEngine
import com.yonsei.koreanlearner.core.audio.TtsManager

/**
 * ══════════════════════════════════════════════════════════════════
 * 全局聚合秒级“超级搜索”与延世大词典界面 (UniversalSearchScreen)
 * 纯白高光 Ins 风格，秒级跨库检索词汇、语法、沙盒功能与歌词
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun UniversalSearchScreen(
    onNavigateToRoute: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf(UniversalSearchEngine.search("")) }

    val hotTags = listOf("안녕하세요", "-더라고요", "한국어", "接龙", "Fate", "音变", "비약적")

    fun onQueryChange(text: String) {
        searchQuery = text
        searchResult = UniversalSearchEngine.search(text)
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFFF7F8FA), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onBack != null) {
                            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF111827))
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { onQueryChange(it) },
                            placeholder = { Text("搜索延世词汇、语法、歌词或功能...", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "搜索", tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "清空", tint = Color(0xFF9CA3AF), modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).defaultMinSize(minHeight = 50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF111827),
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                    }

                    // 热门搜索标签
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(hotTags) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
                                    .clickable { onQueryChange(tag) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(tag, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Medium)
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("全局跨库超级检索", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "支持 4,446 延世词汇、328 条语法与 (G)I-DLE 歌词秒级中韩双向搜索",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                // 1. 词汇匹配组
                if (searchResult.wordMatches.isNotEmpty()) {
                    item {
                        Text("📘 延世词汇库匹配 (${searchResult.wordMatches.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    items(searchResult.wordMatches) { word ->
                        WordSearchCard(word = word, onPlayAudio = { ttsManager.speak(word.korean) })
                    }
                }

                // 2. 语法树匹配组
                if (searchResult.grammarMatches.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("🌲 延世语法树匹配 (${searchResult.grammarMatches.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    items(searchResult.grammarMatches) { grammar ->
                        GrammarSearchCard(grammar = grammar)
                    }
                }

                // 3. 专项沙盒匹配组
                if (searchResult.featureMatches.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("🚀 专项研习沙盒直达 (${searchResult.featureMatches.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    items(searchResult.featureMatches) { feature ->
                        FeatureSearchCard(feature = feature, onNavigate = { onNavigateToRoute(feature.route) })
                    }
                }

                // 4. 歌词出处匹配组
                if (searchResult.lyricalMatches.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text("🎵 歌词与台词出处匹配 (${searchResult.lyricalMatches.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    }
                    items(searchResult.lyricalMatches) { lyrical ->
                        LyricalSearchCard(lyrical = lyrical, onPlayAudio = { ttsManager.speak(lyrical.matchedSentence) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WordSearchCard(
    word: SearchResultItem.WordMatch,
    onPlayAudio: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.korean, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    if (word.hanja != null) {
                        Spacer(Modifier.width(6.dp))
                        Text("(${word.hanja})", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("延世第${word.bookLevel}册", fontSize = 9.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(word.chinese, fontSize = 13.sp, color = Color(0xFF4B5563))
            }

            IconButton(onClick = onPlayAudio, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读", tint = Color(0xFF111827), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun GrammarSearchCard(
    grammar: SearchResultItem.GrammarMatch
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(grammar.pattern, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF111827))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("第${grammar.bookLevel}册语法", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(grammar.meaning, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
    }
}

@Composable
private fun FeatureSearchCard(
    feature: SearchResultItem.FeatureMatch,
    onNavigate: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAFAFC),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onNavigate() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Spacer(Modifier.height(2.dp))
                Text(feature.description, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
            Text("直达 →", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
    }
}

@Composable
private fun LyricalSearchCard(
    lyrical: SearchResultItem.LyricalMatch,
    onPlayAudio: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${lyrical.songTitle} · ${lyrical.artist}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B7280))
                IconButton(onClick = onPlayAudio, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "朗读", tint = Color(0xFF111827), modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(lyrical.matchedSentence, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
            if (lyrical.chinese != null) {
                Spacer(Modifier.height(2.dp))
                Text(lyrical.chinese, fontSize = 11.sp, color = Color(0xFF9CA3AF))
            }
        }
    }
}
