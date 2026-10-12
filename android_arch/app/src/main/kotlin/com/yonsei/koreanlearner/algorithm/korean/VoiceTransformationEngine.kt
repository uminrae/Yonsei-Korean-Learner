package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语法语态（Voice）与句法分析重构引擎 (VoiceTransformationEngine)
 * ══════════════════════════════════════════════════════════════════
 */

enum class GrammaticalVoice(val displayName: String, val koreanName: String) {
    ACTIVE("主动态 (Active)", "능동 / 주동"),
    PASSIVE("被动态 (Passive)", "피동"),
    CAUSATIVE("使动态 (Causative)", "사동")
}

enum class AffixCategory(val affixChar: String, val description: String) {
    I("이", "以 vowel 或特定辅音结尾词根派生"),
    HI("히", "以 ㄱ, ㄷ, ㅂ 等塞音结尾词根派生"),
    RI("리", "以 ㄹ 结尾或 ㄷ不规则词根派生"),
    GI("기", "以 ㄴ, ㅁ, ㅈ, ㅊ 结尾词根派生"),
    WOO_CHOO("우/구/추", "元音变形或特殊使动词缀派生"),
    COMPOUND("규칙 파생", "-아/어지다 / -게 하다 / -시키다 派生")
}

/** 词汇语态映射字典条目 */
data class VerbVoiceEntry(
    val baseVerb       : String,            // 主动态原形（如 "잡다", "입다", "열다", "자다"）
    val passiveVerb    : String?,           // 短被动形（如 "잡히다", "열리다"）
    val causativeVerb  : String?,           // 短使动形（如 "입히다", "재우다"）
    val affix          : AffixCategory,     // 词缀类型
    val chineseMeaning : String             // 基础语义
)

/** 句子成分拆解与语态转换结果 */
data class SentenceVoiceTransformation(
    val activeSentence    : String,         // 主动态例句（如 "경찰이 도둑을 잡았습니다."）
    val passiveSentence   : String?,        // 被动态例句（如 "도둑이 경찰에게 잡혔습니다."）
    val causativeSentence : String?,        // 使动态例句（如 "엄마가 아이에게 옷을 입힙니다."）
    val agent             : String,         // 施动者（如 "경찰", "엄마"）
    val patient           : String,         // 受动者 / 承受者（如 "도둑", "아이"）
    val targetObject      : String? = null, // 二级宾语（如 "옷", "책"）
    val baseVerb          : String,         // 原形动词
    val explanation       : String          // 句法格助词与语态变形解析
)

object VoiceTransformationEngine {

    // ── 1. 被动与使动词根变化核心字典 ─────────────────────────
    val VOICE_DICTIONARY = listOf(
        // 이 词缀
        VerbVoiceEntry("보다", "보이다", "보이다", AffixCategory.I, "看 / 被看见 / 给出展示"),
        VerbVoiceEntry("쓰다", "쓰이다", "씌우다", AffixCategory.I, "写 / 被使用 / 给戴上"),
        VerbVoiceEntry("끓다", null, "끓이다", AffixCategory.I, "沸腾 / 使沸腾(烧开)"),
        VerbVoiceEntry("줄다", null, "줄이다", AffixCategory.I, "减少 / 使减少(缩减)"),
        VerbVoiceEntry("녹다", "녹이다", "녹이다", AffixCategory.I, "融化 / 使融化"),

        // 히 词缀
        VerbVoiceEntry("닫다", "닫히다", null, AffixCategory.HI, "关 / 被关上"),
        VerbVoiceEntry("읽다", "읽히다", "읽히다", AffixCategory.HI, "读 / 被读 / 让人读"),
        VerbVoiceEntry("입다", null, "입히다", AffixCategory.HI, "穿 / 给穿上"),
        VerbVoiceEntry("눕다", null, "눕히다", AffixCategory.HI, "躺 / 使躺下"),
        VerbVoiceEntry("잡다", "잡히다", "잡히다", AffixCategory.HI, "抓 / 被抓获 / 使抓住"),
        VerbVoiceEntry("먹다", "먹히다", "먹이다", AffixCategory.HI, "吃 / 被吃 / 喂食"),

        // 리 词缀
        VerbVoiceEntry("열다", "열리다", null, AffixCategory.RI, "开 / 被打开"),
        VerbVoiceEntry("듣다", "들리다", "들리다", AffixCategory.RI, "听 / 能听见 / 播放让听"),
        VerbVoiceEntry("알다", null, "알리다", AffixCategory.RI, "知道 / 告知(通知)"),
        VerbVoiceEntry("살다", null, "살리다", AffixCategory.RI, "活 / 救活(振兴)"),
        VerbVoiceEntry("팔다", "팔리다", null, AffixCategory.RI, "卖 / 被售出(畅销)"),

        // 기 词缀
        VerbVoiceEntry("안다", "안기다", "안기다", AffixCategory.GI, "抱 / 被拥抱 / 让抱"),
        VerbVoiceEntry("씻다", "씻기다", "씻기다", AffixCategory.GI, "洗 / 被洗净 / 给洗澡"),
        VerbVoiceEntry("웃다", null, "웃기다", AffixCategory.GI, "笑 / 逗笑(搞笑)"),
        VerbVoiceEntry("맡다", null, "맡기다", AffixCategory.GI, "保管/闻 / 委托交付"),
        VerbVoiceEntry("벗다", "벗기다", "벗기다", AffixCategory.GI, "脱 / 被脱掉 / 给脱下"),

        // 우 / 추 词缀
        VerbVoiceEntry("자다", null, "재우다", AffixCategory.WOO_CHOO, "睡 / 哄睡"),
        VerbVoiceEntry("타다", null, "태우다", AffixCategory.WOO_CHOO, "乘车/烧 / 载客/点燃"),
        VerbVoiceEntry("깨다", null, "깨우다", AffixCategory.WOO_CHOO, "醒 / 叫醒"),
        VerbVoiceEntry("낮다", null, "낮추다", AffixCategory.WOO_CHOO, "低 / 降低"),
        VerbVoiceEntry("늦다", null, "늦추다", AffixCategory.WOO_CHOO, "迟/慢 / 推迟延后")
    )

