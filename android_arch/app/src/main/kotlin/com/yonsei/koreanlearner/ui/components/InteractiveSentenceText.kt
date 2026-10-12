package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.algorithm.korean.KoreanTokenizer
import com.yonsei.koreanlearner.algorithm.korean.SentenceToken
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.ui.theme.extended
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ══════════════════════════════════════════════════════════════════
 * 例句点词即查交互文本组件（InteractiveSentenceText）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心交互：
 * 1. 自动对输入的例句进行智能 Token 划分（剥离助词与词尾）。
 * 2. 用户点击任意单词，触发触感振动，并在当前词下添加微高亮。
 * 3. 异步在 Room 数据库中匹配词干，底部弹出 Instagram 极简 Mini 查词卡片。
 *
 * @param sentence 完整韩文例句（如 "눈이 오기 때문에 길이 미끄럽습니다."）
 * @param wordDao 单词数据库 DAO
 * @param reviewRecordDao 复习记录 DAO（用于生词本收藏）
 * @param ttsManager 全局 TTS 引擎
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InteractiveSentenceText(
    sentence         : String,
    wordDao          : WordDao,
    reviewRecordDao  : ReviewRecordDao? = null,
    ttsManager       : TtsManager? = null,
    modifier         : Modifier = Modifier,
    fontSize         : Int = 18,
    highlightColor   : Color = MaterialTheme.colorScheme.primary,
    onNavigateToWord : ((Int) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // 智能分词 Token 列表
    val tokens = remember(sentence) { KoreanTokenizer.tokenize(sentence) }

    var selectedToken by remember { mutableStateOf<SentenceToken?>(null) }
    var matchedWords by remember { mutableStateOf<List<WordEntity>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }

    // ── 例句 Token 流式排版（FlowRow 自动换行） ───────────────
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tokens.forEach { token ->
            val isSelected = selectedToken?.index == token.index

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) highlightColor.copy(alpha = 0.15f)
                        else Color.Transparent
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // 1. 触发触感振动
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedToken = token
                        showSheet = true
                        isSearching = true

                        // 2. 异步在 Room 中匹配候选词
                        scope.launch {
                            val results = withContext(Dispatchers.IO) {
                                wordDao.lookupWordsByCandidates(token.candidates, limit = 4)
                            }
                            matchedWords = results
                            isSearching = false
                        }
                    }
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                Text(
                    text = token.rawText,
                    fontSize = fontSize.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) highlightColor else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isSelected) TextDecoration.Underline else TextDecoration.None
                )
            }
        }
    }

    // ── 底部弹出的 Mini 查词 BottomSheet ────────────────────────
    if (showSheet && selectedToken != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                selectedToken = null
            },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            WordLookupMiniPanel(
                token = selectedToken!!,
                matchedWords = matchedWords,
                isSearching = isSearching,
                ttsManager = ttsManager,
                reviewRecordDao = reviewRecordDao,
                onNavigateToWord = { wordId ->
                    showSheet = false
                    selectedToken = null
                    onNavigateToWord?.invoke(wordId)
                },
                onClose = {
                    showSheet = false
                    selectedToken = null
                }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// Mini 查词面板组件
// ══════════════════════════════════════════════════════════════════

@Composable
private fun WordLookupMiniPanel(
    token            : SentenceToken,
    matchedWords     : List<WordEntity>,
    isSearching      : Boolean,
    ttsManager       : TtsManager?,
    reviewRecordDao  : ReviewRecordDao?,
    onNavigateToWord : (Int) -> Unit,
    onClose          : () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isAddedToVocabulary by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 标头与选中的 Token ────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = token.cleanWord,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                // 发音按钮
                if (ttsManager != null) {
                    IconButton(
                        onClick = { ttsManager.speak(token.cleanWord) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("", fontSize = 16.sp)
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "点词即查",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

        // ── 查词结果匹配列表 ──────────────────────────────────
        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        } else if (matchedWords.isEmpty()) {
            // 未在内置延世词库中精确命中
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("“${token.cleanWord}”", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "暂未在延世 1~6 册核心词库中收录此变形或专有名词",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // 命中的单词卡片列表
            matchedWords.forEach { word ->
                MatchedWordRowCard(
                    word = word,
                    reviewRecordDao = reviewRecordDao,
                    isAddedToVocabulary = isAddedToVocabulary,
                    onAdded = { isAddedToVocabulary = true },
                    onNavigateToWord = { onNavigateToWord(word.id) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MatchedWordRowCard(
    word                : WordEntity,
    reviewRecordDao     : ReviewRecordDao?,
    isAddedToVocabulary : Boolean,
    onAdded             : () -> Unit,
    onNavigateToWord    : () -> Unit
) {
    val scope = rememberCoroutineScope()

    Surface(
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape  = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 原形与所属册次
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.korean,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "第${word.book}册 · ${word.partOfSpeech}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                // 中文释义
                Text(
                    text = word.chinese,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 不规则标签
                word.irregularType?.let { irregular ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "[$irregular]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // 快捷操作按钮组
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // 加入生词本
                IconButton(
                    onClick = {
                        if (reviewRecordDao != null) {
                            scope.launch(Dispatchers.IO) {
                                val initRecord = com.yonsei.koreanlearner.algorithm.MemoryScheduler
                                    .createInitialRecord(word.id, "WORD")
                                reviewRecordDao.upsert(initRecord)
                            }
                            onAdded()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isAddedToVocabulary) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface
                        )
                ) {
                    Text(
                        text = if (isAddedToVocabulary) "✓" else "+",
                        color = if (isAddedToVocabulary) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
