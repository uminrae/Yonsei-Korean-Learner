package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语间接引语与口语缩略形自动推导引擎（IndirectSpeechTransposer）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心特性：
 * 1. 深度覆盖延世韩国语 3~4 册间接引语体系（陈述、疑问、命令、共动）。
 * 2. 自动生成标准完整形（-다고/냐고/라고/자고 하다）与日常高频口语缩略形（-대요/냬요/래요/재요）。
 * 3. 严格处理谓词收音、ㄹ 脱落、时态变化及助动词“달라고 vs 주라고”委托特例。
 */
object IndirectSpeechTransposer {

    /** 句型类型 */
    enum class SentenceType(val displayName: String, val fullSuffixTag: String, val shortSuffixTag: String) {
        DECLARATIVE("陈述句", "-ㄴ/는다고 하다", "-대요 / -(이)래요"),
        INTERROGATIVE("疑问句", "-냐고 하다", "-냬요 / -(이)냬요"),
        IMPERATIVE("命令句", "-(으)라고 하다", "-(으)래요 / -지 말래요"),
        PROPOSITIVE("共动句", "-자고 하다", "-재요 / -지 말재요")
    }

    /** 词性类型 */
    enum class PartOfSpeech {
        VERB,       // 动词（如 가다, 먹다, 살다）
        ADJECTIVE,  // 形容词（如 예쁘다, 좋다, 바쁘다）
        NOUN        // 名词（如 학생, 의사, 한국 사람）
    }

    /** 时态类型 */
    enum class Tense {
        PRESENT,    // 现在时
        PAST,       // 过去时 (-았/었/였-)
        FUTURE      // 将来时 (-(으)ㄹ 것이다 / -겠-)
    }

    /** 委托对象（仅用于命令句） */
    enum class Beneficiary {
        NONE,           // 普通命令
        SPEAKER_SELF,   // 为说话人自己做 (달라고 하다 / 달래요)
        THIRD_PARTY     // 为第三方做 (주라고 하다 / 주래요)
    }

    /**
     * 间接引语推导结果模型
     */
    data class IndirectSpeechResult(
        val originalText       : String,            // 原句或原型（如 "밥을 먹다"）
        val sentenceType       : SentenceType,      // 句型类型
        val partOfSpeech       : PartOfSpeech,      // 词性
        val tense              : Tense,             // 时态
        val fullForm           : String,            // 完整间接引语（如 "밥을 먹는다고 해요"）
        val contractedForm     : String,            // 口语缩略形式（如 "밥을 먹는대요"）
        val stepFormula        : String,            // 公式拆解（如 "먹(有收音) + -는대요"）
        val tip                : String,            // 避坑与语用说明
        val honorificFullForm  : String? = null     // 敬语转述（如 "밥을 드신다고 하셨어요"）
    )

    // ════════════════════════════════════════════════════════════
    // 核心推导接口
    // ════════════════════════════════════════════════════════════

    /**
     * 间接引语通用推导函数
     *
     * @param baseWord 谓词原型或名词（如 "가다", "먹다", "살다", "예쁘다", "학생", "의사"）
     * @param partOfSpeech 词性
     * @param sentenceType 目标句型类型
     * @param tense 时态
     * @param isNegative 是否为否定句（如 "가지 말자"）
     * @param beneficiary 命令句委托对象（默认为普通 NONE）
     */
    fun transpose(
        baseWord     : String,
        partOfSpeech : PartOfSpeech,
        sentenceType : SentenceType,
        tense        : Tense = Tense.PRESENT,
        isNegative   : Boolean = false,
        beneficiary  : Beneficiary = Beneficiary.NONE
    ): IndirectSpeechResult {
        return when (sentenceType) {
            SentenceType.DECLARATIVE ->
                transposeDeclarative(baseWord, partOfSpeech, tense, isNegative)

            SentenceType.INTERROGATIVE ->
                transposeInterrogative(baseWord, partOfSpeech, tense, isNegative)

            SentenceType.IMPERATIVE ->
                transposeImperative(baseWord, isNegative, beneficiary)

            SentenceType.PROPOSITIVE ->
                transposePropositive(baseWord, isNegative)
        }
    }

    // ── 1. 陈述句推导 (-ㄴ/는다고 하다 -> -대요 / -(이)래요) ────────

