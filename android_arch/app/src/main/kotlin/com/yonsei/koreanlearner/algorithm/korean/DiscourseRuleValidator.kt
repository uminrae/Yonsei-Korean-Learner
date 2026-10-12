package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 篇章逻辑接续词与转折矩阵领域模型与校验器 (DiscourseRuleValidator)
 * ══════════════════════════════════════════════════════════════════
 */

enum class DiscourseCategory(val displayName: String, val colorHex: String) {
    ADDITIVE("顺承 / 并列 (Additive)", "#833AB4"),
    ADVERSATIVE("转折 / 让步 (Adversative)", "#E1306C"),
    CAUSAL("因果 / 推论 (Causal)", "#F59E0B"),
    COMPARATIVE("对照 / 分流 (Comparative)", "#10B981")
}

enum class DiscourseRegister(val displayName: String) {
    SPOKEN("口语常用"),
    WRITTEN("书面正式"),
    NEUTRAL("通用语体")
}

enum class SubjectivityType(val displayName: String) {
    OBJECTIVE("客观事实陈述"),
    SUBJECTIVE("主观意志 / 建议 / 决断"),
    HYBRID("兼具主客观")
}

data class ContrastExamplePair(
    val koreanSentence : String,            // 包含接续词的典型例句
    val chineseSentence: String,            // 中文释义
    val usageNote      : String             // 语用要点
)

data class DiscourseMarker(
    val id             : String,
    val korean         : String,            // 接续词韩文（如 "그러니까", "따라서"）
    val chinese        : String,            // 中文含义
    val category       : DiscourseCategory, // 分类
    val register       : DiscourseRegister, // 语体
    val intensityLevel : Float,             // 语感强弱条 (0.0f ~ 1.0f)
    val subjectivity   : SubjectivityType,  // 主客观倾向
    val coreFunction   : String,            // 核心逻辑功能
    val constraints    : List<String>,      // 语法限制条件
    val examples       : List<ContrastExamplePair>
)

object DiscourseMarkerDatabase {

