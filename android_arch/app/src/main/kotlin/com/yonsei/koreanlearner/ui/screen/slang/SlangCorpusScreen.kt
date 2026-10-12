package com.yonsei.koreanlearner.ui.screen.slang

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.slang.SlangCorpusData
import com.yonsei.koreanlearner.data.local.slang.SlangItem

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩国现代流行新造词与职场暗语词典 (SlangCorpusScreen)
 * 纯白高光 Ins 风格，收录 MZ 最潮新造词与真实职场沟通规范
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SlangCorpusScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("全部") }

    val filteredList = remember(searchQuery, selectedCategory) {
        SlangCorpusData.SLANG_LIST.filter { item ->
            val matchCat = selectedCategory == "全部" || item.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    item.termKr.contains(searchQuery, ignoreCase = true) ||
                    item.chineseTitle.contains(searchQuery, ignoreCase = true) ||
                    item.explanation.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            SlangTopBar(onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. 搜索栏 ─────────────────────────────────────
            item(key = "search_box") {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = { Text("搜索流行语或中文释义（如：갓생, 踩点下班...）", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(20.dp)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF111827),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                }
            }

            // ── 2. 分类筛选 Chip ────────────────────────────────
            item(key = "category_row") {
                val categories = listOf("全部", "MZ流行新造词", "职场黑话与礼仪")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it }) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isSelected) Modifier.background(Color(0xFF111827))
                                    else Modifier
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            // ── 3. 新造词卡片列表 ─────────────────────────────
            items(filteredList, key = { it.termKr }) { slang ->
                SlangDetailCard(
                    slang = slang,
                    onPlayAudio = { ttsManager.speak(slang.termKr) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SlangTopBar(onBack: () -> Unit) {
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
                    text = "流行新造词与职场",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "KOREAN SLANG & WORKPLACE",
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
// 流行词卡片（含 KakaoTalk 气泡对话还原）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun SlangDetailCard(
    slang: SlangItem,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = slang.termKr,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = slang.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        )
                    }
                }

                IconButton(onClick = onPlayAudio) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "发音", tint = Color(0xFF111827))
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = "来源：${slang.originKr}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4B5563)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = slang.chineseTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = slang.explanation,
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                lineHeight = 18.sp
            )

            if (slang.sampleChat.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))

                // 对话情景卡
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slang.sampleChat.forEach { (speaker, msg) ->
                            Row {
                                Text(
                                    text = "$speaker: ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
