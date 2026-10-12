package com.yonsei.koreanlearner.ui.screen.radio

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * ══════════════════════════════════════════════════════════════════
 * 锁屏后台“随身磨耳朵”电台界面 (BackgroundRadioScreen)
 * 纯白高光 Ins 风格，支持后台随身循环磨耳朵、语意顿音与全景声学律动
 * ══════════════════════════════════════════════════════════════════
 */

data class RadioTrackItem(
    val id: String,
    val koreanWord: String,
    val chineseMeaning: String,
    val exampleSentence: String,
    val exampleChinese: String,
    val bookTag: String
)

object RadioTrackData {
    val TRACKS = listOf(
        RadioTrackItem("r1", "안녕하세요", "您好（日常最核心问候）", "선생님, 안녕하세요!", "老师，您好！", "延世第 1 册"),
        RadioTrackItem("r2", "감사합니다", "谢谢/由衷感谢", "도와주셔서 정말 감사합니다.", "非常感谢您的热情帮助。", "延世第 1 册"),
        RadioTrackItem("r3", "공부하다", "学习/用功钻研", "도서관에서 한국어를 공부합니다.", "在图书馆里学习韩国语。", "延世第 1 册"),
        RadioTrackItem("r4", "아름답다", "美丽的/优美的", "한국의 사계절은 참 아름답습니다.", "韩国的四季真的非常优美。", "延世第 2 册"),
        RadioTrackItem("r5", "약속하다", "约定/许下诺言", "친구와 주말에 만나기로 약속했습니다.", "和朋友约好周末见面。", "延世第 2 册"),
        RadioTrackItem("r6", "중요하다", "重要的/至关重要的", "건강을 유지하는 것이 가장 중요합니다.", "保持身体健康是最重要的事情。", "延世第 3 册"),
        RadioTrackItem("r7", "파급력", "影响力/波及效应", "K-Pop의 세계적인 파급력이 대단합니다.", "K-Pop 的全球影响力非常巨大。", "延世第 4 册"),
        RadioTrackItem("r8", "비약적", "飞跃性的/突飞猛进的", "인공지능 기술이 비약적으로 발전하고 있습니다.", "人工智能技术正在突飞猛进地发展。", "延世第 5 册")
    )
}

@Composable
fun BackgroundRadioScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var speed by remember { mutableFloatStateOf(1.0f) }

    val currentTrack = RadioTrackData.TRACKS[currentIndex]

    // 磨耳朵核心自动播报轮播协程
    LaunchedEffect(isPlaying, currentIndex) {
        if (isPlaying) {
            while (isActive && isPlaying) {
                // 1. 播放韩语单词
                ttsManager.speak(currentTrack.koreanWord)
                delay(2200)

                // 2. 停顿后播放例句
                ttsManager.speak(currentTrack.exampleSentence)
                delay(3500)

                // 3. 自动下一首
                currentIndex = (currentIndex + 1) % RadioTrackData.TRACKS.size
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
                            text = "随身磨耳朵电台",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "KOREAN IMMERSION RADIO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF111827))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("后台播报", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── 1. 播放模式选择 ──────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("延世 1~6 册词汇流", "经典长句精听流", "高频生活口语流").forEachIndexed { i, title ->
                    val isSel = i == 0
                    item {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSel) Modifier.background(Color(0xFF111827))
                                    else Modifier
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── 2. 核心大黑胶/卡片展示区 ─────────────────────────
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(currentTrack.bookTag, fontSize = 11.sp, color = Color(0xFF6B7280), fontWeight = FontWeight.Bold)
                    }

                    // 单词韩文大字
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentTrack.koreanWord,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF111827)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = currentTrack.chineseMeaning,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }

                    // 律动声波
                    RadioWaveformVisualizer(isPlaying = isPlaying, modifier = Modifier.fillMaxWidth().height(40.dp))

                    // 例句卡片
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF9FAFB),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = currentTrack.exampleSentence,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827),
                                lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = currentTrack.exampleChinese,
                                fontSize = 12.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 3. 控制器底栏 ─────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 上一首
                    IconButton(
                        onClick = {
                            currentIndex = if (currentIndex > 0) currentIndex - 1 else RadioTrackData.TRACKS.size - 1
                        }
                    ) {
                        Text("⏮", fontSize = 20.sp)
                    }

                    // 播放/暂停
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF111827),
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .clickable { isPlaying = !isPlaying }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isPlaying) "⏸" else "▶",
                                fontSize = 20.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 下一首
                    IconButton(
                        onClick = {
                            currentIndex = (currentIndex + 1) % RadioTrackData.TRACKS.size
                        }
                    ) {
                        Text("⏭", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radio_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radio_phase"
    )

    Canvas(modifier = modifier) {
        val barCount = 24
        val spacing = size.width / barCount
        val barWidth = spacing * 0.45f
        val maxHeight = size.height

        for (i in 0 until barCount) {
            val progress = (i.toFloat() / barCount + phase) % 1f
            val waveHeight = if (isPlaying) {
                maxHeight * (0.25f + 0.75f * kotlin.math.sin(progress * Math.PI.toFloat()).coerceAtLeast(0f))
            } else {
                maxHeight * 0.15f
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
