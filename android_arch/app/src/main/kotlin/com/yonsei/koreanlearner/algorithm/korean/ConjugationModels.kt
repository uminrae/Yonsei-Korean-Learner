package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语谓词变音与变形模型定义
 * ══════════════════════════════════════════════════════════════════
 */

/**
 * 延世韩语 7 大不规则变音类型
 */
enum class IrregularType(val displayName: String, val ruleSummary: String) {
    /** ㄷ 不规则：遇到元音时 ㄷ 变 ㄹ（如 걷다 -> 걸어요, 듣다 -> 들어요） */
    D_IRREGULAR("ㄷ 不规则", "词干末尾收音 ㄷ 遇到元音开头的词尾时变为 ㄹ"),

    /** ㅂ 不规则：遇到元音时 ㅂ 变 우/와（如 돕다 -> 도와요, 눕다 -> 누워요, 춥다 -> 추워요） */
    B_IRREGULAR("ㅂ 不规则", "词干末尾收音 ㅂ 遇到元音开头的词尾时脱落并转为 우（单音节돕/곱转为 와）"),

    /** ㅅ 不规则：遇到元音时 ㅅ 脱落，元音不缩合（如 낫다 -> 나아요, 짓다 -> 지어요, 붓다 -> 부어요） */
    S_IRREGULAR("ㅅ 不规则", "词干末尾收音 ㅅ 遇到元音开头的词尾时脱落，且前后元音保持独立不缩合"),

    /** 르 不规则：遇到 -아/어 时 ㅡ 脱落且前一音节加 ㄹ 收音（如 모르다 -> 몰라요, 빠르다 -> 빨라요, 부르다 -> 불러요） */
    REU_IRREGULAR("르 不规则", "词干末音节为 르 时，遇到 -아/어 词尾时 ㅡ 脱落，前一音节添加 ㄹ 收音，根据前字元音接 라/러"),

    /** 으 不规则（으 脱落）：遇到元音词尾时 ㅡ 脱落（如 크다 -> 커요, 아프다 -> 아파요, 쓰다 -> 써요, 바쁘다 -> 바빠요） */
    EU_DROPPING("으 脱落", "词干末尾元音为 ㅡ 时，遇到元音词尾 ㅡ 脱落，结合前一音节的元音阴阳性决定接 아/어"),

    /** ㅎ 不规则：遇到元音词尾时 ㅎ 脱落且元音融合为 애/얘（如 그렇다 -> 그래요, 까맣다 -> 까매요, 하얗다 -> 하얘요） */
    H_IRREGULAR("ㅎ 不规则", "形容词词干末收音 ㅎ 遇到元音时脱落，与后接元音融合为 애/얘；遇到 (으) 时 ㅎ 与 으 同时脱落"),

    /** ㄹ 脱落：词干末收音为 ㄹ 时，遇到 ㄴ, ㅂ, ㅅ, (으) 词尾时 ㄹ 脱落（如 살다 -> 삽니다, 사세요, 사니, 사는） */
    L_DROPPING("ㄹ 脱落", "词干末收音为 ㄹ 时，遇到以 ㄴ, ㅂ, ㅅ 开头的词尾或 -(으) 词尾时，ㄹ 收音脱落且不添加 으"),

    /** 规则变化（Regular） */
    REGULAR("规则变形", "遵循韩语标准音变与规则接续");

    companion object {
        fun fromLabel(label: String?): IrregularType {
            if (label == null) return REGULAR
            return when {
                label.contains("ㄷ") -> D_IRREGULAR
                label.contains("ㅂ") -> B_IRREGULAR
                label.contains("ㅅ") -> S_IRREGULAR
                label.contains("르") -> REU_IRREGULAR
                label.contains("으") -> EU_DROPPING
                label.contains("ㅎ") -> H_IRREGULAR
                label.contains("ㄹ") -> L_DROPPING
                else -> REGULAR
            }
        }
    }
}

/**
 * 常见词尾语法类型分类（用于精准匹配音变路径）
 */
enum class EndingPatternType {
    /** 1. -아/어 系列：如 -아/어요, -았/었어요, -아/어서, -아/어야 하다 */
    A_EO_STYLE,

    /** 2. -(으) 系列：如 -(으)면, -(으)니까, -(으)려고, -(으)면서, -(으)ㄹ 거예요 */
    EU_STYLE,

    /** 3. -(으)ㅂ/습 系列：如 -(으)ㅂ니다/습니다, -(으)ㅂ시다/읍시다 */
    FORMAL_STYLE,

    /** 4. -(으)시/세 系列：如 -(으)세요, -(으)십시오, -(으)시면 */
    HONORIFIC_STYLE,

    /** 5. 辅音 ㄴ/ㄹ 直接接续系列：如 -는 (定语), -니, -냐 */
    N_CONSONANT_STYLE,

    /** 6. 无音变直接接续系列：如 -고, -지만, -지 않다, -게, -기 때문에 */
    DIRECT_STYLE
}

/**
 * 谓词变形计算结果输出数据类
 */
data class ConjugationResult(
    /** 变形后的韩文结果（如 "걸어요", "도와요", "몰라요"） */
    val conjugatedText     : String,

    /** 原型原词（如 "걷다", "돕다", "모르다"） */
    val baseVerb           : String,

    /** 附加的词尾原型（如 "-아/어요", "-(으)면", "-(으)ㅂ니다"） */
    val endingPattern      : String,

    /** 实际应用的变音规则类型 */
    val appliedIrregular   : IrregularType,

    /** 音标发音与连音同化提示（如 "[거러요]", "[도와요]"） */
    val pronunciationGuide : String? = null,

    /** 详细中文规则解析（教学用途） */
    val ruleExplanation    : String,

    /** 是否为特例排除词（如 닫다/받다 属 ㄷ 规则，입다 属 ㅂ 规则） */
    val isExceptionRegular : Boolean = false
)
