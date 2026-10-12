package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文字符底层 Unicode 拆解与合成引擎（HangulJamo）
 * ══════════════════════════════════════════════════════════════════
 *
 * Unicode 算法公式：
 *   S = 0xAC00 + (初声 × 21 + 中声) × 28 + 终声
 *
 * 反向拆解公式：
 *   code = S - 0xAC00
 *   终声 (jong) = code % 28
 *   中声 (jung) = (code / 28) % 21
 *   初声 (cho)  = (code / 28) / 21
 */
object HangulJamo {

    const val HANGUL_BASE = 0xAC00
    const val HANGUL_END  = 0xD7A3

    // 19 个初声 (Choseong)
    val CHOSEONG = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    // 21 个中声 (Jungseong)
    val JUNGSEONG = charArrayOf(
        'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
        'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    )

    // 28 个终声 (Jongseong，首位为 0 表示无收音)
    val JONGSEONG = charArrayOf(
        '\u0000', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
        'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    /**
     * 判断单个字符是否为完整的韩文音节 (가 ~ 힣)
     */
    fun isHangulSyllable(c: Char): Boolean = c.code in HANGUL_BASE..HANGUL_END

    /**
     * 拆解韩文音节为 (初声, 中声, 终声) 结构体
     */
    fun decompose(c: Char): DecomposedHangul {
        if (!isHangulSyllable(c)) {
            return DecomposedHangul(c, '\u0000', '\u0000', isHangul = false)
        }
        val code = c.code - HANGUL_BASE
        val jongIdx = code % 28
        val jungIdx = (code / 28) % 21
        val choIdx  = (code / 28) / 21

        return DecomposedHangul(
            cho = CHOSEONG[choIdx],
            jung = JUNGSEONG[jungIdx],
            jong = JONGSEONG[jongIdx],
            isHangul = true,
            choIndex = choIdx,
            jungIndex = jungIdx,
            jongIndex = jongIdx
        )
    }

    /**
     * 将 (初声, 中声, 终声) 组装为一个完整的韩文音节字符（带安全边界防护）
     */
    fun compose(cho: Char, jung: Char, jong: Char = '\u0000'): Char {
        val choIdx = CHOSEONG.indexOf(cho)
        val jungIdx = JUNGSEONG.indexOf(jung)
        val jongIdx = if (jong == '\u0000') 0 else JONGSEONG.indexOf(jong)

        if (choIdx == -1 || jungIdx == -1 || jongIdx == -1) {
            // 安全防崩兜底：若不是标准韩文字母，返回原字符
            return cho
        }

        val code = HANGUL_BASE + (choIdx * 21 + jungIdx) * 28 + jongIdx
        return code.toChar()
    }

    /**
     * 通过索引快速合成音节
     */
    fun composeByIndex(choIdx: Int, jungIdx: Int, jongIdx: Int = 0): Char {
        require(choIdx in 0..18 && jungIdx in 0..20 && jongIdx in 0..27) {
            "索引越界: cho=$choIdx, jung=$jungIdx, jong=$jongIdx"
        }
        val code = HANGUL_BASE + (choIdx * 21 + jungIdx) * 28 + jongIdx
        return code.toChar()
    }

    /**
     * 判断是否具有收音（终声）
     */
    fun hasJongseong(c: Char): Boolean {
        if (!isHangulSyllable(c)) return false
        val code = c.code - HANGUL_BASE
        return (code % 28) != 0
    }

    /**
     * 判断中声元音是否属于阳性元音 (ㅏ, ㅗ, ㅑ, ㅛ, ㅘ, ㅚ, ㅙ)
     * 在延世韩语中，词干末尾包含阳性元音时通常接 -아 / -아서 / -았
     */
    fun isYangVowel(jung: Char): Boolean = when (jung) {
        'ㅏ', 'ㅗ', 'ㅑ', 'ㅛ', 'ㅘ', 'ㅚ', 'ㅙ' -> true
        else -> false
    }

    /**
     * 替换韩文字符的终声（收音）
     */
    fun setJongseong(c: Char, newJong: Char): Char {
        val dec = decompose(c)
        if (!dec.isHangul) return c
        return compose(dec.cho, dec.jung, newJong)
    }

    /**
     * 移除韩文字符的收音（变为无收音音节）
     */
    fun removeJongseong(c: Char): Char = setJongseong(c, '\u0000')

    /**
     * 替换中声元音
     */
    fun setJungseong(c: Char, newJung: Char): Char {
        val dec = decompose(c)
        if (!dec.isHangul) return c
        return compose(dec.cho, newJung, dec.jong)
    }
}

/**
 * 拆解后的音节结构体
 */
data class DecomposedHangul(
    val cho       : Char,
    val jung      : Char,
    val jong      : Char,
    val isHangul  : Boolean = true,
    val choIndex  : Int = 0,
    val jungIndex : Int = 0,
    val jongIndex : Int = 0
) {
    val hasJongseong: Boolean get() = jong != '\u0000'
    val isYangVowel: Boolean get() = HangulJamo.isYangVowel(jung)
}
