package com.yonsei.koreanlearner.algorithm.essay

/**
 * ══════════════════════════════════════════════════════════════════
 * TOPIK 54 题大作文智能诊断与评分引擎 (TopikEssayEvaluator)
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心诊断指标：
 * 1. 【字数与篇幅】：目标 600~700 字（包含空格/标点）。
 * 2. 【文体基本阶一致性】：TOPIK II 大作文强制要求使用客观书面语「-ㄴ/는다」（해라체），严禁出现口语「-아요/어요」或「-습니다」。
 * 3. 【篇章接续词密度】：检测逻辑衔接词（첫째, 둘째, 반면에, 따라서, 그럼에도 불구하고 等）。
 * 4. 【段落结构完备度】：引言（도입）、正文展开（전개）、总结（결론）三段式结构。
 */

data class EssayDiagnosticReport(
    val charCount: Int,
    val wordCount: Int,
    val paragraphCount: Int,
    val hasCasualEndings: Boolean,
    val casualEndingExamples: List<String>,
    val detectedConnectors: List<String>,
    val estimatedScore: Int, // 满分 50 分（TOPIK 54 写作标准分）
    val scoreGrade: String,  // S / A / B / C / D
    val feedbackComments: List<String>
)

data class TopikPrompt(
    val title: String,
    val topicDescription: String,
    val writingPoints: List<String>,
    val modelEssay: String
)

object TopikEssayEvaluator {

    // 严禁出现在 TOPIK 54 作文中的口语敬语尾词
    private val FORBIDDEN_CASUAL_ENDINGS = listOf(
        "해요", "이에요", "예요", "습니다", "ㅂ니다", "세요", "주세요", "아요", "어요", "했어", "갔어", "먹어"
    )

    // 高分逻辑接续词词典
    private val HIGH_SCORE_CONNECTORS = listOf(
        "첫째", "둘째", "셋째", "반면에", "이에 반해", "따라서", "그러므로",
        "결과적으로", "이와 같이", "그럼에도 불구하고", "뿐만 아니라", "한편",
        "우선", "마지막으로", "결론적으로", "이러한 관점에서"
    )

    // 官方经典真题题库
    val PRESET_PROMPTS = listOf(
        TopikPrompt(
            title = "현대 사회에서의 '성공'의 의미 (现代社会中成功的意义)",
            topicDescription = "现代社会人们对成功的定义各不相同。请就你所认为的成功标准、其理由以及为了实现这种成功需要怎样的态度进行论述。",
            writingPoints = listOf(
                "내가 생각하는 진정한 성공의 기준은 무엇인가?",
                "그렇게 생각하는 이유는 무엇인가?",
                "이러한 성공을 이루기 위해 어떤 자세가 필요한가?"
            ),
            modelEssay = """현대 사회에서 사람들은 저마다 다른 기준으로 성공을 정의한다. 과거에는 높은 사회적 지위나 경제적 부를 획득하는 것만이 성공의 척도로 여겨졌다. 그러나 오늘날 진정한 성공의 의미는 물질적 풍요를 넘어 개인의 내면적 만족과 자아실현으로 확장되고 있다.

진정한 성공이란 자신이 가치 있다고 여기는 일을 주체적으로 선택하고, 그 과정에서 성취감과 보람을 느끼는 것이다. 남들의 시선이나 사회적 기대에 얽매여 살아가는 삶은 겉보기에 화려할지라도 내면의 공허함을 채우기 어렵다. 반면에 자신이 진정으로 원하는 삶을 살아가는 사람은 어떠한 역경 속에서도 흔들리지 않는 내적 평화를 누릴 수 있다.

이러한 성공을 이루기 위해서는 첫째로 끊임없는 자기 성찰이 필요하다. 타인의 평가에 휘둘리지 않고 자신의 신념과 흥미를 탐색해야 한다. 둘째로 실패를 두려워하지 않는 도전 정신을 갖추어야 한다. 실패는 좌절의 이유가 아니라 성장을 위한 소중한 밑거름이기 때문이다. 결론적으로 진정한 성공은 외적인 결과가 아닌, 자신의 가치를 실현해 나가는 부단한 과정 그 자체에 있다."""
        ),
        TopikPrompt(
            title = "청소년 독서의 중요성과 활성화 방안 (青少年阅读的重要性与推广方案)",
            topicDescription = "随着数字媒体的发展，青少年阅读量逐渐减少。请针对青少年阅读的重要性以及如何提高青少年的阅读兴趣进行论述。",
            writingPoints = listOf(
                "청소년 시기에 독서가 중요한 이유는 무엇인가?",
                "청소년들의 독서량이 감소하는 원인은 무엇인가?",
                "청소년 독서를 활성화하기 위한 구체적 방안은 무엇인가?"
            ),
            modelEssay = """디지털 매체의 발달로 인해 현대 청소년들의 독서량이 급격히 감소하고 있다. 청소년기는 인격이 형성되고 사고력이 확장되는 결정적인 시기이므로 독서의 가치는 아무리 강조해도 지나치지 않다.

청소년 시기의 독서는 비판적 사고력과 공감 능력을 기르는 데 핵심적인 역할을 한다. 다양한 책을 접함으로써 세상을 바라보는 넓은 안목을 형성할 수 있으며, 타인의 삶을 간접적으로 체험하며 타인을 이해하는 성숙한 태도를 배우게 된다. 그러나 입시 위주의 교육 환경과 스마트폰의 과도한 사용으로 인해 청소년들이 책을 멀리하고 있는 것이 현실이다.

이를 해결하기 위해서는 첫째로 가정과 학교에서 책 읽는 문화를 조성해야 한다. 단순한 독서 강요가 아닌 자유로운 토론과 나눔이 있는 독서 프로그램을 마련해야 한다. 둘째로 디지털 기기를 활용한 전자책 등 흥미를 유발할 수 있는 다채로운 독서 환경을 제공해야 한다. 결론적으로 청소년 독서 활성화는 미래 사회를 이끌어갈 건강한 인재를 양성하는 밑거름이 될 것이다."""
        )
    )

