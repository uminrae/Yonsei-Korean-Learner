package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文规范空格（띄어쓰기）与拼写校对校验引擎 (KoreanSpacingCheckerEngine)
 * ══════════════════════════════════════════════════════════════════
 */

enum class SpacingRuleCategory(val ruleTitle: String, val chineseExplanation: String) {
    PARTICLE_ATTACHMENT("助词紧贴体词", "格助词(이/가, 을/를, 에, 에서, 에게)与添意助词(은/는, 도, 만)必须紧贴名词连写，严禁空格。"),
    BOUND_NOUN_SPACING("依存名词必须空格", "依存名词(것, 수, 줄, 때, 뿐, 만큼, 지, 셈, 법, 리, 턱)必须与前面的修饰语空格分写。"),
    UNIT_NOUN_SPACING("数词与量词必须空格", "数词与单位依存名词(개, 권, 마리, 명, 살, 원, 층)之间必须空格分写。"),
    AUXILIARY_VERB_SPACING("辅助动词/惯用型空格", "-기 때문에, -는 동안, -아/어 보다, -고 싶다, -지 않다 等结构中关键词必须规范隔写。")
}

data class SpacingViolation(
    val charIndex     : Int,                    // 违规字符位置
    val tokenFragment : String,                 // 触发片段（如 "한지" 或 "세달" 或 "한국어를"）
    val ruleCategory  : SpacingRuleCategory,    // 规则分类
    val explanation   : String                  // 规则解析
)

data class SpacingCheckResult(
    val originalText     : String,              // 用户输入文本
    val correctedText    : String,              // 规范空格矫正后文本
    val violations       : List<SpacingViolation>, // 违规诊断列表
    val appliedRules     : Set<SpacingRuleCategory>
)

object KoreanSpacingCheckerEngine {

    // 常用依存名词列表
    val BOUND_NOUNS = listOf("것", "수", "줄", "때", "뿐", "만큼", "지", "셈", "법", "리", "턱", "척", "체", "따름", "바")

    // 常用单位名词列表
    val UNIT_NOUNS = listOf("개", "권", "마리", "명", "살", "원", "층", "대", "병", "잔", "채", "그릇", "자루", "번", "달", "해", "년", "월", "일", "시", "분", "초")

    // 数词冠形词列表
    val NUMERALS = listOf("한", "두", "세", "네", "다섯", "여섯", "일곱", "여덟", "아홉", "열", "스물", "스물한", "서른", "마흔", "쉰", "백", "천", "만", "몇")

    // 预置常见易错样例库
    val PRESET_DRILL_CASES = listOf(
        DrillCase(
            id = 1,
            rawUnspaced = "한국어를공부한지세달이되었어요",
            standardSpaced = "한국어를 공부한 지 세 달이 되었어요.",
            explanation = "1) '공부한 지': '지' 表示时间流逝时作为依存名词，必须与修饰语 '공부한' 空格。\n2) '세 달': 数词 '세' 与单位名词 '달' 之间必须空格。"
        ),
        DrillCase(
            id = 2,
            rawUnspaced = "내일은도서관에서책을읽을수있어요",
            standardSpaced = "내일은 도서관에서 책을 읽을 수 있어요.",
            explanation = "1) 助词 '은', '에서', '을' 紧贴体词连写。\n2) '읽을 수': 依存名词 '수' 与冠形词形 '읽을' 必须空格。"
        ),
        DrillCase(
            id = 3,
            rawUnspaced = "날씨가좋기때문에공원에갈거예요",
            standardSpaced = "날씨가 좋기 때문에 공원에 갈 거예요.",
            explanation = "1) '좋기 때문에': 原因惯用型中 '때문에' 必须与名词化词尾 '-기' 空格。\n2) '갈 거예요': 依存名词 '거(것)' 必须与 '갈' 空格。"
        ),
        DrillCase(
            id = 4,
            rawUnspaced = "사과한개와우유두병을샀습니다",
            standardSpaced = "사과 한 개와 우유 두 병을 샀습니다.",
            explanation = "数词与量词 '한 개', '두 병' 必须空格；助词 '와', '을' 紧贴量词连写。"
        )
    )

    data class DrillCase(
        val id             : Int,
        val rawUnspaced    : String,
        val standardSpaced : String,
        val explanation    : String
    )

    /**
     * 自动对韩文文本执行标准隔写规范校对
     */
    fun correctSpacing(input: String): SpacingCheckResult {
        val clean = input.trim()
        val violations = mutableListOf<SpacingViolation>()
        val appliedRules = mutableSetOf<SpacingRuleCategory>()

        // 匹配内置已知句式库
        val matchedCase = PRESET_DRILL_CASES.firstOrNull { it.rawUnspaced == clean.replace(" ", "").replace(".", "") }

        val targetResult = if (matchedCase != null) {
            matchedCase.standardSpaced
        } else {
            // 通用启发式分词矫正
            heuristicSpacingCorrection(clean)
        }

        // 规则诊断分析
        if (clean.contains("한지") || clean.contains("갈지") || clean.contains("공부한지")) {
            violations.add(
                SpacingViolation(
                    charIndex = 0,
                    tokenFragment = "공부한지",
                    ruleCategory = SpacingRuleCategory.BOUND_NOUN_SPACING,
                    explanation = "'지' 表示时间流逝时为依存名词，必须与冠形词修饰语空格（공부한 지）。"
                )
            )
            appliedRules.add(SpacingRuleCategory.BOUND_NOUN_SPACING)
        }

        if (clean.contains("할수") || clean.contains("읽을수") || clean.contains("갈수")) {
            violations.add(
                SpacingViolation(
                    charIndex = 0,
                    tokenFragment = "읽을수",
                    ruleCategory = SpacingRuleCategory.BOUND_NOUN_SPACING,
                    explanation = "'수' 作为依存名词（-을 수 있다），必须与修饰语空格（읽을 수）。"
                )
            )
            appliedRules.add(SpacingRuleCategory.BOUND_NOUN_SPACING)
        }

        if (clean.contains("세달") || clean.contains("한개") || clean.contains("두병")) {
            violations.add(
                SpacingViolation(
                    charIndex = 0,
                    tokenFragment = "세달 / 한개",
                    ruleCategory = SpacingRuleCategory.UNIT_NOUN_SPACING,
                    explanation = "数词（한/두/세）与单位依存名词（개/병/달）之间必须空格（세 달, 한 개）。"
                )
            )
            appliedRules.add(SpacingRuleCategory.UNIT_NOUN_SPACING)
        }

        return SpacingCheckResult(
            originalText = clean,
            correctedText = targetResult,
            violations = violations,
            appliedRules = appliedRules
        )
    }

    private fun heuristicSpacingCorrection(raw: String): String {
        var text = raw

        // 依存名词隔写
        BOUND_NOUNS.forEach { bn ->
            text = text.replace("을$bn", "을 $bn")
            text = text.replace("ㄹ$bn", "ㄹ $bn")
            text = text.replace("은$bn", "은 $bn")
            text = text.replace("ㄴ$bn", "ㄴ $bn")
            text = text.replace("는$bn", "는 $bn")
        }

        // 때문에 隔写
        text = text.replace("기때문에", "기 때문에")

        // 수 있다 隔写
        text = text.replace("수있다", "수 있다")
        text = text.replace("수없다", "수 없다")
        text = text.replace("수있어요", "수 있어요")

        return text
    }
}
