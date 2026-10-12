package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语语用褒贬与句式制约规则引擎 (PragmaticConstraintEngine)
 * ══════════════════════════════════════════════════════════════════
 */

data class PragmaticRule(
    val badgeLabel: String,          // 标签（如 "消极负面专属", "严禁命令句", "积极回报专属"）
    val isNegativeConstraint: Boolean, // true 为警示/禁止（柔橙/红），false 为推荐/积极（微青）
    val ruleDetail: String           // 详细制约说明
)

object PragmaticConstraintEngine {

    /**
     * 根据语法模式与用法说明，智能匹配语用褒贬与句式限制规则
     */
    fun analyzePragmaticRules(grammarPattern: String, explanation: String): List<PragmaticRule> {
        val rules = mutableListOf<PragmaticRule>()
        val text = "$grammarPattern $explanation"

        // 1. 负面消极结果专属语法
        if (text.contains("는 바람에") || text.contains("느라고") || text.contains("는 탓에") || text.contains("는 통에") || text.contains("기로서니")) {
            rules.add(
                PragmaticRule(
                    badgeLabel = "消极负面专属",
                    isNegativeConstraint = true,
                    ruleDetail = "后句必须是消极、遗憾、不如意或造成损失的不良结果，严禁接正面成就或褒义结果。"
                )
            )
        }

        // 2. 积极回报正面专属语法
        if (text.contains("덕분에") || text.contains("덕택에")) {
            rules.add(
                PragmaticRule(
                    badgeLabel = "积极回报专属",
                    isNegativeConstraint = false,
                    ruleDetail = "多亏了前项的帮助或有利条件，后句必须接积极、成功或带来收益的正面结果。"
                )
            )
        }

        // 3. 严禁命令句与共动句
        if (text.contains("아/어서") || text.contains("느라고") || text.contains("는 바람에") || text.contains("기 때문에") || text.contains("는 탓에")) {
            rules.add(
                PragmaticRule(
                    badgeLabel = "禁命令 / 共动句",
                    isNegativeConstraint = true,
                    ruleDetail = "后句严禁使用命令句 (-(으)십시오 / -(으)세요) 或共动提议句 (-(으)ㅂ시다 / -자)，建议改用 -(으)니까。"
                )
            )
        }

        // 4. 支持命令句与共动句 (如 -(으)니까)
        if (text.contains("으니까") || text.contains("(으)니까")) {
            rules.add(
                PragmaticRule(
                    badgeLabel = "支持命令 / 提议句",
                    isNegativeConstraint = false,
                    ruleDetail = "后句可完美承接命令句 (-(으)세요)、劝诱共动句 (-(으)ㅂ시다) 与问句。"
                )
            )
        }

        // 5. 前后主语一致性制约
        if (text.contains("느라고") || text.contains("으려고") || text.contains("으러") || text.contains("고자")) {
            rules.add(
                PragmaticRule(
                    badgeLabel = "前后主语需一致",
                    isNegativeConstraint = true,
                    ruleDetail = "前句与后句的行为主体（主语）必须为同一人，不可出现主语转换。"
                )
            )
        }

        // 6. 前项时态制约（严禁过去时 -았/었-）
        if (text.contains("아/어서") || text.contains("자마자") || text.contains("러 가다") || text.contains("려고 하다")) {
            rules.add(
                PragmaticRule(
                    badgeLabel = "前项禁过去时",
                    isNegativeConstraint = true,
                    ruleDetail = "前项词干禁止叠加过去时标记 -았/었-，全句时态统一由句末终结谓语体现。"
                )
            )
        }

        return rules.distinctBy { it.badgeLabel }
    }
}