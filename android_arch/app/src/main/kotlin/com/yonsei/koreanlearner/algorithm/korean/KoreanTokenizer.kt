package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语轻量级离线分词与助词词尾逆向剥离引擎（KoreanTokenizer）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心功能：
 * 1. 接收韩文整句，按空格和标点断句并生成 SentenceToken。
 * 2. 剥离 20+ 种延世高频附着助词（이/가, 은/는, 을/를, 에, 에서, (으)로, 와/과 等）。
 * 3. 谓词活用形逆向推导候选原形（如 미끄럽습니다 -> 미끄럽다, 걸어요 -> 걷다, 추워요 -> 춥다）。
 */
object KoreanTokenizer {

    /** 延世高频助词表（按长度降序排列，优先匹配长助词） */
    private val JOSA_LIST = listOf(
        "에서는", "에게서", "한테서", "으로부터", "에게는", "한테는",
        "에서", "에게", "한테", "으로", "로", "부터", "까지",
        "이나", "이나마", "이랑", "하고",
        "은", "는", "이", "가", "을", "를", "의", "와", "과", "도", "만", "요"
    )

    /** 常见终结词尾前缀（用于动词/形容词词干还原） */
    private val VERB_ENDING_SUFFIXES = listOf(
        "습니다", "ㅂ니다", "습니까", "ㅂ니까", "십시오", "으세요", "세요",
        "았어요", "었어요", "였어요", "았어", "었어", "였어",
        "어요", "아요", "여요",
        "겠어요", "겠어", "겠습니다",
        "는데요", "은데요", "ㄴ데요", "는데", "은데", "ㄴ데",
        "잖아요", "잖아", "군요", "구나", "네요"
    )

    /**
     * 将输入句子拆分为可交互的 Token 序列
     *
     * @param sentence 完整例句（如 "눈이 오기 때문에 길이 미끄럽습니다."）
     */
    fun tokenize(sentence: String): List<SentenceToken> {
        if (sentence.isBlank()) return emptyList()

        // 按空白字符切分词块（保持标点定位）
        val rawChunks = sentence.trim().split(Regex("\\s+"))

        return rawChunks.mapIndexed { index, raw ->
            val clean = raw.replace(Regex("[^가-힣a-zA-Z0-9]"), "")
            val candidates = generateCandidateStems(clean)

            SentenceToken(
                index = index,
                rawText = raw,
                cleanWord = clean,
                candidates = candidates
            )
        }
    }

    /**
     * 针对单个韩文单词生成词干与原形候选列表（用于 Room 数据库精确匹配）
     */
    fun generateCandidateStems(word: String): List<String> {
        if (word.isBlank()) return emptyList()

        val candidates = LinkedHashSet<String>()
        candidates.add(word) // 1. 原词本身（如 "학생", "학교"）

        // 2. 剥离助词（Josa Stripping）
        for (josa in JOSA_LIST) {
            if (word.endsWith(josa) && word.length > josa.length) {
                val stem = word.removeSuffix(josa)
                candidates.add(stem)
                // 词干加 '다'（如 "오기" -> "오다"）
                if (stem.endsWith("기")) {
                    candidates.add(stem.removeSuffix("기") + "다")
                }
            }
        }

        // 3. 谓词终结词尾逆向还原（Verb Stem Normalization）
        for (ending in VERB_ENDING_SUFFIXES) {
            if (word.endsWith(ending) && word.length >= ending.length) {
                val stem = word.removeSuffix(ending)
                if (stem.isNotEmpty()) {
                    // 标准原形：stem + "다"
                    candidates.add(stem + "다")

                    val lastChar = stem.last()
                    val dec = HangulJamo.decompose(lastChar)
                    val prefix = stem.dropLast(1)

                    // 逆向 ㅂ 不规则：如 "추워" -> "춥다", "아름다워" -> "아름답다", "도와" -> "돕다"
                    if (lastChar == '워' || lastChar == '와') {
                        if (lastChar == '와' && prefix == "도") candidates.add("돕다")
                        else if (lastChar == '와' && prefix == "고") candidates.add("곱다")
                        else if (prefix.isNotEmpty()) {
                            val newPrev = HangulJamo.setJongseong(prefix.last(), 'ㅂ')
                            candidates.add(prefix.dropLast(1) + newPrev + "다")
                        }
                    }

                    // 逆向 ㄷ 不规则：如 "걸어" -> "걷다", "들어" -> "듣다"
                    if (dec.jong == 'ㄹ') {
                        val dChar = HangulJamo.setJongseong(lastChar, 'ㄷ')
                        candidates.add(prefix + dChar + "다")
                    }

                    // 逆向 르 不规则：如 "몰라" -> "모르다", "빨라" -> "빠르다", "불러" -> "부르다"
                    if (prefix.isNotEmpty() && prefix.last().let { HangulJamo.decompose(it).jong == 'ㄹ' }) {
                        val prevWithoutL = HangulJamo.removeJongseong(prefix.last())
                        candidates.add(prefix.dropLast(1) + prevWithoutL + "르다")
                    }
                }
            }
        }

        // 4. 特例：直接加 "다"（如 "미끄럽" -> "미끄럽다"）
        if (!word.endsWith("다")) {
            candidates.add(word + "다")
        }

        return candidates.toList()
    }
}

/**
 * 分词 Token 结构体
 */
data class SentenceToken(
    val index      : Int,               // 单词在句子中的索引序号
    val rawText    : String,            // 原始文本（含空格或标点，如 "길이"）
    val cleanWord  : String,            // 纯净韩文字符串（如 "길"）
    val candidates : List<String>       // 候选原形词条（如 ["길이", "길", "길다"]）
)