    // ── 2. 预置典型句式语态转换案例库 ─────────────────────────
    val SAMPLE_TRANSFORMATIONS = listOf(
        // 案例 1：警察抓小偷（主动态 <-> 被动态）
        SentenceVoiceTransformation(
            activeSentence = "경찰이 도둑을 잡았습니다.",
            passiveSentence = "도둑이 경찰에게 잡혔습니다.",
            causativeSentence = null,
            agent = "경찰 (警察)",
            patient = "도둑 (小偷)",
            baseVerb = "잡다 (抓)",
            explanation = "【主动态 -> 被动态转换】：原宾语 '도둑' 升级为主格 '도둑이'，原主语施动者 '경찰' 降格为行为主体助词 '경찰에게'，动词 '잡다' 派生短被动 '잡히다'。"
        ),
        // 案例 2：风吹关门（主动态 <-> 被动态，无生命施动者用 -에）
        SentenceVoiceTransformation(
            activeSentence = "바람이 문을 닫았습니다.",
            passiveSentence = "문이 바람에 닫혔습니다.",
            causativeSentence = null,
            agent = "바람 (风)",
            patient = "문 (门)",
            baseVerb = "닫다 (关)",
            explanation = "【无生命施动者被动】：当施动者为无生命自然力（如 바람 风、비 雨）时，助词使用 '-에'（바람에），动词 '닫다' 派生为 '닫히다'。"
        ),
        // 案例 3：妈妈给孩子穿衣服（主动态 <-> 使动态，有宾语他动词）
        SentenceVoiceTransformation(
            activeSentence = "아이가 옷을 입습니다.",
            passiveSentence = null,
            causativeSentence = "엄마가 아이에게 옷을 입힙니다.",
            agent = "엄마 (妈妈)",
            patient = "아이 (孩子)",
            targetObject = "옷 (衣服)",
            baseVerb = "입다 (穿)",
            explanation = "【有宾语使动态转换】：使动主体 '엄마' 作为新主语(엄마가)，原主语行为主体 '아이' 转换为间接宾语(아이에게)，原宾语 '옷을' 维持不变，动词派生使动 '입히다'。"
        ),
        // 案例 4：妈妈哄宝宝睡觉（主动态 <-> 使动态，无宾语自动词）
        SentenceVoiceTransformation(
            activeSentence = "아기가 잡니다.",
            passiveSentence = null,
            causativeSentence = "엄마가 아기를 재웁니다.",
            agent = "엄마 (妈妈)",
            patient = "아기 (宝宝)",
            baseVerb = "자다 (睡)",
            explanation = "【自动词使动态转换】：原主语 '아기' 直接转换为直接宾语 '아기를'，使动词汇发生元音变形派生为 '재우다'。"
        ),
        // 案例 5：门被打开（主动态 <-> 被动态）
        SentenceVoiceTransformation(
            activeSentence = "누군가가 문을 열었습니다.",
            passiveSentence = "문이 활짝 열렸습니다.",
            causativeSentence = null,
            agent = "누군가 (某人)",
            patient = "문 (门)",
            baseVerb = "열다 (开)",
            explanation = "【状态被动态】：强调动作完成后的自然敞开状态，'열다' 派生 '열리다'。"
        )
    )

    /**
     * 查询指定动词原形的被动与使动形态
     */
    fun findVoiceEntry(verbStem: String): VerbVoiceEntry? {
        val clean = if (verbStem.endsWith("다")) verbStem else "${verbStem}다"
        return VOICE_DICTIONARY.firstOrNull { it.baseVerb == clean }
    }

    /**
     * 根据主动态动词派生规则长被动 (-아/어지다) 与 长使动 (-게 하다)
     */
    fun deriveCompoundVoices(verb: String): Pair<String, String> {
        val stem = verb.removeSuffix("다")
        val longPassive = "${stem}아/어지다"
        val longCausative = "${stem}게 하다"
        return Pair(longPassive, longCausative)
    }
}
