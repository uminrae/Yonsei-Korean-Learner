package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语音变规则计算引擎 (HangulPronunciationEngine)
 * ══════════════════════════════════════════════════════════════════
 *
 * 严格遵循韩国国立国语院（국립국어원）《标准发音法（표준 발음법）》：
 * 1. 激音化 / 送气化 (Aspiration / 거센소리되기)
 * 2. 口盖音化 / 腭音化 (Palatalization / 구개음화)
 * 3. 辅音同化 - 流音化 (Lateralization / 유음화)
 * 4. 辅音同化 - 鼻音化 (Nasalization / 비음화)
 * 5. 紧音化 / 硬音化 (Tensification / 된소리되기)
 * 6. 连音化 (Liaison / 연음화)
 * 7. 终声代表音化 / 音节末收音规则 (Neutralization of Coda)
 */
object HangulPronunciationEngine {

    /** 音变规则分类枚举 */
    enum class PhonologicalRule(val koreanName: String, val chineseName: String) {
        ASPIRATION("거센소리되기", "激音化 (送气化)"),
        PALATALIZATION("구개음화", "口盖音化 (腭音化)"),
        LATERALIZATION("유음화", "流音化"),
        NASALIZATION("비음화", "鼻音化"),
        TENSIFICATION("된소리되기", "紧音化 (硬音化)"),
        LIAISON("연음화", "连音化"),
        H_DELETION("ㅎ탈락", "ㅎ 脱落"),
        CODA_NEUTRALIZATION("음절의 끝소리 규칙", "收音代表音化")
    }

    /** 音变计算结果输出模型 */
    data class PronunciationResult(
        val originalText        : String,               // 输入原文（如 "같이", "한국어"）
        val pronunciationText   : String,               // 标准音标（如 "[가치]", "[한구거]"）
        val phoneticString      : String,               // 纯发音文本（如 "가치", "한구거"）
        val appliedRules        : List<PhonologicalRule>,// 触发的音变类型列表
        val explanations        : List<String>          // 详细中文音变解释
    )

    /**
     * 核心公开函数：输入任意韩文字符串，计算并返回其实际发音与音变报告
     *
     * @param hangulText 韩文字符串（如 "같이", "국물", "입학", "한국어", "신라", "입고"）
     */
    fun getActualPronunciation(hangulText: String): PronunciationResult {
        if (hangulText.isBlank()) {
            return PronunciationResult(hangulText, "[]", "", emptyList(), emptyList())
        }

        val appliedRules = mutableSetOf<PhonologicalRule>()
        val explanations = mutableListOf<String>()

        // 按词块处理（空格保留）
        val words = hangulText.split(" ")
        val pronouncedWords = words.map { word ->
            processWordPronunciation(word, appliedRules, explanations)
        }

        val finalPronunciation = pronouncedWords.joinToString(" ")
        return PronunciationResult(
            originalText = hangulText,
            pronunciationText = "[$finalPronunciation]",
            phoneticString = finalPronunciation,
            appliedRules = appliedRules.toList(),
            explanations = explanations.distinct()
        )
    }

    // ════════════════════════════════════════════════════════════
    // 内部音变推导管道
    // ════════════════════════════════════════════════════════════

    private fun processWordPronunciation(
        word: String,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): String {
        val chars = word.toCharArray()
        if (chars.isEmpty()) return ""

        val syllables = chars.map { HangulJamo.decompose(it) }.toMutableList()

        // ── 阶段 1：相邻音节音变规则遍历（激音化、口盖音化、流音化、鼻音化、紧音化、连音化） ──
        for (i in 0 until syllables.size - 1) {
            val curr = syllables[i]
            val next = syllables[i + 1]

            if (!curr.isHangul || !next.isHangul) continue

            // 1. 激音化 / 送气化 (Aspiration)
            if (applyAspiration(curr, next, syllables, i, appliedRules, explanations)) continue

            // 2. 口盖音化 / 腭音化 (Palatalization)
            if (applyPalatalization(curr, next, syllables, i, appliedRules, explanations)) continue

            // 3. 流音化 (Lateralization)
            if (applyLateralization(curr, next, syllables, i, appliedRules, explanations)) continue

            // 4. 流音的鼻音化（如 독립 -> 독닙, 종로 -> 종노）
            applyLiquidNasalization(curr, next, syllables, i, appliedRules, explanations)

            // 5. 鼻音化 (Nasalization)
            applyNasalization(curr, next, syllables, i, appliedRules, explanations)

            // 6. 紧音化 (Tensification)
            applyTensification(curr, next, syllables, i, appliedRules, explanations)

            // 7. 连音化 与 ㅎ脱落 (Liaison & H-Deletion)
            applyLiaison(curr, next, syllables, i, appliedRules, explanations)
        }

        // ── 阶段 2：单字词尾 / 独立末尾收音代表音化 ──────────────
        for (i in syllables.indices) {
            val curr = syllables[i]
            if (!curr.isHangul || curr.jong == '\u0000') continue

            // 如果是最后一个字，或者后一个字是辅音且未被连音处理
            val isLastChar = (i == syllables.size - 1)
            val nextIsConsonant = !isLastChar && syllables[i + 1].isHangul && syllables[i + 1].cho != 'ㅇ'

            if (isLastChar || nextIsConsonant) {
                val repJong = getRepresentativeCoda(curr.jong)
                if (repJong != curr.jong) {
                    syllables[i] = curr.copy(jong = repJong)
                    appliedRules.add(PhonologicalRule.CODA_NEUTRALIZATION)
                    explanations.add("收音 '${curr.jong}' 代表音归并为 '$repJong'。")
                }
            }
        }

        return syllables.map {
            if (it.isHangul) HangulJamo.compose(it.cho, it.jung, it.jong) else it.cho
        }.joinToString("")
    }

