package com.yonsei.koreanlearner.ui.screen.grammar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yonsei.koreanlearner.algorithm.korean.MorphologicalAlert
import com.yonsei.koreanlearner.algorithm.korean.MorphologicalConjugationEngine
import com.yonsei.koreanlearner.algorithm.korean.PragmaticConstraintEngine
import com.yonsei.koreanlearner.algorithm.korean.PragmaticRule
import com.yonsei.koreanlearner.ui.components.GrammarComparisonCard
import com.yonsei.koreanlearner.ui.components.GrammarComparisonGroup
import com.yonsei.koreanlearner.ui.components.YonseiGrammarComparisonPresets
import com.yonsei.koreanlearner.ui.theme.*

// ══════════════════════════════════════════════════════════════════
// GrammarTreeScreen — 语法进阶模块主界面 (Instagram 极简轻奢风)
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarTreeScreen(
    viewModel: GrammarTreeViewModel = hiltViewModel()
) {
    val state        by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val sheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHost) },
        containerColor = Color(0xFFF7F8FA),
        topBar = {
            GrammarTopBar(
                state          = state,
                onSearch       = { viewModel.onIntent(GrammarTreeIntent.UpdateSearchQuery(it)) },
                onClear        = { viewModel.onIntent(GrammarTreeIntent.ClearSearch) },
                onSelectBook   = { viewModel.onIntent(GrammarTreeIntent.SelectBook(it)) },
                onToggleStarredOnly = { viewModel.onIntent(GrammarTreeIntent.ToggleStarredOnly) },
                onSelectHangulInitial = { viewModel.onIntent(GrammarTreeIntent.SelectHangulInitial(it)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))
                .padding(top = padding.calculateTopPadding())
        ) {
            when {
                state.isLoading -> GrammarLoadingState()
                state.isSearchActive -> GrammarSearchResults(state, viewModel)
                else -> GrammarGroupedTree(state, viewModel)
            }
        }
    }

    // ── 语法详情 BottomSheet ──────────────────────────────────
    if (state.showDetailSheet && state.selectedGrammar != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onIntent(GrammarTreeIntent.CloseGrammarDetail) },
            sheetState       = sheetState,
            containerColor   = Color.White,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            GrammarDetailPanel(state = state, viewModel = viewModel)
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 顶栏（Instagram 极简高光白）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun GrammarTopBar(
    state                 : GrammarTreeUiState,
    onSearch              : (String) -> Unit,
    onClear               : () -> Unit,
    onSelectBook          : (Int) -> Unit,
    onToggleStarredOnly   : () -> Unit,
    onSelectHangulInitial : (String) -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current

    Surface(
        color = Color(0xFFF7F8FA),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ── 标题行 ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text          = "语法研习",
                        fontSize      = 22.sp,
                        fontWeight    = FontWeight.Black,
                        color         = Color(0xFF111827),
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text          = "YONSEI GRAMMAR · 328",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        color         = Color(0xFF9CA3AF),
                        letterSpacing = 1.sp
                    )
                }

                if (state.starredPatterns.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (state.isStarredOnly) Color(0xFF111827) else Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                            .clickable { onToggleStarredOnly() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "已收藏 ${state.starredPatterns.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isStarredOnly) Color.White else Color(0xFF111827)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── 搜索框 ─────────────────────────────────────────
            OutlinedTextField(
                value         = state.searchQuery,
                onValueChange = onSearch,
                modifier      = Modifier.fillMaxWidth().height(50.dp),
                textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color(0xFF111827)),
                placeholder   = { Text("搜索语法句型（如：-기 때문에）", fontSize = 13.sp, color = Color(0xFF9CA3AF)) },
                leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF9CA3AF)) },
                trailingIcon  = if (state.searchQuery.isNotBlank()) {
                    { IconButton(onClick = onClear) { Icon(Icons.Default.Clear, contentDescription = "清除", tint = Color(0xFF9CA3AF)) } }
                } else null,
                shape         = RoundedCornerShape(14.dp),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor   = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor     = Color(0xFF111827),
                    unfocusedBorderColor   = Color(0xFFE5E7EB)
                )
            )

            Spacer(Modifier.height(10.dp))

            // ── 册数筛选行 ────────────────────────────────────
            BookFilterRow(
                selectedBook        = state.selectedBook,
                isStarredOnly       = state.isStarredOnly,
                starredCount        = state.starredPatterns.size,
                onSelect            = onSelectBook,
                onToggleStarredOnly = onToggleStarredOnly
            )

            Spacer(Modifier.height(8.dp))

            // ── 韩文字母首辅音索引栏 (ㄱ~ㅎ) ────────────────────
            HangulInitialFilterRow(
                selectedInitial = state.selectedHangulInitial,
                onSelect        = onSelectHangulInitial
            )
        }
    }
}

