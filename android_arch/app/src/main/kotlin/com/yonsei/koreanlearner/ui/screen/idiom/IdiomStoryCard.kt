package com.yonsei.koreanlearner.ui.screen.idiom

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.audio.TtsManager
import com.yonsei.koreanlearner.data.local.entity.IdiomEntity
import com.yonsei.koreanlearner.data.local.entity.IdiomType
import com.yonsei.koreanlearner.ui.theme.extended

/**
 * ══════════════════════════════════════════════════════════════════
 * 俗语/成语/拟声拟态词 Instagram 快照卡片 (IdiomStoryCard)
 * ══════════════════════════════════════════════════════════════════
 *
 * @param idiom 俗语数据实体
 * @param ttsManager 全局 TTS 引擎
 * @param onAddToReview 加入今日复习流回调
 */
@Composable
fun IdiomStoryCard(
    idiom           : IdiomEntity,
    ttsManager      : TtsManager? = null,
    onAddToReview   : (() -> Unit)? = null,
    modifier        : Modifier = Modifier
) {
    var isAdded by remember { mutableStateOf(false) }

    // 拟声拟态词动态图标微动效 (铃铛摇摆 / 心跳跳动)
    val infiniteTransition = rememberInfiniteTransition(label = "icon_anim")
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake_anim"
    )

    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat_anim"
    )

    Card(
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border    = BorderStroke(1.dp, MaterialTheme.extended.cardBorder),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 顶部类型 Badge 与微动效图标 ────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (idiom.type) {
                        IdiomType.IDIOM -> Color(0xFFE1306C).copy(alpha = 0.12f)
                        IdiomType.FOUR_CHAR -> Color(0xFF833AB4).copy(alpha = 0.12f)
                        IdiomType.ONOMATOPOEIA -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = idiom.type.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (idiom.type) {
                            IdiomType.IDIOM -> Color(0xFFE1306C)
                            IdiomType.FOUR_CHAR -> Color(0xFF833AB4)
                            IdiomType.ONOMATOPOEIA -> Color(0xFFD97706)
                        }
                    )
                }

                // 动态小图标（摇摆或跳动）
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .then(
                            if (idiom.korean.contains("따르릉")) {
                                Modifier.graphicsLayer { rotationZ = animatedRotation }
                            } else if (idiom.korean.contains("두근두근")) {
                                Modifier.scale(animatedScale)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = idiom.iconSymbol, fontSize = 24.sp)
                }
            }

            // ── 韩文大字与汉字词源 ──────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = idiom.korean,
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    if (!idiom.hanja.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "(${idiom.hanja})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = "隐喻义: ${idiom.metaphorMeaning}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "字面直译: ${idiom.literalMeaning}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)

            // ── 韩国文化背景与典故框 ─────────────────────────────
            Surface(
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape  = RoundedCornerShape(10.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.extended.cardBorder)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "️ 韩国文化背景 & 语境",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = idiom.culturalBackground,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp,
                        fontSize = 11.sp
                    )
                }
            }

            // ── 底部操作栏（发音 + 放入复习流） ─────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (ttsManager != null) {
                    IconButton(
                        onClick = { ttsManager.speak(idiom.korean) },
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("", fontSize = 14.sp)
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                OutlinedButton(
                    onClick = {
                        isAdded = true
                        onAddToReview?.invoke()
                    },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isAdded) "✓ 已加入复习流" else "+ 标为待复习",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