    val MARKERS = listOf(
        // ── 1. 顺承 / 并列 (Additive) ──────────────────────────
        DiscourseMarker(
            id = "and_1",
            korean = "그리고",
            chinese = "而且 / 还有 / 并且",
            category = DiscourseCategory.ADDITIVE,
            register = DiscourseRegister.NEUTRAL,
            intensityLevel = 0.5f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "前后分句顺承连接，补充说明并列事实或动作时间先后。",
            constraints = listOf("最通用无限制，口语与书面皆宜。"),
            examples = listOf(
                ContrastExamplePair(
                    "한국어를 공부합니다. 그리고 매일 단어를 외웁니다.",
                    "学习韩语。而且每天背诵单词。",
                    "日常顺承并列"
                )
            )
        ),
        DiscourseMarker(
            id = "and_2",
            korean = "또한",
            chinese = "并且 / 此外 / 又",
            category = DiscourseCategory.ADDITIVE,
            register = DiscourseRegister.WRITTEN,
            intensityLevel = 0.8f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "在已有事实基础上追加同等权重的论据，常用于演讲、论文与新闻报告。",
            constraints = listOf("偏向书面语，日常口语对话较少使用。"),
            examples = listOf(
                ContrastExamplePair(
                    "이 정책은 경제를 활성화합니다. 또한 일자리를 창출합니다.",
                    "这项政策促进经济活跃。并且能创造就业岗位。",
                    "书面正式论述"
                )
            )
        ),
        DiscourseMarker(
            id = "and_3",
            korean = "게다가",
            chinese = "加上 / 而且 / 甚至 (递进强化)",
            category = DiscourseCategory.ADDITIVE,
            register = DiscourseRegister.NEUTRAL,
            intensityLevel = 0.9f,
            subjectivity = SubjectivityType.HYBRID,
            coreFunction = "在已有状况上进一步强化递进（常用于雪上加霜或锦上添花）。",
            constraints = listOf("前后情感色彩倾向通常一致（皆积极或皆消极）。"),
            examples = listOf(
                ContrastExamplePair(
                    "비가 많이 옵니다. 게다가 바람까지 심하게 붑니다.",
                    "下大雨。再加上连大风也刮得很凶。",
                    "消极情况雪上加霜"
                )
            )
        ),

        // ── 2. 转折 / 让步 (Adversative) ────────────────────────
        DiscourseMarker(
            id = "but_1",
            korean = "그러나",
            chinese = "然而 / 但是",
            category = DiscourseCategory.ADVERSATIVE,
            register = DiscourseRegister.WRITTEN,
            intensityLevel = 0.85f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "书面语中最标准的强转折连词，前后两项对立或对比突出。",
            constraints = listOf("公文、新闻、备考写作专属，口语中较生硬。"),
            examples = listOf(
                ContrastExamplePair(
                    "그는 열심히 노력했습니다. 그러나 결과는 좋지 않았습니다.",
                    "他付出了努力。然而结果并不理想。",
                    "书面事实强转折"
                )
            )
        ),
        DiscourseMarker(
            id = "but_2",
            korean = "그렇지만",
            chinese = "但是 / 尽管如此",
            category = DiscourseCategory.ADVERSATIVE,
            register = DiscourseRegister.NEUTRAL,
            intensityLevel = 0.75f,
            subjectivity = SubjectivityType.HYBRID,
            coreFunction = "直接对立转折，承认前句事实但强调后句的相反事实。",
            constraints = listOf("口语与书面皆可使用，语气鲜明直接。"),
            examples = listOf(
                ContrastExamplePair(
                    "한국어가 어렵습니다. 그렇지만 정말 재미있습니다.",
                    "韩语很难。但是真的很有趣。",
                    "承认困难但强调乐趣"
                )
            )
        ),
        DiscourseMarker(
            id = "but_3",
            korean = "그런데",
            chinese = "可是 / 不过 / 顺便说一句",
            category = DiscourseCategory.ADVERSATIVE,
            register = DiscourseRegister.SPOKEN,
            intensityLevel = 0.45f,
            subjectivity = SubjectivityType.SUBJECTIVE,
            coreFunction = "日常口语高频轻微转折，或用于提供新背景并转换谈话话题。",
            constraints = listOf("兼具'转折'与'转换话题'双重功能。"),
            examples = listOf(
                ContrastExamplePair(
                    "약속 장소에 갔어요. 그런데 아무도 없었어요.",
                    "我去了约定地点。可是那里一个人也没有。",
                    "日常口语轻转折与意外发现"
                )
            )
        ),
        DiscourseMarker(
            id = "but_4",
            korean = "그래도",
            chinese = "即便如此 / 虽说如此",
            category = DiscourseCategory.ADVERSATIVE,
            register = DiscourseRegister.SPOKEN,
            intensityLevel = 0.65f,
            subjectivity = SubjectivityType.SUBJECTIVE,
            coreFunction = "让步转折，承认前文不利条件，后文仍坚持采取行动或维持信念。",
            constraints = listOf("后句常体现说话人的坚持或主观意志。"),
            examples = listOf(
                ContrastExamplePair(
                    "날씨가 춥습니다. 그래도 운동하러 갈 거예요.",
                    "天气很冷。即便如此我还是打算去运动。",
                    "坚持主观意志"
                )
            )
        ),
        DiscourseMarker(
            id = "but_5",
            korean = "오히려",
            chinese = "反而 / 反倒是 (出乎意料)",
            category = DiscourseCategory.ADVERSATIVE,
            register = DiscourseRegister.NEUTRAL,
            intensityLevel = 0.95f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "实际结果与常理或预期完全相反，发生戏剧性反转。",
            constraints = listOf("必须存在预期与实际的反差。"),
            examples = listOf(
                ContrastExamplePair(
                    "약을 먹었는데 낫지 않고 오히려 더 아픕니다.",
                    "吃了药没见好，反而更疼了。",
                    "出乎意料的反转"
                )
            )
        ),

        // ── 3. 因果 / 推论 (Causal) ────────────────────────────
        DiscourseMarker(
            id = "so_1",
            korean = "그래서",
            chinese = "所以 / 因此 (客观因果)",
            category = DiscourseCategory.CAUSAL,
            register = DiscourseRegister.NEUTRAL,
            intensityLevel = 0.6f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "前句为客观原因，后句为自然发生的客观陈述结果。",
            constraints = listOf("后句严禁接命令句（-세요）或共动句（-ㅂ시다）。"),
            examples = listOf(
                ContrastExamplePair(
                    "눈이 왔습니다. 그래서 길이 미끄럽습니다.",
                    "下雪了。所以道路很滑。",
                    "客观因果陈述"
                )
            )
        ),
        DiscourseMarker(
            id = "so_2",
            korean = "그러니까",
            chinese = "因此 / 正因为如此 (主观要求)",
            category = DiscourseCategory.CAUSAL,
            register = DiscourseRegister.SPOKEN,
            intensityLevel = 0.8f,
            subjectivity = SubjectivityType.SUBJECTIVE,
            coreFunction = "前句为说话人提出理由，后句引出命令、请求、提议或强烈主观决定。",
            constraints = listOf("后句极适于接命令句 -(으)세요/-(으)십시오 或共动句 -(으)ㅂ시다。"),
            examples = listOf(
                ContrastExamplePair(
                    "날씨가 춥습니다. 그러니까 옷을 따뜻하게 입으세요.",
                    "天气冷。因此请穿暖和些。",
                    "后接命令/劝诱句型"
                )
            )
        ),
        DiscourseMarker(
            id = "so_3",
            korean = "따라서",
            chinese = "因此 / 从而 (书面严密推导)",
            category = DiscourseCategory.CAUSAL,
            register = DiscourseRegister.WRITTEN,
            intensityLevel = 0.9f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "在议论文或学术论述中，严格依据前项前提推导出后项必然结论。",
            constraints = listOf("书面语专属，严禁后接命令句与共动句。"),
            examples = listOf(
                ContrastExamplePair(
                    "인구가 감소하고 있습니다. 따라서 대책 마련이 시급합니다.",
                    "人口正在减少。因此制定对策迫在眉睫。",
                    "学术与公文论证推导"
                )
            )
        ),

        // ── 4. 对照 / 分流 (Comparative) ────────────────────────
        DiscourseMarker(
            id = "comp_1",
            korean = "반면에",
            chinese = "相反 / 另一方面 (对比)",
            category = DiscourseCategory.COMPARATIVE,
            register = DiscourseRegister.WRITTEN,
            intensityLevel = 0.85f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "将同一事物的两个截然相反的性质，或两个不同对象进行鲜明对比。",
            constraints = listOf("常用于优缺点、好坏两面的平行对照。"),
            examples = listOf(
                ContrastExamplePair(
                    "형은 외향적입니다. 반면에 동생은 내성적입니다.",
                    "哥哥性格外向。相反弟弟性格内向。",
                    "截然相反的性格对照"
                )
            )
        ),
        DiscourseMarker(
            id = "comp_2",
            korean = "한편",
            chinese = "另一方面 / 与此同时 (视点切换)",
            category = DiscourseCategory.COMPARATIVE,
            register = DiscourseRegister.WRITTEN,
            intensityLevel = 0.7f,
            subjectivity = SubjectivityType.OBJECTIVE,
            coreFunction = "在新闻或记叙中切换叙事视角，展示同一时间另一场所发生的事件。",
            constraints = listOf("新闻报道与记叙文高频。"),
            examples = listOf(
                ContrastExamplePair(
                    "정부는 협상을 진행했습니다. 한편 시민들은 시위를 벌였습니다.",
                    "政府推进了谈判。与此同时市民们举行了示威。",
                    "新闻视点分流"
                )
            )
        )
    )

