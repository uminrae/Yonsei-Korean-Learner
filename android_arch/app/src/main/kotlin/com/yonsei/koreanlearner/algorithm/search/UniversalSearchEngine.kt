package com.yonsei.koreanlearner.algorithm.search

import com.yonsei.koreanlearner.data.local.kpop.KPopLyricsLibrary
import com.yonsei.koreanlearner.data.local.reading.GradedReadingData

/**
 * ══════════════════════════════════════════════════════════════════
 * 全局跨库多维秒级聚合搜索引擎 (UniversalSearchEngine)
 * 支持词汇、语法树、专项沙盒、(G)I-DLE歌词与分级阅读统一检索
 * ══════════════════════════════════════════════════════════════════
 */

sealed interface SearchResultItem {
    data class WordMatch(
        val korean: String,
        val chinese: String,
        val partOfSpeech: String,
        val bookLevel: Int,
        val lesson: Int,
        val hanja: String? = null
    ) : SearchResultItem

    data class GrammarMatch(
        val pattern: String,
        val title: String,
        val meaning: String,
        val bookLevel: Int
    ) : SearchResultItem

    data class FeatureMatch(
        val title: String,
        val description: String,
        val route: String
    ) : SearchResultItem

    data class LyricalMatch(
        val songTitle: String,
        val artist: String,
        val matchedSentence: String,
        val chinese: String?
    ) : SearchResultItem
}

data class UniversalSearchResult(
    val query: String,
    val wordMatches: List<SearchResultItem.WordMatch>,
    val grammarMatches: List<SearchResultItem.GrammarMatch>,
    val featureMatches: List<SearchResultItem.FeatureMatch>,
    val lyricalMatches: List<SearchResultItem.LyricalMatch>
) {
    val totalCount: Int get() = wordMatches.size + grammarMatches.size + featureMatches.size + lyricalMatches.size
}

object UniversalSearchEngine {

    // 预置高频核心词汇索引库（用于离线超轻量瞬时响应）
    private val PRESET_VOCAB = listOf(
        SearchResultItem.WordMatch("안녕하세요", "您好（日常最核心问候）", "감탄사", 1, 1),
        SearchResultItem.WordMatch("공부하다", "学习，用功", "동사", 1, 2, "工夫"),
        SearchResultItem.WordMatch("선생님", "老师，先生", "명사", 1, 1, "先生"),
        SearchResultItem.WordMatch("한국어", "韩国语，韩文", "명사", 1, 1, "韓國語"),
        SearchResultItem.WordMatch("감사합니다", "谢谢，感谢", "동사", 1, 1, "感謝"),
        SearchResultItem.WordMatch("친구", "朋友，同伴", "명사", 1, 2, "親舊"),
        SearchResultItem.WordMatch("학교", "学校", "명사", 1, 1, "學校"),
        SearchResultItem.WordMatch("재미있다", "有趣，有意思", "형용사", 1, 3),
        SearchResultItem.WordMatch("도서관", "图书馆", "명사", 1, 4, "圖書館"),
        SearchResultItem.WordMatch("약속하다", "约定，许诺", "동사", 2, 1, "約束"),
        SearchResultItem.WordMatch("아름답다", "美丽的，优美的", "형용사", 2, 3),
        SearchResultItem.WordMatch("날씨", "天气，气候", "명사", 1, 5),
        SearchResultItem.WordMatch("삼겹살", "五花肉", "명사", 2, 6),
        SearchResultItem.WordMatch("중요하다", "重要的，至关重要的", "형용사", 3, 2, "重要"),
        SearchResultItem.WordMatch("파급력", "影响力，波及效应", "명사", 4, 1, "波及力"),
        SearchResultItem.WordMatch("비약적", "飞跃性的，突飞猛进的", "관형사", 5, 2, "飛躍的"),
        SearchResultItem.WordMatch("추석", "中秋节", "명사", 3, 5, "秋夕")
    )