// ── 册数筛选行 ───────────────────────────────────────────────────

@Composable
private fun BookFilterRow(
    selectedBook        : Int,
    isStarredOnly       : Boolean,
    starredCount        : Int,
    onSelect            : (Int) -> Unit,
    onToggleStarredOnly : () -> Unit
) {
    val books = listOf(0 to "全部", 1 to "第1册", 2 to "第2册", 3 to "第3册", 4 to "第4册", 5 to "第5册", 6 to "第6册")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier             = Modifier.padding(bottom = 2.dp)
    ) {
        items(books, key = { it.first }, contentType = { "book_chip" }) { (id, label) ->
            val isSelected = (!isStarredOnly && selectedBook == id)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (isSelected) Modifier.background(Color(0xFF111827))
                        else Modifier
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                    )
                    .clickable { onSelect(id) }
                    .padding(horizontal = 13.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label,
                    fontSize   = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) Color.White else Color(0xFF6B7280)
                )
            }
        }

        // 星标收藏专用 Chip
        item {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .then(
                        if (isStarredOnly) Modifier.background(Color(0xFF111827))
                        else Modifier
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                    )
                    .clickable { onToggleStarredOnly() }
                    .padding(horizontal = 13.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = if (starredCount > 0) "已标星 $starredCount" else "已标星",
                    fontSize   = 12.sp,
                    fontWeight = if (isStarredOnly) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isStarredOnly) Color.White else Color(0xFF6B7280)
                )
            }
        }
    }
}

// ── 韩文字母首辅音索引栏 (ㄱ~ㅎ) ──────────────────────────────────

@Composable
private fun HangulInitialFilterRow(
    selectedInitial: String,
    onSelect: (String) -> Unit
) {
    val initials = listOf("全部", "ㄱ", "ㄴ", "ㄷ", "ㄹ", "ㅁ", "ㅂ", "ㅅ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        items(initials) { initial ->
            val isAll = initial == "全部"
            val isSelected = (isAll && selectedInitial.isBlank()) || selectedInitial == initial
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (isSelected) Modifier.background(Color(0xFF111827))
                        else Modifier
                            .background(Color(0xFFF3F4F6))
                    )
                    .clickable { onSelect(if (isAll) "" else initial) }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF4B5563)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 语法树（分组展示）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun GrammarGroupedTree(state: GrammarTreeUiState, viewModel: GrammarTreeViewModel) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = viewModel.appStateManager.getGrammarScrollIndex(),
        initialFirstVisibleItemScrollOffset = viewModel.appStateManager.getGrammarScrollOffset()
    )

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (!listState.isScrollInProgress) {
            viewModel.appStateManager.setGrammarScroll(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset
            )
        }
    }

    if (state.lessonGroups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isStarredOnly) "暂无标星收藏的语法\n可在语法卡片右侧点击星标加入" else "暂无符合条件的语法",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
        return
    }

    LazyColumn(
        state               = listState,
        contentPadding      = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        state.lessonGroups.forEach { group ->
            item(key = "header_${group.book}_${group.lesson}", contentType = "lesson_header") {
                LessonGroupHeader(
                    group     = group,
                    onClick   = { viewModel.onIntent(GrammarTreeIntent.ToggleLessonGroup(group.lesson)) }
                )
            }

            if (group.isExpanded) {
                items(
                    items       = group.nodes,
                    key         = { it.id },
                    contentType = { "grammar_card" }
                ) { node ->
                    GrammarNodeCard(
                        node        = node,
                        onClick     = { viewModel.onIntent(GrammarTreeIntent.OpenGrammarDetail(node)) },
                        onToggleStar = { viewModel.onIntent(GrammarTreeIntent.ToggleStar(node.grammarPattern)) }
                    )
                }
            }
        }
    }
}

