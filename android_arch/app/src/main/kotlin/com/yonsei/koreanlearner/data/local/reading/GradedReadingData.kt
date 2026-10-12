package com.yonsei.koreanlearner.data.local.reading

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世双语分级精读文库数据模型 (GradedReadingData)
 * ══════════════════════════════════════════════════════════════════
 */

data class GradedSentence(
    val korean: String,
    val chinese: String,
    val grammarNote: String? = null
)

data class GradedArticle(
    val id: String,
    val bookLevel: Int,
    val title: String,
    val chineseTitle: String,
    val summary: String,
    val sentences: List<GradedSentence>,
    val coreVocabList: List<Pair<String, String>>
)

object GradedReadingData {

    val ARTICLES = listOf(
        // ── 1. 延世第 1 册精读 ────────────────────────────────────
        GradedArticle(
            id = "reading_book_1",
            bookLevel = 1,
            title = "한국의 사계절과 아름다운 날씨",
            chineseTitle = "韩国的四季与明媚天气",
            summary = "介绍韩国鲜明的春夏秋冬四季气候特色与日常生活。",
            coreVocabList = listOf(
                "사계절" to "四季",
                "봄" to "春天",
                "꽃이 피다" to "鲜花盛开",
                "단풍" to "枫叶/红叶",
                "눈사람" to "雪人"
            ),
            sentences = listOf(
                GradedSentence(
                    korean = "한국은 사계절이 뚜렷한 아름다운 나라입니다.",
                    chinese = "韩国是一个四季分明的美丽国家。",
                    grammarNote = "-은/는 뚜렷한：说明主语特征；-입니다 格式体陈述"
                ),
                GradedSentence(
                    korean = "봄에는 따뜻한 바람이 불고 공원에 예쁜 꽃이 많이 핍니다.",
                    chinese = "春天吹拂着温暖的微风，公园里盛开着许多美丽的花朵。",
                    grammarNote = "-에는 时间状语；-고 连接并列谓语"
                ),
                GradedSentence(
                    korean = "여름은 날씨가 덥기 때문에 사람들이 바다로 수영하러 갑니다.",
                    chinese = "夏天因为天气炎热，所以人们常常去海边游泳。",
                    grammarNote = "-기 때문에 原因；-(으)러 가다 前往做某事的意图"
                ),
                GradedSentence(
                    korean = "가을에는 산에 아름다운 붉은 단풍이 듭니다.",
                    chinese = "秋天山野间会染上瑰丽鲜艳的红枫。",
                    grammarNote = "단풍이 들다 固定搭配（染上枫红）"
                ),
                GradedSentence(
                    korean = "겨울에는 하얀 눈이 내려서 친구들과 같이 눈사람을 만듭니다.",
                    chinese = "冬天会飘落洁白的雪花，可以和朋友们一起堆雪人。",
                    grammarNote = "-아/어서 原因与顺承；-(으)ㅂ니다"
                )
            )
        ),

        // ── 2. 延世第 2 册精读 ────────────────────────────────────
        GradedArticle(
            id = "reading_book_2",
            bookLevel = 2,
            title = "한국의 따뜻한 밥상과 식사 예절",
            chineseTitle = "韩国温馨的餐桌与就餐礼仪",
            summary = "解析韩国传统的汤饭文化、共同分享小菜与对长辈的礼仪。",
            coreVocabList = listOf(
                "식사 예절" to "用餐礼仪",
                "어른" to "长辈/年长者",
                "수저" to "勺筷（匙箸）",
                "나누어 먹다" to "分着吃/分享",
                "진지" to "米饭（敬语尊称）"
            ),
            sentences = listOf(
                GradedSentence(
                    korean = "한국 식사 문화에서 가장 중요한 것은 어른을 배려하는 예절입니다.",
                    chinese = "在韩国的饮食文化中，最重要的是体谅并尊敬长辈的礼节。",
                    grammarNote = "-에서 范围；-는 것 名词化从句"
                ),
                GradedSentence(
                    korean = "식사를 시작할 때 어른이 먼저 숟가락을 드신 후에 아랫사람이 먹기 시작합니다.",
                    chinese = "用餐开始时，通常由长辈先动筷动勺后，晚辈方才开始进食。",
                    grammarNote = "-(으)ㄴ 후에 在……之后；-시- 尊称词尾"
                ),
                GradedSentence(
                    korean = "찌개와 반찬을 식탁 가운데에 두고 다 함께 나누어 먹는 정이 있습니다.",
                    chinese = "将汤锅与小菜置于餐桌中央大家共同分享，蕴含着深厚的人情味。",
                    grammarNote = "-아/어 두다 放置状态；-는 정 情谊"
                ),
                GradedSentence(
                    korean = "밥그릇을 손에 들고 먹지 않고 식탁에 내려놓고 먹는 것이 바른 예절입니다.",
                    chinese = "不端着饭碗而是放置在桌上食用，才是符合规范的端正礼仪。",
                    grammarNote = "-지 않고 代替连接；-는 것이 바른 예절"
                )
            )
        ),

        // ── 3. 延世第 3 册精读 ────────────────────────────────────
        GradedArticle(
            id = "reading_book_3",
            bookLevel = 3,
            title = "한국의 대명절 추석과 고향의 정",
            chineseTitle = "韩国传统大节中秋与故乡情怀",
            summary = "描绘中秋团聚、松饼制作与拜祭祖先的传统家庭温情。",
            coreVocabList = listOf(
                "추석" to "中秋节（秋夕）",
                "송편" to "松饼（中秋节传统米糕）",
                "보름달" to "满月/圆月",
                "귀성길" to "返乡之路",
                "소원을 빌다" to "许愿"
            ),
            sentences = listOf(
                GradedSentence(
                    korean = "추석은 한 해 동안 정성껏 기른 곡식과 과일을 수확하여 조상께 감사하는 날입니다.",
                    chinese = "中秋是收获一年来辛勤培育的谷物与水果，并向先祖表达感恩的日子。",
                    grammarNote = "-하여 书面顺承；-께 敬语给与助词"
                ),
                GradedSentence(
                    korean = "온 가족이 한자리에 둘러앉아 솔잎 향이 가득한 송편을 빚곤 합니다.",
                    chinese = "全家人围坐在一处，常常一同包制散发着淡淡松针清香的松饼。",
                    grammarNote = "-곤 하다 习惯性反复做某事"
                ),
                GradedSentence(
                    korean = "밤하늘에 떠오른 둥근 보름달을 바라보며 가족들의 건강과 행복을 위한 소원을 빕니다.",
                    chinese = "凝望着夜空中升起的一轮圆润明月，由衷许下祈愿全家健康幸福的心愿。",
                    grammarNote = "-(으)며 伴随动作；-을/를 위한 소원 目的"
                )
            )
        ),

        // ── 4. 延世第 4 册精读 ────────────────────────────────────
        GradedArticle(
            id = "reading_book_4",
            bookLevel = 4,
            title = "K-Pop과 현대 한류 문화의 세계적인 파급력",
            chineseTitle = "K-Pop 与现代韩流文化的全球影响力",
            summary = "探讨音乐叙事、全球粉丝社群与跨文化传播的独特魅力。",
            coreVocabList = listOf(
                "파급력" to "影响力/波及效应",
                "독창적" to "独创性的",
                "공감대" to "共鸣/共识",
                "선도하다" to "引领/先导",
                "융합되다" to "融合"
            ),
            sentences = listOf(
                GradedSentence(
                    korean = "K-Pop은 중독성 있는 멜로디와 독창적인 퍼포먼스를 바탕으로 전 세계 청년들의 마음을 사로잡았습니다.",
                    chinese = "K-Pop 凭借极具感染力的旋律与独树一帜的舞台表演，深深抓住了全球年轻一代的心扉。",
                    grammarNote = "-을/를 바탕으로 以……为基础/依托"
                ),
                GradedSentence(
                    korean = "단순한 음악 소비를 넘어서 아티스트와 팬덤이 진정성 있는 메시지를 통해 깊은 유대감을 형성하고 있습니다.",
                    chinese = "它不仅超越了单纯的音乐消费，艺人与粉丝群体更通过饱含真诚的理念构建起深厚的情感纽带。",
                    grammarNote = "-을/를 넘어서 超越；-을/를 통해 通过媒介"
                ),
                GradedSentence(
                    korean = "이러한 문화적 현상은 언어의 장벽을 뛰어넘어 다양한 문화 간의 교류와 소통을 촉진하는 계기가 되었습니다.",
                    chinese = "这一文化现象跨越了语言的藩篱，成为了促进多元文化间对话与互通的重要契机。",
                    grammarNote = "-는 계기가 되다 成为……的契机"
                )
            )
        ),

        // ── 5. 延世第 5~6 册学术精读 ───────────────────────────────
        GradedArticle(
            id = "reading_book_5_6",
            bookLevel = 5,
            title = "인공지능 시대의 도래와 인간성의 가치",
            chineseTitle = "人工智能时代的降临与人性价值的重塑",
            summary = "高级学术探讨技术飞跃、自主性与未来社会伦理考量。",
            coreVocabList = listOf(
                "도래하다" to "（时代）到来/降临",
                "주체성" to "主体性/主导性",
                "윤리적 고찰" to "伦理层面的审视考量",
                "대체 불가능하다" to "不可替代的",
                "지향점" to "追求目标/方向"
            ),
            sentences = listOf(
                GradedSentence(
                    korean = "인공지능 기술의 비약적인 발전은 인류에게 무한한 편의를 제공하는 한편, 인간 고유의 영역에 대한 새로운 성찰을 요구하고 있습니다.",
                    chinese = "人工智能技术的飞跃式发展在为人类带来无限便利的同时，亦对人类独有的本质领域提出了全新的深刻审视。",
                    grammarNote = "-는 한편 一方面……另一方面；-에 대한 针对"
                ),
                GradedSentence(
                    korean = "기계가 복잡한 연산과 지식 처리를 대신할진대, 인간에게 요구되는 본질적인 역량은 공감과 창의적 비판 의식일 것입니다.",
                    chinese = "既然精密机械能够代劳纷繁复杂的运算与知识处理，人类被赋予的核心能力必将是共情与创造性的批判思维。",
                    grammarNote = "-(으)ㄹ진대 高级学术假定/既然（延世6册语法）"
                ),
                GradedSentence(
                    korean = "우리는 기술의 발전을 맹목적으로 수용하기보다는 인간 존엄성을 최우선으로 삼는 윤리적 기준을 정립해야 합니다.",
                    chinese = "与其盲目跟从并接纳技术狂飙，我们更应当确立以维护人类尊严为最高准则的伦理规范体系。",
                    grammarNote = "-기보다는 与其……不如；-을/를 우선으로 삼다"
                )
            )
        )
    )
}
