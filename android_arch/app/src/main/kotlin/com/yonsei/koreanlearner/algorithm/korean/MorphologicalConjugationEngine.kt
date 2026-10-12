package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语形态学接续与不规则变形警示引擎 (MorphologicalConjugationEngine)
 * ══════════════════════════════════════════════════════════════════
 */

data class MorphologicalAlert(
    val type: String,               // 变化类型（如 "ㅂ 不规则", "ㄹ 脱落"）
    val condition: String,          // 触发条件（如 "遇元音 -아/어 接续"）
    val ruleSummary: String,        // 变形要点说明
    val exampleBefore: String,      // 词干 + 语法原形
    val exampleAfter: String        // 最终合体标准形态
)

object MorphologicalConjugationEngine {

    /**
     * 根据语法模式或接续公式，智能提取该语法涉及的形态学变形警示列表
     */
    fun analyzeConjugationAlerts(grammarPattern: String, conjugationRules: String): List<MorphologicalAlert> {
        val alerts = mutableListOf<MorphologicalAlert>()
        val text = "$grammarPattern $conjugationRules"

        // 1. 触发 -아/어/여 型元音接续的语法
        if (text.contains("아/어") || text.contains("아서") || text.contains("아야") || text.contains("았다") || text.contains("아요")) {
            alerts.add(
                MorphologicalAlert(
                    type = "ㅂ 不规则",
                    condition = "遇元音 -아/어 接续",
                    ruleSummary = "词干末尾 ㅂ 变为 워 / 와",
                    exampleBefore = "돕다 + -아/어서",
                    exampleAfter = "도와서 (고마워요)"
                )
            )
            alerts.add(
                MorphologicalAlert(
                    type = "ㄷ 不规则",
                    condition = "遇元音 -아/어 接续",
                    ruleSummary = "词干末尾 ㄷ 变为 ㄹ",
                    exampleBefore = "듣다 + -아/어서",
                    exampleAfter = "들어서 (들어요)"
                )
            )
            alerts.add(
                MorphologicalAlert(
                    type = "ㅅ 不规则",
                    condition = "遇元音 -아/어 接续",
                    ruleSummary = "词干末尾 ㅅ 脱落，但不发生元音收缩",
                    exampleBefore = "낫다 + -아/어서",
                    exampleAfter = "나아서 (나아요)"
                )
            )
            alerts.add(
                MorphologicalAlert(
                    type = "르 不规则",
                    condition = "遇元音 -아/어 接续",
                    ruleSummary = "词干 르 变为 ㄹ라 / ㄹ러，前一音节添底辅音 ㄹ",
                    exampleBefore = "모르다 + -아/어서",
                    exampleAfter = "몰라서 (몰라요)"
                )
            )
        }

        // 2. 触发 -(으) 型元音接续的语法
        if (text.contains("(으)") || text.contains("으면") || text.contains("으니까") || text.contains("으려고") || text.contains("으러") || text.contains("을 수")) {
            alerts.add(
                MorphologicalAlert(
                    type = "ㄹ 脱落",
                    condition = "遇辅音 ㄴ, ㅂ, ㅅ 或 -(으) 接续",
                    ruleSummary = "词干末尾底辅音 ㄹ 直接脱落，无需插入 으",
                    exampleBefore = "살다 + -(으)면",
                    exampleAfter = "살면 (사는 / 삽니다)"
                )
            )
            alerts.add(
                MorphologicalAlert(
                    type = "ㅂ 不规则",
                    condition = "遇辅音/元音 -(으) 接续",
                    ruleSummary = "词干末尾 ㅂ 变为 우",
                    exampleBefore = "춥다 + -(으)니까",
                    exampleAfter = "추우니까 (추우면)"
                )
            )
            alerts.add(
                MorphologicalAlert(
                    type = "ㄷ 不规则",
                    condition = "遇元音 -(으) 接续",
                    ruleSummary = "词干末尾 ㄷ 变为 ㄹ",
                    exampleBefore = "걷다 + -(으)면",
                    exampleAfter = "걸으면 (걸으세요)"
                )
            )
            alerts.add(
                MorphologicalAlert(
                    type = "ㅅ 不规则",
                    condition = "遇元音 -(으) 接续",
                    ruleSummary = "词干末尾 ㅅ 脱落，保留 으",
                    exampleBefore = "짓다 + -(으)면",
                    exampleAfter = "지으면 (지으세요)"
                )
            )
        }

        // 3. 触发 辅音 ㄴ/는 接续的语法
        if (text.contains("는데") || text.contains("는 바람") || text.contains("는 탓") || text.contains("느라고") || text.contains("는 동안")) {
            if (alerts.none { it.type == "ㄹ 脱落" }) {
                alerts.add(
                    MorphologicalAlert(
                        type = "ㄹ 脱落",
                        condition = "遇辅音 ㄴ, 는 接续",
                        ruleSummary = "动词词干末尾 ㄹ 直接脱落后加 -는",
                        exampleBefore = "만들다 + -는데",
                        exampleAfter = "만드는데 (만드는 바람에)"
                    )
                )
            }
        }

        // 4. 触发 ㅎ 不规则形容词
        if (text.contains("으면") || text.contains("아서") || text.contains("(으)ㄴ데") || text.contains("은/는")) {
            alerts.add(
                MorphologicalAlert(
                    type = "ㅎ 不规则",
                    condition = "遇元音接续",
                    ruleSummary = "ㅎ 脱落；遇 -아/어 时变为 애/얘",
                    exampleBefore = "그렇다 + -아/어서",
                    exampleAfter = "그래서 (그러면)"
                )
            )
        }

        return alerts.distinctBy { it.type }
    }
}