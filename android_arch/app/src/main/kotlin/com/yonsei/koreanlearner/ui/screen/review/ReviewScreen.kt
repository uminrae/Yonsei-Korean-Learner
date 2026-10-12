package com.yonsei.koreanlearner.ui.screen.review

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.domain.model.ReviewRating
import com.yonsei.koreanlearner.algorithm.MemoryScheduler
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import kotlinx.coroutines.flow.collectLatest

// ════════════════════════════════════════════════════════════════
// ReviewScreen — 顶层入口
// ════════════════════════════════════════════════════════════════

/**
 * 复习主界面（纯 Compose + MVI）
 *
 * UI 结构：
 * ┌─────────────────────────────────┐
 * │  进度条 + 今日完成 x / 总 n     │
 * ├─────────────────────────────────┤
 * │                                 │
 * │      翻卡区域（3D翻转动画）       │
 * │   [正面] 韩文 + 轮次徽章         │
 * │   [背面] 中文 + 例句 + 记忆信息  │
 * │                                 │
 * ├─────────────────────────────────┤
 * │  [完全遗忘][模糊][掌握][秒杀] │
 * └─────────────────────────────────┘
 *
 * @param onSessionComplete 全部复习完成后的回调（导航到完成页面）
 */
@Composable
fun ReviewScreen(
    onSessionComplete: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── 消费一次性 Effect ──────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ReviewEffect.NavigateToCompletion -> onSessionComplete()
                is ReviewEffect.ShowSnackbar         -> snackbarHostState.showSnackbar(effect.message)
                else                                 -> { /* 动画 Effect 在各子组件内部消费 */ }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF7F8FA)
    ) { paddingValues ->
        when {
            state.isLoading         -> FullScreenLoading()
            state.isSessionComplete -> SessionCompleteScreen(
                completedCount = state.completedCount,
                onRestart = { viewModel.onIntent(ReviewIntent.RestartSession) }
            )
            state.currentItem == null -> EmptyReviewScreen(
                onRestart = { viewModel.onIntent(ReviewIntent.RestartSession) }
            )
            else -> ReviewContent(
                state   = state,
                onFlip  = { viewModel.onIntent(ReviewIntent.FlipCard) },
                onRate  = { rating -> viewModel.onIntent(ReviewIntent.SubmitRating(rating)) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 主内容区域
// ════════════════════════════════════════════════════════════════

@Composable
private fun ReviewContent(
    state: ReviewUiState,
    onFlip: () -> Unit,
    onRate: (ReviewRating) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // ── 进度条 ────────────────────────────────────────────
        ReviewProgressBar(
            completed = state.completedCount,
            total     = state.totalDueCount,
            progress  = state.progress
        )

        // ── 翻卡区域 ──────────────────────────────────────────
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            state.currentItem?.let { item ->
                key(item.record.id) {
                    FlipCard(
                        item      = item,
                        isFlipped = state.isFlipped,
                        onClick   = onFlip
                    )
                }
            }
        }

        // ── 评分按钮区（仅翻面后显示） ────────────────────────
        RatingButtonRow(
            visible = state.isFlipped,
            onRate  = onRate
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ════════════════════════════════════════════════════════════════
// 进度条
// ════════════════════════════════════════════════════════════════

@Composable
private fun ReviewProgressBar(
    completed: Int,
    total: Int,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text          = "今日复习队列",
                fontSize      = 18.sp,
                fontWeight    = FontWeight.ExtraBold,
                color         = Color(0xFF111827),
                letterSpacing = (-0.5).sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF111827))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text          = "$completed / $total",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
        LinearProgressIndicator(
            progress       = { progress },
            modifier       = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color          = Color(0xFF111827),
            trackColor     = Color(0xFFE5E7EB)
        )
    }
}

// ════════════════════════════════════════════════════════════════
// 3D 翻转卡片
// ════════════════════════════════════════════════════════════════

/**
 * 使用 rotationY 动画实现 3D 翻转效果。
 *
 * 动画策略：
 * - 正面（rotation < 90°）：显示卡片正面
 * - 背面（rotation ≥ 90°）：X轴翻转180°显示背面，文字保持正向
 */
@Composable
private fun FlipCard(
    item: ReviewQueueItem,
    isFlipped: Boolean,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue  = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label        = "card_flip"
    )

    val isFrontVisible = rotation < 90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape     = RoundedCornerShape(24.dp),
        border    = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        if (isFrontVisible) {
            CardFront(item = item)
        } else {
            // 背面需要反转回正向（否则文字镜像显示）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                CardBack(item = item)
            }
        }
    }
}

// ── 卡片正面（韩文） ────────────────────────────────────────────

