package com.yonsei.koreanlearner.ui.screen.explore

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.core.state.AppStateManager
import com.yonsei.koreanlearner.ui.navigation.Screen

/**
 * ══════════════════════════════════════════════════════════════════
 * 拓展研习沙盒枢纽界面 (ExploreHubScreen)
 * 纯白高光 Ins 风格，系统化收纳辅助工具、语言学实验室与实战专栏
 * ══════════════════════════════════════════════════════════════════
 */

data class ExploreCategory(
    val categoryName: String,
    val items: List<ExploreCardData>
)

data class ExploreCardData(
    val title: String,
    val subtitle: String,
    val route: String
)

private val EXPLORE_CATEGORIES = listOf(
    ExploreCategory(
        "语言学实验室",
        listOf(
            ExploreCardData("音变实验室", "7 大音变规律动态推演与发音", Screen.PhonologyLab.route),
            ExploreCardData("敬语阶称", "6 大格式体与非格式体阶称转换", Screen.SpeechLevelSandbox.route),
            ExploreCardData("声调纠音", "母语声调音高曲线重叠跟读", Screen.ShadowingTrainer.route),
            ExploreCardData("规范隔写", "韩国国立国语院隔写准则诊断", Screen.SpacingCorrection.route),
            ExploreCardData("笔顺临摹", "韩文初声/中声/终声笔画轨迹临摹", Screen.HangulStroke.route)
        )
    ),
    ExploreCategory(
        "音乐与随身听",
        listOf(
            ExploreCardData("歌词精听", "(G)I-DLE 热门单曲逐句精听听写", Screen.Dictation.route),
            ExploreCardData("随身磨耳朵", "延世 1~6 册锁屏后台循环播报流", Screen.BackgroundRadio.route)
        )
    ),
    ExploreCategory(
        "竞技与实战",
        listOf(
            ExploreCardData("词尾接龙", "AI 头音法则终极词尾接龙对决", Screen.WordChain.route),
            ExploreCardData("TOPIK 作文", "54 题大作文评分诊断与智能批改", Screen.TopikEssayGrader.route),
            ExploreCardData("TOPIK 考点", "真题高频核心考点专项自测", Screen.TopikExam.route),
            ExploreCardData("情境对话", "韩国日常生活全景会话沙盒", Screen.DialogueSandbox.route),
            ExploreCardData("外来语对照", "外来语韩文转写规律与对照表", Screen.LoanwordModule.route),
            ExploreCardData("俗语成语", "韩国地道惯用语与四字成语典故", Screen.IdiomModule.route)
        )
    ),
    ExploreCategory(
        "深度辅助工具",
        listOf(
            ExploreCardData("语素拆解镜", "长难句原形还原与附着成分逆向剖析", Screen.KoreanLens.route),
            ExploreCardData("双语分级精读", "延世精读短文与双语分级阅读专栏", Screen.GradedReader.route),
            ExploreCardData("能力分析雷达", "TOPIK 六维能力模型与 365 天复习年鉴", Screen.LearningAnalytics.route),
            ExploreCardData("全局超级检索", "跨库秒级检索 4446 词汇与 328 语法", Screen.UniversalSearch.route)
        )
    )
)

@Composable
fun ExploreHubScreen(
    appStateManager: AppStateManager,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember {
        mutableIntStateOf(appStateManager.getLastExploreCategory().coerceIn(0, EXPLORE_CATEGORIES.size - 1))
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFFF7F8FA),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "拓展研习库",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "KOREAN EXTENDED LAB & SANDBOX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }
            }
        },
        containerColor = Color(0xFFF7F8FA),
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // ── 分类切换胶囊栏 ─────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    itemsIndexed(EXPLORE_CATEGORIES) { index, cat ->
                        val isSel = index == selectedCategoryIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (isSel) Modifier.background(Color(0xFF111827))
                                    else Modifier
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                                )
                                .clickable {
                                    selectedCategoryIndex = index
                                    appStateManager.setLastExploreCategory(index)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cat.categoryName,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }

            // ── 当前分类下的卡片流 ─────────────────────────────
            val currentItems = EXPLORE_CATEGORIES[selectedCategoryIndex].items
            items(currentItems.size) { i ->
                val card = currentItems[i]
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRoute(card.route) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = card.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = card.subtitle,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                lineHeight = 16.sp
                            )
                        }

                        Text(
                            text = "→",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                    }
                }
            }
        }
    }
}
