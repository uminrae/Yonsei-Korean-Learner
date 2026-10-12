package com.yonsei.koreanlearner.ui.screen.dialogue

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.algorithm.korean.DialogueMessage
import com.yonsei.koreanlearner.algorithm.korean.DialogueOption
import com.yonsei.koreanlearner.algorithm.korean.DialogueStateMachine
import com.yonsei.koreanlearner.algorithm.korean.ResponseRating
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世生活情境会话沙盒界面 (SituationalDialogueScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun SituationalDialogueScreen(
    viewModel: SituationalDialogueViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()

    // 消息增加时平滑滚动到底部
    LaunchedEffect(state.messages.size, state.isNpcTyping) {
        if (state.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(state.messages.size)
        }
    }

    Scaffold(
        topBar = {
            DialogueTopBar(
                currentScenario = state.currentScenario,
                totalScore = state.totalScore,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── 1. 顶部场景切换横向 FilterChips ─────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DialogueStateMachine.SCENARIOS.forEach { sc ->
                    val isSelected = sc.id == state.currentScenario.id
                    FilterChip(
                        selected = isSelected,
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectScenario(sc)
                        },
                        label    = { Text("${sc.iconEmoji} ${sc.title}") },
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // ── 2. Instagram DM 风格气泡消息流 ─────────────────
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    if (msg.isUser) {
                        UserMessageBubble(message = msg)
                    } else {
                        NpcMessageBubble(
                            message = msg,
                            avatarEmoji = state.currentScenario.iconEmoji,
                            onSpeak = { ttsManager?.speak(msg.koreanText) }
                        )
                    }
                }

                // NPC 打字中动效 (Typing Indicator)
                if (state.isNpcTyping) {
                    item {
                        NpcTypingIndicator(avatarEmoji = state.currentScenario.iconEmoji)
                    }
                }
            }

            // ── 3. 底部选项选择区 / 通关总评 ─────────────────────
            if (state.isCompleted) {
                DialogueCompletionCard(
                    score = state.totalScore,
                    onRestart = { viewModel.selectScenario(state.currentScenario) }
                )
            } else if (state.currentStep != null) {
                DialogueOptionChoiceDeck(
                    options = state.currentStep?.userOptions ?: emptyList(),
                    onOptionClick = { opt ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.chooseOption(opt)
                    }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 1. Instagram DM 风格消息气泡
// ══════════════════════════════════════════════════════════════════

@Composable
private fun NpcMessageBubble(
    message     : DialogueMessage,
    avatarEmoji : String,
    onSpeak     : () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // NPC 圆形头像
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(avatarEmoji, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.width(8.dp))

        // 气泡卡片
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(message.senderName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                        Text("", fontSize = 11.sp)
                    }
                }

                Text(
                    text = message.koreanText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )

                Text(
                    text = message.chineseText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: DialogueMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = message.koreanText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 22.sp
                )

                Text(
                    text = message.chineseText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                // 评分与语法反馈 Badge
                message.rating?.let { r ->
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.displayName, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        Text("+${r.score}分", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    message.feedback?.let { fb ->
                        Text(fb, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 2. NPC 打字中动效 (Typing Indicator)
// ══════════════════════════════════════════════════════════════════

@Composable
private fun NpcTypingIndicator(avatarEmoji: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dotAlpha1 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "d1")
    val dotAlpha2 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse), label = "d2")
    val dotAlpha3 by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse), label = "d3")

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) { Text(avatarEmoji, fontSize = 18.sp) }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder)
        ) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha1)))
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha2)))
                Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha3)))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 3. 底部选项卡片与总评卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DialogueOptionChoiceDeck(
    options       : List<DialogueOption>,
    onOptionClick : (DialogueOption) -> Unit
) {
    Card(
        shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(" 选择您的回复（考查敬语礼貌与地道表达）：", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

            options.forEach { opt ->
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                    modifier = Modifier.fillMaxWidth().clickable { onOptionClick(opt) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(opt.koreanText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(opt.chineseText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogueCompletionCard(
    score     : Int,
    onRestart : () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(" 会话演练圆满完成！", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("总得分: $score 分", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("重新演练本场景 ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DialogueTopBar(
    currentScenario: com.yonsei.koreanlearner.algorithm.korean.DialogueScenario,
    totalScore: Int,
    onBack: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Text("←", fontSize = 20.sp)
            }
        } else {
            Spacer(Modifier.width(36.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(currentScenario.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${currentScenario.npcRole} ⟷ ${currentScenario.userRole}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
            Text(
                text = "得分: $totalScore",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