    private fun transposeDeclarative(
        baseWord     : String,
        partOfSpeech : PartOfSpeech,
        tense        : Tense,
        isNegative   : Boolean
    ): IndirectSpeechResult {
        val cleanStem = if (baseWord.endsWith("다")) baseWord.dropLast(1) else baseWord

        when (partOfSpeech) {
            PartOfSpeech.NOUN -> {
                val lastChar = baseWord.last()
                val hasJong = HangulJamo.decompose(lastChar).hasJongseong
                val particle = if (hasJong) "이" else ""
                val full = "${baseWord}${particle}라고 해요"
                val contracted = "${baseWord}${particle}래요"
                return IndirectSpeechResult(
                    originalText = baseWord,
                    sentenceType = SentenceType.DECLARATIVE,
                    partOfSpeech = partOfSpeech,
                    tense = Tense.PRESENT,
                    fullForm = full,
                    contractedForm = contracted,
                    stepFormula = "${baseWord} + (${if (hasJong) "有收音-이" else "无收音"}) + -래요",
                    tip = "名词陈述句间接引语使用 -(이)라고 하다 / -(이)래요，注意不是 -다고 하다。"
                )
            }

            PartOfSpeech.ADJECTIVE -> {
                val (full, contracted, formula) = when (tense) {
                    Tense.PRESENT -> {
                        Triple("${cleanStem}다고 해요", "${cleanStem}대요", "${cleanStem} + -대요")
                    }
                    Tense.PAST -> {
                        val pastConj = KoreanConjugationEngine.conjugate(baseWord, "-았/었어요").conjugatedText.removeSuffix("요")
                        Triple("${pastConj}고 해요", "${pastConj.dropLast(1)}${pastConj.last()}대요".replace("었대요", "었대요"), "${pastConj} + -대요")
                    }
                    Tense.FUTURE -> {
                        val f = getFutureStem(cleanStem)
                        Triple("${f} 거라고 해요", "${f} 거래요", "${f} + 거래요")
                    }
                }
                return IndirectSpeechResult(
                    originalText = baseWord,
                    sentenceType = SentenceType.DECLARATIVE,
                    partOfSpeech = partOfSpeech,
                    tense = tense,
                    fullForm = full,
                    contractedForm = contracted,
                    stepFormula = formula,
                    tip = "形容词现在时直接接 -다고 하다 / -대요，千万不要像动词那样加 -ㄴ/는-！"
                )
            }

            PartOfSpeech.VERB -> {
                val lastChar = cleanStem.last()
                val dec = HangulJamo.decompose(lastChar)

                val (full, contracted, formula) = when (tense) {
                    Tense.PRESENT -> {
                        if (dec.jong == 'ㄹ') {
                            // ㄹ 收音脱落并填入 ㄴ
                            val stemWithoutL = HangulJamo.removeJongseong(lastChar)
                            val prefix = cleanStem.dropLast(1)
                            val newChar = HangulJamo.setJongseong(stemWithoutL, 'ㄴ')
                            val finalStem = "$prefix$newChar"
                            Triple("${finalStem}다고 해요", "${finalStem}대요", "${cleanStem}(ㄹ脱落填ㄴ) + -대요")
                        } else if (dec.hasJongseong) {
                            // 有收音 -> -는다고 해요 / -는대요
                            Triple("${cleanStem}는다고 해요", "${cleanStem}는대요", "${cleanStem}(有收音) + -는대요")
                        } else {
                            // 无收音 -> 填入 ㄴ 收音
                            val prefix = cleanStem.dropLast(1)
                            val newChar = HangulJamo.setJongseong(lastChar, 'ㄴ')
                            val finalStem = "$prefix$newChar"
                            Triple("${finalStem}다고 해요", "${finalStem}대요", "${cleanStem}(无收音加ㄴ) + -대요")
                        }
                    }
                    Tense.PAST -> {
                        val pastConj = KoreanConjugationEngine.conjugate(baseWord, "-았/었어요").conjugatedText.removeSuffix("요")
                        Triple("${pastConj}고 해요", "${pastConj}대요", "${pastConj} + -대요")
                    }
                    Tense.FUTURE -> {
                        val f = getFutureStem(cleanStem)
                        Triple("${f} 거라고 해요", "${f} 거래요", "${f} + 거래요")
                    }
                }

                return IndirectSpeechResult(
                    originalText = baseWord,
                    sentenceType = SentenceType.DECLARATIVE,
                    partOfSpeech = partOfSpeech,
                    tense = tense,
                    fullForm = full,
                    contractedForm = contracted,
                    stepFormula = formula,
                    tip = "动词现在时区分有无收音（有收音-는대요 / 无收音-ㄴ대요）；过去时统一接 -았/었대요。"
                )
            }
        }
    }