    private val PRESET_GRAMMARS = listOf(
        SearchResultItem.GrammarMatch("-(으)ㄹ 때", "时间状语", "表示当……的时候", 1),
        SearchResultItem.GrammarMatch("-기 때문에", "原因连词", "表示客观确凿的原因理由", 2),
        SearchResultItem.GrammarMatch("-(으)니까", "主观原因", "表示发现前项事实或主观建议原因", 2),
        SearchResultItem.GrammarMatch("-더라고(요)", "口语虚词", "亲身经历见闻的生动回想转述", 3),
        SearchResultItem.GrammarMatch("-잖아(요)", "口语反诘", "表双方皆知的反诘提醒（不是……嘛）", 3),
        SearchResultItem.GrammarMatch("-거든(요)", "口语解释", "补充说明对方不知道的事实前提", 3),
        SearchResultItem.GrammarMatch("-(으)ㄹ진대", "高级学术假定", "既然处于前项事实，理所当然……", 6),
        SearchResultItem.GrammarMatch("-거니와", "高级并列递进", "不仅前项如此，后项亦更为显著", 5),
        SearchResultItem.GrammarMatch("-(으)ㄹ망정", "让步转折", "哪怕宁愿承受前项，也绝不后项", 6)
    )

    private val PRESET_FEATURES = listOf(
        SearchResultItem.FeatureMatch("词尾接龙对决", "韩国国民游戏 끝말잇기 人机回合对抗", "word_chain"),
        SearchResultItem.FeatureMatch("随身磨耳朵电台", "延世 1~6 册高频词与例句后台循环播报", "background_radio"),
        SearchResultItem.FeatureMatch("声调纠音跟读", "母语声调基准线与录音音高双轨重叠对比", "shadowing_trainer"),
        SearchResultItem.FeatureMatch("长句语素拆解镜", "长难句动词原形推导与附着助词/词尾剥离", "korean_lens"),
        SearchResultItem.FeatureMatch("盲打打字机", "2-Set 标准韩语键盘左右手盲打训练", "typing_master"),
        SearchResultItem.FeatureMatch("双语分级精读", "延世 1~6 册经典文化短文点句即读", "graded_reader"),
        SearchResultItem.FeatureMatch("歌词精听沙盒", "(G)I-DLE [I burn] 等神级收录曲逐句听写", "dictation"),
        SearchResultItem.FeatureMatch("TOPIK 作文智能批改", "TOPIK 54 题大作文实时字数与基本阶诊断", "topik_essay_grader"),
        SearchResultItem.FeatureMatch("音变规律动态实验室", "连音/鼻音/流音/紧音/激音/腭音演变拆解", "phonology_lab"),
        SearchResultItem.FeatureMatch("敬语与 6 大阶称", "하십시오/해요/해라/해체 语气一键转换", "speech_level_sandbox")
    )

    /**
     * 执行跨库秒级聚合搜索
     */
    fun search(query: String): UniversalSearchResult {
        val q = query.trim()
        if (q.isBlank()) {
            return UniversalSearchResult(
                query = "",
                wordMatches = emptyList(),
                grammarMatches = emptyList(),
                featureMatches = emptyList(),
                lyricalMatches = emptyList()
            )
        }

        // 1. 词汇检索（韩文、中文、汉字词）
        val words = PRESET_VOCAB.filter {
            it.korean.contains(q, ignoreCase = true) ||
            it.chinese.contains(q, ignoreCase = true) ||
            (it.hanja?.contains(q, ignoreCase = true) == true)
        }

        // 2. 语法树检索
        val grammars = PRESET_GRAMMARS.filter {
            it.pattern.contains(q, ignoreCase = true) ||
            it.title.contains(q, ignoreCase = true) ||
            it.meaning.contains(q, ignoreCase = true)
        }

        // 3. 专项功能沙盒检索
        val features = PRESET_FEATURES.filter {
            it.title.contains(q, ignoreCase = true) ||
            it.description.contains(q, ignoreCase = true)
        }

        // 4. K-Pop 歌词库检索
        val lyrical = mutableListOf<SearchResultItem.LyricalMatch>()
        KPopLyricsLibrary.SONGS.forEach { song ->
            song.lrcContent.lines().forEach { line ->
                if (line.contains(q, ignoreCase = true)) {
                    val parts = line.substringAfter("]").split("|")
                    lyrical.add(
                        SearchResultItem.LyricalMatch(
                            songTitle = song.title,
                            artist = song.artist,
                            matchedSentence = parts.firstOrNull()?.trim() ?: line,
                            chinese = parts.getOrNull(1)?.trim()
                        )
                    )
                }
            }
        }

        return UniversalSearchResult(
            query = q,
            wordMatches = words,
            grammarMatches = grammars,
            featureMatches = features,
            lyricalMatches = lyrical.take(4)
        )
    }
}
