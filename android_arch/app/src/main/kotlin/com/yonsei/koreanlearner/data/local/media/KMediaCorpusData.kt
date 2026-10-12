package com.yonsei.koreanlearner.data.local.media

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩剧影视名台词与真实口语虚词精析语料库 (KMediaCorpusData)
 * ══════════════════════════════════════════════════════════════════
 */

data class KMediaQuote(
    val dramaTitle: String,
    val speaker: String,
    val lineKorean: String,
    val lineChinese: String,
    val keySpokenEnding: String,
    val endingGrammarExplanation: String
)

object KMediaCorpusData {

    val SPOKEN_ENDINGS_LIST = listOf(
        KMediaQuote(
            dramaTitle = "쓸쓸하고 찬란하神 - 도깨비 (孤单又灿烂的神：鬼怪)",
            speaker = "김신 (金侁)",
            lineKorean = "너와 함께한 모든 시간이 눈부셨다. 날이 좋아서, 날이 좋지 않아서, 날이 적당해서. 모든 날이 좋았다.",
            lineChinese = "与你在一起的所有时光都很耀眼。因为天气好，因为天气不好，因为天气刚刚好。每一天都很美好。",
            keySpokenEnding = "-아서/어서 (因果理由)",
            endingGrammarExplanation = "日常口语中最自然流畅的因果叙事接续，用于抒发主观情感与理由。"
        ),
        KMediaQuote(
            dramaTitle = "사랑의 불시착 (爱的迫降)",
            speaker = "윤세리 (尹世理)",
            lineKorean = "바람이 왜 부는지 알아요? 지나가려고 부는 거예요. 머물려고 부는 게 아니라.",
            lineChinese = "你知道风为什么吹吗？吹拂是为了穿过，而不是为了停留。",
            keySpokenEnding = "-(으)려고 (意图/目的)",
            endingGrammarExplanation = "口语表达主观目的，强调行为的真正企图或自然哲理。"
        ),
        KMediaQuote(
            dramaTitle = "응답하라 1988 (请回答 1988)",
            speaker = "성덕선 (成德善)",
            lineKorean = "가족이니까 말 안 해도 다 알 줄 알았거든. 근데 말하지 않으면 아무도 몰라.",
            lineChinese = "因为是家人，我原以为不说大家也会懂的呢。但如果不说出来，谁都不会明白。",
            keySpokenEnding = "-거든(요) (补充说明事实/理由)",
            endingGrammarExplanation = "口语虚词核心，向对方提供对方所不知晓的背景事实或解释内心委屈。"
        ),
        KMediaQuote(
            dramaTitle = "눈물의 여왕 (泪之女王)",
            speaker = "백현우 (白贤佑)",
            lineKorean = "내가 널 얼마나 사랑하는지 알잖아. 단 한 순간도 잊은 적 없어.",
            lineChinese = "你不是知道我有多爱你嘛。我连一瞬间都从未忘记过。",
            keySpokenEnding = "-잖아(요) (对方已知事实确认)",
            endingGrammarExplanation = "极高频日常口语，用于提示对方其实心知肚明的事实，拉近亲密情感距离。"
        ),
        KMediaQuote(
            dramaTitle = "더 글로리 (黑暗荣耀)",
            speaker = "문동은 (文东恩)",
            lineKorean = "오늘부터 모든 날이 흉흉할 거야. 막을 수도, 없앨 수도 없을 거거든.",
            lineChinese = "从今天开始，你的每一天都会风雨凄凄。因为你既挡不住，也无法消除。",
            keySpokenEnding = "-(으)ㄹ 거거든 (坚决预告语气)",
            endingGrammarExplanation = "未来推测 -(으)ㄹ 것이다 与虚词 -거든 的组合，形成强烈坚定的宿命预告感。"
        )
    )
}