    // ── 1. 激音化 / 送气化 (Aspiration) ─────────────────────────

    private fun applyAspiration(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): Boolean {
        // 顺行激音化：前字收音含 ㅎ，后字初声为 ㄱ, ㄷ, ㅈ
        if (curr.jong == 'ㅎ' || curr.jong == 'ㄶ' || curr.jong == 'ㅀ') {
            val aspiratedCho = when (next.cho) {
                'ㄱ' -> 'ㅋ'
                'ㄷ' -> 'ㅌ'
                'ㅈ' -> 'ㅊ'
                else -> null
            }
            if (aspiratedCho != null) {
                val remainJong = when (curr.jong) {
                    'ㄶ' -> 'ㄴ'
                    'ㅀ' -> 'ㄹ'
                    else -> '\u0000'
                }
                syllables[index] = curr.copy(jong = remainJong)
                syllables[index + 1] = next.copy(cho = aspiratedCho)
                appliedRules.add(PhonologicalRule.ASPIRATION)
                explanations.add("收音 '${curr.jong}' 遇初声 '${next.cho}' 激音化为 '$aspiratedCho'。")
                return true
            }
        }

        // 逆行激音化：前字收音为 ㄱ, ㄷ, ㅂ, ㅈ (含 ㄺ, ㄼ)，后字初声为 ㅎ
        if (next.cho == 'ㅎ') {
            val aspiratedCho = when (curr.jong) {
                'ㄱ', 'ㅋ', 'ㄲ', 'ㄳ', 'ㄺ' -> 'ㅋ'
                'ㄷ', 'ㅅ', 'ㅆ', 'ㅈ', 'ㅊ', 'ㅌ' -> 'ㅌ'
                'ㅂ', 'ㅍ', 'ㄼ', 'ㄿ', 'ㅄ' -> 'ㅍ'
                else -> null
            }
            if (aspiratedCho != null) {
                val remainJong = when (curr.jong) {
                    'ㄺ', 'ㄼ' -> 'ㄹ'
                    'ㄳ', 'ㅄ' -> 'ㅂ'
                    else -> '\u0000'
                }
                syllables[index] = curr.copy(jong = remainJong)
                syllables[index + 1] = next.copy(cho = aspiratedCho)
                appliedRules.add(PhonologicalRule.ASPIRATION)
                explanations.add("收音 '${curr.jong}' 遇初声 'ㅎ' 激音化合并为 '$aspiratedCho'。")
                return true
            }
        }
        return false
    }

    // ── 2. 口盖音化 / 腭音化 (Palatalization) ────────────────────

    private fun applyPalatalization(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): Boolean {
        // 前字收音为 ㄷ, ㅌ, ㄾ，后字初声为 ㅇ 且元音为 ㅣ 或 ㅑ, ㅕ, ㅛ, ㅠ
        if (next.cho == 'ㅇ' && (next.jung == 'ㅣ' || next.jung == 'ㅕ' || next.jung == 'ㅑ' || next.jung == 'ㅛ' || next.jung == 'ㅠ')) {
            val newNextCho = when (curr.jong) {
                'ㄷ' -> 'ㅈ'
                'ㅌ' -> 'ㅊ'
                'ㄾ' -> 'ㅊ'
                else -> null
            }
            if (newNextCho != null) {
                val remainJong = if (curr.jong == 'ㄾ') 'ㄹ' else '\u0000'
                syllables[index] = curr.copy(jong = remainJong)
                syllables[index + 1] = next.copy(cho = newNextCho)
                appliedRules.add(PhonologicalRule.PALATALIZATION)
                explanations.add("收音 '${curr.jong}' 遇元音 '${next.jung}' 发生口盖音化（腭音化），变为 '$newNextCho'。")
                return true
            }
        }
        return false
    }

