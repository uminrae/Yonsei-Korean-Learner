package com.yonsei.koreanlearner.ui.screen.pitch

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ══════════════════════════════════════════════════════════════════
 * 口语跟读纠音与双轨声调音高重叠对比界面 (ShadowingTrainerScreen)
 * 纯白高光 Ins 风格，标准母语声调基线与用户跟读音高动态拟合
 * ══════════════════════════════════════════════════════════════════
 */

data class ShadowingSample(
    val id: String,
    val koreanText: String,
    val chineseMeaning: String,
    val toneRuleTip: String,
    val nativePitchPoints: List<Float> // 0f ~ 1f 归一化音高走势
)

object ShadowingSampleData {
    val SAMPLES = listOf(
        ShadowingSample(
            "s1",
            "안녕하세요, 처음 뵙겠습니다.",
            "您好，初次见面。",
            "问候语：开头平稳起音，句尾「뵙겠습니다」平缓下沉收束",
            listOf(0.5f, 0.55f, 0.6f, 0.5f, 0.45f, 0.4f, 0.35f, 0.3f)
        ),
        ShadowingSample(
            "s2",
            "지금 어디에 가시는 길이에요?",
            "您现在是正要去哪里吗？",
            "疑问句：尾音「길이에요?」呈现明显上扬曲线",
            listOf(0.4f, 0.45f, 0.5f, 0.6f, 0.65f, 0.75f, 0.9f)
        ),
        ShadowingSample(
            "s3",
            "한국어 공부가 정말 재미있어요!",
            "韩语学习真的太有意思了！",
            "感叹句：「정말」重音强调微峰值，「재미있어요」欢快起伏",
            listOf(0.4f, 0.45f, 0.8f, 0.75f, 0.5f, 0.6f, 0.7f, 0.5f)
        ),
        ShadowingSample(
            "s4",
            "나는 아픈 건 딱 질색이니까.",
            "(G)I-DLE 经典歌词：因为我最讨厌生病难受了。",
            "歌词语调：「딱 질색」短促紧音重音，随后自然衔接",
            listOf(0.5f, 0.55f, 0.6f, 0.85f, 0.9f, 0.65f, 0.5f, 0.4f)
        )
    )
}

@Composable
fun ShadowingTrainerScreen(
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val ttsManager = remember { TtsManager(context) }
    val scope = rememberCoroutineScope()

    var selectedSample by remember { mutableStateOf(ShadowingSampleData.SAMPLES.first()) }
    var isRecording by remember { mutableStateOf(false) }
    var userRecordedPoints by remember { mutableStateOf<List<Float>?>(null) }
    var matchScore by remember { mutableIntStateOf(0) }

    fun startRecordingSimulation() {
        isRecording = true
        userRecordedPoints = null
        matchScore = 0
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        scope.launch {
            delay(2500) // 模拟 2.5 秒跟读录音
            isRecording = false
            // 生成贴近母语基线的拟真发音音高
            val native = selectedSample.nativePitchPoints
            val userPoints = native.map { p ->
                (p + (kotlin.random.Random.nextFloat() * 0.16f - 0.08f)).coerceIn(0.1f, 0.95f)
            }
            userRecordedPoints = userPoints
            matchScore = kotlin.random.Random.nextInt(91, 99)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                            text = "双轨声调纠音跟读",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "SHADOWING PITCH VISUALIZER",
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
                        Text(if (matchScore > 0) "吻合度 $matchScore%" else "双轨音高", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. 例句选择 Chip ─────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(ShadowingSampleData.SAMPLES, key = { it.id }) { sample ->
                    val isSel = sample.id == selectedSample.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSel) Modifier.background(Color(0xFF111827))
                                else Modifier
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                            )
                            .clickable {
                                selectedSample = sample
                                userRecordedPoints = null
                                matchScore = 0
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sample.koreanText.take(8) + "...",
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // ── 2. 当前跟读目标卡片 ─────────────────────────────
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
                        Text("跟读目标句", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                        IconButton(onClick = { ttsManager.speak(selectedSample.koreanText) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "母语朗读",
                                tint = Color(0xFF111827),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = selectedSample.koreanText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        lineHeight = 28.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = selectedSample.chineseMeaning,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "💡 声调规律: ${selectedSample.toneRuleTip}",
                            fontSize = 11.sp,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }

            // ── 3. 双轨音高对比画布 (Canvas) ───────────────────────
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF111827)))
                                Spacer(Modifier.width(4.dp))
                                Text("母语标准基准线", fontSize = 10.sp, color = Color(0xFF6B7280))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2563EB)))
                                Spacer(Modifier.width(4.dp))
                                Text("我的跟读声调", fontSize = 10.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    DualPitchCanvas(
                        nativePoints = selectedSample.nativePitchPoints,
                        userPoints = userRecordedPoints,
                        isRecording = isRecording,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ── 4. 跟读录音触发按钮 ───────────────────────────────
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isRecording) Color(0xFFEF4444) else Color(0xFF111827),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(!isRecording) { startRecordingSimulation() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isRecording) "🎙️ 正在倾听您的跟读声调..." else "🎙️ 按住开始跟读纠音",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun DualPitchCanvas(
    nativePoints: List<Float>,
    userPoints: List<Float>?,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // 绘制辅助水平参考虚线
        drawLine(
            color = Color(0xFFF3F4F6),
            start = Offset(0f, height * 0.25f),
            end = Offset(width, height * 0.25f),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0xFFF3F4F6),
            start = Offset(0f, height * 0.5f),
            end = Offset(width, height * 0.5f),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0xFFF3F4F6),
            start = Offset(0f, height * 0.75f),
            end = Offset(width, height * 0.75f),
            strokeWidth = 1f
        )

        // 1. 绘制母语标准音高曲线（黑色平滑曲线）
        if (nativePoints.size > 1) {
            val step = width / (nativePoints.size - 1)
            val path = Path().apply {
                moveTo(0f, height * (1f - nativePoints.first()))
                for (i in 1 until nativePoints.size) {
                    lineTo(i * step, height * (1f - nativePoints[i]))
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF111827),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 2. 绘制用户录音音高曲线（蓝色平滑曲线）
        if (userPoints != null && userPoints.size > 1) {
            val step = width / (userPoints.size - 1)
            val userPath = Path().apply {
                moveTo(0f, height * (1f - userPoints.first()))
                for (i in 1 until userPoints.size) {
                    lineTo(i * step, height * (1f - userPoints[i]))
                }
            }
            drawPath(
                path = userPath,
                color = Color(0xFF2563EB),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
