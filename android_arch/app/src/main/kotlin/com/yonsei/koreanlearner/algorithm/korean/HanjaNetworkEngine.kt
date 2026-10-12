package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 汉字词词根知识图谱领域模型 (Hanja Models)
 * ══════════════════════════════════════════════════════════════════
 */

/**
 * 单个汉字词根模型（如 "학", "學", "学习/学术"）
 */
data class HanjaRoot(
    val koreanChar    : Char,               // 韩文字（如 '학'）
    val hanjaChar     : String,             // 对应汉字（如 "學"）
    val chineseMeaning: String,             // 核心义项（如 "学习 · 学问"）
    val strokeCount   : Int = 16,           // 汉字笔画数
    val frequencyRank : Int = 1,            // 延世词频等级 1~6
    val radicalTag    : String = "教育部常用 1800 汉字"
)

/**
 * 派生词汇模型（如 "학생", "學生", "学生"）
 */
data class DerivedHanjaWord(
    val id            : Int,                // 词汇 ID
    val korean        : String,             // 韩文词汇（如 "대학교"）
    val hanja         : String,             // 汉字形式（如 "大學校"）
    val breakdown     : String,             // 拆解注音（如 "대(大) + 학(學) + 교(校)"）
    val chinese       : String,             // 中文释义（如 "大学"）
    val partOfSpeech  : String = "名词",     // 词性
    val book          : Int = 1,            // 延世教材册数
    val lesson        : Int = 1,            // 课时
    val level         : String = "TOPIK 1"  // 等级
)

/**
 * 辐射图谱网络节点包装体（用于 Canvas 渲染）
 */
data class HanjaRadialNetwork(
    val centerRoot     : HanjaRoot,                 // 中心词根
    val derivedWords   : List<DerivedHanjaWord>,    // 辐射外围派生词
    val relatedRoots   : List<HanjaRoot> = emptyList() // 关联二级词根（如 学 与 校、院 的交集）
)

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世汉字词词根关联知识图谱核心引擎 (HanjaNetworkEngine)
 * ══════════════════════════════════════════════════════════════════
 */
object HanjaNetworkEngine {

    // ── 1. 核心词根字典库 ──────────────────────────────────────

    val ROOTS_DATABASE = listOf(
        HanjaRoot('학', "學", "学习 · 学问 · 学术", 16, 1),
        HanjaRoot('원', "院", "机构 · 庭院 · 场所", 10, 1),
        HanjaRoot('회', "會", "聚集 · 聚会 · 组织", 13, 1),
        HanjaRoot('식', "食", "饮食 · 进食 / 仪式", 9, 1),
        HanjaRoot('사', "社", "社会 · 会社 · 团体", 8, 1),
        HanjaRoot('력', "力", "力量 · 能力 · 效力", 2, 2),
        HanjaRoot('인', "人", "人类 · 人物 · 人格", 2, 1),
        HanjaRoot('국', "國", "国家 · 国度 · 国土", 11, 1),
        HanjaRoot('교', "校", "学校 · 教授 · 校正", 10, 1),
        HanjaRoot('생', "生", "生命 · 学生 · 诞生", 5, 1),
        HanjaRoot('업', "業", "事业 · 职业 · 学业", 13, 2),
        HanjaRoot('문', "門", "大门 · 部门 · 专业", 8, 2)
    )

    // ── 2. 词汇关联衍生库 ──────────────────────────────────────

