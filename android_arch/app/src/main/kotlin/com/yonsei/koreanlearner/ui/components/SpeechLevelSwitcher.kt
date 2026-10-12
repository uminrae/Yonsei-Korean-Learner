package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.yonsei.koreanlearner.algorithm.korean.KoreanConjugationEngine
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语三大语体枚举定义
 * ══════════════════════════════════════════════════════════════════
 */
enum class KoreanSpeechLevel(
    val title         : String,
    val subtitle      : String,
    val grammarTag    : String,
    val usageScenario : String,
    val endingSuffix  : String
) {
    /** 1. 格式体敬语（하십시오체）：庄重、官方、职场 */
    FORMAL_POLITE(
        title         = "格式体敬语",
        subtitle      = "합쇼체",
        grammarTag    = "-(으)ㅂ니다",
        usageScenario = "新闻播报 · 正式演讲 · 商务职场 · 初次见面",
        endingSuffix  = "-(으)ㅂ니다"
    ),

    /** 2. 非格式体敬语（해요체）：日常最常用、柔和礼貌 */
    INFORMAL_POLITE(
        title         = "非格式体敬语",
        subtitle      = "해요체",
        grammarTag    = "-아/어요",
        usageScenario = "日常交流 · 对长辈/平辈礼貌交流 · 服务场景",
        endingSuffix  = "-아/어요"
    ),

    /** 3. 半语 / 非敬语（해체/반말）：朋友、晚辈、亲密关系 */
    CASUAL_PANMAL(
        title         = "半语 / 非敬语",
        subtitle      = "반말 / 해체",
        grammarTag    = "-아/어 (다/자)",
        usageScenario = "挚友死党 · 对晚辈 · 亲密伴侣 · 自言自语",
        endingSuffix  = "-아/어"
    )
}

/**
 * ══════════════════════════════════════════════════════════════════
 * 语体三段一键切换器（极简胶囊滑块 Pill Slider）
 * ══════════════════════════════════════════════════════════════════
 *
 * @param selectedLevel 当前选中的语体
 * @param onLevelChanged 语体切换回调
 */
@Composable
fun SpeechLevelPillSlider(
    selectedLevel: KoreanSpeechLevel,
    onLevelChanged: (KoreanSpeechLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val levels = KoreanSpeechLevel.entries
    val selectedIndex = levels.indexOf(selectedLevel).coerceAtLeast(0)

    // 滑块平滑位移动画（Spring 弹性动画）
    val sliderOffsetFraction by animateFloatAsState(
        targetValue   = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
        label         = "pill_slider_offset"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(3.dp)
    ) {
        val segmentWidth = maxWidth / levels.size

        // ── 悬浮跟随滑块（白色/暗色卡片） ────────────────────
        Box(
            modifier = Modifier
                .width(segmentWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = sliderOffsetFraction * segmentWidth.toPx()
                }
                .clip(RoundedCornerShape(19.dp))
                .background(MaterialTheme.colorScheme.surface)
                .then(
                    if (MaterialTheme.colorScheme.background.red > 0.5f) {
                        Modifier.graphicsLayer { shadowElevation = 3f }
                    } else {
                        Modifier
                    }
                )
        )

        // ── 三个点击选项标签 ──────────────────────────────────
        Row(modifier = Modifier.fillMaxSize()) {
            levels.forEach { level ->
                val isSelected = level == selectedLevel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(19.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onLevelChanged(level) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = level.title,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color      = if (isSelected) MaterialTheme.colorScheme.onSurface
                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize   = 12.sp,
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * ══════════════════════════════════════════════════════════════════
 * 语体多维动态变音与例句卡片（SpeechLevelConjugationCard）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心特性：
 * 1. 动态集成 KoreanConjugationEngine，根据选择的动词原型实时生成 3 种语体的变音。
 * 2. AnimatedContent 驱动的淡入淡出与上下平滑位移过渡。
 * 3. Instagram 极简黑白灰底色 + 1dp 细边框。
 *
 * @param baseVerb 动词/形容词原型（如 "먹다", "가다", "공부하다", "돕다", "모르다"）
 * @param baseExampleContext 前后语境句子模板（如 "선생님, 지금 밥을 ___."）
 * @param onSpeak 发音回调
 */
@Composable
fun SpeechLevelConjugationCard(
    baseVerb           : String,
    baseExampleContext : String? = null,
    onSpeak            : ((String) -> Unit)? = null,
    modifier           : Modifier = Modifier
) {
    var currentLevel by remember { mutableStateOf(KoreanSpeechLevel.INFORMAL_POLITE) }

    // 实时计算当前语体下的变音结果
    val conjugationResult = remember(baseVerb, currentLevel) {
        when (currentLevel) {
            KoreanSpeechLevel.FORMAL_POLITE ->
                KoreanConjugationEngine.conjugate(baseVerb, "-(으)ㅂ니다")

            KoreanSpeechLevel.INFORMAL_POLITE ->
                KoreanConjugationEngine.conjugate(baseVerb, "-아/어요")

            KoreanSpeechLevel.CASUAL_PANMAL -> {
                val r = KoreanConjugationEngine.conjugate(baseVerb, "-아/어요")
                // 半语去 -요
                val panmalText = r.conjugatedText.removeSuffix("요")
                r.copy(
                    conjugatedText = panmalText,
                    endingPattern = "-아/어",
                    ruleExplanation = "${r.ruleExplanation} (半语去掉敬语词尾 요)"
                )
            }
        }
    }

    // 组合完整例句
    val conjugatedSentence = remember(baseExampleContext, conjugationResult.conjugatedText) {
        if (baseExampleContext != null && baseExampleContext.contains("___")) {
            baseExampleContext.replace("___", conjugationResult.conjugatedText)
        } else {
            "${conjugationResult.conjugatedText}."
        }
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── 顶部标题与语体胶囊切换器 ───────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "语体三段切换 (Speech Level)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = baseVerb,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(14.dp))

            // 胶囊滑块组件
            SpeechLevelPillSlider(
                selectedLevel = currentLevel,
                onLevelChanged = { currentLevel = it }
            )

            Spacer(Modifier.height(18.dp))

            // ── 动态切换内容区（AnimatedContent 120fps 平滑过渡） ──
            AnimatedContent(
                targetState = currentLevel,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                            slideInVertically(
                                animationSpec = tween(220, easing = FastOutSlowInEasing),
                                initialOffsetY = { it / 6 }
                            ) togetherWith
                            fadeOut(animationSpec = tween(160))
                },
                label = "speech_level_content"
            ) { targetLevel ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 变形大字展示行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = conjugationResult.conjugatedText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${targetLevel.subtitle} · 词尾: ${targetLevel.grammarTag}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // 发音播放按钮
                        if (onSpeak != null) {
                            IconButton(
                                onClick = { onSpeak(conjugatedSentence) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Text("", fontSize = 16.sp)
                            }
                        }
                    }

                    // 语境适用场景提示条
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("", fontSize = 12.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = targetLevel.usageScenario,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // 语境完整例句展示
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = conjugatedSentence,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "规则: ${conjugationResult.ruleExplanation}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
