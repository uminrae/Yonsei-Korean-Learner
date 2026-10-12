package com.yonsei.koreanlearner.ui.screen.grammar

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.GrammarExample

// ══════════════════════════════════════════════════════════════════
// 语法领域模型（UI 层，@Immutable 保证稳定性）
// ══════════════════════════════════════════════════════════════════

/** 语法树节点（展示层聚合模型） */
@Immutable
data class GrammarNode(
    val id               : Int,
    val book             : Int,
    val lesson           : Int,
    val subLesson        : String,
    val grammarPattern   : String,
    val category         : String,
    val explanation      : String,
    val conjugationRules : String,
    val examples         : List<GrammarExample>,
    val relatedGrammars  : List<String>,
    /** 是否已标星收藏 */
    val isStarred        : Boolean = false,
    /** 该语法的学习状态 */
    val learningState    : GrammarLearningState = GrammarLearningState.UNLEARNED,
    /** 填空练习正确率（0.0~1.0，-1表示未做过） */
    val exerciseAccuracy : Float = -1f
)

/** 语法学习状态 */
enum class GrammarLearningState {
    UNLEARNED,    // 未学习
    LEARNING,     // 学习中（查看过但未练习）
    PRACTICED,    // 已练习
    MASTERED      // 已掌握（练习准确率≥80%）
}

/** 语法课时分组（用于树状信息流的分节头） */
@Immutable
data class GrammarLessonGroup(
    val book    : Int,
    val lesson  : Int,
    val title   : String,         // 如 "第2册 · 第3课"
    val nodes   : List<GrammarNode>,
    val isExpanded: Boolean = true
)

// ──────────────────────────────────────────────────────────────────
// 综合练习题模型（填空 + 四选一 + 改错题）
// ──────────────────────────────────────────────────────────────────

enum class ExerciseType {
    FILL_BLANK,        // 变形填空题
    MULTIPLE_CHOICE,   // TOPIK 四选一
    ERROR_CORRECTION   // 语用/句式找错改错题
}

/**
 * 单道综合练习题
 */
@Immutable
data class FillBlankQuestion(
    val id              : Int,
    val grammarPattern  : String,          // 所属语法
    val stem            : String,          // 给出的词干原形（如 "먹다"）
    val context         : String,          // 句子语境（如 "밥을 ___ 학교에 갔어요."）
    val contextChinese  : String,          // 语境中文翻译
    val correctAnswer   : String,          // 正确答案（如 "먹어서"）
    val answerHint      : String,          // 提示（如 "먹다 + 어서 → 먹어서"）
    val type            : ExerciseType = ExerciseType.FILL_BLANK,
    val options         : List<String> = emptyList(), // 选择题选项
    val difficulty      : Int = 1,
    val conjugationNote : String = ""      // 变形说明
)

/** 练习答题状态 */
@Immutable
data class ExerciseAnswer(
    val questionId     : Int,
    val userInput      : String,
    val isCorrect      : Boolean,
    val isSubmitted    : Boolean = false,
    val showHint       : Boolean = false
)

// ══════════════════════════════════════════════════════════════════
// MVI — GrammarTreeUiState
// ══════════════════════════════════════════════════════════════════

@Immutable
data class GrammarTreeUiState(

    // ── 导航 & 筛选状态 ──────────────────────────────────────────
    val selectedBook         : Int = 0,         // 0 = 全部册数
    val selectedLesson       : Int = 0,         // 0 = 全部课次
    val selectedCategory     : String = "",     // 空 = 全部分类
    val isStarredOnly        : Boolean = false, // 仅查看已标星
    val selectedHangulInitial: String = "",     // 首字母辅音过滤 (如 "ㄱ", "ㄴ" 或 "")
    val starredPatterns      : Set<String> = emptySet(),

    // ── 列表数据 ──────────────────────────────────────────────────
    val lessonGroups         : List<GrammarLessonGroup> = emptyList(),
    val allCategories        : List<String> = emptyList(),

    // ── 搜索 ──────────────────────────────────────────────────────
    val searchQuery          : String = "",
    val searchResults        : List<GrammarNode> = emptyList(),
    val isSearchActive       : Boolean = false,

    // ── 详情面板状态 ──────────────────────────────────────────────
    val selectedGrammar      : GrammarNode? = null,
    val showDetailSheet      : Boolean = false,

    // ── 填空练习状态 ──────────────────────────────────────────────
    val exerciseMode         : Boolean = false,
    val currentQuestions     : List<FillBlankQuestion> = emptyList(),
    val answers              : Map<Int, ExerciseAnswer> = emptyMap(),  // key = questionId
    val exerciseProgress     : Int = 0,           // 当前题目索引
    val exerciseComplete     : Boolean = false,

    // ── 比较辨析面板 ──────────────────────────────────────────────
    val showComparePanel     : Boolean = false,
    val compareTargets       : List<GrammarNode> = emptyList(),

    // ── 加载/错误 ─────────────────────────────────────────────────
    val isLoading            : Boolean = true,
    val errorMessage         : String? = null
) {
    val filteredNodes: List<GrammarNode> get() =
        if (isSearchActive && searchQuery.isNotBlank()) searchResults
        else lessonGroups.flatMap { it.nodes }

    val exerciseAccuracy: Float get() {
        val submitted = answers.values.filter { it.isSubmitted }
        return if (submitted.isEmpty()) 0f
        else submitted.count { it.isCorrect }.toFloat() / submitted.size
    }
}