    val WORDS_DATABASE = listOf(
        // 学 (학)
        DerivedHanjaWord(101, "학생", "學生", "학(學) + 생(生)", "学生", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(102, "학교", "學校", "학(學) + 교(校)", "学校", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(103, "대학교", "大學校", "대(大) + 학(學) + 교(校)", "大学", "名词", 1, 2, "TOPIK 1"),
        DerivedHanjaWord(104, "학원", "學院", "학(學) + 원(院)", "学院，补习班", "名词", 1, 5, "TOPIK 1"),
        DerivedHanjaWord(105, "장학생", "奬學生", "장(奬) + 학(學) + 생(生)", "奖学金获得者", "名词", 2, 3, "TOPIK 2"),
        DerivedHanjaWord(106, "입학하다", "入學-", "입(入) + 학(學)", "入学", "动词", 1, 10, "TOPIK 1"),
        DerivedHanjaWord(107, "경제학", "經濟學", "경(經) + 제(濟) + 학(學)", "经济学", "名词", 3, 1, "TOPIK 3"),
        DerivedHanjaWord(108, "역사학", "歷史學", "역(歷) + 사(史) + 학(學)", "历史学", "名词", 3, 2, "TOPIK 3"),
        DerivedHanjaWord(109, "유학", "留學", "유(留) + 학(學)", "留学", "名词", 2, 8, "TOPIK 2"),
        DerivedHanjaWord(110, "수학", "數學", "수(數) + 학(學)", "数学", "名词", 2, 4, "TOPIK 2"),

        // 院 (원)
        DerivedHanjaWord(201, "병원", "病院", "병(病) + 원(院)", "医院", "名词", 1, 3, "TOPIK 1"),
        DerivedHanjaWord(202, "대학원", "大學院", "대(大) + 학(學) + 원(院)", "研究生院", "名词", 2, 1, "TOPIK 2"),
        DerivedHanjaWord(203, "연구원", "硏究院", "연(硏) + 구(究) + 원(院)", "研究院，研究员", "名词", 3, 4, "TOPIK 3"),
        DerivedHanjaWord(204, "입원하다", "入院-", "입(入) + 원(院)", "住院", "动词", 2, 6, "TOPIK 2"),
        DerivedHanjaWord(205, "퇴원하다", "退院-", "퇴(退) + 원(院)", "出院", "动词", 2, 6, "TOPIK 2"),
        DerivedHanjaWord(206, "법원", "法院", "법(法) + 원(院)", "法院", "名词", 4, 2, "TOPIK 4"),
        DerivedHanjaWord(207, "공원", "公園", "공(公) + 원(園)", "公园", "名词", 1, 2, "TOPIK 1"),

        // 会 (회)
        DerivedHanjaWord(301, "회사", "會社", "회(會) + 사(社)", "公司", "名词", 1, 2, "TOPIK 1"),
        DerivedHanjaWord(302, "사회", "社會", "사(社) + 회(會)", "社会", "名词", 2, 5, "TOPIK 2"),
        DerivedHanjaWord(303, "회의", "會議", "회(會) + 의(議)", "会议", "名词", 2, 1, "TOPIK 2"),
        DerivedHanjaWord(304, "회식", "會食", "회(會) + 식(食)", "聚餐", "名词", 2, 7, "TOPIK 2"),
        DerivedHanjaWord(305, "회원", "會員", "회(會) + 원(員)", "会员", "名词", 2, 3, "TOPIK 2"),
        DerivedHanjaWord(306, "기회", "機會", "기(機) + 회(會)", "机会", "名词", 3, 2, "TOPIK 3"),
        DerivedHanjaWord(307, "대회", "大會", "대(大) + 회(會)", "大会，比赛", "名词", 3, 5, "TOPIK 3"),

        // 食 (식)
        DerivedHanjaWord(401, "식사", "食事", "식(食) + 사(事)", "用餐，就餐", "名词", 1, 3, "TOPIK 1"),
        DerivedHanjaWord(402, "음식", "飮食", "음(飮) + 식(食)", "食物，饮食", "名词", 1, 2, "TOPIK 1"),
        DerivedHanjaWord(403, "한식", "韓食", "한(韓) + 식(食)", "韩餐", "名词", 1, 4, "TOPIK 1"),
        DerivedHanjaWord(404, "간식", "間食", "간(間) + 식(食)", "零食，点心", "名词", 2, 2, "TOPIK 2"),
        DerivedHanjaWord(405, "결혼식", "結婚式", "결(結) + 혼(婚) + 식(式)", "结婚典礼", "名词", 2, 5, "TOPIK 2"),

        // 力 (력)
        DerivedHanjaWord(501, "능력", "能力", "능(能) + 력(力)", "能力", "名词", 3, 1, "TOPIK 3"),
        DerivedHanjaWord(502, "노력", "努力", "노(努) + 력(力)", "努力", "名词", 2, 3, "TOPIK 2"),
        DerivedHanjaWord(503, "체력", "體力", "체(體) + 력(力)", "体力", "名词", 3, 6, "TOPIK 3"),
        DerivedHanjaWord(504, "집중력", "集中力", "집(集) + 중(中) + 력(力)", "注意力，专注力", "名词", 4, 1, "TOPIK 4"),
        DerivedHanjaWord(505, "기억력", "記憶力", "기(記) + 억(億) + 력(力)", "记忆力", "名词", 4, 3, "TOPIK 4"),

        // 人 (인)
        DerivedHanjaWord(601, "한국인", "韓國人", "한(韓) + 국(國) + 인(人)", "韩国人", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(602, "외국인", "外國人", "외(外) + 국(國) + 인(人)", "外国人", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(603, "인기", "人氣", "인(人) + 기(氣)", "人气，受欢迎", "名词", 2, 2, "TOPIK 2"),
        DerivedHanjaWord(604, "인구", "人口", "인(人) + 구(口)", "人口", "名词", 3, 2, "TOPIK 3"),

        // 国 (국)
        DerivedHanjaWord(701, "한국", "韓國", "한(韓) + 국(國)", "韩国", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(702, "중국", "中國", "중(中) + 국(國)", "中国", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(703, "외국", "外國", "외(外) + 국(國)", "外国", "名词", 1, 1, "TOPIK 1"),
        DerivedHanjaWord(704, "국제", "國際", "국(國) + 제(際)", "国际", "名词", 3, 3, "TOPIK 3")
    )

    // ════════════════════════════════════════════════════════════
    // 3. 知识图谱查询与分解算法
    // ════════════════════════════════════════════════════════════

    /**
     * 根据单字词根获取辐射知识网络
     */
    fun getNetworkByRoot(rootChar: Char): HanjaRadialNetwork {
        val root = ROOTS_DATABASE.firstOrNull { it.koreanChar == rootChar }
            ?: HanjaRoot(rootChar, "漢", "汉字义项", 10, 1)

        val derived = WORDS_DATABASE.filter { it.korean.contains(rootChar) }
            .sortedBy { it.book * 100 + it.lesson }

        // 查找关联的二级词根
        val relatedCharSet = mutableSetOf<Char>()
        derived.forEach { word ->
            word.korean.forEach { c ->
                if (c != rootChar && c.toString().matches(Regex("[가-힣]"))) {
                    relatedCharSet.add(c)
                }
            }
        }
        val relatedRoots = ROOTS_DATABASE.filter { relatedCharSet.contains(it.koreanChar) }

        return HanjaRadialNetwork(
            centerRoot = root,
            derivedWords = derived,
            relatedRoots = relatedRoots
        )
    }

    /**
     * 智能分解汉字词（如输入 "대학교" -> 分解出 대(大), 학(學), 교(校)）
     */
    fun decomposeHanjaWord(koreanWord: String): List<HanjaRadialNetwork> {
        val clean = koreanWord.trim()
        val networks = mutableListOf<HanjaRadialNetwork>()

        for (c in clean) {
            val root = ROOTS_DATABASE.firstOrNull { it.koreanChar == c }
            if (root != null) {
                networks.add(getNetworkByRoot(c))
            }
        }
        return networks
    }

    /**
     * 全局模糊搜索词根或词汇
     */
    fun searchHanja(query: String): List<HanjaRoot> {
        val q = query.trim()
        if (q.isBlank()) return ROOTS_DATABASE

        return ROOTS_DATABASE.filter {
            it.koreanChar.toString().contains(q) ||
            it.hanjaChar.contains(q) ||
            it.chineseMeaning.contains(q)
        }
    }
}
