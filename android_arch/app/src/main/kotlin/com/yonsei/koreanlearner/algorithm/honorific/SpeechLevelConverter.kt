package com.yonsei.koreanlearner.algorithm.honorific

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语 6 大阶称与主客体尊谦语转换引擎 (SpeechLevelConverter)
 * ══════════════════════════════════════════════════════════════════
 */

enum class SpeechLevel(val code: String, val titleKr: String, val titleCn: String, val usageContext: String) {
    HASIPSISHO("하십시오체", "하십시오체", "格式体最高敬语", "新闻播报、庄重职场、初次见面、演讲报告"),
    HAEYO("해요체", "해요체", "非格式体日常敬语", "日常交流、朋友长辈、最通用礼貌语气"),
    HAGE("하게체", "하게체", "权威长辈体", "老一辈对成年晚辈、岳父对女婿、平辈老友"),
    HAO("하오체", "하오체", "复古庄字体", "书信、传统古装剧、网络自嘲特色语气"),
    HAERA("해라체", "해라체", "书面客观基本阶", "报纸书籍、学术论文、日记、无对话对象陈述"),
    HAE("해체", "해체", "亲密平语半语", "同龄好友、晚辈、极度亲近的家人")
}

data class HonorificWordPair(
    val plain: String,
    val honorific: String,
    val category: String,
    val chineseMeaning: String
)

data class SpeechLevelResult(
    val level: SpeechLevel,
    val sentenceText: String,
    val endingParticle: String,
    val politenessScore: Int // 1 ~ 6
)

object SpeechLevelConverter {

    // 核心尊谦词汇对照表
    val HONORIFIC_VOCABULARY = listOf(
        HonorificWordPair("밥", "진지", "名词", "饭 / 用餐"),
        HonorificWordPair("나이", "연세", "名词", "年龄 / 岁数"),
        HonorificWordPair("이름", "성함", "名词", "名字 / 尊姓大名"),
        HonorificWordPair("집", "댁", "名词", "家 / 府上"),
        HonorificWordPair("사람/명", "분", "量词", "人 / 位"),
        HonorificWordPair("생일", "생신", "名词", "生日 / 寿辰"),
        HonorificWordPair("말", "말씀", "名词", "话 / 尊言、赐教"),
        HonorificWordPair("있다", "계시다", "动词", "在（主语敬语）"),
        HonorificWordPair("자다", "주무시다", "动词", "睡觉 / 就寝"),
        HonorificWordPair("먹다", "드시다 / 잡수시다", "动词", "吃 / 用膳"),
        HonorificWordPair("마시다", "드시다", "动词", "喝 / 饮用"),
        HonorificWordPair("죽다", "돌아가시다", "动词", "去世 / 仙逝"),
        HonorificWordPair("아프다", "편찮으시다", "形容词", "身体不适 / 抱恙"),
        HonorificWordPair("주다", "드리다 (谦) / 주시다 (尊)", "动词", "给 / 呈奉 (谦语)"),
        HonorificWordPair("보다/만나다", "뵙다 / 뵈다", "动词", "拜见 / 谒见 (谦语)"),
        HonorificWordPair("물어보다", "여쭙다 / 여쭤보다", "动词", "请教 / 询问 (谦语)")
    )

    // 助词尊谦映射
    val HONORIFIC_PARTICLES = listOf(
        Triple("이/가 (主格)", "께서 (主格尊称)", "선생님께서 오십니다."),
        Triple("은/는 (主题)", "께서는 (主题尊称)", "아버지께서는 회사에 가셨습니다."),
        Triple("에게/한테 (与格)", "께 (敬称与格)", "교수님께 선물을 드렸습니다.")
    )

