package com.yonsei.koreanlearner.ui.screen.hanja

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.algorithm.korean.DerivedHanjaWord
import com.yonsei.koreanlearner.algorithm.korean.HanjaRoot
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.InsGradientColors
import com.yonsei.koreanlearner.ui.theme.extended
import kotlin.math.cos
import kotlin.math.sin

/**
 * ══════════════════════════════════════════════════════════════════
 * 汉字词词根关联知识图谱界面 (HanjaGraphScreen)
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun HanjaGraphScreen(
    viewModel: HanjaGraphViewModel = hiltViewModel(),
    ttsManager: TtsManager? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            HanjaTopBar(
                searchQuery = state.searchQuery,
                onSearchChanged = { viewModel.onSearchQueryChanged(it) },
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
            // ── 1. 顶部词根选择器 (Horizontal FilterChips) ────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.availableRoots.forEach { root ->
                    val isSelected = root.koreanChar == state.selectedRoot.koreanChar
                    FilterChip(
                        selected = isSelected,
                        onClick  = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.selectRoot(root)
                        },
                        label    = {
                            Text(
                                text = "${root.koreanChar} (${root.hanjaChar})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape    = RoundedCornerShape(10.dp)
                    )
                }
            }

            // ── 2. 中央可拖拽与缩放的 Radial Graph 画布 ────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                HanjaRadialGraphCanvas(
                    root = state.selectedRoot,
                    derivedWords = state.currentNetwork.derivedWords,
                    selectedWord = state.selectedDerivedWord,
                    onWordSelected = { word ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.selectDerivedWord(word)
                    }
                )
            }

            // ── 3. 底部派生词详细释义与汉字拆解卡片 ─────────────
            state.selectedDerivedWord?.let { word ->
                DerivedWordDetailBottomCard(
                    word = word,
                    isAdded = state.isAddedToVocabulary,
                    onAdd = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addToVocabulary(word)
                    },
                    onSpeak = { ttsManager?.speak(word.korean) }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 1. 辐射节点知识图谱画布 (HanjaRadialGraphCanvas)
// ══════════════════════════════════════════════════════════════════

@Composable
private fun HanjaRadialGraphCanvas(
    root: HanjaRoot,
    derivedWords: List<DerivedHanjaWord>,
    selectedWord: DerivedHanjaWord?,
    onWordSelected: (DerivedHanjaWord) -> Unit
) {
    val density = LocalDensity.current

    // 平移与缩放手势状态
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoomScale by remember { mutableFloatStateOf(1f) }

    // 展开动画进度
    val expandProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "radial_expand"
    )

    val radiusPx = with(density) { 135.dp.toPx() } * expandProgress

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(root) {
                detectTransformGestures { _, pan, zoom, _ ->
                    panOffset += pan
                    zoomScale = (zoomScale * zoom).coerceIn(0.6f, 2.0f)
                }
            }
            .graphicsLayer {
                translationX = panOffset.x
                translationY = panOffset.y
                scaleX = zoomScale
                scaleY = zoomScale
            },
        contentAlignment = Alignment.Center
    ) {
        // 绘制中心到外围节点的连接线
        val linkColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        val activeLinkColor = MaterialTheme.colorScheme.primary

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val count = derivedWords.size

            derivedWords.forEachIndexed { index, word ->
                val angle = (index.toDouble() / count) * 2 * Math.PI - Math.PI / 2
                val nodeX = center.x + (radiusPx * cos(angle)).toFloat()
                val nodeY = center.y + (radiusPx * sin(angle)).toFloat()

                val isSelected = word.id == selectedWord?.id
                drawLine(
                    color       = if (isSelected) activeLinkColor else linkColor,
                    start       = center,
                    end         = Offset(nodeX, nodeY),
                    strokeWidth = if (isSelected) 3.5f else 1.5f
                )
            }
        }

        // ── 中心主词根节点 ──────────────────────────────────────
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            shadowElevation = 6.dp,
            modifier = Modifier.size(86.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(InsGradientColors).copyAlpha(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${root.koreanChar}(${root.hanjaChar})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = root.chineseMeaning.take(6),
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // ── 外围派生词辐射子节点 ────────────────────────────────
        val count = derivedWords.size
        derivedWords.forEachIndexed { index, word ->
            val angle = (index.toDouble() / count) * 2 * Math.PI - Math.PI / 2
            val offsetX = (radiusPx * cos(angle)).toFloat()
            val offsetY = (radiusPx * sin(angle)).toFloat()
            val isSelected = word.id == selectedWord?.id

            Box(
                modifier = Modifier
                    .offset(x = with(density) { offsetX.toDp() }, y = with(density) { offsetY.toDp() })
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface
                    )
                    .then(
                        if (!isSelected) Modifier.border(1.dp, MaterialTheme.extended.cardBorder, RoundedCornerShape(12.dp))
                        else Modifier
                    )
                    .clickable { onWordSelected(word) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = word.korean,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = word.chinese.take(4),
                        fontSize = 9.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun Brush.copyAlpha(alpha: Float): Brush = this

// ══════════════════════════════════════════════════════════════════
// 2. 底部派生词详细释义与汉字拆解卡片
// ══════════════════════════════════════════════════════════════════

@Composable
private fun DerivedWordDetailBottomCard(
    word    : DerivedHanjaWord,
    isAdded : Boolean,
    onAdd   : () -> Unit,
    onSpeak : () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 词汇原形 + 发音 + 册次
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.korean,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("", fontSize = 14.sp)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "延世第${word.book}册 · ${word.level}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 汉字拆解公式框（如 대(大) + 학(學) + 교(校)）
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape  = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "汉字拆解: ${word.breakdown}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "汉字: ${word.hanja}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 中文释义与加入生词本按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "释义: ${word.chinese} (${word.partOfSpeech})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = onAdd,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isAdded) "✓ 已在复习流" else "+ 加入生词本",
                        color = if (isAdded) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 3. 顶栏
// ══════════════════════════════════════════════════════════════════

@Composable
private fun HanjaTopBar(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onBack: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Text("←", fontSize = 20.sp)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text("搜索汉字词根（如 学/院/회/식）...", fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