    // ── 3. 流音化 (Lateralization) ──────────────────────────────

    private fun applyLateralization(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): Boolean {
        // 顺行流音化：前字收音为 ㄹ (含 ㄾ, ㅀ, ㄼ)，后字初声为 ㄴ -> 后字初声变为 ㄹ
        if ((curr.jong == 'ㄹ' || curr.jong == 'ㄾ' || curr.jong == 'ㅀ' || curr.jong == 'ㄼ') && next.cho == 'ㄴ') {
            syllables[index + 1] = next.copy(cho = 'ㄹ')
            appliedRules.add(PhonologicalRule.LATERALIZATION)
            explanations.add("收音 'ㄹ' 使得后字初声 'ㄴ' 同化为流音 'ㄹ'。")
            return true
        }

        // 逆行流音化：前字收音为 ㄴ (含 ㄵ, ㄶ)，后字初声为 ㄹ -> 前字收音变为 ㄹ
        if ((curr.jong == 'ㄴ' || curr.jong == 'ㄵ' || curr.jong == 'ㄶ') && next.cho == 'ㄹ') {
            syllables[index] = curr.copy(jong = 'ㄹ')
            appliedRules.add(PhonologicalRule.LATERALIZATION)
            explanations.add("后字初声 'ㄹ' 使得前字收音 'ㄴ' 逆行同化为流音 'ㄹ'。")
            return true
        }
        return false
    }

    // ── 4. 流音的鼻音化 与 辅音同化鼻音化 ─────────────────────────

    private fun applyLiquidNasalization(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ) {
        // ㄱ, ㅂ, ㅇ, ㅁ 后的初声 ㄹ 变为 ㄴ（如 독립 -> 독닙, 종로 -> 종노）
        if (next.cho == 'ㄹ') {
            val repJong = getRepresentativeCoda(curr.jong)
            if (repJong == 'ㄱ' || repJong == 'ㅂ' || repJong == 'ㅇ' || repJong == 'ㅁ') {
                syllables[index + 1] = next.copy(cho = 'ㄴ')
                appliedRules.add(PhonologicalRule.NASALIZATION)
                explanations.add("收音 '$repJong' 使得后字初声 'ㄹ' 鼻音化为 'ㄴ'。")
            }
        }
    }

    private fun applyNasalization(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): Boolean {
        // 后字初声为 ㄴ, ㅁ 时，前字收音代表音发生鼻音化：
        // ㄱ 族 -> ㅇ, ㄷ 族 -> ㄴ, ㅂ 族 -> ㅁ
        if (next.cho == 'ㄴ' || next.cho == 'ㅁ') {
            val rep = getRepresentativeCoda(curr.jong)
            val nasalJong = when (rep) {
                'ㄱ' -> 'ㅇ'
                'ㄷ' -> 'ㄴ'
                'ㅂ' -> 'ㅁ'
                else -> null
            }
            if (nasalJong != null && nasalJong != curr.jong) {
                syllables[index] = curr.copy(jong = nasalJong)
                appliedRules.add(PhonologicalRule.NASALIZATION)
                explanations.add("收音 '${curr.jong}'（代表音 '$rep'）遇初声 '${next.cho}' 鼻音化为 '$nasalJong'。")
                return true
            }
        }
        return false
    }

    // ── 5. 紧音化 / 硬音化 (Tensification) ───────────────────────

    private fun applyTensification(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): Boolean {
        // 代表收音为 ㄱ, ㄷ, ㅂ 时，后字初声 ㄱ, ㄷ, ㅂ, ㅅ, ㅈ 变为紧音 ㄲ, ㄸ, ㅃ, ㅆ, ㅉ
        val rep = getRepresentativeCoda(curr.jong)
        if (rep == 'ㄱ' || rep == 'ㄷ' || rep == 'ㅂ') {
            val tensedCho = when (next.cho) {
                'ㄱ' -> 'ㄲ'
                'ㄷ' -> 'ㄸ'
                'ㅂ' -> 'ㅃ'
                'ㅅ' -> 'ㅆ'
                'ㅈ' -> 'ㅉ'
                else -> null
            }
            if (tensedCho != null) {
                syllables[index + 1] = next.copy(cho = tensedCho)
                appliedRules.add(PhonologicalRule.TENSIFICATION)
                explanations.add("收音 '$rep' 使后字初声 '${next.cho}' 紧音化为 '$tensedCho'。")
                return true
            }
        }
        return false
    }

