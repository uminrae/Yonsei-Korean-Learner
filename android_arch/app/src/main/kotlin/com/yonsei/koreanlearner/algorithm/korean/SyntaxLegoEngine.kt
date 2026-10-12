package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 语法乐高积木拼装领域模型与算法引擎 (SyntaxLegoEngine)
 * ══════════════════════════════════════════════════════════════════
 */

/** 单个乐高积木块 */
data class LegoBlock(
    val id           : String,              // 唯一 ID（如 "B1"）
    val label        : String,              // 积木显示文字（如 "눈이 오다", "-기 때문에", "길이 미끄럽습니다."）
    val category     : LegoCategory,        // 积木语法成分
    val fusedText    : String               // 拼接合成后的规范文字（如 "눈이 오"）
)

enum class LegoCategory(val displayName: String, val colorHex: String) {
    CLAUSE_HEAD("前从句主谓", "#833AB4"),
    GRAMMAR_CONNECTOR("连接语法词尾", "#E1306C"),
    CLAUSE_TAIL("后主句谓语", "#F59E0B"),
    OBJECT_PHRASE("宾语修饰块", "#10B981")
}

/** 乐高积木谜题关卡 */
data class LegoPuzzle(
    val id              : Int,
    val targetChinese   : String,           // 目标中文句子
    val targetSentence  : String,           // 目标合成完整韩语句子
    val scatteredBlocks : List<LegoBlock>,  // 乱序积木块列表
    val correctSequence : List<String>,     // 正确的 ID 拼接顺序
    val spacingRules    : String            // 隔写与活用变音原理解析
)

object SyntaxLegoEngine {

    val PRESET_PUZZLES = listOf(
        LegoPuzzle(
            id = 1,
            targetChinese = "因为下雪，所以路很滑。",
            targetSentence = "눈이 오기 때문에 길이가 미끄럽습니다.",
            scatteredBlocks = listOf(
                LegoBlock("B1", "눈이 오다", LegoCategory.CLAUSE_HEAD, "눈이 오"),
                LegoBlock("B2", "-기 때문에", LegoCategory.GRAMMAR_CONNECTOR, "기 때문에 "),
                LegoBlock("B3", "길이 미끄럽다", LegoCategory.CLAUSE_TAIL, "길이 미끄럽습니다.")
            ),
            correctSequence = listOf("B1", "B2", "B3"),
            spacingRules = "词干 '오-' 遇名次化词尾 '-기' 合成 '오기'；后接依存名词 '때문에' 时需空一格（오기 때문에）。"
        ),
        LegoPuzzle(
            id = 2,
            targetChinese = "在学校见了朋友，一起学习了。",
            targetSentence = "학교에서 친구를 만나서 같이 공부했어요.",
            scatteredBlocks = listOf(
                LegoBlock("B1", "학교에서", LegoCategory.CLAUSE_HEAD, "학교에서 "),
                LegoBlock("B2", "친구를 만나다", LegoCategory.OBJECT_PHRASE, "친구를 만나"),
                LegoBlock("B3", "-아/어서", LegoCategory.GRAMMAR_CONNECTOR, "서 "),
                LegoBlock("B4", "같이 공부하다", LegoCategory.CLAUSE_TAIL, "같이 공부했어요.")
            ),
            correctSequence = listOf("B1", "B2", "B3", "B4"),
            spacingRules = "动词 '만나다' 词干元音为 'ㅏ'，接 '-아서' 缩略为 '만나서'，表示前后动作紧密衔接。"
        ),
        LegoPuzzle(
            id = 3,
            targetChinese = "虽然天气冷，但是心情很好。",
            targetSentence = "날씨가 춥지만 기분이 아주 좋습니다.",
            scatteredBlocks = listOf(
                LegoBlock("B1", "날씨가 춥다", LegoCategory.CLAUSE_HEAD, "날씨가 춥"),
                LegoBlock("B2", "-지만", LegoCategory.GRAMMAR_CONNECTOR, "지만 "),
                LegoBlock("B3", "기분이 아주 좋다", LegoCategory.CLAUSE_TAIL, "기분이 아주 좋습니다.")
            ),
            correctSequence = listOf("B1", "B2", "B3"),
            spacingRules = "转折连接词尾 '-지만' 直接附着于形容词词干 '춥-' 之后，合成 '춥지만'，后接主句需空一格。"
        )
    )

    /**
     * 校验用户拼装积木的正确性与自动合成句子
     */
    fun evaluateAssembly(
        puzzle: LegoPuzzle,
        assembledBlockIds: List<String>
    ): LegoEvaluationResult {
        if (assembledBlockIds.size != puzzle.correctSequence.size) {
            return LegoEvaluationResult(
                isCompleted = false,
                isCorrect = false,
                assembledSentence = "",
                feedback = "积木尚未拼装完整（${assembledBlockIds.size}/${puzzle.correctSequence.size}）。"
            )
        }

        val isSequenceCorrect = assembledBlockIds == puzzle.correctSequence

        // 根据拼接顺序自动合成句子
        val idToBlockMap = puzzle.scatteredBlocks.associateBy { it.id }
        val assembledText = buildString {
            assembledBlockIds.forEach { id ->
                idToBlockMap[id]?.let { append(it.fusedText) }
            }
        }.trim()

        return if (isSequenceCorrect) {
            LegoEvaluationResult(
                isCompleted = true,
                isCorrect = true,
                assembledSentence = puzzle.targetSentence,
                feedback = " 完美拼装！语法活用变音与隔写（띄어쓰기）完全正确。\n${puzzle.spacingRules}"
            )
        } else {
            LegoEvaluationResult(
                isCompleted = true,
                isCorrect = false,
                assembledSentence = assembledText,
                feedback = "积木拼接逻辑顺序有误，请注意韩语‘主语从句 -> 连接词尾 -> 最终谓语’的语序。"
            )
        }
    }
}

data class LegoEvaluationResult(
    val isCompleted       : Boolean,
    val isCorrect         : Boolean,
    val assembledSentence : String,
    val feedback          : String
)