    // ── 2. 疑问句推导 (-냐고 하다 -> -냬요 / -(이)냬요) ──────────

    private fun transposeInterrogative(
        baseWord     : String,
        partOfSpeech : PartOfSpeech,
        tense        : Tense,
        isNegative   : Boolean
    ): IndirectSpeechResult {
        val cleanStem = if (baseWord.endsWith("다")) baseWord.dropLast(1) else baseWord

        if (partOfSpeech == PartOfSpeech.NOUN) {
            val lastChar = baseWord.last()
            val hasJong = HangulJamo.decompose(lastChar).hasJongseong
            val particle = if (hasJong) "이" else ""
            return IndirectSpeechResult(
                originalText = baseWord,
                sentenceType = SentenceType.INTERROGATIVE,
                partOfSpeech = partOfSpeech,
                tense = Tense.PRESENT,
                fullForm = "${baseWord}${particle}냐고 해요",
                contractedForm = "${baseWord}${particle}냬요",
                stepFormula = "${baseWord} + (${if (hasJong) "有收音-이" else "无收音"}) + -냬요",
                tip = "名词疑问转述使用 -(이)냐고 하다 / -(이)냬요。"
            )
        }

        val (full, contracted, formula) = when (tense) {
            Tense.PRESENT -> {
                Triple("${cleanStem}냐고 해요", "${cleanStem}냬요", "${cleanStem} + -냬요")
            }
            Tense.PAST -> {
                val pastConj = KoreanConjugationEngine.conjugate(baseWord, "-았/었어요").conjugatedText.removeSuffix("요")
                Triple("${pastConj}냐고 해요", "${pastConj}냬요", "${pastConj} + -냬요")
            }
            Tense.FUTURE -> {
                val f = getFutureStem(cleanStem)
                Triple("${f} 거냐고 해요", "${f} 거냬요", "${f} + 거냬요")
            }
        }

        return IndirectSpeechResult(
            originalText = baseWord,
            sentenceType = SentenceType.INTERROGATIVE,
            partOfSpeech = partOfSpeech,
            tense = tense,
            fullForm = full,
            contractedForm = contracted,
            stepFormula = formula,
            tip = "动词与形容词疑问句转述规则相同，词干直接接 -냐고 하다 / -냬요。"
        )
    }

    // ── 3. 命令句推导 (-(으)라고 하다 -> -(으)래요 / 달라고 vs 주라고) ─