@Composable
private fun CardFront(item: ReviewQueueItem) {
    val (korean, level, roundBadge) = when (item) {
        is ReviewQueueItem.WordItem    -> Triple(
            item.word.korean,
            item.word.level,
            "第${item.record.repetitionLevel}轮"
        )
        is ReviewQueueItem.GrammarItem -> Triple(
            item.grammar.grammarPattern,
            item.grammar.category,
            "第${item.record.repetitionLevel}轮"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        // 轮次徽章
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text     = roundBadge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(32.dp))

        // 核心韩文
        Text(
            text      = korean,
            fontSize  = 36.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurface,
            lineHeight = 44.sp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text  = level,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // 提示点击翻面
        Text(
            text  = "点击查看答案",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

// ── 卡片背面（中文 + 详情） ─────────────────────────────────────

@Composable
private fun CardBack(item: ReviewQueueItem) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (item) {
            is ReviewQueueItem.WordItem -> WordCardBack(item)
            is ReviewQueueItem.GrammarItem -> GrammarCardBack(item)
        }
    }
}

@Composable
private fun WordCardBack(item: ReviewQueueItem.WordItem) {
    val word   = item.word
    val record = item.record

    // 中文释义
    Text(
        text       = word.chinese,
        fontSize   = 28.sp,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSecondaryContainer
    )

    Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

    // 词性 + 不规则标签
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip(word.partOfSpeech)
        word.irregularType?.let { Chip(it, color = Color(0xFFEF4444).copy(alpha = 0.15f)) }
    }

    Spacer(Modifier.height(8.dp))

    // 记忆状态
    MemoryStatusRow(record)
}

@Composable
private fun GrammarCardBack(item: ReviewQueueItem.GrammarItem) {
    val grammar = item.grammar
    val record  = item.record

    // 语法分类
    Text(
        text       = grammar.category,
        fontSize   = 22.sp,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSecondaryContainer
    )

    // 接续规则
    Text(
        text  = grammar.conjugationRules,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer
    )

    Divider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))

    // 说明摘要（截取前80字符，防止溢出）
    Text(
        text  = grammar.explanation.take(80) + if (grammar.explanation.length > 80) "…" else "",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
    )

    Spacer(Modifier.height(8.dp))
    MemoryStatusRow(record)
}

// ════════════════════════════════════════════════════════════════
// 记忆状态行（EF / 间隔 / 保留率）
// ════════════════════════════════════════════════════════════════

@Composable
private fun MemoryStatusRow(record: ReviewRecordEntity) {
    val retentionRate = MemoryScheduler.estimateRetentionRate(record, System.currentTimeMillis())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatChip(label = "间隔", value = "${record.intervalDays}天")
        StatChip(label = "EF", value = String.format("%.1f", record.easeFactor))
        StatChip(
            label = "保留率",
            value = "${(retentionRate * 100).toInt()}%",
            color = when {
                retentionRate > 0.8 -> Color(0xFF10B981)
                retentionRate > 0.5 -> Color(0xFFF59E0B)
                else                -> Color(0xFFEF4444)
            }
        )
    }
}

// ════════════════════════════════════════════════════════════════
// 评分按钮行
// ════════════════════════════════════════════════════════════════

@Composable
private fun RatingButtonRow(
    visible: Boolean,
    onRate: (ReviewRating) -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter   = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
        exit    = androidx.compose.animation.fadeOut()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReviewRating.entries.forEach { rating ->
                RatingButton(
                    rating   = rating,
                    onClick  = { onRate(rating) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RatingButton(
    rating: ReviewRating,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPerfect = rating == ReviewRating.PERFECT
    val accentColor = Color(android.graphics.Color.parseColor(rating.colorHex))

    Button(
        onClick   = onClick,
        modifier  = modifier.height(52.dp),
        shape     = RoundedCornerShape(14.dp),
        colors    = ButtonDefaults.buttonColors(
            containerColor = if (isPerfect) Color(0xFF111827) else Color.White,
            contentColor   = if (isPerfect) Color.White else Color(0xFF111827)
        ),
        border    = if (isPerfect) null else BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isPerfect) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
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

// ════════════════════════════════════════════════════════════════
// 复习完成页面
// ════════════════════════════════════════════════════════════════

@Composable
private fun EmptyReviewScreen(
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text          = "REVIEW",
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Bold,
            color         = Color(0xFF9CA3AF),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text       = "今日暂无到期复习",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "所有词汇均在最佳记忆周期内",
            fontSize = 14.sp,
            color    = Color(0xFF6B7280)
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onRestart,
            shape   = RoundedCornerShape(14.dp),
            colors  = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111827),
                contentColor   = Color.White
            )
        ) {
            Text(
                text       = "刷新复习队列",
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SessionCompleteScreen(
    completedCount: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FA))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text          = "REVIEW COMPLETE",
            fontSize      = 12.sp,
            fontWeight    = FontWeight.Bold,
            color         = Color(0xFF9CA3AF),
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text       = "今日复习已达成",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text     = "本次已完成 $completedCount 条记忆复习",
            fontSize = 14.sp,
            color    = Color(0xFF6B7280)
        )
        Spacer(Modifier.height(36.dp))
        Button(
            onClick = onRestart,
            shape   = RoundedCornerShape(14.dp),
            colors  = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111827),
                contentColor   = Color.White
            )
        ) {
            Text(
                text       = "再来一轮",
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 工具组件
// ════════════════════════════════════════════════════════════════

@Composable
private fun Chip(
    text: String,
    color: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Surface(color = color, shape = RoundedCornerShape(8.dp)) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style    = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text  = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun FullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
