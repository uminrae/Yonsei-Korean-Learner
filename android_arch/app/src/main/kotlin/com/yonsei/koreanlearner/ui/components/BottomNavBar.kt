package com.yonsei.koreanlearner.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ══════════════════════════════════════════════════════════════════
 * 底部导航栏（语法第一 · 单词第二 · 纯文字极简高光 Ins 风格）
 * ══════════════════════════════════════════════════════════════════
 */

@Immutable
data class NavDestination(
    val route: String,
    val label: String
)

/** 全局底部导航四大核心目的地列表（语法居首，单词次之） */
val AppNavDestinations = listOf(
    NavDestination(route = "grammar_tree", label = "语法"),
    NavDestination(route = "wordcard",     label = "单词"),
    NavDestination(route = "review",       label = "复习"),
    NavDestination(route = "explore",      label = "拓展")
)

/**
 * Instagram 极简轻奢纯白纯文字底部导航栏
 */
@Composable
fun KoreanBottomNavBar(
    destinations  : List<NavDestination> = AppNavDestinations,
    selectedRoute : String,
    onNavigate    : (String) -> Unit,
    modifier      : Modifier = Modifier
) {
    Surface(
        modifier        = modifier.fillMaxWidth(),
        color           = Color.White,
        tonalElevation  = 0.dp,
        shadowElevation = 4.dp
    ) {
        Column {
            // ── 顶部 1dp 极细柔灰分割线 ────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE9ECEF))
            )

            // ── 纯文字 Tab 行 ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectableGroup()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                destinations.forEach { dest ->
                    val isSelected = when {
                        dest.route == "grammar_tree" -> selectedRoute.startsWith("grammar_tree")
                        dest.route == "wordcard"     -> selectedRoute.startsWith("wordcard")
                        dest.route == "review"       -> selectedRoute.startsWith("review")
                        dest.route == "explore"      -> selectedRoute.startsWith("explore")
                        else                         -> dest.route == selectedRoute
                    }

                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF111827) else Color(0xFF9CA3AF),
                        animationSpec = tween(200),
                        label = "nav_text_color"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(dest.route) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text          = dest.label,
                            fontSize      = 15.sp,
                            fontWeight    = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color         = textColor,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(3.dp))

                        // 选中底部小黑圆点指示器
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF111827) else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}
