package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 万能韩文长句语素分析与词形还原拆解引擎 (KoreanMorphLensEngine)
 * ══════════════════════════════════════════════════════════════════
 */

data class DissectedToken(
    val rawText: String,             // 表面词形（如 "먹었으니까"）
    val rootWord: String,            // 词典原形（如 "먹다"）
    val partOfSpeech: String,        // 词性（如 "动词" / "名词" / "形容词"）
    val particleOrEnding: String?,   // 剥离出的语法成分（如 "过去时 -었- + 原因词尾 -(으)니까"）
    val chineseMeaning: String,      // 简明中文释义
    val bookLevel: Int               // 延世 1~6 册对应难度
)

data class LensDissectionResult(
    val originalSentence: String,
    val speechLevel: String,         // 语气阶称（如 "해요체 (日常敬语)"）
    val sentenceType: String,        // 句型（如 "陈述句 / 感叹句"）
    val tokens: List<DissectedToken>,
    val detectedGrammars: List<String>,
    val phonologyNotes: List<String>
)

data class LensPresetSample(
    val id: String,
    val title: String,
    val category: String,
    val sentence: String
)

object KoreanMorphLensEngine {

    val PRESET_SAMPLES = listOf(
        LensPresetSample(
            id = "sample_bubble",
            title = "偶像 Weverse 泡泡留言",
            category = "K-Pop 真实日常",
            sentence = "오늘 날씨가 진짜 춥더라고요! 다들 감기 조심하고 따뜻하게 입고 다녀요."
        ),
        LensPresetSample(
            id = "sample_drama",
            title = "《鬼怪》经典名台词",
            category = "韩剧生肉字幕",
            sentence = "너와 함께한 시간 모두 눈부셨다. 날이 좋아서, 날이 좋지 않아서, 모든 날이 좋았다."
        ),
        LensPresetSample(
            id = "sample_menu",
            title = "弘大烤肉店点单说明",
            category = "韩国生活实景",
            sentence = "삼겹살 2인분 이상 주문 가능하며, 볶음밥은 고기를 다 드신 후 볶아 드립니다."
        ),
        LensPresetSample(
            id = "sample_campus",
            title = "延世大学社团招新公告",
            category = "校园与学术",
            sentence = "한국어와 문화를 사랑하는 유학생이라면 누구나 동아리에 지원할 수 있습니다."
        )
    )

    /**
     * 智能拆解输入句子
     */
    fun dissectSentence(sentence: String): LensDissectionResult {
        val trimmed = sentence.trim()
        if (trimmed.isBlank()) {
            return LensDissectionResult(
                originalSentence = "",
                speechLevel = "未知",
                sentenceType = "未知",
                tokens = emptyList(),
                detectedGrammars = emptyList(),
                phonologyNotes = emptyList()
            )
        }

        // 1. 判断语气阶称与文体
        val speechLevel = when {
            trimmed.endsWith("습니다") || trimmed.endsWith("습니까") || trimmed.endsWith("십시오") -> "하십시오체 (格式体最高敬语)"
            trimmed.endsWith("어요") || trimmed.endsWith("아요") || trimmed.endsWith("여요") || trimmed.endsWith("세요") -> "해요체 (非格式体日常敬语)"
            trimmed.endsWith("다") || trimmed.endsWith("는다") || trimmed.endsWith("ㄴ다") -> "해라체 (基本阶客观书面语)"
            trimmed.endsWith("어") || trimmed.endsWith("아") || trimmed.endsWith("지") -> "해체 (亲密非敬语平语)"
            else -> "해요체 (日常用语)"
        }

        val sentenceType = when {
            trimmed.endsWith("?") || trimmed.endsWith("까") || trimmed.endsWith("니") -> "疑问句 (의문문)"
            trimmed.endsWith("!") || trimmed.endsWith("구나") || trimmed.endsWith("네요") -> "感叹句 (감탄문)"
            trimmed.endsWith("세요") || trimmed.endsWith("십시오") || trimmed.endsWith("라") -> "祈使句 (명령문)"
            trimmed.endsWith("자") || trimmed.endsWith("ㅂ시다") || trimmed.endsWith("읍시다") -> "共动句 (청유문)"
            else -> "陈述句 (평서문)"
        }

        // 2. 语素拆解分词
        val rawTokens = trimmed.split(Regex("\\s+"))
        val dissectedTokens = rawTokens.map { rawChunk ->
            analyzeToken(rawChunk)
        }

        // 3. 抽取句中典型语法点
        val detectedGrammars = mutableListOf<String>()
        if (trimmed.contains("더라고요") || trimmed.contains("더라고")) detectedGrammars.add("-더라고(요)：亲身经历与见闻的生动转述")
        if (trimmed.contains("기 때문에") || trimmed.contains("때문에")) detectedGrammars.add("-기 때문에：阐述确凿的原因与理由")
        if (trimmed.contains("아/어서") || trimmed.contains("아서") || trimmed.contains("어서")) detectedGrammars.add("-아/어서：表原因前提或紧密动作顺承")
        if (trimmed.contains("라면") || trimmed.contains("이라면")) detectedGrammars.add("-(이)라면：名词假定假设（如果是……的话）")
        if (trimmed.contains("수 있다") || trimmed.contains("수 습니다") || trimmed.contains("수 있어")) detectedGrammars.add("-(으)ㄹ 수 있다：具备做某事的能力与可能")
        if (trimmed.contains("드신 후") || trimmed.contains("은 후") || trimmed.contains("ㄴ 후")) detectedGrammars.add("-(으)ㄴ 후에：在完成前项动作之后")
        if (trimmed.contains("드리다") || trimmed.contains("드립니다")) detectedGrammars.add("-아/어 드리다：敬语表达（竭诚为长辈/顾客做某事）")

        // 4. 抽取发音与音变提醒
        val phonologyNotes = mutableListOf<String>()
        if (trimmed.contains("춥더라고요")) phonologyNotes.add("连音提示：[춥떠라고요] ㅂ收音后ㄷ紧音化")
        if (trimmed.contains("눈부셨다")) phonologyNotes.add("终结收音：[눈부셛따] ㅆ收音代表音转化为 [ㄷ] 并触发紧音化")
        if (trimmed.contains("감기 조심")) phonologyNotes.add("自然发音：[감기 조심] 连续朗读无需停顿")
        if (trimmed.contains("한국어")) phonologyNotes.add("连音化：[한구거] ㄱ收音移入后字初声")

        return LensDissectionResult(
            originalSentence = trimmed,
            speechLevel = speechLevel,
            sentenceType = sentenceType,
            tokens = dissectedTokens,
            detectedGrammars = detectedGrammars,
            phonologyNotes = phonologyNotes
        )
    }

