package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 纯 Kotlin 韩语谓词变音与变形计算引擎（KoreanConjugationEngine）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心特性：
 * 1. 深度覆盖延世韩国语 1~6 册全部 7 大不规则变音规则（ㄷ, ㅂ, ㅅ, 르, 으, ㅎ, ㄹ）。
 * 2. 具备完整的正向变形推导与常用词尾（-아/어요, -(으)면, -(으)ㅂ니다 等）音变自适应。
 * 3. 包含延世核心排除特例库（如 닫다/받다/입다/벗다/좋다 规则变形识别）。
 * 4. 输出音标发音指南与详细规则解析。
 */
object KoreanConjugationEngine {

    // ── 1. 特例排除词库（常被误判为不规则的规则词） ─────────────

    /** ㄷ 规则词（遇到元音 ㄷ 不变 ㄹ） */
    private val D_REGULAR_EXCEPTIONS = setOf(
        "닫다", "받다", "얻다", "믿다", "뜯다", "굳다", "묻다(埋/沾)", "돋다"
    )

    /** ㅂ 规则词（遇到元音 ㅂ 不变 우） */
    private val B_REGULAR_EXCEPTIONS = setOf(
        "입다", "잡다", "씹다", "좁다", "넓다", "뽑다", "접다", "업다", "집다"
    )

    /** ㅂ 变 와 的特殊词（仅 돕다 与 곱다） */
    private val B_TO_WA_WORDS = setOf("돕다", "곱다")

    /** ㅅ 规则词（遇到元音 ㅅ 不脱落） */
    private val S_REGULAR_EXCEPTIONS = setOf(
        "벗다", "씻다", "빗다", "웃다", "빼앗다"
    )

    /** ㅎ 规则词（遇到元音 ㅎ 不脱落） */
    private val H_REGULAR_EXCEPTIONS = setOf(
        "좋다", "놓다", "낳다", "넣다", "쌓다", "빻다"
    )

    // ════════════════════════════════════════════════════════════
    // 核心公共接口
    // ════════════════════════════════════════════════════════════

