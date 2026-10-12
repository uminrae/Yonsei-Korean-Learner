package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世生活情境会话沙盒领域模型与状态机 (DialogueStateMachine)
 * ══════════════════════════════════════════════════════════════════
 */

enum class ResponseRating(val score: Int, val displayName: String, val badgeColorHex: String) {
    OPTIMAL(100, "最佳敬语 · 地道", "#10B981"),
    FLAWED(70, "语法瑕疵 · 勉强理解", "#F59E0B"),
    INAPPROPRIATE(30, "非敬语误用 · 失礼", "#EF4444")
}

/** 用户回复选项模型 */
data class DialogueOption(
    val optionId            : String,               // 选项 ID（如 "opt_1_a"）
    val koreanText          : String,               // 韩文选项内容
    val chineseText         : String,               // 中文释义
    val rating              : ResponseRating,       // 评价等级
    val feedbackExplanation : String,               // 语法与礼节反馈说明
    val nextStepId          : String?               // 下一跳剧情节点 ID（null 表示对话结束）
)

/** 对话历史记录消息项（用于 Instagram DM 气泡渲染） */
data class DialogueMessage(
    val id         : String,
    val senderName : String,                        // 发送者（如 "점원", "나"）
    val isUser     : Boolean,                       // 是否为用户
    val koreanText : String,                        // 韩文发言
    val chineseText: String,                        // 中文对照
    val rating     : ResponseRating? = null,        // 若为用户发言，显示评级
    val feedback   : String? = null                 // 语法反馈
)

/** 单个对话节点步骤 (DialogueStep) */
data class DialogueStep(
    val stepId          : String,                   // 步骤 ID
    val npcSpeakerName  : String,                   // NPC 姓名/称谓（如 "편의점 점원"）
    val npcAvatarEmoji  : String,                   // 头像（如 "", "", "‍⚕️", "", ""）
    val npckoreanText   : String,                   // NPC 提问韩文
    val npcChineseText  : String,                   // NPC 提问中文翻译
    val userOptions     : List<DialogueOption>      // 用户可选择的 3 个分支选项
)

/** 剧本情境定义 (DialogueScenario) */
data class DialogueScenario(
    val id              : String,                   // 场景 ID
    val title           : String,                   // 场景标题（如 "便利店购物与结账"）
    val categoryName    : String,                   // 分类
    val iconEmoji       : String,                   // 图标
    val npcRole         : String,                   // NPC 身份
    val userRole        : String,                   // 用户身份
    val steps           : Map<String, DialogueStep>,// 对话树节点集合
    val initialStepId   : String                    // 起始步骤 ID
)

object DialogueStateMachine {

