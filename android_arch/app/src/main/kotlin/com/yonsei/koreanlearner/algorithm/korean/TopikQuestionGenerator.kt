package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * TOPIK 考点自适应自测与能力雷达评估模型 (Topik Models)
 * ══════════════════════════════════════════════════════════════════
 */

enum class TopikQuestionType(val displayName: String) {
    GRAMMAR_BLANK("语法空缺填空"),
    SYNONYM_REPLACEMENT("同义表达替换"),
    HONORIFIC_JUDGMENT("敬语纠错判断"),
    PHONOLOGY_IDENTIFICATION("音变发音辨析"),
    READING_COMPREHENSION("阅读推理分析")
}

/** 五维能力评估雷达数据模型 */
data class TopikAbilityScores(
    val vocabularyScore : Float = 85f,      // 词汇量 (0~100)
    val grammarScore    : Float = 80f,      // 语法活用 (0~100)
    val honorificScore  : Float = 75f,      // 敬语得体度 (0~100)
    val phonologyScore  : Float = 90f,      // 音变听辨 (0~100)
    val readingScore    : Float = 80f       // 阅读理解 (0~100)
) {
    /** 综合预估 TOPIK 等级 */
    val estimatedLevel: String
        get() {
            val avg = (vocabularyScore + grammarScore + honorificScore + phonologyScore + readingScore) / 5f
            return when {
                avg >= 90 -> "TOPIK 6 级 (高级精通)"
                avg >= 80 -> "TOPIK 5 级 (高级流利)"
                avg >= 70 -> "TOPIK 4 级 (中级进阶)"
                avg >= 60 -> "TOPIK 3 级 (中级实用)"
                avg >= 45 -> "TOPIK 2 级 (初级达标)"
                else -> "TOPIK 1 级 (入门新手)"
            }
        }
}

/** 单道 TOPIK 试题模型 */
data class TopikQuestion(
    val id              : Int,
    val type            : TopikQuestionType,
    val questionStem    : String,           // 题干韩文
    val options         : List<String>,     // 4 个备选选项
    val correctIndex    : Int,              // 正确答案索引 (0~3)
    val targetDimension : String,           // 考查维度 (VOCAB / GRAMMAR / HONORIFIC / PHONOLOGY / READING)
    val explanation     : String            // 考点详细解析
)

object TopikQuestionGenerator {

