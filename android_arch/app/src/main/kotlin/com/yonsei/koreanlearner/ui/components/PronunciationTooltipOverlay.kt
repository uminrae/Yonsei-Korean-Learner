package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.yonsei.koreanlearner.algorithm.korean.HangulJamo
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语实际发音形态与音变分析模型
 * ══════════════════════════════════════════════════════════════════
 */
data class PhoneticDetail(
    val originalText    : String,       // 原文字形（如 "같이"）
    val standardPhonetic: String,       // 实际发音形态（如 "[가치]"）
    val soundShiftType  : String?,      // 音变类型（如 "腭音化", "鼻音化", "流音化", "硬音化", "连音化"）
    val ruleExplanation : String        // 规则原理解析
)

object KoreanPhoneticTranscriber {

    /**
     * 自动分析韩文词条的实际发音形态与音变规则（调用底层音系学计算引擎）
     */
    fun transcribe(word: String): PhoneticDetail {
        val result = com.yonsei.koreanlearner.algorithm.korean.HangulPronunciationEngine.getActualPronunciation(word)
        val shiftType = result.appliedRules.firstOrNull()?.chineseName

        val explanation = if (result.explanations.isNotEmpty()) {
            result.explanations.joinToString("\n")
        } else {
            "遵循韩语标准字母拼读规范，无特殊同化音变。"
        }

        return PhoneticDetail(
            originalText = result.originalText,
            standardPhonetic = result.pronunciationText,
            soundShiftType = shiftType,
            ruleExplanation = explanation
        )
    }
}

/**
 * ══════════════════════════════════════════════════════════════════
 * 音标与连音变音浮层组件 (PronunciationTooltipOverlay)
 * ══════════════════════════════════════════════════════════════════
 *
 * @param text 目标韩文词条或短语
 * @param ttsManager 全局 TTS 引擎
 */
@Composable
fun PronunciationTooltipOverlay(
    text: String,
    ttsManager: TtsManager? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var showPopup by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val detail = remember(text) { KoreanPhoneticTranscriber.transcribe(text) }

    Box(
        modifier = modifier
            .pointerInput(text) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showPopup = true
                    },
                    onTap = {
                        // 快速点击也可触发
                        showPopup = true
                    }
                )
            }
    ) {
        content()

        if (showPopup) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { showPopup = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    shape     = RoundedCornerShape(14.dp),
                    color     = MaterialTheme.colorScheme.surface,
                    border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
                    shadowElevation = 8.dp,
                    modifier  = Modifier
                        .padding(16.dp)
                        .widthIn(min = 220.dp, max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 标头与发音音标
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = detail.originalText,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = detail.standardPhonetic,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // 音变类型 Badge
                            detail.soundShiftType?.let { shift ->
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = shift,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

                        // 规则说明
                        Text(
                            text = detail.ruleExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )

                        // 发音试听按钮
                        if (ttsManager != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                    .clickable { ttsManager.speak(detail.originalText) }
                                    .padding(vertical = 6.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(" 试听实际发音", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