    /**
     * 谓词变形计算主函数
     *
     * @param baseVerb 谓词原型（如 "걷다", "돕다", "모르다", "크다", "살다", "하얗다", "공부하다"）
     * @param ending 目标词尾（如 "-아/어요", "-(으)면", "-(으)ㅂ니다", "-(으)세요", "-았/었어요", "-고", "-지만"）
     * @param irregularType 可选显式指定的变音类型；若未指定则自动智能推断
     */
    fun conjugate(
        baseVerb: String,
        ending: String,
        irregularType: IrregularType? = null
    ): ConjugationResult {
        require(baseVerb.endsWith("다")) { "谓词原型必须以 '다' 结尾: $baseVerb" }

        val stem = baseVerb.dropLast(1) // 提取词干（去掉 '다'）
        if (stem.isEmpty()) {
            return ConjugationResult(baseVerb, baseVerb, ending, IrregularType.REGULAR, null, "原型无效")
        }

        // 识别或推断不规则类型
        val detectedIrregular = irregularType ?: detectIrregularType(baseVerb, stem)
        val endingCategory = classifyEnding(ending)

        return when (endingCategory) {
            EndingPatternType.DIRECT_STYLE -> {
                // 直接接续类（如 -고, -지만, -게, -기 때문에）
                val result = stem + cleanEnding(ending)
                ConjugationResult(
                    conjugatedText = result,
                    baseVerb = baseVerb,
                    endingPattern = ending,
                    appliedIrregular = IrregularType.REGULAR,
                    pronunciationGuide = null,
                    ruleExplanation = "直接接续词尾，词干保持不变。"
                )
            }

            EndingPatternType.A_EO_STYLE -> {
                conjugateAEoStyle(baseVerb, stem, ending, detectedIrregular)
            }

            EndingPatternType.EU_STYLE -> {
                conjugateEuStyle(baseVerb, stem, ending, detectedIrregular)
            }

            EndingPatternType.FORMAL_STYLE -> {
                conjugateFormalStyle(baseVerb, stem, ending, detectedIrregular)
            }

            EndingPatternType.HONORIFIC_STYLE -> {
                conjugateHonorificStyle(baseVerb, stem, ending, detectedIrregular)
            }

            EndingPatternType.N_CONSONANT_STYLE -> {
                conjugateNConsonantStyle(baseVerb, stem, ending, detectedIrregular)
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    // 分支 1：-아/어 系列变形（核心：-아/어요, -았/었어요, -아/어서）
    // ════════════════════════════════════════════════════════════

    private fun conjugateAEoStyle(
        baseVerb: String,
        stem: String,
        ending: String,
        irregular: IrregularType
    ): ConjugationResult {
        val lastChar = stem.last()
        val dec = HangulJamo.decompose(lastChar)
        val prefix = stem.dropLast(1)

        val suffix = if (ending.startsWith("-았/었") || ending.startsWith("-았/었어")) {
            ending.substringAfter("-았/었")
        } else {
            ending.removePrefix("-아/어").removePrefix("-아/").removePrefix("-어/").removePrefix("-")
        }
        val isPastTense = ending.contains("았/었")

        // 1. 特例：하다 动词
        if (stem.endsWith("하")) {
            val hPrefix = stem.dropLast(1)
            val conjStem = if (isPastTense) "${hPrefix}했" else "${hPrefix}해"
            val text = conjStem + suffix
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.REGULAR,
                ruleExplanation = "하다 动词/形容词特例：하 + 아/어 融合为 해（过去时为 했）。"
            )
        }

        // 2. 르 不规则 (모르다 -> 몰라요, 빠르다 -> 빨라요, 부르다 -> 불러요)
        if (irregular == IrregularType.REU_IRREGULAR && stem.endsWith("르")) {
            if (prefix.isNotEmpty()) {
                val prevChar = prefix.last()
                val prevDec = HangulJamo.decompose(prevChar)
                val newPrevChar = HangulJamo.setJongseong(prevChar, 'ㄹ')
                val rootPrefix = prefix.dropLast(1)

                val isYang = prevDec.isYangVowel
                val leuVowelChar = if (isPastTense) {
                    if (isYang) "랐" else "렀"
                } else {
                    if (isYang) "라" else "러"
                }
                val text = "$rootPrefix$newPrevChar$leuVowelChar$suffix"
                return ConjugationResult(
                    conjugatedText = text,
                    baseVerb = baseVerb,
                    endingPattern = ending,
                    appliedIrregular = IrregularType.REU_IRREGULAR,
                    ruleExplanation = "르 不规则：ㅡ 脱落，前一音节添加 ㄹ 收音；前字元音为${if (isYang) "阳性(ㅏ/ㅗ)接 라" else "阴性接 러"}。"
                )
            }
        }

        // 3. 으 脱落 (크다 -> 커요, 아프다 -> 아파요, 쓰다 -> 써요, 바쁘다 -> 바빠요)
        if (irregular == IrregularType.EU_DROPPING || (!dec.hasJongseong && dec.jung == 'ㅡ')) {
            val isYang = if (prefix.isNotEmpty()) {
                HangulJamo.decompose(prefix.last()).isYangVowel
            } else {
                false // 单音节 (크다, 쓰다) 默认阴性
            }
            val targetVowel = if (isYang) 'ㅏ' else 'ㅓ'
            val targetJong = if (isPastTense) 'ㅆ' else '\u0000'
            val combinedChar = HangulJamo.compose(dec.cho, targetVowel, targetJong)
            val text = "$prefix$combinedChar$suffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.EU_DROPPING,
                ruleExplanation = "으 脱落：词干末尾元音 ㅡ 遇到元音脱落，结合${if (prefix.isNotEmpty()) "前一音节" else "单音节阴性"}判定接 ${if (isYang) "아" else "어"}。"
            )
        }

        // 4. ㄷ 不规则 (걷다 -> 걸어요, 듣다 -> 들어요)
        if (irregular == IrregularType.D_IRREGULAR && dec.jong == 'ㄷ') {
            val isException = D_REGULAR_EXCEPTIONS.contains(baseVerb)
            if (!isException) {
                val newChar = HangulJamo.setJongseong(lastChar, 'ㄹ')
                val isYang = dec.isYangVowel
                val connect = if (isPastTense) (if (isYang) "았" else "었") else (if (isYang) "아" else "어")
                val text = "$prefix$newChar$connect$suffix"
                return ConjugationResult(
                    conjugatedText = text,
                    baseVerb = baseVerb,
                    endingPattern = ending,
                    appliedIrregular = IrregularType.D_IRREGULAR,
                    ruleExplanation = "ㄷ 不规则：词干末收音 ㄷ 遇到元音变为 ㄹ，接 ${if (isYang) "아" else "어"}。"
                )
            }
        }

        // 5. ㅂ 不规则 (돕다 -> 도와요, 눕다 -> 누워요, 춥다 -> 추워요)
        if (irregular == IrregularType.B_IRREGULAR && dec.jong == 'ㅂ') {
            val isException = B_REGULAR_EXCEPTIONS.contains(baseVerb)
            if (!isException) {
                val stemWithoutB = HangulJamo.removeJongseong(lastChar)
                val isSpecialWa = B_TO_WA_WORDS.contains(baseVerb)
                val bridge = if (isPastTense) {
                    if (isSpecialWa) "왔" else "웠"
                } else {
                    if (isSpecialWa) "와" else "워"
                }
                val text = "$prefix$stemWithoutB$bridge$suffix"
                return ConjugationResult(
                    conjugatedText = text,
                    baseVerb = baseVerb,
                    endingPattern = ending,
                    appliedIrregular = IrregularType.B_IRREGULAR,
                    ruleExplanation = "ㅂ 不规则：收音 ㅂ 脱落，${if (isSpecialWa) "돕/곱 特例转为 와" else "转为 우 + 어 → 워"}。"
                )
            }
        }

        // 6. ㅅ 不规则 (낫다 -> 나아요, 짓다 -> 지어요, 붓다 -> 부어요)
        if (irregular == IrregularType.S_IRREGULAR && dec.jong == 'ㅅ') {
            val isException = S_REGULAR_EXCEPTIONS.contains(baseVerb)
            if (!isException) {
                val stemWithoutS = HangulJamo.removeJongseong(lastChar)
                val isYang = dec.isYangVowel
                val connect = if (isPastTense) (if (isYang) "았" else "었") else (if (isYang) "아" else "어")
                val text = "$prefix$stemWithoutS$connect$suffix"
                return ConjugationResult(
                    conjugatedText = text,
                    baseVerb = baseVerb,
                    endingPattern = ending,
                    appliedIrregular = IrregularType.S_IRREGULAR,
                    ruleExplanation = "ㅅ 不规则：收音 ㅅ 遇到元音脱落，前后元音不缩合，接 ${if (isYang) "아" else "어"}。"
                )
            }
        }

        // 7. ㅎ 不规则 (그렇다 -> 그래요, 까맣다 -> 까매요, 하얗다 -> 하얘요)
        if (irregular == IrregularType.H_IRREGULAR && dec.jong == 'ㅎ') {
            val isException = H_REGULAR_EXCEPTIONS.contains(baseVerb)
            if (!isException) {
                val mergedJung = when (dec.jung) {
                    'ㅑ' -> 'ㅒ'
                    'ㅕ' -> 'ㅖ'
                    else -> 'ㅐ'
                }
                val targetJong = if (isPastTense) 'ㅆ' else '\u0000'
                val mergedChar = HangulJamo.compose(dec.cho, mergedJung, targetJong)
                val text = "$prefix$mergedChar$suffix"
                return ConjugationResult(
                    conjugatedText = text,
                    baseVerb = baseVerb,
                    endingPattern = ending,
                    appliedIrregular = IrregularType.H_IRREGULAR,
                    ruleExplanation = "ㅎ 不规则：收音 ㅎ 脱落，词干元音与元音融合为 ${mergedJung}。"
                )
            }
        }

        // 8. 规则变形与元音缩合（Contraction）
        val isYang = dec.isYangVowel
        if (dec.hasJongseong) {
            // 有收音：直接接 -아 / -어
            val connect = if (isPastTense) (if (isYang) "았" else "었") else (if (isYang) "아" else "어")
            val text = "$stem$connect$suffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.REGULAR,
                ruleExplanation = "规则变形：词干有收音，元音为${if (isYang) "阳性接 아" else "阴性接 어"}。"
            )
        } else {
            // 无收音：发生元音缩合
            val contractedChar = contractVowels(dec.cho, dec.jung, isPastTense)
            val text = "$prefix$contractedChar$suffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.REGULAR,
                ruleExplanation = "规则变形：词干无收音，元音发生自然缩合。"
            )
        }
    }

    // ════════════════════════════════════════════════════════════
    // 分支 2：-(으) 系列变形（如 -(으)면, -(으)니까, -(으)려고）
    // ════════════════════════════════════════════════════════════

    private fun conjugateEuStyle(
        baseVerb: String,
        stem: String,
        ending: String,
        irregular: IrregularType
    ): ConjugationResult {
        val lastChar = stem.last()
        val dec = HangulJamo.decompose(lastChar)
        val prefix = stem.dropLast(1)
        val pureEnding = ending.removePrefix("-").removePrefix("(으)")

        // 1. ㄹ 收音（ㄹ 脱落特例规则）：遇到 -(으) 时直接不加 으
        if (dec.jong == 'ㄹ' || irregular == IrregularType.L_DROPPING) {
            val text = "$stem$pureEnding"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.L_DROPPING,
                ruleExplanation = "ㄹ 词干遇到 -(으) 语法时，直接接续，无需添加 으。"
            )
        }

        // 2. ㄷ 不规则 (걷다 -> 걸으면, 듣다 -> 들으면)
        if (irregular == IrregularType.D_IRREGULAR && dec.jong == 'ㄷ' && !D_REGULAR_EXCEPTIONS.contains(baseVerb)) {
            val newChar = HangulJamo.setJongseong(lastChar, 'ㄹ')
            val text = "$prefix$newChar" + "으" + pureEnding
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.D_IRREGULAR,
                ruleExplanation = "ㄷ 不规则：遇到元音 (으) 时 ㄷ 变为 ㄹ，后接 으$pureEnding。"
            )
        }

        // 3. ㅂ 不规则 (돕다 -> 도우면, 춥다 -> 추우면)
        if (irregular == IrregularType.B_IRREGULAR && dec.jong == 'ㅂ' && !B_REGULAR_EXCEPTIONS.contains(baseVerb)) {
            val withoutB = HangulJamo.removeJongseong(lastChar)
            val text = "$prefix$withoutB" + "우" + pureEnding
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.B_IRREGULAR,
                ruleExplanation = "ㅂ 不规则：遇到元音 (으) 时 ㅂ 脱落转为 우，后接 $pureEnding。"
            )
        }

        // 4. ㅅ 不规则 (낫다 -> 나으면, 짓다 -> 지으면)
        if (irregular == IrregularType.S_IRREGULAR && dec.jong == 'ㅅ' && !S_REGULAR_EXCEPTIONS.contains(baseVerb)) {
            val withoutS = HangulJamo.removeJongseong(lastChar)
            val text = "$prefix$withoutS" + "으" + pureEnding
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.S_IRREGULAR,
                ruleExplanation = "ㅅ 不规则：遇到元音时 ㅅ 脱落，保留 으$pureEnding。"
            )
        }

        // 5. ㅎ 不规则 (그렇다 -> 그러면, 어떻다 -> 어쩌면/어떠면)
        if (irregular == IrregularType.H_IRREGULAR && dec.jong == 'ㅎ' && !H_REGULAR_EXCEPTIONS.contains(baseVerb)) {
            val withoutH = HangulJamo.removeJongseong(lastChar)
            val text = "$prefix$withoutH$pureEnding"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.H_IRREGULAR,
                ruleExplanation = "ㅎ 不规则：遇到 (으) 时 ㅎ 与 으 同时脱落。"
            )
        }

        // 6. 规则变形
        val text = if (dec.hasJongseong) {
            "$stem" + "으" + pureEnding
        } else {
            "$stem$pureEnding"
        }
        return ConjugationResult(
            conjugatedText = text,
            baseVerb = baseVerb,
            endingPattern = ending,
            appliedIrregular = IrregularType.REGULAR,
            ruleExplanation = if (dec.hasJongseong) "规则变形：词干有收音，接 으$pureEnding。" else "规则变形：词干无收音，直接接 $pureEnding。"
        )
    }

    // ════════════════════════════════════════════════════════════
    // 分支 3：-(으)ㅂ니다 / -(으)ㅂ시다 系列变形
    // ════════════════════════════════════════════════════════════

    private fun conjugateFormalStyle(
        baseVerb: String,
        stem: String,
        ending: String,
        irregular: IrregularType
    ): ConjugationResult {
        val lastChar = stem.last()
        val dec = HangulJamo.decompose(lastChar)
        val prefix = stem.dropLast(1)
        val formalSuffix = if (ending.contains("시다")) "니다" else "니다"

        // 1. ㄹ 脱落：살다 -> 삽니다, 만들다 -> 만듭니다
        if (dec.jong == 'ㄹ' || irregular == IrregularType.L_DROPPING) {
            val newChar = HangulJamo.setJongseong(lastChar, 'ㅂ')
            val text = "$prefix$newChar$formalSuffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.L_DROPPING,
                ruleExplanation = "ㄹ 脱落：遇到 ㅂ/ㄴ/ㅅ 辅音时，收音 ㄹ 脱落并填入 ㅂ 收音。"
            )
        }

        // 2. 规则无收音 -> 填入 ㅂ 收音（가다 -> 갑니다）
        if (!dec.hasJongseong) {
            val newChar = HangulJamo.setJongseong(lastChar, 'ㅂ')
            val text = "$prefix$newChar$formalSuffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.REGULAR,
                ruleExplanation = "规则变形：无收音词干添加 ㅂ 收音接 니다。"
            )
        } else {
            // 3. 有收音 -> 接 습니다（먹다 -> 먹습니다, 듣다 -> 듣습니다, 돕다 -> 돕습니다）
            val text = "$stem" + "습$formalSuffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.REGULAR,
                ruleExplanation = "规则接续：有收音词干接 습니다（辅音开头不触发 ㄷ/ㅂ/ㅅ 元音不规则）。"
            )
        }
    }

    // ════════════════════════════════════════════════════════════
    // 分支 4：-(으)세요 / -(으)십시오 敬语系列
    // ════════════════════════════════════════════════════════════

    private fun conjugateHonorificStyle(
        baseVerb: String,
        stem: String,
        ending: String,
        irregular: IrregularType
    ): ConjugationResult {
        val lastChar = stem.last()
        val dec = HangulJamo.decompose(lastChar)
        val prefix = stem.dropLast(1)
        val honorificSuffix = ending.removePrefix("-").removePrefix("(으)")

        // 1. ㄹ 脱落：살다 -> 사세요, 만들다 -> 만드세요
        if (dec.jong == 'ㄹ' || irregular == IrregularType.L_DROPPING) {
            val withoutL = HangulJamo.removeJongseong(lastChar)
            val text = "$prefix$withoutL$honorificSuffix"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.L_DROPPING,
                ruleExplanation = "ㄹ 脱落：遇到 ㅅ 开头的敬语词尾时，收音 ㄹ 脱落且不添加 으。"
            )
        }

        // 2. ㄷ 不规则 (듣다 -> 들으세요)
        if (irregular == IrregularType.D_IRREGULAR && dec.jong == 'ㄷ' && !D_REGULAR_EXCEPTIONS.contains(baseVerb)) {
            val newChar = HangulJamo.setJongseong(lastChar, 'ㄹ')
            val text = "$prefix$newChar" + "으" + honorificSuffix
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.D_IRREGULAR,
                ruleExplanation = "ㄷ 不规则：遇到元音 ㄷ 变 ㄹ，接 으$honorificSuffix。"
            )
        }

        // 3. ㅂ 不规则 (돕다 -> 도우세요)
        if (irregular == IrregularType.B_IRREGULAR && dec.jong == 'ㅂ' && !B_REGULAR_EXCEPTIONS.contains(baseVerb)) {
            val withoutB = HangulJamo.removeJongseong(lastChar)
            val text = "$prefix$withoutB" + "우" + honorificSuffix
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.B_IRREGULAR,
                ruleExplanation = "ㅂ 不规则：ㅂ 脱落转为 우，接 $honorificSuffix。"
            )
        }

        // 4. 规则变化
        val text = if (dec.hasJongseong) "$stem" + "으" + honorificSuffix else "$stem$honorificSuffix"
        return ConjugationResult(
            conjugatedText = text,
            baseVerb = baseVerb,
            endingPattern = ending,
            appliedIrregular = IrregularType.REGULAR,
            ruleExplanation = if (dec.hasJongseong) "有收音词干接 으$honorificSuffix。" else "无收音词干直接接 $honorificSuffix。"
        )
    }

    // ════════════════════════════════════════════════════════════
    // 分支 5：辅音 ㄴ 开头（-는, -니）
    // ════════════════════════════════════════════════════════════

    private fun conjugateNConsonantStyle(
        baseVerb: String,
        stem: String,
        ending: String,
        irregular: IrregularType
    ): ConjugationResult {
        val lastChar = stem.last()
        val dec = HangulJamo.decompose(lastChar)
        val prefix = stem.dropLast(1)
        val pureEnding = cleanEnding(ending)

        // ㄹ 脱落：살다 -> 사는, 사니
        if (dec.jong == 'ㄹ' || irregular == IrregularType.L_DROPPING) {
            val withoutL = HangulJamo.removeJongseong(lastChar)
            val text = "$prefix$withoutL$pureEnding"
            return ConjugationResult(
                conjugatedText = text,
                baseVerb = baseVerb,
                endingPattern = ending,
                appliedIrregular = IrregularType.L_DROPPING,
                ruleExplanation = "ㄹ 脱落：遇到 ㄴ 开头的词尾时，收音 ㄹ 必须脱落。"
            )
        }

        val text = "$stem$pureEnding"
        return ConjugationResult(
            conjugatedText = text,
            baseVerb = baseVerb,
            endingPattern = ending,
            appliedIrregular = IrregularType.REGULAR,
            ruleExplanation = "辅音词尾直接接续。"
        )
    }

    // ════════════════════════════════════════════════════════════
    // 辅助音变与推导函数
    // ════════════════════════════════════════════════════════════

    /**
     * 无收音音节与 -아/어 的元音自然缩合（Vowel Contraction）
     */
    private fun contractVowels(cho: Char, jung: Char, isPastTense: Boolean): String {
        val pastJong = if (isPastTense) 'ㅆ' else '\u0000'
        return when (jung) {
            'ㅏ' -> HangulJamo.compose(cho, 'ㅏ', pastJong).toString()
            'ㅓ' -> HangulJamo.compose(cho, 'ㅓ', pastJong).toString()
            'ㅗ' -> HangulJamo.compose(cho, 'ㅘ', pastJong).toString() // 오+아->와, 보+아->봐
            'ㅜ' -> HangulJamo.compose(cho, 'ㅝ', pastJong).toString() // 주+어->줘, 배우+어->배워
            'ㅣ' -> HangulJamo.compose(cho, 'ㅕ', pastJong).toString() // 마시+어->마셔, 기다리+어->기다려
            'ㅐ' -> HangulJamo.compose(cho, 'ㅐ', pastJong).toString() // 보내+어->보내
            'ㅔ' -> HangulJamo.compose(cho, 'ㅔ', pastJong).toString() // 세+어->세
            'ㅚ' -> HangulJamo.compose(cho, 'ㅙ', pastJong).toString() // 되+어->돼
            'ㅡ' -> HangulJamo.compose(cho, 'ㅓ', pastJong).toString() // 默认阴性缩合
            else -> {
                val base = HangulJamo.compose(cho, jung, '\u0000')
                if (isPastTense) "${base}었" else "${base}어"
            }
        }
    }

    /**
     * 智能推断谓词的不规则变音类型
     */
    private fun detectIrregularType(baseVerb: String, stem: String): IrregularType {
        if (stem.endsWith("르")) return IrregularType.REU_IRREGULAR

        val lastChar = stem.last()
        val dec = HangulJamo.decompose(lastChar)

        return when {
            dec.jong == 'ㄷ' && !D_REGULAR_EXCEPTIONS.contains(baseVerb) -> IrregularType.D_IRREGULAR
            dec.jong == 'ㅂ' && !B_REGULAR_EXCEPTIONS.contains(baseVerb) -> IrregularType.B_IRREGULAR
            dec.jong == 'ㅅ' && !S_REGULAR_EXCEPTIONS.contains(baseVerb) -> IrregularType.S_IRREGULAR
            dec.jong == 'ㅎ' && !H_REGULAR_EXCEPTIONS.contains(baseVerb) -> IrregularType.H_IRREGULAR
            dec.jong == 'ㄹ' -> IrregularType.L_DROPPING
            !dec.hasJongseong && dec.jung == 'ㅡ' -> IrregularType.EU_DROPPING
            else -> IrregularType.REGULAR
        }
    }

    /**
     * 分类词尾语法模式
     */
    private fun classifyEnding(ending: String): EndingPatternType {
        val e = ending.trim()
        return when {
            e.contains("아/어") || e.contains("았/었") || e.startsWith("-아") || e.startsWith("-어") ->
                EndingPatternType.A_EO_STYLE

            e.contains("ㅂ니다") || e.contains("습니다") || e.contains("ㅂ시다") || e.contains("읍시다") ->
                EndingPatternType.FORMAL_STYLE

            e.contains("세요") || e.contains("십시오") || e.contains("시") ->
                EndingPatternType.HONORIFIC_STYLE

            e.startsWith("-는") || e.startsWith("-니") || e.startsWith("-냐") ->
                EndingPatternType.N_CONSONANT_STYLE

            e.contains("(으)") ->
                EndingPatternType.EU_STYLE

            else ->
                EndingPatternType.DIRECT_STYLE
        }
    }

    private fun cleanEnding(ending: String): String =
        ending.removePrefix("-").removePrefix("(으)")
}