    val SCENARIOS = listOf(
        // ── 场景 1：便利店购物砍价 (점원 vs 손님) ───────────────
        DialogueScenario(
            id = "store",
            title = "便利店购物与结账",
            categoryName = "日常生活",
            iconEmoji = "",
            npcRole = "편의점 점원 (店员)",
            userRole = "손님 (顾客)",
            initialStepId = "step_1",
            steps = mapOf(
                "step_1" to DialogueStep(
                    stepId = "step_1",
                    npcSpeakerName = "점원",
                    npcAvatarEmoji = "",
                    npckoreanText = "어서 오세요! 찾으시는 물건 있으세요?",
                    npcChineseText = "欢迎光临！有您要找的商品吗？",
                    userOptions = listOf(
                        DialogueOption(
                            "1_a",
                            "네, 따뜻한 캔커피는 어디에 있어요?",
                            "有的，请问热罐装咖啡在哪里？",
                            ResponseRating.OPTIMAL,
                            "礼貌得体，使用敬语 '-어/아요' 疑问句，表达准确清晰。",
                            "step_2"
                        ),
                        DialogueOption(
                            "1_b",
                            "커피 어디 있어?",
                            "咖啡在哪？",
                            ResponseRating.INAPPROPRIATE,
                            "对初次见面的店员使用了非敬语平语（반말），非常不礼貌。",
                            "step_2"
                        ),
                        DialogueOption(
                            "1_c",
                            "저기요, 커피가 주십시오.",
                            "劳驾，请给咖啡。",
                            ResponseRating.FLAWED,
                            "语法助词误用：'커피가' 应该用宾格助词 '커피를'。",
                            "step_2"
                        )
                    )
                ),
                "step_2" to DialogueStep(
                    stepId = "step_2",
                    npcSpeakerName = "점원",
                    npcAvatarEmoji = "",
                    npckoreanText = "음료 코너 안쪽에 있습니다. 봉투 필요하세요?",
                    npcChineseText = "在饮料专区内侧。请问需要塑料袋吗？",
                    userOptions = listOf(
                        DialogueOption(
                            "2_a",
                            "아니요, 괜찮습니다. 영수증만 주세요.",
                            "不用了，没关系。只要小票就行。",
                            ResponseRating.OPTIMAL,
                            "标准日常礼貌用语，精准拒绝并请求小票。",
                            null
                        ),
                        DialogueOption(
                            "2_b",
                            "봉투 싫어.",
                            "不要袋子。",
                            ResponseRating.INAPPROPRIATE,
                            "使用了粗鲁的平语，应使用 '괜찮아요' 或 '필요 없어요'。",
                            null
                        ),
                        DialogueOption(
                            "2_c",
                            "네, 봉투에 넣어 주시기 바랍니다.",
                            "好的，请装进袋子里。",
                            ResponseRating.FLAWED,
                            "'-기 바랍니다' 语气过于正式生硬（公文广播常用），日常便利店显得不自然。",
                            null
                        )
                    )
                )
            )
        ),

        // ── 场景 2：出租车出行问路 (기사 vs 승객) ───────────────
        DialogueScenario(
            id = "taxi",
            title = "出租车出行与到达目的地",
            categoryName = "出行交通",
            iconEmoji = "",
            npcRole = "택시 기사 (司机)",
            userRole = "승객 (乘客)",
            initialStepId = "taxi_1",
            steps = mapOf(
                "taxi_1" to DialogueStep(
                    stepId = "taxi_1",
                    npcSpeakerName = "택시 기사",
                    npcAvatarEmoji = "",
                    npckoreanText = "손님, 어디로 모실까요?",
                    npcChineseText = "乘客您好，请问去哪里？",
                    userOptions = listOf(
                        DialogueOption(
                            "t1_a",
                            "연세대학교 정문으로 가 주세요.",
                            "请去延世大学正门。",
                            ResponseRating.OPTIMAL,
                            "使用 '-아/어 주세요' 请求句型与方向助词 '-으로'，标准自然。",
                            "taxi_2"
                        ),
                        DialogueOption(
                            "t1_b",
                            "연세대 가자.",
                            "去延世大吧。",
                            ResponseRating.INAPPROPRIATE,
                            "'-자' 是共动平语，绝对不可对长辈或司机使用。",
                            "taxi_2"
                        ),
                        DialogueOption(
                            "t1_c",
                            "연세대학교에 운전해라.",
                            "给我开去延世大。",
                            ResponseRating.INAPPROPRIATE,
                            "使用了无礼的命令式非敬语，属于严重失礼表达。",
                            "taxi_2"
                        )
                    )
                ),
                "taxi_2" to DialogueStep(
                    stepId = "taxi_2",
                    npcSpeakerName = "택시 기사",
                    npcAvatarEmoji = "",
                    npckoreanText = "다 왔습니다. 요금은 8,500원입니다.",
                    npcChineseText = "已经到了。车费一共 8,500 韩元。",
                    userOptions = listOf(
                        DialogueOption(
                            "t2_a",
                            "카드로 계산해 주세요. 감사합니다!",
                            "请刷卡结账。谢谢您！",
                            ResponseRating.OPTIMAL,
                            "清晰说明支付方式并致谢，满分礼貌会话。",
                            null
                        ),
                        DialogueOption(
                            "t2_b",
                            "여기 카드 받아.",
                            "这里卡给你拿去。",
                            ResponseRating.INAPPROPRIATE,
                            "使用了平语命令式，极不尊重对方。",
                            null
                        ),
                        DialogueOption(
                            "t2_c",
                            "카드에 결제하십시오.",
                            "请刷在卡上。",
                            ResponseRating.FLAWED,
                            "助词误用：表示方式手段应使用 '카드로'。",
                            null
                        )
                    )
                )
            )
        ),

        // ── 场景 3：医院看病拿药 (의사 vs 환자) ─────────────────
        DialogueScenario(
            id = "hospital",
            title = "医院内科看病就诊",
            categoryName = "就医健康",
            iconEmoji = "‍⚕️",
            npcRole = "내과 의사 (医生)",
            userRole = "환자 (患者)",
            initialStepId = "hosp_1",
            steps = mapOf(
                "hosp_1" to DialogueStep(
                    stepId = "hosp_1",
                    npcSpeakerName = "의사 선생님",
                    npcAvatarEmoji = "‍⚕️",
                    npckoreanText = "어디가 어떻게 불편해서 오셨어요?",
                    npcChineseText = "请问您哪里不舒服呢？",
                    userOptions = listOf(
                        DialogueOption(
                            "h1_a",
                            "어제부터 머리가 아프고 열이 나요.",
                            "从昨天开始头痛，还发烧。",
                            ResponseRating.OPTIMAL,
                            "准确表达病症与发病时间，使用 '-고' 罗列症状，自然流畅。",
                            "hosp_2"
                        ),
                        DialogueOption(
                            "h1_b",
                            "머리 아파. 약 줘.",
                            "头疼，给药。",
                            ResponseRating.INAPPROPRIATE,
                            "在医院与医生对话使用了粗鲁平语。",
                            "hosp_2"
                        ),
                        DialogueOption(
                            "h1_c",
                            "어제부터 머리가 편찮으세요.",
                            "从昨天开始头很不舒服。",
                            ResponseRating.FLAWED,
                            "敬语用词主体错误：'편찮으시다' 只能用于长辈身体不适，自己生病只能用 '아프다'。",
                            "hosp_2"
                        )
                    )
                ),
                "hosp_2" to DialogueStep(
                    stepId = "hosp_2",
                    npcSpeakerName = "의사 선생님",
                    npcAvatarEmoji = "‍⚕️",
                    npckoreanText = "감기 몸살이네요. 따뜻한 물을 많이 드시고 푹 쉬세요.",
                    npcChineseText = "是感冒引起的全身酸痛。请多喝温水并好好休息。",
                    userOptions = listOf(
                        DialogueOption(
                            "h2_a",
                            "네, 알겠습니다. 처방전은 어디서 받나요?",
                            "好的，明白了。请问在哪里拿处方单呢？",
                            ResponseRating.OPTIMAL,
                            "敬语确认并使用 '-나요?' 委婉疑问句，体现高阶会话素养。",
                            null
                        ),
                        DialogueOption(
                            "h2_b",
                            "응, 알았어.",
                            "嗯，知道了。",
                            ResponseRating.INAPPROPRIATE,
                            "使用了极度非敬语词汇 '응' 与 '알았어'。",
                            null
                        ),
                        DialogueOption(
                            "h2_c",
                            "네, 잘 쉬겠습니다. 처방전을 드립니다.",
                            "好的，我会好好休息。给处方单。",
                            ResponseRating.FLAWED,
                            "谦逊动词误用：'드립니다' 是自己给对方，这里向医生索取应使用 '받나요'。",
                            null
                        )
                    )
                )
            )
        ),

        // ── 场景 4：不动产中介租房 (중개인 vs 세입자) ───────────
        DialogueScenario(
            id = "realty",
            title = "不动产中介租房看房",
            categoryName = "租房生活",
            iconEmoji = "",
            npcRole = "부동산 중개인 (中介)",
            userRole = "세입자 (租客)",
            initialStepId = "realty_1",
            steps = mapOf(
                "realty_1" to DialogueStep(
                    stepId = "realty_1",
                    npcSpeakerName = "공인중개사",
                    npcAvatarEmoji = "",
                    npckoreanText = "신촌역 근처에 보증금 500에 월세 45만 원 원룸 보시겠어요?",
                    npcChineseText = "在新村站附近有押金 500 万、月租 45 万韩元的单间公寓，要看看吗？",
                    userOptions = listOf(
                        DialogueOption(
                            "r1_a",
                            "네, 채광이 좋고 관리비가 포함되어 있는지 궁금합니다.",
                            "好的，我想了解一下采光好不好以及是否包含管理费。",
                            ResponseRating.OPTIMAL,
                            "使用格式体敬语 '-습니다' 与 '-ㄴ지 궁금하다' 结构，极具商务签约严谨度。",
                            null
                        ),
                        DialogueOption(
                            "r1_b",
                            "방 보여줘.",
                            "给我看房。",
                            ResponseRating.INAPPROPRIATE,
                            "商业租赁咨询中使用了不可接受的平语。",
                            null
                        ),
                        DialogueOption(
                            "r1_c",
                            "네, 방이 보십시오.",
                            "好的，请房间看。",
                            ResponseRating.FLAWED,
                            "语态与助词混乱：'방을 보겠습니다' 误写为命令句 '보십시오'。",
                            null
                        )
                    )
                )
            )
        ),

        // ── 场景 5：外企求职面试 (면접관 vs 지원자) ─────────────
        DialogueScenario(
            id = "interview",
            title = "外企求职面试与自我介绍",
            categoryName = "求职职场",
            iconEmoji = "",
            npcRole = "면접관 (面试官)",
            userRole = "지원자 (求职者)",
            initialStepId = "int_1",
            steps = mapOf(
                "int_1" to DialogueStep(
                    stepId = "int_1",
                    npcSpeakerName = "면접관",
                    npcAvatarEmoji = "",
                    npckoreanText = "지원자께서는 우리 회사에 지원하게 된 가장 큰 동기가 무엇입니까?",
                    npcChineseText = "应聘者请问您应聘本公司最大的动机是什么？",
                    userOptions = listOf(
                        DialogueOption(
                            "i1_a",
                            "귀사의 글로벌 시장 진출 비전에 매료되어 제 어학 능력을 발휘하고자 지원했습니다.",
                            "我被贵公司的全球化开拓愿景深深吸引，希望发挥自身的语言能力因而提出应聘。",
                            ResponseRating.OPTIMAL,
                            "完美使用商业书面敬语 '귀사(贵公司)'、意图表达 '-고자 하다' 与最高格式体 '-했습니다'。",
                            null
                        ),
                        DialogueOption(
                            "i1_b",
                            "돈 많이 벌고 싶어서 지원했어.",
                            "想多赚钱所以申请了。",
                            ResponseRating.INAPPROPRIATE,
                            "面试场合使用了平语并缺乏职业素养。",
                            null
                        ),
                        DialogueOption(
                            "i1_c",
                            "너희 회사가 유명하기 때문에 들어왔어요.",
                            "因为你们公司出名所以进来了。",
                            ResponseRating.FLAWED,
                            "用词不敬：对公司长辈称呼 '너희' 属于极不礼貌词汇，应用 '귀사' 或 '회사'。",
                            null
                        )
                    )
                )
            )
        )
    )
}