    // ════════════════════════════════════════════════════════════
    // 逻辑约束校验算法
    // ════════════════════════════════════════════════════════════

    data class ValidationResult(
        val isValid     : Boolean,
        val feedback    : String,
        val suggestedMarker: String? = null
    )

    /**
     * 校验在指定前后句语境中使用接续词是否合法
     */
    fun validateUsage(
        marker: String,
        priorSentence: String,
        postSentence: String
    ): ValidationResult {
        val cleanPost = postSentence.trim()
        val isImperativeOrPropositive = cleanPost.endsWith("세요") ||
                cleanPost.endsWith("십시오") ||
                cleanPost.endsWith("ㅂ시다") ||
                cleanPost.endsWith("자") ||
                cleanPost.endsWith("아라") ||
                cleanPost.endsWith("어라")

        // 规则 1：若后句为命令/共动句，使用 '따라서' 或 '그래서' 则报错，提示改用 '그러니까'
        if (isImperativeOrPropositive && (marker == "따라서" || marker == "그래서")) {
            return ValidationResult(
                isValid = false,
                feedback = "后句是命令句（-세요）或共动句（-ㅂ시다），不可使用客观因果的 '$marker'，必须使用包含主观要求与劝诱的 '그러니까'！",
                suggestedMarker = "그러니까"
            )
        }

        // 规则 2：若后句是命令/共动句且选了 '그러니까' -> 完美匹配
        if (isImperativeOrPropositive && marker == "그러니까") {
            return ValidationResult(
                isValid = true,
                feedback = " 完美运用！'그러니까' 正是后接命令句（-세요）或共动句（-ㅂ시다）的标准表达。"
            )
        }

        return ValidationResult(
            isValid = true,
            feedback = "逻辑关系连接顺畅，语体风格得体。"
        )
    }
}