    /**
     * 对输入的韩语大作文进行全自动多维智能诊断
     */
    fun evaluate(text: String): EssayDiagnosticReport {
        val trimmed = text.trim()
        val charCount = trimmed.length
        val words = if (trimmed.isEmpty()) emptyList() else trimmed.split("\\s+".toRegex())
        val wordCount = words.size
        val paragraphs = if (trimmed.isEmpty()) emptyList() else trimmed.split("\n\n").filter { it.isNotBlank() }
        val paragraphCount = paragraphs.size

        // 1. 文体基本阶违规检测
        val foundCasual = mutableListOf<String>()
        FORBIDDEN_CASUAL_ENDINGS.forEach { ending ->
            if (trimmed.contains(ending)) {
                foundCasual.add(ending)
            }
        }

        // 2. 接续词检测
        val detectedConnectors = HIGH_SCORE_CONNECTORS.filter { trimmed.contains(it) }

        // 3. 智能评分模型 (满分 50 分)
        var score = 0
        val feedback = mutableListOf<String>()

        // 字数分 (15分)
        when {
            charCount in 550..750 -> {
                score += 15
                feedback.add("✅ 字数控制完美（${charCount}字），符合 600~700 标准要求。")
            }
            charCount in 400..549 -> {
                score += 10
                feedback.add("⚠️ 篇幅偏短（${charCount}字），建议充实正文论据至 600 字以上。")
            }
            charCount > 750 -> {
                score += 11
                feedback.add("⚠️ 篇幅略有超出（${charCount}字），注意把握考场时间与格子限制。")
            }
            charCount > 0 -> {
                score += 5
                feedback.add("❌ 篇幅严重不足（${charCount}字），低于 400 字将被大幅扣分。")
            }
            else -> {
                feedback.add("💡 请在上方编辑框输入作文，或选择下方真题开始作答。")
            }
        }

        // 文体基本阶分 (15分)
        if (foundCasual.isEmpty() && charCount > 50) {
            score += 15
            feedback.add("✅ 全文严格使用客观书面语「-ㄴ/는다」（해라체），文体规范纯正。")
        } else if (foundCasual.isNotEmpty()) {
            score += 4
            feedback.add("❌ 发现口语敬语尾词（如：${foundCasual.take(3).joinToString(", ")}），TOPIK 54 大作文严禁使用敬语，请统一改为基本阶。")
        }

        // 篇章结构分 (10分)
        if (paragraphCount in 3..4) {
            score += 10
            feedback.add("✅ 具备标准的「引言 - 正文展开 - 总结」三段式黄金结构。")
        } else if (paragraphCount > 0) {
            score += 5
            feedback.add("⚠️ 段落结构建议划分为清晰的 3~4 个自然段（空一行分段）。")
        }

        // 逻辑连接词分 (10分)
        if (detectedConnectors.size >= 3) {
            score += 10
            feedback.add("✅ 逻辑衔接词丰富（${detectedConnectors.joinToString(", ")}），篇章逻辑清晰。")
        } else if (detectedConnectors.isNotEmpty()) {
            score += 6
            feedback.add("💡 建议增加逻辑接续词（如：첫째, 반면에, 따라서, 결과적으로）增强段落连贯性。")
        }

        val grade = when {
            score >= 45 -> "S (卓越 45+)"
            score >= 38 -> "A (优秀 38~44)"
            score >= 30 -> "B (良好 30~37)"
            score >= 20 -> "C (中等 20~29)"
            else -> "D (需提升)"
        }

        return EssayDiagnosticReport(
            charCount = charCount,
            wordCount = wordCount,
            paragraphCount = paragraphCount,
            hasCasualEndings = foundCasual.isNotEmpty(),
            casualEndingExamples = foundCasual,
            detectedConnectors = detectedConnectors,
            estimatedScore = score,
            scoreGrade = grade,
            feedbackComments = feedback
        )
    }
}
