package com.yonsei.koreanlearner.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yonsei.koreanlearner.ui.navigation.Screen

/**
 * ══════════════════════════════════════════════════════════════════
 * 首页（HomeScreen）— 120Hz 极速满帧、分类矩阵排版与 Ins 纯白高光视觉
 * ══════════════════════════════════════════════════════════════════
 */
@Composable
fun HomeScreen(
    onNavigateToReview  : () -> Unit,
    onNavigateToLearn   : () -> Unit,
    onNavigateToFeature : (String) -> Unit,
    currentBook         : Int = 1,
    currentLesson       : Int = 1,
    streakDays          : Int = 7,
    dueReviewCount      : Int = 23,
    newWordCount        : Int = 10
) {
    val listState = rememberLazyListState()

    Scaffold(
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            HomeTopBar(
                streakDays = streakDays,
                onSearchClick = { onNavigateToFeature(Screen.UniversalSearch.route) }
            )
        }
    ) { padding ->
        LazyColumn(
            state    = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(top = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── 1. 册数选择圆环流 ─────────────────────────────
            item(key = "story_row") {
                Spacer(Modifier.height(10.dp))
                BookStoryRow(
                    currentBook = currentBook,
                    onBookClick = { book ->
                        onNavigateToFeature(Screen.WordCard.createRoute(book, 1))
                    }
                )
                Spacer(Modifier.height(18.dp))
            }

            // ── 2. 今日核心动作卡片（双大卡排版）─────────────
            item(key = "today_card") {
                TodayActionRow(
                    dueCount = dueReviewCount,
                    newCount = newWordCount,
                    onReview = onNavigateToReview,
                    onLearn  = onNavigateToLearn,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(22.dp))
            }

            // ── 3. 专项研习分类矩阵（分类胶囊 + 高质感卡片）─────
            item(key = "feature_matrix") {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "专项研习体系",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "23+ 语言学沙盒",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    CategorizedFeatureMatrix(onNavigate = onNavigateToFeature)
                }
                Spacer(Modifier.height(26.dp))
            }

            // ── 4. 延世核心词汇流 ─────────────────────────────
            item(key = "feed_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "延世第${currentBook}册 · 核心词汇流",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "第${currentLesson}课",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // 示例单词列表（绑定稳定 Key，零重建）
            items(
                count       = STATIC_SAMPLE_WORDS.size,
                key         = { "sample_word_$it" },
                contentType = { "sample_word_card" }
            ) { index ->
                SampleWordFeedCard(index = index, onClick = onNavigateToLearn)
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏（纯净白底 + 搜索直达 + 打卡胶囊，无 Emoji）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun HomeTopBar(
    streakDays: Int,
    onSearchClick: () -> Unit
) {
    Surface(
        color = Color(0xFFF7F8FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text          = "未来的韩语",
                        fontSize      = 22.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color(0xFF111827),
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text          = "YONSEI KOREAN",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.SemiBold,
                        color         = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // 极简搜索圆钮（纯矢量图标，无 Emoji）
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onSearchClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "查",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    // 极简打卡胶囊（纯文字，无 Emoji）
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "连续 $streakDays 天",
                            color      = Color(0xFF111827),
                            fontWeight = FontWeight.Bold,
                            fontSize   = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 册数选择流（直接显示第1册/第2册，无多余B1/B2）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun BookStoryRow(currentBook: Int, onBookClick: (Int) -> Unit) {
    val books = listOf("第1册", "第2册", "第3册", "第4册", "第5册", "第6册")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(books.indices.toList(), key = { it }) { index ->
            val bookNum = index + 1
            val title = books[index]
            val isActive = bookNum == currentBook

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isActive) Color(0xFF111827) else Color.White,
                border = BorderStroke(1.dp, if (isActive) Color(0xFF111827) else Color(0xFFE5E7EB)),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onBookClick(bookNum) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isActive) Color.White else Color(0xFF4B5563),
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 今日核心动作区（高质感双卡）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun TodayActionRow(
    dueCount : Int,
    newCount : Int,
    onReview : () -> Unit,
    onLearn  : () -> Unit,
    modifier : Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 复习卡片（深色高对比主按键）
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onReview() },
            color = Color(0xFF111827),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "待复习 (FSRS)",
                    color      = Color(0xFF9CA3AF),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text       = "$dueCount 条",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp
                )
            }
        }

        // 新词卡片（纯白高光副按键）
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(84.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onLearn() },
            color  = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            shape  = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text       = "今日新词",
                    color      = Color(0xFF6B7280),
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text       = "$newCount 词",
                    color      = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 专项功能分类矩阵（纯文字胶囊 + 彻底避免小字截断）
// ══════════════════════════════════════════════════════════════════

data class FeatureCategory(
    val categoryName: String,
    val items: List<FeatureCardData>
)

data class FeatureCardData(
    val title: String,
    val route: String
)

private val CATEGORIZED_FEATURES = listOf(
    FeatureCategory(
        "核心工具",
        listOf(
            FeatureCardData("语素拆解镜", Screen.KoreanLens.route),
            FeatureCardData("双语分级精读", Screen.GradedReader.route),
            FeatureCardData("能力分析雷达", Screen.LearningAnalytics.route)
        )
    ),
    FeatureCategory(
        "语言学实验室",
        listOf(
            FeatureCardData("音变实验室", Screen.PhonologyLab.route),
            FeatureCardData("敬语阶称", Screen.SpeechLevelSandbox.route),
            FeatureCardData("声调纠音", Screen.ShadowingTrainer.route),
            FeatureCardData("规范隔写", Screen.SpacingCorrection.route),
            FeatureCardData("笔顺临摹", Screen.HangulStroke.route)
        )
    ),
    FeatureCategory(
        "音乐与随身听",
        listOf(
            FeatureCardData("歌词精听", Screen.Dictation.route),
            FeatureCardData("随身磨耳朵", Screen.BackgroundRadio.route)
        )
    ),
    FeatureCategory(
        "竞技与实战",
        listOf(
            FeatureCardData("词尾接龙", Screen.WordChain.route),
            FeatureCardData("TOPIK 作文", Screen.TopikEssayGrader.route),
            FeatureCardData("TOPIK 考点", Screen.TopikExam.route),
            FeatureCardData("情境对话", Screen.DialogueSandbox.route),
            FeatureCardData("外来语对照", Screen.LoanwordModule.route),
            FeatureCardData("俗语成语", Screen.IdiomModule.route)
        )
    )
)

@Composable
private fun CategorizedFeatureMatrix(onNavigate: (String) -> Unit) {
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    val currentCategory = CATEGORIZED_FEATURES[selectedCategoryIndex]

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 分类切换胶囊栏（纯净文字，无 Emoji）
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            itemsIndexed(CATEGORIZED_FEATURES) { index, cat ->
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
                        .clickable { selectedCategoryIndex = index }
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

        // 卡片网格（2 列大字清晰排版，无易被截断的小字副标）
        val rows = currentCategory.items.chunked(2)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(item.route) },
                        color  = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shape  = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = item.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = Color(0xFF111827)
                            )
                            Text(
                                text       = "→",
                                fontSize   = 14.sp,
                                color      = Color(0xFF9CA3AF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                // 单数补位
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 示例词汇卡片流（纯白卡片，零堆内存分配）
// ══════════════════════════════════════════════════════════════════

private val STATIC_SAMPLE_WORDS = listOf(
    Triple("안녕하세요", "您好（日常问候）", "감탄사"),
    Triple("공부하다", "学习，用功", "동사"),
    Triple("선생님", "老师，先生", "명사"),
    Triple("한국어", "韩国语，韩文", "명사"),
    Triple("감사합니다", "谢谢，感谢", "동사"),
    Triple("친구", "朋友，同伴", "명사"),
    Triple("학교", "学校", "명사"),
    Triple("재미있다", "有趣，有意思", "형용사")
)

@Composable
private fun SampleWordFeedCard(index: Int, onClick: () -> Unit) {
    val word = STATIC_SAMPLE_WORDS[index % STATIC_SAMPLE_WORDS.size]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color  = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = word.first,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = Color(0xFF111827),
                        letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text       = word.third,
                            fontSize   = 10.sp,
                            color      = Color(0xFF6B7280),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = word.second,
                    fontSize = 13.sp,
                    color    = Color(0xFF6B7280)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF111827))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text       = "记忆",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
        }
    }
}