    private fun transposeImperative(
        baseWord    : String,
        isNegative  : Boolean,
        beneficiary : Beneficiary
    ): IndirectSpeechResult {
        val cleanStem = if (baseWord.endsWith("다")) baseWord.dropLast(1) else baseWord

        // 1. 否定命令句 (-지 말라고 하다 -> -지 말래요)
        if (isNegative) {
            return IndirectSpeechResult(
                originalText = "${cleanStem}지 마세요",
                sentenceType = SentenceType.IMPERATIVE,
                partOfSpeech = PartOfSpeech.VERB,
                tense = Tense.PRESENT,
                fullForm = "${cleanStem}지 말라고 해요",
                contractedForm = "${cleanStem}지 말래요",
                stepFormula = "${cleanStem} + -지 말래요",
                tip = "否定命令句固定接 -지 말라고 하다 / -지 말래요。"
            )
        }

        // 2. 委托命令句（经典避坑特例：달라고 vs 주라고）
        if (beneficiary == Beneficiary.SPEAKER_SELF) {
            val vConj = KoreanConjugationEngine.conjugate(baseWord, "-아/어요").conjugatedText.removeSuffix("요")
            return IndirectSpeechResult(
                originalText = "${vConj} 주세요 (对我说)",
                sentenceType = SentenceType.IMPERATIVE,
                partOfSpeech = PartOfSpeech.VERB,
                tense = Tense.PRESENT,
                fullForm = "${vConj} 달라고 해요",
                contractedForm = "${vConj} 달래요",
                stepFormula = "${vConj} + -달래요",
                tip = "【高频避坑】如果说话人是请求对方为“说话人自己”做某事，주다 必须变为 달라고 하다 / 달래요！"
            )
        } else if (beneficiary == Beneficiary.THIRD_PARTY) {
            val vConj = KoreanConjugationEngine.conjugate(baseWord, "-아/어요").conjugatedText.removeSuffix("요")
            return IndirectSpeechResult(
                originalText = "${vConj} 주세요 (对第三方)",
                sentenceType = SentenceType.IMPERATIVE,
                partOfSpeech = PartOfSpeech.VERB,
                tense = Tense.PRESENT,
                fullForm = "${vConj} 주라고 해요",
                contractedForm = "${vConj} 주래요",
                stepFormula = "${vConj} + -주래요",
                tip = "如果说话人是要求为“第三方他人”做事，则保持使用 주라고 하다 / 주래요。"
            )
        }

        // 3. 普通肯定命令句 (-(으)라고 하다 -> -(으)래요)
        val lastChar = cleanStem.last()
        val dec = HangulJamo.decompose(lastChar)

        val (full, contracted, formula) = if (dec.jong == 'ㄹ') {
            // ㄹ 收音直接接 -라고 / -래요
            Triple("${cleanStem}라고 해요", "${cleanStem}래요", "${cleanStem}(ㄹ收音不加으) + -래요")
        } else if (dec.hasJongseong) {
            // 有收音接 -으라고 / -으래요
            Triple("${cleanStem}으라고 해요", "${cleanStem}으래요", "${cleanStem}(有收音) + -으래요")
        } else {
            // 无收音接 -라고 / -래요
            Triple("${cleanStem}라고 해요", "${cleanStem}래요", "${cleanStem}(无收音) + -래요")
        }

        return IndirectSpeechResult(
            originalText = "${cleanStem}으세요/세요",
            sentenceType = SentenceType.IMPERATIVE,
            partOfSpeech = PartOfSpeech.VERB,
            tense = Tense.PRESENT,
            fullForm = full,
            contractedForm = contracted,
            stepFormula = formula,
            tip = "命令句间接引语仅限动词，有收音接 -으래요，无收音或 ㄹ 收音接 -래요。"
        )
    }

    // ── 4. 共动句推导 (-자고 하다 -> -재요 / -지 말재요) ──────────

    private fun transposePropositive(
        baseWord   : String,
        isNegative : Boolean
    ): IndirectSpeechResult {
        val cleanStem = if (baseWord.endsWith("다")) baseWord.dropLast(1) else baseWord

        if (isNegative) {
            return IndirectSpeechResult(
                originalText = "${cleanStem}지 맙시다",
                sentenceType = SentenceType.PROPOSITIVE,
                partOfSpeech = PartOfSpeech.VERB,
                tense = Tense.PRESENT,
                fullForm = "${cleanStem}지 말자고 해요",
                contractedForm = "${cleanStem}지 말재요",
                stepFormula = "${cleanStem} + -지 말재요",
                tip = "否定共动句固定接 -지 말자고 하다 / -지 말재요。"
            )
        }

        return IndirectSpeechResult(
            originalText = "${cleanStem}자",
            sentenceType = SentenceType.PROPOSITIVE,
            partOfSpeech = PartOfSpeech.VERB,
            tense = Tense.PRESENT,
            fullForm = "${cleanStem}자고 해요",
            contractedForm = "${cleanStem}재요",
            stepFormula = "${cleanStem} + -재요",
            tip = "共动句提议转述不分有无收音，动词词干直接接 -자고 하다 / -재요。"
        )
    }

    private fun getFutureStem(cleanStem: String): String {
        val lastChar = cleanStem.last()
        val dec = HangulJamo.decompose(lastChar)
        return if (dec.jong == 'ㄹ') {
            cleanStem
        } else if (dec.hasJongseong) {
            "${cleanStem}을"
        } else {
            val prefix = cleanStem.dropLast(1)
            val newChar = HangulJamo.setJongseong(lastChar, 'ㄹ')
            "$prefix$newChar"
        }
    }
}
