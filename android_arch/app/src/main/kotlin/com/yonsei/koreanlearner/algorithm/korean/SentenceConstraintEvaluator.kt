package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 开放式造句语境约束与语法润色评估器 (SentenceConstraintEvaluator)
 * ══════════════════════════════════════════════════════════════════
 */
object SentenceConstraintEvaluator {

    enum class EvaluationStatus {
        PASS,       // 语法与语境完全正确（100分）
        WARNING,    // 语法基本正确但语境略有牵强（80分）
        ERROR       // 触犯语法禁忌或严重结构错误（40分）
    }

    data class ConstraintEvaluationResult(
        val status        : EvaluationStatus,
        val score         : Int,
        val title         : String,
        val suggestions   : List<String>,
        val polishedSample: String
    )

    /**
     * 校验用户造句是否满足指定语法的语境约束
     *
     * @param targetGrammar 语法点（如 "-는 바람에", "-기 때문에", "-(으)니까", "-느라고", "-아/어/여야지"）
     * @param userInputSentence 用户输入的韩文完整句子
     */
    fun evaluateSentence(
        targetGrammar: String,
        userInputSentence: String
    ): ConstraintEvaluationResult {
        val clean = userInputSentence.trim()

        if (clean.isBlank()) {
            return ConstraintEvaluationResult(
                status = EvaluationStatus.ERROR,
                score = 0,
                title = "输入为空",
                suggestions = listOf("请输入使用目标语法的韩文造句。"),
                polishedSample = ""
            )
        }

        when {
            // ── 1. -는 바람에 约束校验 ─────────────────────────
            targetGrammar.contains("는 바람에") || targetGrammar.contains("바람에") -> {
                if (!clean.contains("바람에")) {
                    return ConstraintEvaluationResult(
                        EvaluationStatus.ERROR,
                        30,
                        "缺少核心语法标志",
                        listOf("句子中未检测到核心语法 '-는 바람에'。"),
                        "비가 오는 바람에 옷이 다 젖었어요."
                    )
                }

                // 禁忌 1：严禁接命令句/共动句（-(으)세요, -(으)십시오, -ㅂ시다, -자）
                if (clean.endsWith("세요") || clean.endsWith("십시오") || clean.endsWith("ㅂ시다") || clean.endsWith("자")) {
                    return ConstraintEvaluationResult(
                        EvaluationStatus.ERROR,
                        40,
                        "违反应用意图禁忌：严禁后接命令句/共动句",
                        listOf("'-는 바람에' 表示消极意外结果，绝对不能后接命令句（-세요）或共动句（-ㅂ시다）。请改用陈述陈述过去时，或改用 '-(으)니까'。"),
                        "차가 막히는 바람에 약속에 늦었습니다."
                    )
                }

                // 禁忌 2：前句严禁过去时（았/었는 바람에）
                if (clean.contains("았는 바람에") || clean.contains("었는 바람에") || clean.contains("였는 바람에")) {
                    return ConstraintEvaluationResult(
                        EvaluationStatus.ERROR,
                        45,
                        "时态错误：前从句严禁接过去时",
                        listOf("'-는 바람에' 前句必须使用动词现在时词干接 '-는'，不可写成 '았/었는 바람에'。"),
                        "눈이 많이 오는 바람에 비행기가 취소되었어요."
                    )
                }

                // 语境约束：必须为消极意外结果
                val positiveWords = listOf("합격", "좋았", "기뻤", "성공", "선물", "칭찬")
                if (positiveWords.any { clean.contains(it) }) {
                    return ConstraintEvaluationResult(
                        EvaluationStatus.WARNING,
                        75,
                        "语境偏离：建议接消极意外结果",
                        listOf("'-는 바람에' 通常用于带来负面、意外的消极结果（如迟到、受阻、取消）。若结果积极美好，建议改用 '-(으)ㄴ 덕분에'。"),
                        "선생님이 도와주신 덕분에 시험에 합격했어요."
                    )
                }

                return ConstraintEvaluationResult(
                    EvaluationStatus.PASS,
                    100,
                    " 语境与语法完全规范！",
                    listOf("消极因果关系表达准确，前后时态与动词现在形衔接自然。"),
                    clean
                )
            }

            // ── 2. -기 때문에 约束校验 ─────────────────────────
            targetGrammar.contains("기 때문에") || targetGrammar.contains("때문에") -> {
                if (!clean.contains("때문에")) {
                    return ConstraintEvaluationResult(
                        EvaluationStatus.ERROR,
                        30,
                        "缺少核心语法标志",
                        listOf("句子中未检测到核心语法 '-기 때문에'。"),
                        "눈이 오기 때문에 길이 미끄럽습니다."
                    )
                }

                // 禁忌：禁止共动句与命令句
                if (clean.endsWith("세요") || clean.endsWith("십시오") || clean.endsWith("ㅂ시다") || clean.endsWith("자")) {
                    return ConstraintEvaluationResult(
                        EvaluationStatus.ERROR,
                        45,
                        "句型冲突：'-기 때문에' 不可后接命令/共动句",
                        listOf("后句若含有请求、命令（-세요）或提议（-ㅂ시다），不能使用 '-기 때문에'，必须改用 '-(으)니까'。"),
                        "날씨가 추우니까 옷을 따뜻하게 입으세요."
                    )
                }

                return ConstraintEvaluationResult(
                    EvaluationStatus.PASS,
                    100,
                    " 客观因果关系表述严谨！",
                    listOf("书面客观理由阐述准确，格助词与谓词隔写规范。"),
                    clean
                )
            }

            // ── 3. 默认通用规则评估 ────────────────────────────
            else -> {
                return ConstraintEvaluationResult(
                    EvaluationStatus.PASS,
                    90,
                    "造句符合基本表达规范",
                    listOf("句子结构完整，请继续保持。"),
                    clean
                )
            }
        }
    }
}