// ══════════════════════════════════════════════════════════════════
// MVI — GrammarTreeIntent
// ══════════════════════════════════════════════════════════════════

sealed class GrammarTreeIntent {
    // ── 筛选/导航 ─────────────────────────────────────────────────
    data class SelectBook(val book: Int) : GrammarTreeIntent()
    data class SelectLesson(val lesson: Int) : GrammarTreeIntent()
    data class SelectCategory(val category: String) : GrammarTreeIntent()
    data class ToggleLessonGroup(val lesson: Int) : GrammarTreeIntent()
    data class ToggleStar(val pattern: String) : GrammarTreeIntent()
    object ToggleStarredOnly : GrammarTreeIntent()
    data class SelectHangulInitial(val initial: String) : GrammarTreeIntent()

    // ── 搜索 ──────────────────────────────────────────────────────
    data class UpdateSearchQuery(val query: String) : GrammarTreeIntent()
    object ClearSearch : GrammarTreeIntent()

    // ── 详情 ──────────────────────────────────────────────────────
    data class OpenGrammarDetail(val node: GrammarNode) : GrammarTreeIntent()
    object CloseGrammarDetail : GrammarTreeIntent()

    // ── 比较辨析 ──────────────────────────────────────────────────
    data class OpenComparePanel(val targets: List<GrammarNode>) : GrammarTreeIntent()
    object CloseComparePanel : GrammarTreeIntent()

    // ── 填空练习 ──────────────────────────────────────────────────
    data class StartExercise(val grammar: GrammarNode) : GrammarTreeIntent()
    data class UpdateAnswer(val questionId: Int, val input: String) : GrammarTreeIntent()
    data class SelectOption(val questionId: Int, val option: String) : GrammarTreeIntent()
    data class SubmitAnswer(val questionId: Int) : GrammarTreeIntent()
    data class ToggleHint(val questionId: Int) : GrammarTreeIntent()
    object NextExerciseQuestion : GrammarTreeIntent()
    object FinishExercise : GrammarTreeIntent()
    object DismissError : GrammarTreeIntent()
}

// ══════════════════════════════════════════════════════════════════
// MVI — GrammarTreeEffect
// ══════════════════════════════════════════════════════════════════

sealed class GrammarTreeEffect {
    data class ScrollToGrammar(val grammarId: Int) : GrammarTreeEffect()
    data class ShowSnackbar(val message: String) : GrammarTreeEffect()
    object ShowExerciseComplete : GrammarTreeEffect()
    data class PlayHaptic(val isCorrect: Boolean) : GrammarTreeEffect()
}

// ══════════════════════════════════════════════════════════════════
// GrammarEntity 映射扩展
// ══════════════════════════════════════════════════════════════════

private val gson = Gson()

fun GrammarEntity.toGrammarNode(): GrammarNode {
    val exType = object : TypeToken<List<GrammarExample>>() {}.type
    val rgType = object : TypeToken<List<String>>() {}.type
    return GrammarNode(
        id               = this.id,
        book             = this.book,
        lesson           = this.lesson,
        subLesson        = this.subLesson,
        grammarPattern   = this.grammarPattern,
        category         = this.category,
        explanation      = this.explanation,
        conjugationRules = this.conjugationRules,
        examples         = try { gson.fromJson(this.examples, exType) ?: emptyList() } catch (_: Exception) { emptyList() },
        relatedGrammars  = try { gson.fromJson(this.relatedGrammars, rgType) ?: emptyList() } catch (_: Exception) { emptyList() }
    )
}