    // ── 6. 连音化 与 ㅎ 脱落 (Liaison & H-Deletion) ──────────────

    private fun applyLiaison(
        curr: DecomposedHangul,
        next: DecomposedHangul,
        syllables: MutableList<DecomposedHangul>,
        index: Int,
        appliedRules: MutableSet<PhonologicalRule>,
        explanations: MutableList<String>
    ): Boolean {
        if (curr.jong == '\u0000' || next.cho != 'ㅇ') return false

        // 1. ㅎ 脱落：收音为 ㅎ, ㄶ, ㅀ 遇元音 ㅇ
        if (curr.jong == 'ㅎ') {
            syllables[index] = curr.copy(jong = '\u0000')
            appliedRules.add(PhonologicalRule.H_DELETION)
            explanations.add("收音 'ㅎ' 遇到元音脱落不发音。")
            return true
        }
        if (curr.jong == 'ㄶ') {
            syllables[index] = curr.copy(jong = '\u0000')
            syllables[index + 1] = next.copy(cho = 'ㄴ')
            appliedRules.add(PhonologicalRule.H_DELETION)
            appliedRules.add(PhonologicalRule.LIAISON)
            explanations.add("双收音 'ㄶ' 中 'ㅎ' 脱落，'ㄴ' 连音移至后字初声。")
            return true
        }
        if (curr.jong == 'ㅀ') {
            syllables[index] = curr.copy(jong = '\u0000')
            syllables[index + 1] = next.copy(cho = 'ㄹ')
            appliedRules.add(PhonologicalRule.H_DELETION)
            appliedRules.add(PhonologicalRule.LIAISON)
            explanations.add("双收音 'ㅀ' 中 'ㅎ' 脱落，'ㄹ' 连音移至后字初声。")
            return true
        }

        // 2. 双收音连音化
        val doubleCodaMap = mapOf(
            'ㄳ' to Pair('ㄱ', 'ㅆ'),
            'ㄵ' to Pair('ㄴ', 'ㅈ'),
            'ㄺ' to Pair('ㄹ', 'ㄱ'),
            'ㄼ' to Pair('ㄹ', 'ㅂ'),
            'ㄽ' to Pair('ㄹ', 'ㅆ'),
            'ㄾ' to Pair('ㄹ', 'ㅌ'),
            'ㄿ' to Pair('ㄹ', 'ㅍ'),
            'ㅄ' to Pair('ㅂ', 'ㅆ')
        )

        val split = doubleCodaMap[curr.jong]
        if (split != null) {
            syllables[index] = curr.copy(jong = split.first)
            syllables[index + 1] = next.copy(cho = split.second)
            appliedRules.add(PhonologicalRule.LIAISON)
            explanations.add("双收音 '${curr.jong}' 分解：保留 '${split.first}'，后部 '${split.second}' 连音移至后字初声。")
            return true
        }

        // 3. 单收音普通连音化（收音 ㅇ 除外）
        if (curr.jong != 'ㅇ') {
            val movedCho = when (curr.jong) {
                'ㅅ' -> 'ㅅ'
                'ㅆ' -> 'ㅆ'
                else -> curr.jong
            }
            syllables[index] = curr.copy(jong = '\u0000')
            syllables[index + 1] = next.copy(cho = movedCho)
            appliedRules.add(PhonologicalRule.LIAISON)
            explanations.add("收音 '${curr.jong}' 连音移至后字元音初声。")
            return true
        }

        return false
    }

    // ── 7. 收音代表音归并辅助函数 ───────────────────────────────

    /**
     * 获取七大代表收音 (ㄱ, ㄴ, ㄷ, ㄹ, ㅁ, ㅂ, ㅇ)
     */
    private fun getRepresentativeCoda(jong: Char): Char {
        return when (jong) {
            'ㄱ', 'ㄲ', 'ㅋ', 'ㄳ', 'ㄺ' -> 'ㄱ'
            'ㄴ', 'ㄵ', 'ㄶ' -> 'ㄴ'
            'ㄷ', 'ㅅ', 'ㅆ', 'ㅈ', 'ㅊ', 'ㅌ', 'ㅎ' -> 'ㄷ'
            'ㄹ', 'ㄽ', 'ㄾ', 'ㅀ' -> 'ㄹ'
            'ㅁ', 'ㄻ' -> 'ㅁ'
            'ㅂ', 'ㅍ', 'ㄼ', 'ㄿ', 'ㅄ' -> 'ㅂ'
            'ㅇ' -> 'ㅇ'
            else -> jong
        }
    }
}