// ── 课时分组标题（可折叠） ──────────────────────────────────────────

@Composable
private fun LessonGroupHeader(group: GrammarLessonGroup, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue   = if (group.isExpanded) 0f else -90f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "arrow_rotation"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint     = Color(0xFF111827),
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
            Spacer(Modifier.width(6.dp))
            Text(group.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE5E7EB))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                "${group.nodes.size} 条",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color    = Color(0xFF4B5563)
            )
        }
    }
}

// ── 语法卡片（纯白独立卡片 + 一键星标） ──────────────────────────────

@Composable
private fun GrammarNodeCard(
    node        : GrammarNode,
    onClick     : () -> Unit,
    onToggleStar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color  = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape  = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text       = node.grammarPattern,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF111827)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text      = "${node.category} · ${node.explanation.take(24)}",
                    fontSize  = 12.sp,
                    color     = Color(0xFF6B7280),
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
            }

            // 星标收藏按钮
            IconButton(
                onClick = onToggleStar,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (node.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "收藏语法",
                    tint = if (node.isStarred) Color(0xFF111827) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text       = "→",
                fontSize   = 16.sp,
                color      = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 搜索结果
// ══════════════════════════════════════════════════════════════════

@Composable
private fun GrammarSearchResults(state: GrammarTreeUiState, viewModel: GrammarTreeViewModel) {
    if (state.searchResults.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到相关语法", color = Color(0xFF9CA3AF))
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp)) {
        items(state.searchResults, key = { it.id }, contentType = { "grammar_card" }) { node ->
            GrammarNodeCard(
                node = node,
                onClick = { viewModel.onIntent(GrammarTreeIntent.OpenGrammarDetail(node)) },
                onToggleStar = { viewModel.onIntent(GrammarTreeIntent.ToggleStar(node.grammarPattern)) }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 语法详情面板（BottomSheet 内容）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun GrammarDetailPanel(state: GrammarTreeUiState, viewModel: GrammarTreeViewModel) {
    val grammar = state.selectedGrammar ?: return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
    ) {
        // ── 详情/练习/对比 三 Tab ─────────────────────────────
        var selectedTab by remember {
            mutableIntStateOf(viewModel.appStateManager.getLastGrammarDetailTab().coerceIn(0, 2))
        }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = Color.White,
            contentColor     = Color(0xFF111827)
        ) {
            listOf("语法详解", "辨析矩阵", "实战速测").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = {
                        selectedTab = index
                        viewModel.appStateManager.setLastGrammarDetailTab(index)
                        when (index) {
                            1 -> viewModel.onIntent(GrammarTreeIntent.OpenComparePanel(listOf(grammar)))
                            2 -> viewModel.onIntent(GrammarTreeIntent.StartExercise(grammar))
                        }
                    },
                    text = {
                        Text(
                            title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) Color(0xFF111827) else Color(0xFF6B7280)
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> GrammarDetailCard(grammar = grammar, viewModel = viewModel)
            1 -> ComparePanel(state = state)
            2 -> MultiTypeExercisePanel(state = state, viewModel = viewModel)
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// GrammarDetailCard（语法详情卡）
// ══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrammarDetailCard(grammar: GrammarNode, viewModel: GrammarTreeViewModel) {
    val pragmaticRules = remember(grammar) {
        PragmaticConstraintEngine.analyzePragmaticRules(grammar.grammarPattern, grammar.explanation)
    }
    val morphologicalAlerts = remember(grammar) {
        MorphologicalConjugationEngine.analyzeConjugationAlerts(grammar.grammarPattern, grammar.conjugationRules)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 顶栏：Pattern + 分类 + 收藏按钮 ─────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = grammar.grammarPattern,
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Black,
                    color      = Color(0xFF111827)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "第${grammar.book}册 · 第${grammar.lesson}课 · ${grammar.category}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = { viewModel.onIntent(GrammarTreeIntent.ToggleStar(grammar.grammarPattern)) }) {
                Icon(
                    imageVector = if (grammar.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    contentDescription = "收藏语法",
                    tint = if (grammar.isStarred) Color(0xFF111827) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── 语用学褒贬与句式限制规则标签 ───────────────────────
        if (pragmaticRules.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pragmaticRules.forEach { rule ->
                    val bg = if (rule.isNegativeConstraint) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                    val border = if (rule.isNegativeConstraint) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    val textCol = if (rule.isNegativeConstraint) Color(0xFFDC2626) else Color(0xFF16A34A)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = rule.badgeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textCol
                        )
                    }
                }
            }
        }

        // ── 形态学接续不规则变形警示卡片 ───────────────────────
        if (morphologicalAlerts.isNotEmpty()) {
            MorphologicalAlertsCard(alerts = morphologicalAlerts)
        }

        // ── 接续规则框 ─────────────────────────────────────────
        ConjugationRuleCard(rules = grammar.conjugationRules)

        // ── 完整说明 ───────────────────────────────────────────
        ExplanationSection(explanation = grammar.explanation)

        // ── 语用限制详细解析（若有）───────────────────────────
        if (pragmaticRules.isNotEmpty()) {
            PragmaticRulesDetailCard(rules = pragmaticRules)
        }

        // ── 例句 ───────────────────────────────────────────────
        if (grammar.examples.isNotEmpty()) {
            ExamplesSection(examples = grammar.examples, pattern = grammar.grammarPattern)
        }

        // ── 关联语法 ───────────────────────────────────────────
        if (grammar.relatedGrammars.isNotEmpty()) {
            RelatedGrammarsSection(
                relatedGrammars = grammar.relatedGrammars,
                onCompare = { relatedPattern ->
                    val relatedNode = GrammarNode(
                        id = -1, book = grammar.book, lesson = grammar.lesson,
                        subLesson = "", grammarPattern = relatedPattern, category = "",
                        explanation = "", conjugationRules = "", examples = emptyList(),
                        relatedGrammars = emptyList()
                    )
                    viewModel.onIntent(GrammarTreeIntent.OpenComparePanel(listOf(grammar, relatedNode)))
                }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── 形态学接续不规则警示卡片 ─────────────────────────────────────

@Composable
private fun MorphologicalAlertsCard(alerts: List<MorphologicalAlert>) {
    Surface(
        color  = Color(0xFFFFFBEB),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFFEF3C7))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "形态学接续变形警示",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )

            alerts.forEach { alert ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("•", color = Color(0xFFB45309), modifier = Modifier.width(14.dp), fontWeight = FontWeight.Bold)
                        Text(
                            text = "${alert.type}：${alert.ruleSummary}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF78350F)
                        )
                    }
                    Text(
                        text = "   例：${alert.exampleBefore} → ${alert.exampleAfter}",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    }
}

// ── 语用限制详细解析卡片 ─────────────────────────────────────────

@Composable
private fun PragmaticRulesDetailCard(rules: List<PragmaticRule>) {
    Surface(
        color  = Color(0xFFF9FAFB),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "语用语境与句式制约",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            rules.forEach { rule ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("•", color = Color(0xFF111827), modifier = Modifier.width(14.dp), fontWeight = FontWeight.Bold)
                    Column {
                        Text(
                            text = "${rule.badgeLabel}：${rule.ruleDetail}",
                            fontSize = 12.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ── 接续规则卡 ─────────────────────────────────────────────────────

@Composable
private fun ConjugationRuleCard(rules: String) {
    Surface(
        color  = Color(0xFFF9FAFB),
        shape  = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "接续法则",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            rules.split("/").forEach { rule ->
                if (rule.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("•", color = Color(0xFF111827), modifier = Modifier.width(16.dp), fontWeight = FontWeight.Bold)
                        Text(
                            rule.trim(),
                            fontSize = 13.sp,
                            color = Color(0xFF4B5563),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ── 说明区 ────────────────────────────────────────────────────────

@Composable
private fun ExplanationSection(explanation: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("用法说明", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(explanation, fontSize = 14.sp, color = Color(0xFF374151), lineHeight = 22.sp)
    }
}

// ── 例句区 ────────────────────────────────────────────────────────

@Composable
private fun ExamplesSection(examples: List<com.yonsei.koreanlearner.data.local.entity.GrammarExample>, pattern: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("核心例句", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        examples.take(4).forEachIndexed { idx, ex ->
            ExampleItem(idx + 1, ex.korean, ex.chinese)
        }
    }
}

@Composable
private fun ExampleItem(num: Int, korean: String, chinese: String) {
    Surface(
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(korean, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(Modifier.height(3.dp))
            Text(chinese, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
    }
}

// ── 关联语法 ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelatedGrammarsSection(relatedGrammars: List<String>, onCompare: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("关联辨析", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            relatedGrammars.forEach { related ->
                AssistChip(
                    onClick = { onCompare(related) },
                    label   = { Text(related, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    shape   = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// ComparePanel（语法对比辨析面板）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun ComparePanel(state: GrammarTreeUiState) {
    val selectedGrammar = state.selectedGrammar
    val pattern = selectedGrammar?.grammarPattern ?: ""

    // 默认自动匹配当前语法最契合的辨析组
    var currentGroupIndex by remember(selectedGrammar) {
        val defaultIdx = when {
            pattern.contains("때문") || pattern.contains("어서") || pattern.contains("니까") || pattern.contains("느라고") || pattern.contains("바람") || pattern.contains("탓") -> 0
            pattern.contains("지만") || pattern.contains("는데") || pattern.contains("반면") -> 1
            pattern.contains("자마자") || pattern.contains("대로") || pattern.contains("무섭게") || pattern.contains("고 나서") -> 2
            pattern.contains("뿐만") || pattern.contains("데다가") || pattern.contains("거니와") -> 3
            pattern.contains("다고") || pattern.contains("냐고") || pattern.contains("자고") || pattern.contains("라고") || pattern.contains("대요") || pattern.contains("래요") -> 4
            else -> 0
        }
        mutableIntStateOf(defaultIdx)
    }

    val groups = YonseiGrammarComparisonPresets.ALL_PRESET_GROUPS
    val activeGroup = groups.getOrElse(currentGroupIndex) { groups[0] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 顶部辨析矩阵切换横滑栏
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(groups) { index, g ->
                val isSel = index == currentGroupIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .then(
                            if (isSel) Modifier.background(Color(0xFF111827))
                            else Modifier
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                        )
                        .clickable { currentGroupIndex = index }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = g.category,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else Color(0xFF4B5563)
                    )
                }
            }
        }

        GrammarComparisonCard(comparisonGroup = activeGroup)
    }
}

// ══════════════════════════════════════════════════════════════════
// MultiTypeExercisePanel（多题型互动速测面板：选择/改错/填空）
// ══════════════════════════════════════════════════════════════════

@Composable
private fun MultiTypeExercisePanel(state: GrammarTreeUiState, viewModel: GrammarTreeViewModel) {
    if (state.currentQuestions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.exerciseComplete) {
        ExerciseCompleteView(state = state, onRetry = {
            state.selectedGrammar?.let {
                viewModel.onIntent(GrammarTreeIntent.StartExercise(it))
            }
        })
        return
    }

    val question = state.currentQuestions.getOrNull(state.exerciseProgress) ?: return
    val answer   = state.answers[question.id]
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 进度
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "第 ${state.exerciseProgress + 1} / ${state.currentQuestions.size} 题 · ${question.stem}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
            Text(
                when (question.type) {
                    ExerciseType.MULTIPLE_CHOICE -> "四选一客观题"
                    ExerciseType.ERROR_CORRECTION -> "语用找错改错"
                    ExerciseType.FILL_BLANK -> "形态学接续填空"
                },
                fontSize = 11.sp,
                color = Color(0xFF6B7280)
            )
        }

        LinearProgressIndicator(
            progress = { (state.exerciseProgress + 1).toFloat() / state.currentQuestions.size },
            modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)),
            color = Color(0xFF111827),
            trackColor = Color(0xFFE5E7EB)
        )

        // 题目语境卡
        Surface(
            color = Color(0xFFF9FAFB),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(question.contextChinese, fontSize = 12.sp, color = Color(0xFF6B7280))
                Text(question.context, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827), lineHeight = 22.sp)
            }
        }

        val isSubmitted = answer?.isSubmitted == true
        val isCorrect   = answer?.isCorrect   == true
        val showHint    = answer?.showHint     == true

        // ── 选项题（选择题 / 纠错题） ─────────────────────────────
        if (question.options.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEach { option ->
                    val isSelected = answer?.userInput == option
                    val optCorrect = option == question.correctAnswer || (question.type == ExerciseType.ERROR_CORRECTION && option.startsWith(question.correctAnswer))

                    val (bg, border, textCol) = when {
                        isSubmitted && optCorrect -> Triple(Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFF16A34A))
                        isSubmitted && isSelected && !isCorrect -> Triple(Color(0xFFFEF2F2), Color(0xFFFEE2E2), Color(0xFFDC2626))
                        isSelected -> Triple(Color(0xFF111827), Color(0xFF111827), Color.White)
                        else -> Triple(Color.White, Color(0xFFE5E7EB), Color(0xFF111827))
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isSubmitted) {
                                viewModel.onIntent(GrammarTreeIntent.SelectOption(question.id, option))
                            },
                        color = bg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, border)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || (isSubmitted && optCorrect)) FontWeight.Bold else FontWeight.Medium,
                                color = textCol
                            )
                            if (isSubmitted && optCorrect) {
                                Text("✓", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            } else if (isSubmitted && isSelected && !isCorrect) {
                                Text("✗", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // ── 填空题输入框 ───────────────────────────────────────
            OutlinedTextField(
                value         = answer?.userInput ?: "",
                onValueChange = { if (!isSubmitted) viewModel.onIntent(GrammarTreeIntent.UpdateAnswer(question.id, it)) },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("请填入正确变形后的形态", fontSize = 13.sp) },
                shape         = RoundedCornerShape(12.dp),
                enabled       = !isSubmitted,
                isError       = isSubmitted && !isCorrect,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Color(0xFF111827),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    if (!isSubmitted) viewModel.onIntent(GrammarTreeIntent.SubmitAnswer(question.id))
                })
            )
        }

        // 提交后的解析与提示
        AnimatedVisibility(visible = isSubmitted) {
            Surface(
                color = if (isCorrect) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isCorrect) "回答正确！" else "正确答案：${question.correctAnswer}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                    Text(
                        text = question.answerHint,
                        fontSize = 12.sp,
                        color = if (isCorrect) Color(0xFF15803D) else Color(0xFF991B1B),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 操作按钮
        if (!isSubmitted && question.options.isEmpty()) {
            Button(
                onClick  = { keyboard?.hide(); viewModel.onIntent(GrammarTreeIntent.SubmitAnswer(question.id)) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                enabled  = answer?.userInput?.isNotBlank() == true
            ) { Text("提交答案", fontWeight = FontWeight.Bold, color = Color.White) }
        } else if (isSubmitted) {
            Button(
                onClick  = { viewModel.onIntent(GrammarTreeIntent.NextExerciseQuestion) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
            ) {
                Text(
                    text = if (state.exerciseProgress + 1 >= state.currentQuestions.size) "查看练习报告" else "下一题 →",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ── 练习完成视图 ─────────────────────────────────────────────────

@Composable
private fun ExerciseCompleteView(state: GrammarTreeUiState, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val accuracy = (state.exerciseAccuracy * 100).toInt()
        Text("练习结算", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF111827))
        Spacer(Modifier.height(8.dp))
        Text(
            "正确率 $accuracy% · 答对 ${state.answers.values.count { it.isCorrect }}/${state.answers.size} 题",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827))
        ) {
            Text("再练一遍", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 工具组件
// ══════════════════════════════════════════════════════════════════

@Composable
private fun GrammarLoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF111827)) }
}