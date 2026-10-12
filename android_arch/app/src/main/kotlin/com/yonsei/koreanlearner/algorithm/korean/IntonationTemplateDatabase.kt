package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 终结词尾标准母语者语调音高曲线模板库 (IntonationTemplateDatabase)
 * ══════════════════════════════════════════════════════════════════
 */

enum class PitchPatternType(val displayName: String, val symbol: String) {
    RISING_TAIL("句尾快速上扬", "↗"),
    FALLING_TAIL("句尾平缓下沉", "↘"),
    FALL_RISE_FALL("先扬后抑强调", "↷"),
    EXCLAMATION_PEAK("高位跳跃拔高", "↑")
}

data class IntonationTemplate(
    val id             : String,
    val endingPattern  : String,            // 终结词尾（如 "-지요?", "-잖아요."）
    val sampleSentence : String,            // 范例全句（如 "한국인이 아니지요?"）
    val chineseMeaning : String,            // 中文释义
    val patternType    : PitchPatternType,  // 语调形态
    val pitchCurve     : List<Float>,       // 20 个离散采样点的标准音高归一化序列 (0.0f ~ 1.0f)
    val syllables      : List<String>,      // 对应分词音节列表
    val feedbackGuide  : String             // 语调要领指导说明
)

object IntonationTemplateDatabase {

    val TEMPLATES = listOf(
        // ── 1. -지요? / -죠? (确认疑问句：句尾大幅快速上扬) ──────
        IntonationTemplate(
            id = "ending_jiyo",
            endingPattern = "-지요? / -죠?",
            sampleSentence = "한국인이 아니지요?",
            chineseMeaning = "不是韩国人，对吧？",
            patternType = PitchPatternType.RISING_TAIL,
            pitchCurve = listOf(
                0.40f, 0.42f, 0.43f, 0.42f, 0.40f, // 한국인이 (平稳)
                0.45f, 0.48f, 0.50f, 0.48f, 0.45f, // 아니 (微起伏)
                0.50f, 0.55f, 0.62f, 0.72f, 0.85f, 0.95f, 0.98f, 0.95f, 0.92f, 0.90f // 지요? (句尾急剧拔高)
            ),
            syllables = listOf("한", "국", "인", "이", "아", "니", "지", "요", "?"),
            feedbackGuide = "【-지요? 疑问确认调】：前半句音高保持中位平直，最后一个音节 '요' 音高迅速上扬至峰值，表示寻求对方确认。"
        ),

        // ── 2. -잖아요. (已知事实提醒：前扬后抑强调) ──────────
        IntonationTemplate(
            id = "ending_jana",
            endingPattern = "-잖아요.",
            sampleSentence = "어제 말했잖아요.",
            chineseMeaning = "昨天不是跟您说过了嘛。",
            patternType = PitchPatternType.FALL_RISE_FALL,
            pitchCurve = listOf(
                0.50f, 0.55f, 0.58f, 0.52f, 0.50f, // 어제 (平稳)
                0.60f, 0.72f, 0.85f, 0.88f, 0.80f, // 말했 (重音前扬)
                0.70f, 0.60f, 0.50f, 0.42f, 0.35f, 0.30f, 0.28f, 0.25f, 0.22f, 0.20f // 잖아요 (平缓回落沉底)
            ),
            syllables = listOf("어", "제", "말", "했", "잖", "아", "요", "."),
            feedbackGuide = "【-잖아요 提醒已知调】：核心谓词 '말했' 处于音高制高点，随后在 '-잖아요' 音调平稳回落沉底，带有‘你明明知道’的语气。"
        ),

        // ── 3. -거든요. (阐述背景原因：中段微扬尾音下降) ───────
        IntonationTemplate(
            id = "ending_geodeun",
            endingPattern = "-거든요.",
            sampleSentence = "시간이 없거든요.",
            chineseMeaning = "因为没有时间嘛。",
            patternType = PitchPatternType.FALLING_TAIL,
            pitchCurve = listOf(
                0.45f, 0.50f, 0.55f, 0.52f, 0.50f, // 시간이 (中位)
                0.65f, 0.70f, 0.65f, 0.55f, 0.45f, // 없 (微扬强调)
                0.40f, 0.35f, 0.30f, 0.25f, 0.22f, 0.20f, 0.18f, 0.16f, 0.15f, 0.14f // 거든요 (坚定下降)
            ),
            syllables = listOf("시", "간", "이", "없", "거", "든", "요", "."),
            feedbackGuide = "【-거든요 背景解释调】：句尾 '-거든요' 音调呈坚定的阶梯式下降，向对方补充对方未知的客观缘由。"
        ),

        // ── 4. -네요! (新发现感叹：末尾跳跃拔高) ─────────────
        IntonationTemplate(
            id = "ending_neyeo",
            endingPattern = "-네요!",
            sampleSentence = "정말 맛있네요!",
            chineseMeaning = "真的很好吃呀！",
            patternType = PitchPatternType.EXCLAMATION_PEAK,
            pitchCurve = listOf(
                0.40f, 0.45f, 0.48f, 0.45f, 0.42f, // 정말 (平中)
                0.55f, 0.62f, 0.68f, 0.65f, 0.60f, // 맛있 (铺垫)
                0.70f, 0.80f, 0.90f, 0.96f, 0.98f, 0.95f, 0.90f, 0.85f, 0.80f, 0.75f // 네요! (惊喜高位)
            ),
            syllables = listOf("정", "말", "맛", "있", "네", "요", "!"),
            feedbackGuide = "【-네요 感叹调】：末尾 '네요' 伴随惊喜情绪突然跳高，呈现轻快上扬的感叹音高。"
        )
    )
}