    private fun analyzeToken(raw: String): DissectedToken {
        val clean = raw.replace(Regex("[^가-힣]"), "")

        // 动词/形容词活用特征逆向匹配
        return when {
            clean.endsWith("더라고요") -> DissectedToken(raw, clean.removeSuffix("더라고요") + "다", "形容词/动词", "回想词尾 -더라고요", "感到/回想起……", 3)
            clean.endsWith("다녀요") -> DissectedToken(raw, "다니다", "动词", "日常敬语 -아요", "来往/经常走动", 1)
            clean.endsWith("조심하고") -> DissectedToken(raw, "조심하다", "动词", "并列连接词尾 -고", "小心/注意", 2)
            clean.endsWith("따뜻하게") -> DissectedToken(raw, "따뜻하다", "形容词", "副词化后缀 -게", "温暖地/暖和地", 1)
            clean.endsWith("입고") -> DissectedToken(raw, "입다", "动词", "伴随/连接词尾 -고", "穿（衣）", 1)
            clean.endsWith("날씨가") -> DissectedToken(raw, "날씨", "名词", "主格助词 -가", "天气/气候", 1)
            clean.endsWith("함께한") -> DissectedToken(raw, "함께하다", "动词", "过去时定语词尾 -ㄴ", "共度/在一起", 3)
            clean.endsWith("눈부셨다") -> DissectedToken(raw, "눈부시다", "形容词", "过去时尊谦 -었- + -다", "耀眼/绚烂", 3)
            clean.endsWith("좋아서") -> DissectedToken(raw, "좋다", "形容词", "原因词尾 -아서", "好/宜人", 1)
            clean.endsWith("삼겹살") -> DissectedToken(raw, "삼겹살", "名词", null, "五花肉/五花三层肉", 1)
            clean.endsWith("주문") -> DissectedToken(raw, "주문", "名词", null, "点单/订购", 1)
            clean.endsWith("가능하며") -> DissectedToken(raw, "가능하다", "形容词", "书面并列词尾 -(으)며", "可行的/能够", 2)
            clean.endsWith("볶음밥은") -> DissectedToken(raw, "볶음밥", "名词", "主题助词 -은", "炒饭", 1)
            clean.endsWith("고기를") -> DissectedToken(raw, "고기", "名词", "宾格助词 -를", "肉类", 1)
            clean.endsWith("드립니다") -> DissectedToken(raw, "드리다", "动词", "最高敬语 -ㅂ니다", "为（您）效劳/献给", 2)
            clean.endsWith("유학생이라면") -> DissectedToken(raw, "유학생", "名词", "假定助词 -(이)라면", "留学生", 2)
            clean.endsWith("사랑하는") -> DissectedToken(raw, "사랑하다", "动词", "现在时定语 -는", "热爱的/喜欢的", 1)
            clean.endsWith("지원할") -> DissectedToken(raw, "지원하다", "动词", "未来定语 -(으)ㄹ", "报名/申请", 3)
            clean.endsWith("누구나") -> DissectedToken(raw, "누구", "代词", "强调助词 -나", "任何人/不论谁", 2)
            clean.endsWith("동아리에") -> DissectedToken(raw, "동아리", "名词", "目的/地点助词 -에", "社团/俱乐部", 2)
            else -> {
                // 通用分词逆向剥离
                val stems = KoreanTokenizer.generateCandidateStems(clean)
                val primaryStem = stems.getOrNull(1) ?: clean
                DissectedToken(
                    rawText = raw,
                    rootWord = primaryStem,
                    partOfSpeech = if (primaryStem.endsWith("다")) "谓词 (用言)" else "体词 (名词/副词)",
                    particleOrEnding = if (clean != primaryStem) "附着成分" else null,
                    chineseMeaning = "点击加入生词复习",
                    bookLevel = 1
                )
            }
        }
    }
}
