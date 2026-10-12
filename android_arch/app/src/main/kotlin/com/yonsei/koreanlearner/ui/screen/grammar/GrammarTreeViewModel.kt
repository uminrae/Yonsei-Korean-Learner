package com.yonsei.koreanlearner.ui.screen.grammar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yonsei.koreanlearner.data.local.dao.GrammarDao
import com.yonsei.koreanlearner.data.local.entity.GrammarExample
import com.yonsei.koreanlearner.core.state.AppStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GrammarTreeScreen ViewModel（MVI 架构）
 */
@HiltViewModel
class GrammarTreeViewModel @Inject constructor(
    private val grammarDao: GrammarDao,
    val appStateManager: AppStateManager
) : ViewModel() {

    private val _state  = MutableStateFlow(
        GrammarTreeUiState(
            selectedBook = appStateManager.getLastGrammarBook(),
            isStarredOnly = appStateManager.isStarredOnlyFilter(),
            selectedHangulInitial = appStateManager.getLastHangulInitial(),
            starredPatterns = appStateManager.getStarredGrammars()
        )
    )
    val state: StateFlow<GrammarTreeUiState> = _state.asStateFlow()

    private val _effect = Channel<GrammarTreeEffect>(Channel.BUFFERED)
    val effect: Flow<GrammarTreeEffect> = _effect.receiveAsFlow()

    // 原始数据缓存（所有册，用于本地过滤/搜索，避免频繁查询 Room）
    private val allNodesCache = MutableStateFlow<List<GrammarNode>>(emptyList())

    init {
        loadAllGrammars()
    }

    // ════════════════════════════════════════════════════════════
    // Intent 分发
    // ════════════════════════════════════════════════════════════

    fun onIntent(intent: GrammarTreeIntent) {
        when (intent) {
            is GrammarTreeIntent.SelectBook          -> selectBook(intent.book)
            is GrammarTreeIntent.SelectLesson        -> selectLesson(intent.lesson)
            is GrammarTreeIntent.SelectCategory      -> selectCategory(intent.category)
            is GrammarTreeIntent.ToggleLessonGroup   -> toggleLessonGroup(intent.lesson)
            is GrammarTreeIntent.ToggleStar          -> toggleStar(intent.pattern)
            is GrammarTreeIntent.ToggleStarredOnly   -> toggleStarredOnly()
            is GrammarTreeIntent.SelectHangulInitial -> selectHangulInitial(intent.initial)
            is GrammarTreeIntent.UpdateSearchQuery   -> updateSearch(intent.query)
            is GrammarTreeIntent.ClearSearch         -> clearSearch()
            is GrammarTreeIntent.OpenGrammarDetail   -> openDetail(intent.node)
            is GrammarTreeIntent.CloseGrammarDetail  -> closeDetail()
            is GrammarTreeIntent.OpenComparePanel    -> openCompare(intent.targets)
            is GrammarTreeIntent.CloseComparePanel   -> _state.update { it.copy(showComparePanel = false, compareTargets = emptyList()) }
            is GrammarTreeIntent.StartExercise       -> startExercise(intent.grammar)
            is GrammarTreeIntent.UpdateAnswer        -> updateAnswer(intent.questionId, intent.input)
            is GrammarTreeIntent.SelectOption        -> selectOption(intent.questionId, intent.option)
            is GrammarTreeIntent.SubmitAnswer        -> submitAnswer(intent.questionId)
            is GrammarTreeIntent.ToggleHint          -> toggleHint(intent.questionId)
            is GrammarTreeIntent.NextExerciseQuestion -> nextQuestion()
            is GrammarTreeIntent.FinishExercise      -> finishExercise()
            is GrammarTreeIntent.DismissError        -> _state.update { it.copy(errorMessage = null) }
        }
    }

    // ════════════════════════════════════════════════════════════
    // 数据加载
    // ════════════════════════════════════════════════════════════

    private fun loadAllGrammars() {
        viewModelScope.launch {
            try {
                val starred = appStateManager.getStarredGrammars()
                grammarDao.getAllGrammars()
                    .collect { entities ->
                        val nodes = entities.map { it.toGrammarNode().copy(isStarred = starred.contains(it.grammarPattern)) }
                            .distinctBy { "${it.book}_${it.lesson}_${it.subLesson}_${it.grammarPattern}" }
                            .sortedWith(compareBy({ it.book }, { it.lesson }, { it.subLesson }))
                        allNodesCache.value = nodes

                        // 提取所有分类
                        val categories = nodes.map { it.category }.distinct().sorted()

                        // 按当前筛选条件与持久化展开状态重建分组
                        rebuildGroups(nodes, categories)

                        // 自动恢复上次停留查看的语法详情弹窗（若有）
                        val lastPattern = appStateManager.getLastSelectedGrammarPattern()
                        if (lastPattern.isNotBlank()) {
                            val targetNode = nodes.find { it.grammarPattern == lastPattern }
                            if (targetNode != null) {
                                _state.update { it.copy(selectedGrammar = targetNode, showDetailSheet = true) }
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "加载语法数据失败：${e.message}") }
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // 筛选 & 搜索
    // ════════════════════════════════════════════════════════════

    private fun selectBook(book: Int) {
        appStateManager.setLastGrammarBook(book)
        _state.update { it.copy(selectedBook = book, selectedLesson = 0) }
        rebuildGroups()
    }

    private fun selectLesson(lesson: Int) {
        appStateManager.setLastGrammarLesson(lesson)
        _state.update { it.copy(selectedLesson = lesson) }
        rebuildGroups()
    }

    private fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
        rebuildGroups()
    }

    private fun toggleStar(pattern: String) {
        val isNowStarred = appStateManager.toggleStarredGrammar(pattern)
        val newStarredSet = appStateManager.getStarredGrammars()
        _state.update { state ->
            val updatedNodes = allNodesCache.value.map {
                if (it.grammarPattern == pattern) it.copy(isStarred = isNowStarred) else it
            }
            allNodesCache.value = updatedNodes
            val updatedSelected = if (state.selectedGrammar?.grammarPattern == pattern) {
                state.selectedGrammar.copy(isStarred = isNowStarred)
            } else state.selectedGrammar
            state.copy(starredPatterns = newStarredSet, selectedGrammar = updatedSelected)
        }
        rebuildGroups()
    }

    private fun toggleStarredOnly() {
        val newStarredOnly = !_state.value.isStarredOnly
        appStateManager.setStarredOnlyFilter(newStarredOnly)
        _state.update { it.copy(isStarredOnly = newStarredOnly) }
        rebuildGroups()
    }

    private fun selectHangulInitial(initial: String) {
        val newInitial = if (_state.value.selectedHangulInitial == initial) "" else initial
        appStateManager.setLastHangulInitial(newInitial)
        _state.update { it.copy(selectedHangulInitial = newInitial) }
        rebuildGroups()
    }

    private fun updateSearch(query: String) {
        _state.update { it.copy(searchQuery = query, isSearchActive = query.isNotBlank()) }
        if (query.isBlank()) { clearSearch(); return }

        viewModelScope.launch {
            grammarDao.searchGrammars(query)
                .collect { entities ->
                    val starred = appStateManager.getStarredGrammars()
                    val results = entities.map { it.toGrammarNode().copy(isStarred = starred.contains(it.grammarPattern)) }
                        .distinctBy { "${it.book}_${it.lesson}_${it.subLesson}_${it.grammarPattern}" }
                    _state.update { it.copy(searchResults = results) }
                }
        }
    }

    private fun clearSearch() {
        _state.update { it.copy(searchQuery = "", isSearchActive = false, searchResults = emptyList()) }
    }

    private fun toggleLessonGroup(lesson: Int) {
        val currentGroups = _state.value.lessonGroups
        val targetGroup = currentGroups.find { it.lesson == lesson }
        val targetBook = targetGroup?.book ?: _state.value.selectedBook
        val newExpanded = !(targetGroup?.isExpanded ?: true)

        // 持久化当前课的折叠/展开记忆
        appStateManager.setLessonExpanded(targetBook, lesson, newExpanded)

        _state.update {
            it.copy(
                lessonGroups = currentGroups.map { group ->
                    if (group.lesson == lesson && (targetBook == 0 || group.book == targetBook)) {
                        group.copy(isExpanded = newExpanded)
                    } else group
                }
            )
        }
    }

    /**
     * 根据当前筛选条件重建分组列表（读取持久化展开状态与四维筛选）。
     */
    private fun rebuildGroups(
        allNodes   : List<GrammarNode> = allNodesCache.value,
        categories : List<String>      = _state.value.allCategories
    ) {
        val state = _state.value

        // 应用筛选条件 (册数 + 课次 + 分类 + 仅标星 + 首辅音)
        val filtered = allNodes.filter { node ->
            val matchesBook = (state.selectedBook == 0 || node.book == state.selectedBook)
            val matchesLesson = (state.selectedLesson == 0 || node.lesson == state.selectedLesson)
            val matchesCategory = (state.selectedCategory.isBlank() || node.category == state.selectedCategory)
            val matchesStarred = (!state.isStarredOnly || node.isStarred)
            val matchesInitial = (state.selectedHangulInitial.isBlank() || getHangulInitial(node.grammarPattern) == state.selectedHangulInitial)
            matchesBook && matchesLesson && matchesCategory && matchesStarred && matchesInitial
        }

        // 按册+课分组
        val groups = filtered
            .groupBy { "${it.book}-${it.lesson}" }
            .entries
            .sortedWith(compareBy({ it.value.first().book }, { it.value.first().lesson }))
            .map { (_, nodes) ->
                val first = nodes.first()
                val isExpanded = appStateManager.isLessonExpanded(first.book, first.lesson)
                GrammarLessonGroup(
                    book       = first.book,
                    lesson     = first.lesson,
                    title      = "第${first.book}册 · 第${first.lesson}课",
                    nodes      = nodes.sortedBy { it.subLesson },
                    isExpanded = isExpanded
                )
            }

        _state.update {
            it.copy(
                lessonGroups   = groups,
                allCategories  = categories,
                isLoading      = false
            )
        }
    }

    // ════════════════════════════════════════════════════════════
    // 详情面板
    // ════════════════════════════════════════════════════════════

    private fun openDetail(node: GrammarNode) {
        appStateManager.setLastSelectedGrammarPattern(node.grammarPattern)
        _state.update { it.copy(selectedGrammar = node, showDetailSheet = true) }
    }

    private fun closeDetail() {
        appStateManager.clearLastSelectedGrammarPattern()
        _state.update { it.copy(showDetailSheet = false, selectedGrammar = null, exerciseMode = false) }
    }

    // ════════════════════════════════════════════════════════════
    // 比较辨析
    // ════════════════════════════════════════════════════════════

    private fun openCompare(targets: List<GrammarNode>) {
        val compareList = if (targets.size >= 2) targets
        else {
            val primary  = targets.firstOrNull() ?: return
            val related  = allNodesCache.value.filter { node ->
                primary.relatedGrammars.any { rel -> node.grammarPattern.contains(rel.take(4)) }
            }.take(3)
            listOf(primary) + related
        }
        _state.update { it.copy(showComparePanel = true, compareTargets = compareList) }
    }

    // ════════════════════════════════════════════════════════════
    // 综合多题型练习引擎（完全离线）
    // ════════════════════════════════════════════════════════════

    private fun startExercise(grammar: GrammarNode) {
        val questions = ExerciseGenerator.generateQuestions(grammar)
        if (questions.isEmpty()) {
            viewModelScope.launch {
                _effect.send(GrammarTreeEffect.ShowSnackbar("暂无该语法的练习题"))
            }
            return
        }
        _state.update {
            it.copy(
                exerciseMode     = true,
                currentQuestions = questions,
                answers          = emptyMap(),
                exerciseProgress = 0,
                exerciseComplete = false
            )
        }
    }

    private fun updateAnswer(questionId: Int, input: String) {
        val current = _state.value.answers[questionId]
        if (current?.isSubmitted == true) return
        _state.update { state ->
            state.copy(
                answers = state.answers + (questionId to
                    (current?.copy(userInput = input) ?: ExerciseAnswer(questionId, input, false)))
            )
        }
    }

    private fun selectOption(questionId: Int, option: String) {
        val current = _state.value.answers[questionId]
        if (current?.isSubmitted == true) return
        updateAnswer(questionId, option)
        submitAnswer(questionId)
    }

    private fun submitAnswer(questionId: Int) {
        val state    = _state.value
        val question = state.currentQuestions.find { it.id == questionId } ?: return
        val answer   = state.answers[questionId] ?: ExerciseAnswer(questionId, "", false)

        val isCorrect = answer.userInput.trim().equals(question.correctAnswer.trim(), ignoreCase = true)

        val updated = answer.copy(isSubmitted = true, isCorrect = isCorrect)
        _state.update {
            it.copy(answers = it.answers + (questionId to updated))
        }

        viewModelScope.launch {
            _effect.send(GrammarTreeEffect.PlayHaptic(isCorrect))
        }
    }

    private fun toggleHint(questionId: Int) {
        val current = _state.value.answers[questionId]
            ?: ExerciseAnswer(questionId, "", false)
        _state.update { state ->
            state.copy(answers = state.answers + (questionId to current.copy(showHint = !current.showHint)))
        }
    }

    private fun nextQuestion() {
        val state = _state.value
        val next  = state.exerciseProgress + 1
        if (next >= state.currentQuestions.size) {
            finishExercise()
        } else {
            _state.update { it.copy(exerciseProgress = next) }
        }
    }

    private fun finishExercise() {
        _state.update { it.copy(exerciseComplete = true) }
        viewModelScope.launch {
            _effect.send(GrammarTreeEffect.ShowExerciseComplete)
        }
    }

    companion object {
        /**
         * 获取韩语首字母初声辅音 (ㄱ, ㄴ, ㄷ, ㄹ, ㅁ, ㅂ, ㅅ, ㅇ, ㅈ, ㅊ, ㅋ, ㅌ, ㅍ, ㅎ)
         */
        fun getHangulInitial(text: String): String {
            val clean = text.removePrefix("-").removePrefix("(").removePrefix("으").removePrefix(")").trim()
            val firstChar = clean.firstOrNull() ?: return ""
            if (firstChar in '가'..'힣') {
                val chosungIndex = (firstChar.code - 0xAC00) / 28 / 21
                return when (chosungIndex) {
                    0, 1 -> "ㄱ"
                    2 -> "ㄴ"
                    3, 4 -> "ㄷ"
                    5 -> "ㄹ"
                    6 -> "ㅁ"
                    7, 8 -> "ㅂ"
                    9, 10 -> "ㅅ"
                    11 -> "ㅇ"
                    12, 13 -> "ㅈ"
                    14 -> "ㅊ"
                    15 -> "ㅋ"
                    16 -> "ㅌ"
                    17 -> "ㅍ"
                    18 -> "ㅎ"
                    else -> ""
                }
            }
            return ""
        }
    }
}

// ══════════════════════════════════════════════════════════════════
// 综合练习题生成器（离线多题型支持：填空 + 选择 + 辨析纠错）
// ══════════════════════════════════════════════════════════════════

object ExerciseGenerator {

    fun generateQuestions(grammar: GrammarNode): List<FillBlankQuestion> {
        val questions = mutableListOf<FillBlankQuestion>()
        val pattern = grammar.grammarPattern

        // 1. TOPIK 四选一接续题 (MULTIPLE_CHOICE)
        val mcQuestion = generateMultipleChoiceQuestion(1, grammar)
        if (mcQuestion != null) questions.add(mcQuestion)

        // 2. 语用/句式改错题 (ERROR_CORRECTION)
        val errorQuestion = generateErrorCorrectionQuestion(2, grammar)
        if (errorQuestion != null) questions.add(errorQuestion)

        // 3. 经典形态学接续填空题 (FILL_BLANK)
        val builtIn = getBuiltInQuestions(pattern)
        if (builtIn.isNotEmpty()) {
            questions.addAll(builtIn.take(2))
        } else {
            val fromExamples = grammar.examples.mapIndexedNotNull { idx, ex ->
                buildQuestionFromExample(idx + 10, grammar, ex)
            }
            questions.addAll(fromExamples.take(2))
        }

        return questions.take(3)
    }

    private fun generateMultipleChoiceQuestion(id: Int, grammar: GrammarNode): FillBlankQuestion? {
        val ex = grammar.examples.firstOrNull() ?: return null
        val pattern = grammar.grammarPattern
        val wrongOptions = when {
            pattern.contains("때문") -> listOf("-아서", "-(으)니까", "-느라고")
            pattern.contains("아서") || pattern.contains("어서") -> listOf("-기 때문에", "-(으)니까", "-지만")
            pattern.contains("니까") -> listOf("-아서", "-기 때문에", "-는데")
            pattern.contains("지만") -> listOf("-는데", "-(으)니까", "-아서")
            pattern.contains("는데") -> listOf("-지만", "-아서", "-고 나서")
            pattern.contains("자마자") -> listOf("-고 나서", "-는 대로", "-기 전에")
            else -> listOf("-아서", "-(으)면", "-는데")
        }.filter { !pattern.contains(it) }.take(3)

        val options = (listOf(pattern) + wrongOptions).shuffled()

        return FillBlankQuestion(
            id = id,
            grammarPattern = pattern,
            stem = "TOPIK 经典选词填空",
            context = "다음 문장의 빈칸에 가장 알맞은 문법을 고르십시오.\n${ex.korean.take(20)}...",
            contextChinese = "【四选一】请选择最贴切语境与限制规则的正确语法：",
            correctAnswer = pattern,
            answerHint = "参考语用规则：$pattern (${grammar.explanation.take(30)}...)",
            type = ExerciseType.MULTIPLE_CHOICE,
            options = options,
            difficulty = 2,
            conjugationNote = "考察语境连贯与前后句制约规则。"
        )
    }

    private fun generateErrorCorrectionQuestion(id: Int, grammar: GrammarNode): FillBlankQuestion? {
        val pattern = grammar.grammarPattern
        val (sentence, correct, hint) = when {
            pattern.contains("바람에") -> Triple(
                "열심히 공부하는 바람에 시험에 1등을 했어요. (맞습니까?)",
                "틀림",
                "-는 바람에 只能用于突发负面影响，不能用于积极正面结果。"
            )
            pattern.contains("느라고") -> Triple(
                "어제 공부하느라고 시험에 합격했어요. (맞습니까?)",
                "틀림",
                "-느라고 后句必须是消极、遗憾或不良后果。"
            )
            pattern.contains("아서") || pattern.contains("어서") -> Triple(
                "날씨가 추워서 따뜻하게 입으세요. (맞습니까?)",
                "틀림",
                "-아/어서 后句严禁使用命令句 (-(으)세요)，命令句需改用 -(으)니까。"
            )
            pattern.contains("덕분에") -> Triple(
                "선생님 덕분에 시험을 망쳤어요. (맞습니까?)",
                "틀림",
                "-덕분에 用于积极回报，负面结果应使用 -탓에。"
            )
            else -> Triple(
                "${grammar.examples.firstOrNull()?.korean ?: "한국어를 열심히 배웁니다."} (맞습니까?)",
                "맞음",
                "该句式语法接续与前后语用规则完全正确。"
            )
        }

        return FillBlankQuestion(
            id = id,
            grammarPattern = pattern,
            stem = "语用规则辨析纠错",
            context = sentence,
            contextChinese = "【找错纠错】请判断上方句子的语法语用搭配是否正确：",
            correctAnswer = correct,
            answerHint = hint,
            type = ExerciseType.MULTIPLE_CHOICE,
            options = listOf("맞음 (正确)", "틀림 (有误)"),
            difficulty = 2,
            conjugationNote = hint
        )
    }

    private fun buildQuestionFromExample(
        id: Int,
        grammar: GrammarNode,
        example: GrammarExample
    ): FillBlankQuestion? {
        val pattern = grammar.grammarPattern.removePrefix("-").removePrefix("(으)")
        val idx = example.korean.indexOf(pattern.take(2))
        if (idx < 2) return null

        val stem = example.korean.substring(0, idx)
        val baseVerb = "${stem}다"
        val context = example.korean.replace(stem + pattern.take(2), "___")

        val conjResult = try {
            com.yonsei.koreanlearner.algorithm.korean.KoreanConjugationEngine.conjugate(
                baseVerb = baseVerb,
                ending = grammar.grammarPattern
            )
        } catch (_: Exception) {
            null
        }

        val correctAnswer = conjResult?.conjugatedText ?: (stem + pattern.take(2))
        val hint = conjResult?.ruleExplanation ?: "${stem}다 + ${grammar.conjugationRules.take(30)}"

        return FillBlankQuestion(
            id              = id,
            grammarPattern  = grammar.grammarPattern,
            stem            = baseVerb,
            context         = context,
            contextChinese  = example.chinese,
            correctAnswer   = correctAnswer,
            answerHint      = hint,
            type            = ExerciseType.FILL_BLANK,
            difficulty      = if (conjResult?.appliedIrregular != com.yonsei.koreanlearner.algorithm.korean.IrregularType.REGULAR) 2 else 1,
            conjugationNote = conjResult?.ruleExplanation ?: ""
        )
    }

    private fun getBuiltInQuestions(pattern: String): List<FillBlankQuestion> =
        BUILT_IN_QUESTIONS[pattern] ?: emptyList()

    private val BUILT_IN_QUESTIONS: Map<String, List<FillBlankQuestion>> = mapOf(
        "-아/어서" to listOf(
            FillBlankQuestion(101, "-아/어서", "먹다", "밥을 ___ 학교에 갔어요.", "吃了饭去学校了。", "먹어서", "먹다+어서→먹어서（元音ㅓ→어서）", ExerciseType.FILL_BLANK, emptyList(), 1, "元音어系: ㅓ/ㅗ以外均接어서"),
            FillBlankQuestion(102, "-아/어서", "오다", "친구가 ___ 기뻐요.", "朋友来了所以很高兴。", "와서", "오다+아서→와서（오+아→와 缩合）", ExerciseType.FILL_BLANK, emptyList(), 1, "오다的特殊缩合：오+아→와")
        ),
        "-(으)면서" to listOf(
            FillBlankQuestion(111, "-(으)면서", "듣다", "음악을 ___ 공부해요.", "边听音乐边学习。", "들으면서", "듣다（ㄷ不规则）→들+으면서", ExerciseType.FILL_BLANK, emptyList(), 2, "ㄷ不规则：ㄷ→ㄹ，再接으면서"),
            FillBlankQuestion(112, "-(으)면서", "걷다", "공원을 ___ 이야기했어요.", "边散步边聊天了。", "걸으면서", "걷다（ㄷ不规则）→걸+으면서", ExerciseType.FILL_BLANK, emptyList(), 2, "걷다：ㄷ→ㄹ")
        ),
        "-기 때문에" to listOf(
            FillBlankQuestion(121, "-기 때문에", "바쁘다", "___ 못 만났어요.", "因为忙所以没见到。", "바쁘기 때문에", "바쁘다+기 때문에", ExerciseType.FILL_BLANK, emptyList(), 1, "-기前词干不变"),
            FillBlankQuestion(122, "-기 때문에", "비가 오다", "___ 우산을 가져왔어요.", "因为下雨所以带了伞。", "비가 오기 때문에", "비가 오다+기 때문에", ExerciseType.FILL_BLANK, emptyList(), 1, "注意：不可用于命令句、共动句")
        )
    )
}