package com.yonsei.koreanlearner.ui.screen.wordcard

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.ui.components.OptimizedFlipCard
import com.yonsei.koreanlearner.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

// ══════════════════════════════════════════════════════════════════
// WordCardScreen — 单词记忆模块主界面
// ══════════════════════════════════════════════════════════════════

/**
 * 单词卡片学习界面（完整闭环，纯离线运行）。
 *
 * 布局结构：
 * ┌────────────────────────────────────┐
 * │  顶栏：册/课 + 进度条 + 准确率       │
 * ├────────────────────────────────────┤
 * │                                    │
 * │   3D 翻卡区（正/背面，含例句）        │
 * │                                    │
 * ├────────────────────────────────────┤
 * │  [遗忘] [吃力] [认识] [掌握] │
 * │  （仅翻面后显示，含渐变滑入动画）      │
 * └────────────────────────────────────┘
 *
 * @param book     初始册数
 * @param lesson   初始课次
 * @param onBack   返回上级
 */
@Composable
fun WordCardScreen(
    book      : Int = 1,
    lesson    : Int = 1,
    onBack    : () -> Unit = {},
    viewModel : WordCardViewModel = hiltViewModel()
) {
    val state           by viewModel.state.collectAsStateWithLifecycle()
    val ttsState        by viewModel.ttsState.collectAsStateWithLifecycle()
    val isPlayingAudio  = ttsState is com.yonsei.koreanlearner.core.audio.TtsPlaybackState.Playing
    val snackbarHost    = remember { SnackbarHostState() }
    val haptic          = LocalHapticFeedback.current
    val coroutineScope  = rememberCoroutineScope()

    // ── 翻卡动画状态 ───────────────────────────────────────────
    var showBack by remember { mutableStateOf(false) }
    var showStoryShare by remember { mutableStateOf(false) }

    // 每次切换新卡片，瞬间重置为正面显示
    LaunchedEffect(state.currentCard?.wordId) {
        showBack = false
    }

    // 卡片滑出动画
    val slideOffsetX = remember { Animatable(0f) }
    val slideAlpha   = remember { Animatable(1f) }

    // ── Effect 消费 ────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                WordCardEffect.TriggerCardFlip -> {
                    showBack = !showBack
                }
                is WordCardEffect.PlayHaptic -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                is WordCardEffect.AnimateCardOut -> {
                    showBack = false
                    val targetX = if (effect.toRight) 1400f else -1400f
                    coroutineScope.launch {
                        launch { slideOffsetX.animateTo(targetX, tween(240, easing = FastOutLinearInEasing)) }
                        launch { slideAlpha.animateTo(0f, tween(200)) }
                    }
                    kotlinx.coroutines.delay(220)
                    // 重置动画状态，准备下一张
                    slideOffsetX.snapTo(0f)
                    slideAlpha.snapTo(1f)
                }
                is WordCardEffect.SpeakKorean -> {
                    viewModel.ttsManager.speak(effect.text)
                }
                is WordCardEffect.ShowSnackbar -> {
                    snackbarHost.showSnackbar(effect.message)
                }
                WordCardEffect.ShowLessonComplete -> { /* handled by state */ }
                is WordCardEffect.NavigateToLesson -> { /* handled by NavGraph */ }
            }
        }
    }

    // ── 初始化课次 ─────────────────────────────────────────────
    LaunchedEffect(book, lesson) {
        viewModel.onIntent(WordCardIntent.SelectLesson(book, lesson))
    }

    Scaffold(
        snackbarHost    = { SnackbarHost(snackbarHost) },
        containerColor  = Color(0xFFF7F8FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 顶栏（含 Story 快照分享按钮）─────────────────────
            WordCardTopBar(
                state = state,
                onBack = onBack,
                onOpenStoryShare = { showStoryShare = true }
            )

            when {
                state.isLoading       -> WordCardLoadingState()
                state.isSessionDone   -> WordCardSessionComplete(state, viewModel)
                state.isQueueEmpty    -> WordCardEmptyState(viewModel)
                else -> {
                    val card = state.currentCard
                    if (card != null) {
                        WordCardContent(
                            card            = card,
                            state           = state,
                            showBack        = showBack,
                            offsetX         = slideOffsetX.value,
                            cardAlpha       = slideAlpha.value,
                            isPlayingAudio  = isPlayingAudio,
                            onFlip          = { viewModel.onIntent(WordCardIntent.FlipCard) },
                            onRate          = { rating -> viewModel.onIntent(WordCardIntent.SubmitRating(rating)) },
                            onSkip          = { viewModel.onIntent(WordCardIntent.SkipCard) },
                            onPronounce     = { viewModel.onIntent(WordCardIntent.PlayPronunciation) }
                        )
                    }
                }
            }
        }
    }

    // ── 9:16 Instagram Story 快照分享弹窗 ────────────────────────
    if (showStoryShare && state.currentCard != null) {
        val snapshotData = remember(state.currentCard) {
            com.yonsei.koreanlearner.ui.components.StorySnapshotGenerator.StorySnapshotData.fromWordCard(
                card = state.currentCard!!,
                streakDays = 7
            )
        }
        com.yonsei.koreanlearner.ui.components.StoryShareBottomSheet(
            snapshotData = snapshotData,
            onDismiss = { showStoryShare = false }
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WordCardTopBar(
    state: WordCardUiState,
    onBack: () -> Unit,
    onOpenStoryShare: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 册/课信息（左侧主标）
            Column {
                Text(
                    text          = "第${state.currentBook}册 · 第${state.currentLesson}课",
                    fontSize      = 20.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = Color(0xFF111827),
                    letterSpacing = (-0.5).sp
                )
                if (state.sessionTotal > 0) {
                    Text(
                        text  = "正确率 ${(state.accuracy * 100).toInt()}%  ·  已刷 ${state.sessionCorrect}/${state.sessionTotal}",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 进度指示胶囊
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111827))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text          = "${state.currentIndex + 1} / ${state.newWordQueue.size}",
                    fontSize      = 12.sp,
                    color         = Color.White,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // 极细进度条
        val progress by animateFloatAsState(
            targetValue   = if (state.newWordQueue.isEmpty()) 0f
                            else (state.currentIndex + 1).toFloat() / state.newWordQueue.size,
            animationSpec = tween(300),
            label         = "progress"
        )
        LinearProgressIndicator(
            progress      = { progress },
            modifier      = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color         = Color(0xFF111827),
            trackColor    = Color(0xFFE5E7EB)
        )
    }
}

// ══════════════════════════════════════════════════════════════════
// 卡片区域
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WordCardContent(
    card            : WordCard,
    state           : WordCardUiState,
    showBack        : Boolean,
    offsetX         : Float,
    cardAlpha       : Float,
    isPlayingAudio  : Boolean,
    onFlip          : () -> Unit,
    onRate          : (ReviewRating) -> Unit,
    onSkip          : () -> Unit,
    onPronounce     : () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 120fps 无撕裂 3D 翻卡（绑定 wordId 保证切词瞬间从正面展开） ───────
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            key(card.wordId) {
                OptimizedFlipCard(
                    isFlipped   = showBack,
                    onCardClick = onFlip,
                    modifier    = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.92f)
                        .graphicsLayer {
                            translationX = offsetX
                            alpha        = cardAlpha
                        },
                    frontContent = {
                        CardFront(
                            card        = card,
                            onPronounce = onPronounce,
                            isPlaying   = isPlayingAudio
                        )
                    },
                    backContent = {
                        CardBack(card = card)
                    }
                )
            }
        }

        // ── 底部评分按钮区（翻面后出现） ─────────────────────────
        RatingSection(
            visible      = showBack && !state.isSubmitting,
            isSubmitting = state.isSubmitting,
            onRate       = onRate,
            onSkip       = onSkip
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ── 卡片正面（韩文 + 发音 + 词性标签） ─────────────────────────────

@Composable
private fun CardFront(
    card: WordCard,
    onPronounce: () -> Unit,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部：轮次 + Level 徽章行
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LevelChip(card.level)
            if (card.irregularType != null) {
                IrregularChip(card.irregularType)
            }
        }

        // 核心视觉区：韩文大字 + 罗马音 + 发音按钮 + 词性
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text       = card.korean,
                fontSize   = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                lineHeight = 52.sp,
                color      = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            card.romanization?.let { rom ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "[$rom]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(18.dp))

            // 发音按钮
            IconButton(
                onClick  = onPronounce,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector       = Icons.Default.VolumeUp,
                    contentDescription = "播放发音",
                    tint = if (isPlaying) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            // 词性
            Text(
                text  = card.partOfSpeech,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 底部提示
        Text(
            text  = "点击翻转查看释义与例句 ↓",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

// ── 卡片背面（中文 + 不规则变形提示 + 例句） ──────────────────────

@Composable
private fun CardBack(card: WordCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 中文释义
        Text(
            text       = card.chinese,
            fontSize   = 30.sp,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )

        // 不规则变形提示卡（核心功能）
        card.irregularType?.let { irregular ->
            IrregularConjugationCard(
                korean       = card.korean,
                irregularType = irregular
            )
        }

        // 例句区
        if (card.exampleSentences.isNotEmpty()) {
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Text(
                "例句",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            card.exampleSentences.forEach { ex ->
                ExampleSentenceCard(ex, card.korean)
            }
        }

        // 记忆状态统计
        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        MemoryStatsRow(card)
    }
}

// ── 不规则变形提示卡 ─────────────────────────────────────────────

/**
 * 根据不规则类型动态生成变形规则说明。
 * 完全离线，基于内置规则表。
 */
@Composable
private fun IrregularConjugationCard(korean: String, irregularType: String) {
    val rules = remember(irregularType) {
        buildIrregularRules(irregularType)
    }

    Surface(
        color  = Color(0xFFFEF2F2),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFFEE2E2))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = irregularType,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
            rules.forEach { (stem, result) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stem,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Text("→", color = MaterialTheme.colorScheme.error)
                    Text(
                        result,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** 内置不规则变形规则表（离线，完整覆盖延世教材） */
private fun buildIrregularRules(type: String): List<Pair<String, String>> = when (type) {
    "ㅂ不规则" -> listOf(
        "词干+아/어" to "ㅂ→워 (돕다→도워)",
        "词干+으" to "ㅂ→우 (곱다→고우면)",
        "例外：돕다、곱다" to "ㅂ→와 (돕다→도와)"
    )
    "ㄷ不规则" -> listOf(
        "词干+아/어" to "ㄷ→ㄹ (듣다→들어)",
        "词干+으" to "ㄷ→ㄹ (듣다→들으면)",
        "例外：받다、믿다" to "ㄷ规则变化"
    )
    "ㅅ不规则" -> listOf(
        "词干+아/어" to "ㅅ脱落 (낫다→나아)",
        "词干+으" to "ㅅ脱落 (낫다→나으면)",
        "例外：벗다、씻다" to "ㅅ规则变化"
    )
    "르不规则" -> listOf(
        "词干+아/어" to "르→르+ㄹ (모르다→몰라)",
        "规律" to "르 前一音节加ㄹ收音"
    )
    "으脱落" -> listOf(
        "词干(으)+아/어" to "으脱落 (크다→커)",
        "조건" to "词干末为으且前有元音时脱落"
    )
    "ㅎ不规则" -> listOf(
        "词干+아/어" to "ㅎ脱落+애 (파랗다→파래)",
        "词干+으면" to "ㅎ脱落 (파랗다→파라면)",
        "例外：좋다" to "ㅎ规则变化"
    )
    else -> listOf("参考" to "规则变化")
}

// ── 例句卡 ───────────────────────────────────────────────────────

@Composable
private fun ExampleSentenceCard(example: ExampleSentence, targetWord: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // 韩文例句（高亮目标词汇）
        val annotatedKorean = remember(example.korean, targetWord) {
            buildAnnotatedString {
                val base = example.korean
                val idx  = base.indexOf(targetWord.substringBefore(" ").take(3))
                if (idx >= 0 && targetWord.isNotEmpty()) {
                    append(base.substring(0, idx))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))) {
                        append(base.substring(idx, (idx + targetWord.length).coerceAtMost(base.length)))
                    }
                    append(base.substring((idx + targetWord.length).coerceAtMost(base.length)))
                } else {
                    append(base)
                }
            }
        }
        Text(annotatedKorean, style = MaterialTheme.typography.bodyMedium)
        Text(
            example.chinese,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── 记忆统计行 ───────────────────────────────────────────────────

@Composable
private fun MemoryStatsRow(card: WordCard) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem("第${card.repetitionLevel}轮", "复习轮次")
        StatItem(String.format("%.1f", card.easeFactor), "难度系数")
        StatItem(if (card.isErrorItem) "错题集" else "正常", "状态")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ══════════════════════════════════════════════════════════════════
// 四级评分按钮区
// ══════════════════════════════════════════════════════════════════

@Composable
private fun RatingSection(
    visible      : Boolean,
    isSubmitting : Boolean,
    onRate       : (ReviewRating) -> Unit,
    onSkip       : () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn() + slideInVertically { it / 3 },
        exit    = fadeOut()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 四色评分按钮
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReviewRating.entries.forEach { rating ->
                    RatingButton(
                        rating      = rating,
                        enabled     = !isSubmitting,
                        onClick     = { onRate(rating) },
                        modifier    = Modifier.weight(1f)
                    )
                }
            }

            // 跳过按钮（文字，低调）
            TextButton(
                onClick  = onSkip,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                enabled  = !isSubmitting
            ) {
                Text(
                    "跳过（稍后再看）",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatingButton(
    rating  : ReviewRating,
    enabled : Boolean,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPerfect = rating == ReviewRating.PERFECT
    val accentColor = Color(android.graphics.Color.parseColor(rating.colorHex))

    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (isPerfect) Color(0xFF111827) else Color.White,
            contentColor   = if (isPerfect) Color.White else Color(0xFF111827)
        ),
        border = if (isPerfect) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isPerfect) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text       = rating.label,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                softWrap   = false
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 工具子组件
// ══════════════════════════════════════════════════════════════════

@Composable
private fun LevelChip(level: String) {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
        Text(level, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun IrregularChip(type: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)) {
        Text(type, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun WordCardLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun WordCardEmptyState(viewModel: WordCardViewModel) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("本课新词已全部学完", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("去复习已学单词巩固记忆", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WordCardSessionComplete(state: WordCardUiState, viewModel: WordCardViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 成就头像
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.radialGradient(InsGradientColors),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("", fontSize = 52.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("本课完成！", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))

        // 准确率展示
        Text(
            "准确率 ${(state.accuracy * 100).toInt()}%  ·  ${state.sessionCorrect}/${state.sessionTotal} 正确",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { viewModel.onIntent(WordCardIntent.RestartLesson) },
                shape   = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Text("再背一遍") }

            Button(
                onClick  = { viewModel.onIntent(WordCardIntent.GoToNextLesson) },
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).height(52.dp)
            ) { Text("下一课 →") }
        }
    }
}
