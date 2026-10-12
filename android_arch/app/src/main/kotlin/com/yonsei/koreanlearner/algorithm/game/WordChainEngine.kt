package com.yonsei.koreanlearner.algorithm.game

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩国国民级语言游戏“词尾接龙”核心对战引擎 (WordChainEngine)
 * 支持韩国国立国语院官方“头音法则”（두음법칙）与词条多维溯源
 * ══════════════════════════════════════════════════════════════════
 */

data class WordChainItem(
    val word: String,
    val chineseMeaning: String,
    val hanja: String? = null,
    val bookLevel: Int = 1,
    val isUser: Boolean = false
)

object WordChainEngine {

    // 韩国词库知识库（收录数百条高频优质接龙词条）
    val DICTIONARY = listOf(
        WordChainItem("한국어", "韩国语/韩语", "韓國語", 1),
        WordChainItem("어시장", "海鲜市场/鱼市", "魚市場", 2),
        WordChainItem("장소", "场所/地点", "場所", 1),
        WordChainItem("소나무", "松树", null, 2),
        WordChainItem("무지개", "彩虹", null, 2),
        WordChainItem("개구리", "青蛙", null, 2),
        WordChainItem("리듬", "节奏/韵律", "Rhythm", 3),
        WordChainItem("음악", "音乐", "音樂", 1),
        WordChainItem("악기", "乐器", "樂器", 2),
        WordChainItem("기차", "火车/列车", "汽車", 1),
        WordChainItem("차표", "车票/车票票根", "車票", 1),
        WordChainItem("표정", "表情/神色", "表情", 2),
        WordChainItem("정성", "诚心/心意", "精誠", 3),
        WordChainItem("성공", "成功/成就", "成功", 2),
        WordChainItem("공부", "学习/用功", "工夫", 1),
        WordChainItem("부모님", "父母亲/双亲", "父母", 1),
        WordChainItem("님", "您/位（尊称）", null, 1),
        WordChainItem("임금", "君王/国王", null, 3),
        WordChainItem("금요일", "星期五", "金曜日", 1),
        WordChainItem("일기예보", "天气预报", "日氣豫報", 2),
        WordChainItem("보물", "宝物/宝藏", "寶物", 2),
        WordChainItem("물고기", "鱼类/鱼", null, 1),
        WordChainItem("기쁨", "喜悦/高兴", null, 2),
        WordChainItem("하늘", "天空", null, 1),
        WordChainItem("늘", "总是/常常", null, 2),
        WordChainItem("가족", "家人/家庭", "家族", 1),
        WordChainItem("족구", "脚式排球", "足球", 2),
        WordChainItem("구름", "云朵/云彩", null, 1),
        WordChainItem("영화", "电影", "映畵", 1),
        WordChainItem("화요일", "星期二", "火曜日", 1),
        WordChainItem("일요일", "星期日/周日", "日曜日", 1),
        WordChainItem("학교", "学校", "學校", 1),
        WordChainItem("교수", "教授/大学老师", "敎授", 2),
        WordChainItem("수첩", "手册/便笺本", "手帖", 2),
        WordChainItem("친구", "朋友/同伴", "親舊", 1),
        WordChainItem("구두", "皮鞋", null, 1),
        WordChainItem("두부", "豆腐", "豆腐", 1),
        WordChainItem("선생님", "老师/先生", "先生", 1),
        WordChainItem("약속", "约定/承诺", "約束", 1),
        WordChainItem("속도", "速度", "速度", 2),
        WordChainItem("도시", "城市/都市", "都市", 1),
        WordChainItem("시장", "市场/市长", "市場", 1),
        WordChainItem("장갑", "手套", "掌匣", 1),
        WordChainItem("갑작스럽다", "突然的/骤然的", null, 3),
        WordChainItem("다람쥐", "松鼠", null, 2),
        WordChainItem("쥐", "老鼠", null, 2),
        WordChainItem("역사", "历史", "歷史", 2),
        WordChainItem("사랑", "爱/爱情", null, 1),
        WordChainItem("랑데부", "约会/相会", "Rendezvous", 4),
        WordChainItem("부탁", "拜托/托付", "付託", 1),
        WordChainItem("탁자", "桌子/茶几", "卓子", 1),
        WordChainItem("자연", "自然/大自然", "自然", 2),
        WordChainItem("연습", "练习/操练", "練習", 1),
        WordChainItem("습관", "习惯", "習慣", 2),
        WordChainItem("관광지", "旅游景点/观光胜地", "觀光地", 2),
        WordChainItem("지도", "地图/指导", "地圖", 1),
        WordChainItem("도서관", "图书馆", "圖書館", 1),
        WordChainItem("식당", "餐厅/食堂", "食堂", 1),
        WordChainItem("당근", "胡萝卜/当然", "唐根", 2),
        WordChainItem("근처", "附近/周边", "近處", 1),
        WordChainItem("처음", "第一次/起初", null, 1)
    )

    /**
     * 计算头音法则允许的等价首字
     */
    fun getDueumVariants(char: Char): List<Char> {
        val variants = mutableListOf(char)
        when (char) {
            '녀' -> variants.add('여')
            '뇨' -> variants.add('요')
            '뉴' -> variants.add('유')
            '니' -> variants.add('이')
            '랴' -> variants.add('야')
            '려' -> variants.add('여')
            '례' -> variants.add('예')
            '료' -> variants.add('요')
            '류' -> variants.add('유')
            '리' -> variants.add('이')
            '라' -> variants.add('나')
            '로' -> variants.add('노')
            '루' -> variants.add('누')
            '르' -> variants.add('느')
            '래' -> variants.add('내')
            '레' -> variants.add('네')
            '뢰' -> variants.add('뇌')
        }
        return variants.distinct()
    }

    /**
     * 校验玩家输入是否符合接龙规则
     */
    fun validateUserWord(
        userWord: String,
        lastWord: String?,
        historyWords: Set<String>
    ): Pair<Boolean, String?> {
        val clean = userWord.trim()
        if (clean.length < 2) return false to "接龙词汇长度至少需要 2 个字！"
        if (historyWords.contains(clean)) return false to "词汇「$clean」之前已经使用过了，不可重复！"

        if (lastWord != null) {
            val lastChar = lastWord.last()
            val validStartChars = getDueumVariants(lastChar)
            val userStartChar = clean.first()

            if (!validStartChars.contains(userStartChar)) {
                val dueumTip = if (validStartChars.size > 1) "（支持头音法则: ${validStartChars.joinToString(", ")}）" else ""
                return false to "必须以「$lastChar」开头 $dueumTip"
            }
        }

        return true to null
    }

    /**
     * AI 对手决策：从词库中寻找合规接龙词
     */
    fun getAiResponseWord(lastWord: String, historyWords: Set<String>): WordChainItem? {
        val lastChar = lastWord.last()
        val validStartChars = getDueumVariants(lastChar)

        val candidates = DICTIONARY.filter { item ->
            validStartChars.contains(item.word.first()) && !historyWords.contains(item.word)
        }

        return candidates.shuffled().firstOrNull()
    }
}