    // 预置句子多阶称转换样本
    private val PRESET_SENTENCES = listOf(
        "밥을 먹다" to mapOf(
            SpeechLevel.HASIPSISHO to "진지를 드십니다",
            SpeechLevel.HAEYO to "진지를 드세요",
            SpeechLevel.HAGE to "밥을 먹게",
            SpeechLevel.HAO to "밥을 먹으오",
            SpeechLevel.HAERA to "밥을 먹는다",
            SpeechLevel.HAE to "밥 먹어"
        ),
        "한국어를 공부하다" to mapOf(
            SpeechLevel.HASIPSISHO to "한국어를 공부합니다",
            SpeechLevel.HAEYO to "한국어를 공부해요",
            SpeechLevel.HAGE to "한국어를 공부하게",
            SpeechLevel.HAO to "한국어를 공부하오",
            SpeechLevel.HAERA to "한국어를 공부한다",
            SpeechLevel.HAE to "한국어 공부해"
        ),
        "집에 가다" to mapOf(
            SpeechLevel.HASIPSISHO to "댁에 가십니다",
            SpeechLevel.HAEYO to "댁에 가세요",
            SpeechLevel.HAGE to "집에 가게",
            SpeechLevel.HAO to "집에 가오",
            SpeechLevel.HAERA to "집에 간다",
            SpeechLevel.HAE to "집에 가"
        ),
        "선생님을 만나다" to mapOf(
            SpeechLevel.HASIPSISHO to "선생님을 뵙습니다",
            SpeechLevel.HAEYO to "선생님을 봬요",
            SpeechLevel.HAGE to "선생을 만나게",
            SpeechLevel.HAO to "선생을 만나오",
            SpeechLevel.HAERA to "선생님을 만난다",
            SpeechLevel.HAE to "선생님 만나"
        )
    )

    /**
     * 将输入短语转换为 6 大阶称形式
     */
    fun convertAllLevels(input: String): List<SpeechLevelResult> {
        val trimmed = input.trim()
        val match = PRESET_SENTENCES.find { it.first == trimmed }

        if (match != null) {
            return SpeechLevel.entries.map { level ->
                SpeechLevelResult(
                    level = level,
                    sentenceText = match.second[level] ?: trimmed,
                    endingParticle = when (level) {
                        SpeechLevel.HASIPSISHO -> "-ㅂ니다/습니다"
                        SpeechLevel.HAEYO -> "-아/어요"
                        SpeechLevel.HAGE -> "-게/네"
                        SpeechLevel.HAO -> "-(으)오"
                        SpeechLevel.HAERA -> "-ㄴ/는다"
                        SpeechLevel.HAE -> "-아/어"
                    },
                    politenessScore = when (level) {
                        SpeechLevel.HASIPSISHO -> 6
                        SpeechLevel.HAEYO -> 5
                        SpeechLevel.HAGE -> 3
                        SpeechLevel.HAO -> 4
                        SpeechLevel.HAERA -> 2
                        SpeechLevel.HAE -> 1
                    }
                )
            }
        }

        // 默认通用变形
        return SpeechLevel.entries.map { level ->
            SpeechLevelResult(
                level = level,
                sentenceText = when (level) {
                    SpeechLevel.HASIPSISHO -> "$trimmed 합니다"
                    SpeechLevel.HAEYO -> "$trimmed 해요"
                    SpeechLevel.HAGE -> "$trimmed 하게"
                    SpeechLevel.HAO -> "$trimmed 하오"
                    SpeechLevel.HAERA -> "$trimmed 한다"
                    SpeechLevel.HAE -> "$trimmed 해"
                },
                endingParticle = level.titleKr,
                politenessScore = when (level) {
                    SpeechLevel.HASIPSISHO -> 6
                    SpeechLevel.HAEYO -> 5
                    SpeechLevel.HAGE -> 3
                    SpeechLevel.HAO -> 4
                    SpeechLevel.HAERA -> 2
                    SpeechLevel.HAE -> 1
                }
            )
        }
    }

    val SAMPLE_PRESETS = PRESET_SENTENCES.map { it.first }
}