    val QUESTION_BANK = listOf(
        // ── 1. 语法空缺填空 ────────────────────────────────────
        TopikQuestion(
            id = 1,
            type = TopikQuestionType.GRAMMAR_BLANK,
            questionStem = "갑자기 비가 (      ) 옷이 다 젖었습니다.",
            options = listOf("오는 바람에", "오기 때문에", "오니까", "오려고"),
            correctIndex = 0,
            targetDimension = "GRAMMAR",
            explanation = "后句 '옷이 다 젖었습니다(衣服全湿了)' 属于意外发生的负面消极结果，根据延世语法约束，表示消极意外原因首选 '-는 바람에'。"
        ),
        TopikQuestion(
            id = 2,
            type = TopikQuestionType.GRAMMAR_BLANK,
            questionStem = "날씨가 많이 (      ) 옷을 따뜻하게 입으세요.",
            options = listOf("추우니까", "춥기 때문에", "춥는 바람에", "추우므로"),
            correctIndex = 0,
            targetDimension = "GRAMMAR",
            explanation = "后句为命令/请求句型 '입으세요'，'-기 때문에' 与 '-는 바람에' 严禁后接命令句，必须使用 '-(으)니까'。"
        ),

        // ── 2. 同义表达替换 ────────────────────────────────────
        TopikQuestion(
            id = 3,
            type = TopikQuestionType.SYNONYM_REPLACEMENT,
            questionStem = "다음 밑줄 친 부분과 의미가 가장 비슷한 것을 고르십시오.\n'눈이 [오기 때문에] 길이 미끄럽습니다.'",
            options = listOf("와서", "오는데", "오지만", "오려고"),
            correctIndex = 0,
            targetDimension = "VOCAB",
            explanation = "'-기 때문에' 表示因果关系，与连接词尾 '-아/어서(와서)' 在陈述句中表达原因语义相同。"
        ),
        TopikQuestion(
            id = 4,
            type = TopikQuestionType.SYNONYM_REPLACEMENT,
            questionStem = "다음 밑줄 친 단어와 바꾸어 쓸 수 있는 것은?\n'한국어를 [부지런히] 연습했습니다.'",
            options = listOf("열심히", "조용히", "천천히", "갑자기"),
            correctIndex = 0,
            targetDimension = "VOCAB",
            explanation = "'부지런히'（勤劳地、勤奋地）与 '열심히'（努力地、刻苦地）互为高频同义副词。"
        ),

        // ── 3. 敬语纠错判断 ────────────────────────────────────
        TopikQuestion(
            id = 5,
            type = TopikQuestionType.HONORIFIC_JUDGMENT,
            questionStem = "다음 중 경어법(敬语) 사용이 올바른 문장을 고르십시오.",
            options = listOf(
                "할아버지께서 진지를 드셨습니다.",
                "할아버지가 밥을 먹었습니다.",
                "선생님이 나이가 많으십니다.",
                "제가 할머니를 데리고 병원에 갔어요."
            ),
            correctIndex = 0,
            targetDimension = "HONORIFIC",
            explanation = "A 选项主格助词 '께서'、名词尊称 '진지'、动词尊称 '드시다' 完全自洽；B 未用敬语；C 年龄尊称应为 '연세'；D 陪伴长辈谦逊动词应为 '모시다'。"
        ),

        // ── 4. 音变发音辨析 ────────────────────────────────────
        TopikQuestion(
            id = 6,
            type = TopikQuestionType.PHONOLOGY_IDENTIFICATION,
            questionStem = "다음 단어의 실제 표준 발음으로 알맞은 것을 고르십시오.\n[ 같이 ]",
            options = listOf("[가치]", "[갇이]", "[가지]", "[가티]"),
            correctIndex = 0,
            targetDimension = "PHONOLOGY",
            explanation = "收音 'ㅌ' 遇以 'ㅣ' 开头的元音时，发生口盖音化（腭音化），实际发音为 '[가치]'。"
        ),

        // ── 5. 阅读理解分析 ────────────────────────────────────
        TopikQuestion(
            id = 7,
            type = TopikQuestionType.READING_COMPREHENSION,
            questionStem = "다음 글을 읽고 중심 생각을 고르십시오.\n'티끌 모아 태산이라는 말처럼 매일 10분씩 한국어 단어를 외우면 1년 후에는 수천 개의 어휘를 습득할 수 있다.'",
            options = listOf(
                "매일 꾸준히 학습하는 것이 중요하다.",
                "한국어는 1년 만에 완전히 끝낼 수 있다.",
                "티끌은 산보다 가치가 없다.",
                "단어만 외우면 문법은 필요 없다."
            ),
            correctIndex = 0,
            targetDimension = "READING",
            explanation = "文章引用 '티끌 모아 태산'（积少成多）旨在强调每日坚持微小积累对于长期词汇习得的核心重要性。"
        )
    )

    /**
     * 自动生成一套包含 5 道自适应测试题的试卷
     */
    fun generateAdaptiveExam(): List<TopikQuestion> {
        return QUESTION_BANK.shuffled().take(5)
    }

    /**
     * 根据用户答题情况计算五维雷达能力分值
     */
    fun calculateAbilityScores(
        examQuestions: List<TopikQuestion>,
        userAnswers: Map<Int, Int> // QuestionId -> SelectedOptionIndex
    ): TopikAbilityScores {
        var vocabCorrect = 0; var vocabTotal = 0
        var grammarCorrect = 0; var grammarTotal = 0
        var honorificCorrect = 0; var honorificTotal = 0
        var phonologyCorrect = 0; var phonologyTotal = 0
        var readingCorrect = 0; var readingTotal = 0

        examQuestions.forEach { q ->
            val userAns = userAnswers[q.id]
            val isCorrect = userAns == q.correctIndex

            when (q.targetDimension) {
                "VOCAB" -> { vocabTotal++; if (isCorrect) vocabCorrect++ }
                "GRAMMAR" -> { grammarTotal++; if (isCorrect) grammarCorrect++ }
                "HONORIFIC" -> { honorificTotal++; if (isCorrect) honorificCorrect++ }
                "PHONOLOGY" -> { phonologyTotal++; if (isCorrect) phonologyCorrect++ }
                "READING" -> { readingTotal++; if (isCorrect) readingCorrect++ }
            }
        }

        fun toScore(correct: Int, total: Int, base: Float): Float {
            return if (total == 0) base else ((correct.toFloat() / total) * 40f + 60f).coerceIn(40f, 100f)
        }

        return TopikAbilityScores(
            vocabularyScore = toScore(vocabCorrect, vocabTotal, 80f),
            grammarScore = toScore(grammarCorrect, grammarTotal, 75f),
            honorificScore = toScore(honorificCorrect, honorificTotal, 70f),
            phonologyScore = toScore(phonologyCorrect, phonologyTotal, 85f),
            readingScore = toScore(readingCorrect, readingTotal, 80f)
        )
    }
}
