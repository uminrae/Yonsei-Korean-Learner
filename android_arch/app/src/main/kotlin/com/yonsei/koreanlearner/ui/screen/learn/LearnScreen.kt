package com.yonsei.koreanlearner.ui.screen.learn

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import kotlinx.coroutines.flow.collectLatest

// ════════════════════════════════════════════════════════════════
// LearnScreen — 今日新词学习界面
// ════════════════════════════════════════════════════════════════

/**
 * 今日新词界面（单向数据流，纯 Compose + MVI）
 *
 * 交互流程：
 *   展示新词卡片 → 用户点击"我记住了" → 初始化记忆记录 → 下一张
 *                                    ↓ 点击"跳过" → 仅前进，不写记录
 *   全部新词完成 → Effect NavigateToReview → 进入复习界面
 *
 * @param onNavigateToReview 学习完毕后跳转复习界面的回调
 */
@Composable
fun LearnScreen(
    onNavigateToReview: () -> Unit,
    viewModel: LearnViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Effect 消费 ───────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LearnEffect.NavigateToReview -> onNavigateToReview()
                is LearnEffect.ShowSnackbar     -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF7F8FA)
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.isLessonComplete -> LessonCompleteCard(
                onGoReview = onNavigateToReview,
                modifier = Modifier.padding(padding)
            )

            else -> LearnContent(
                state     = state,
                onLearned = { viewModel.onIntent(LearnIntent.MarkWordLearned) },
                onSkip    = { viewModel.onIntent(LearnIntent.SkipWord) },
                modifier  = Modifier.padding(padding)
            )
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 主内容区域
// ════════════════════════════════════════════════════════════════

@Composable
private fun LearnContent(
    state: LearnUiState,
    onLearned: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 标题栏 ────────────────────────────────────────────
        Text(
            text       = "第 ${state.book} 册 · 第 ${state.lesson} 课",
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF111827)
        )
        Text(
            text       = "今日新词  剩余 ${state.remainingWords} / ${state.newWords.size}",
            style      = MaterialTheme.typography.bodySmall,
            color      = Color(0xFF6B7280)
        )

        LinearProgressIndicator(
            progress   = { state.progress },
            modifier   = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color      = Color(0xFF111827),
            trackColor = Color(0xFFE5E7EB)
        )

        // ── 新词卡片（带滑入动画）─────────────────────────────
        AnimatedContent(
            targetState = state.currentWord,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "word_card_transition"
        ) { word ->
            if (word != null) {
                NewWordCard(word = word, modifier = Modifier.fillMaxSize())
            }
        }

        // ── 底部操作按钮 ───────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 跳过按钮
            OutlinedButton(
                onClick  = onSkip,
                modifier = Modifier.weight(1f).height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                border   = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280))
            ) {
                Text("跳过", fontWeight = FontWeight.Medium)
            }

            // 已记住按钮
            Button(
                onClick  = onLearned,
                modifier = Modifier.weight(2f).height(56.dp),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("我记住了", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ════════════════════════════════════════════════════════════════
// 新词展示卡片
// ════════════════════════════════════════════════════════════════

@Composable
private fun NewWordCard(word: WordEntity, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(24.dp),
        border    = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            // TOPIK 等级标签
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text     = word.level,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.height(24.dp))

            // 韩文单词（主体）
            Text(
                text       = word.korean,
                fontSize   = 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                lineHeight = 52.sp
            )

            Spacer(Modifier.height(16.dp))

            // 中文释义
            Text(
                text      = word.chinese,
                fontSize  = 22.sp,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // 词性 + 不规则类型
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text     = word.partOfSpeech,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style    = MaterialTheme.typography.labelSmall
                    )
                }
                word.irregularType?.let { irregular ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text     = irregular,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// 课次完成提示卡片
// ════════════════════════════════════════════════════════════════

@Composable
private fun LessonCompleteCard(
    onGoReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✅", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text       = "本课新词学完！",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "去复习巩固一下吧 ",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onGoReview,
                shape   = RoundedCornerShape(16.dp)
            ) {
                Text("开始复习", modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
            }
        }
    }
}
