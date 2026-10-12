package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语敬语/谦语/助词成对映射与纠错引擎 (HonorificCorrectionEngine)
 * ══════════════════════════════════════════════════════════════════
 */
object HonorificCorrectionEngine {

    /** 敬语/谦语词汇映射条目 */
    data class HonorificPair(
        val plainText         : String,             // 平语/普通形（如 "밥", "먹다", "이/가"）
        val honorificText     : String,             // 尊称/谦逊形（如 "진지", "드시다", "께서"）
        val category          : HonorificCategory,  // 分类
        val explanation       : String              // 语言学说明
    )

    enum class HonorificCategory(val displayName: String) {
        NOUN_HONORIFIC("名词尊称"),
        VERB_SUBJECT_HONORIFIC("主体尊称动词"),
        VERB_HUMBLE("客体谦逊动词"),
        PARTICLE_HONORIFIC("敬语助词格")
    }

    /** 逐词纠错比对详情 */
    data class HonorificDiff(
        val originalToken     : String,             // 原文词块（如 "밥을"）
        val correctedToken    : String,             // 敬语纠正词块（如 "진지를"）
        val pairRule          : HonorificPair,      // 匹配到的敬语规则
        val startIndex        : Int                 // 句子中的起始字符索引
    )

    /** 整句转换与诊断报告 */
    data class HonorificCorrectionReport(
        val originalSentence  : String,             // 输入原句
        val correctedSentence : String,             // 转换后的最高敬语规范句
        val diffList          : List<HonorificDiff>,// 发生转换的词元清单
        val totalReplacements : Int,                // 替换词元总数
        val explanation       : String              // 综合语法解析
    )

    // ── 1. 语言学成对映射规则字典库 ────────────────────────────

    val NOUN_PAIRS = listOf(
        HonorificPair("나이", "연세", HonorificCategory.NOUN_HONORIFIC, "长辈的年龄尊称为 '연세'。"),
        HonorificPair("이름", "성함", HonorificCategory.NOUN_HONORIFIC, "长辈的姓名尊称为 '성함'。"),
        HonorificPair("집", "댁", HonorificCategory.NOUN_HONORIFIC, "长辈的家/宅邸尊称为 '댁'。"),
        HonorificPair("말", "말씀", HonorificCategory.NOUN_HONORIFIC, "长辈的话尊称为 '말씀'（自己对长辈说话谦称也用 '말씀'）。"),
        HonorificPair("밥", "진지", HonorificCategory.NOUN_HONORIFIC, "长辈的饭食尊称为 '진지'。"),
        HonorificPair("생일", "생신", HonorificCategory.NOUN_HONORIFIC, "长辈的生日尊称为 '생신'。"),
        HonorificPair("사람", "분", HonorificCategory.NOUN_HONORIFIC, "称呼长辈/客人的量词尊称为 '분(位)'。"),
        HonorificPair("아내", "부인", HonorificCategory.NOUN_HONORIFIC, "对方的妻子尊称为 '부인'。")
    )

    val VERB_SUBJECT_PAIRS = listOf(
        HonorificPair("자다", "주무시다", HonorificCategory.VERB_SUBJECT_HONORIFIC, "主语是长辈睡觉时，专用尊称动词 '주무시다'。"),
        HonorificPair("먹다", "드시다", HonorificCategory.VERB_SUBJECT_HONORIFIC, "主语是长辈进餐时，专用尊称动词 '드시다'（或 잡수시다）。"),
        HonorificPair("마시다", "드시다", HonorificCategory.VERB_SUBJECT_HONORIFIC, "主语是长辈饮水喝茶时，专用尊称动词 '드시다'。"),
        HonorificPair("있다", "계시다", HonorificCategory.VERB_SUBJECT_HONORIFIC, "长辈在某处（存在）时用 '계시다'；若长辈拥有某物则用 '있으시다'。"),
        HonorificPair("아프다", "편찮으시다", HonorificCategory.VERB_SUBJECT_HONORIFIC, "长辈身体不适/生病时，专用尊称形容词 '편찮으시다'。"),
        HonorificPair("죽다", "돌아가시다", HonorificCategory.VERB_SUBJECT_HONORIFIC, "长辈去世时，专用委婉尊称动词 '돌아가시다'。")
    )

    val VERB_HUMBLE_PAIRS = listOf(
        HonorificPair("주다", "드리다", HonorificCategory.VERB_HUMBLE, "将物品给予长辈时，自身动作谦称为 '드리다'。"),
        HonorificPair("데리다", "모시다", HonorificCategory.VERB_HUMBLE, "陪伴/护送长辈同行时，使用谦逊动词 '모시다'。"),
        HonorificPair("보다", "뵙다", HonorificCategory.VERB_HUMBLE, "拜见/会见长辈时，自身动作谦称为 '뵙다'（뵈다）。"),
        HonorificPair("만나다", "뵙다", HonorificCategory.VERB_HUMBLE, "与长辈见面时，使用谦逊动词 '뵙다'。"),
        HonorificPair("말하다", "말씀드리다", HonorificCategory.VERB_HUMBLE, "对长辈说话/汇报时，使用谦逊表达 '말씀드리다'。"),
        HonorificPair("묻다", "여쭙다", HonorificCategory.VERB_HUMBLE, "向长辈请教/询问时，使用谦逊动词 '여쭙다'。")
    )

    val PARTICLE_PAIRS = listOf(
        HonorificPair("이가", "께서", HonorificCategory.PARTICLE_HONORIFIC, "长辈做主语时，主格助词 이/가 替换为 '께서'。"),
        HonorificPair("은는", "께서는", HonorificCategory.PARTICLE_HONORIFIC, "长辈做主题时，主题助词 은/는 替换为 '께서는'。"),
        HonorificPair("에게", "께", HonorificCategory.PARTICLE_HONORIFIC, "对象是长辈时，与格/给予助词 에게/한테 替换为 '께'。"),
        HonorificPair("한테", "께", HonorificCategory.PARTICLE_HONORIFIC, "对象是长辈时，口语助词 한테 替换为敬语 '께'。")
    )

    // ════════════════════════════════════════════════════════════
    // 2. 核心智能诊断与转换算法
    // ════════════════════════════════════════════════════════════

    /**
     * 将包含长辈平语句子自动纠正诊断为最高敬语规范句
     *
     * @param sentence 平语或混用错句（如 "할아버지가 밥을 먹고 집에서 자요."）
     */
    fun correctToHonorific(sentence: String): HonorificCorrectionReport {
        if (sentence.isBlank()) {
            return HonorificCorrectionReport(sentence, sentence, emptyList(), 0, "句子为空")
        }

        var resultText = sentence
        val diffList = mutableListOf<HonorificDiff>()

        // 1. 助词敬语替换（가/이 -> 께서, 에게/한테 -> 께）
        val honorificSubjects = listOf("할아버지", "할머니", "선생님", "아버지", "어머니", "부모님", "사장님", "교수님")
        for (subj in honorificSubjects) {
            if (resultText.contains("${subj}가")) {
                val orig = "${subj}가"
                val corr = "${subj}께서"
                diffList.add(HonorificDiff(orig, corr, PARTICLE_PAIRS[0], resultText.indexOf(orig)))
                resultText = resultText.replace(orig, corr)
            } else if (resultText.contains("${subj}이 ")) {
                val orig = "${subj}이"
                val corr = "${subj}께서"
                diffList.add(HonorificDiff(orig, corr, PARTICLE_PAIRS[0], resultText.indexOf(orig)))
                resultText = resultText.replace(orig, corr)
            } else if (resultText.contains("${subj}는")) {
                val orig = "${subj}는"
                val corr = "${subj}께서는"
                diffList.add(HonorificDiff(orig, corr, PARTICLE_PAIRS[1], resultText.indexOf(orig)))
                resultText = resultText.replace(orig, corr)
            } else if (resultText.contains("${subj}은")) {
                val orig = "${subj}은"
                val corr = "${subj}께서는"
                diffList.add(HonorificDiff(orig, corr, PARTICLE_PAIRS[1], resultText.indexOf(orig)))
                resultText = resultText.replace(orig, corr)
            }

            if (resultText.contains("${subj}한테")) {
                val orig = "${subj}한테"
                val corr = "${subj}께"
                diffList.add(HonorificDiff(orig, corr, PARTICLE_PAIRS[3], resultText.indexOf(orig)))
                resultText = resultText.replace(orig, corr)
            } else if (resultText.contains("${subj}에게")) {
                val orig = "${subj}에게"
                val corr = "${subj}께"
                diffList.add(HonorificDiff(orig, corr, PARTICLE_PAIRS[2], resultText.indexOf(orig)))
                resultText = resultText.replace(orig, corr)
            }
        }

        // 2. 名词尊称替换（밥을 -> 진지를, 집에서 -> 댁에서, 나이 -> 연세）
        if (resultText.contains("밥을")) {
            val orig = "밥을"
            val corr = "진지를"
            diffList.add(HonorificDiff(orig, corr, NOUN_PAIRS[4], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        } else if (resultText.contains("밥이")) {
            val orig = "밥이"
            val corr = "진지가"
            diffList.add(HonorificDiff(orig, corr, NOUN_PAIRS[4], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        if (resultText.contains("집에서")) {
            val orig = "집에서"
            val corr = "댁에서"
            diffList.add(HonorificDiff(orig, corr, NOUN_PAIRS[2], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        if (resultText.contains("나이가")) {
            val orig = "나이가"
            val corr = "연세가"
            diffList.add(HonorificDiff(orig, corr, NOUN_PAIRS[0], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        if (resultText.contains("이름이")) {
            val orig = "이름이"
            val corr = "성함이"
            diffList.add(HonorificDiff(orig, corr, NOUN_PAIRS[1], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        // 3. 动词尊称替换（먹고 -> 드시고, 자요 -> 주무세요, 있어요 -> 계세요）
        if (resultText.contains("먹고")) {
            val orig = "먹고"
            val corr = "드시고"
            diffList.add(HonorificDiff(orig, corr, VERB_SUBJECT_PAIRS[1], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        } else if (resultText.contains("먹어요") || resultText.contains("먹었습니다")) {
            val orig = if (resultText.contains("먹었습니다")) "먹었습니다" else "먹어요"
            val corr = if (orig == "먹었습니다") "드셨습니다" else "드세요"
            diffList.add(HonorificDiff(orig, corr, VERB_SUBJECT_PAIRS[1], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        if (resultText.contains("자요") || resultText.contains("잡니다")) {
            val orig = if (resultText.contains("잡니다")) "잡니다" else "자요"
            val corr = if (orig == "잡니다") "주무십니다" else "주무세요"
            diffList.add(HonorificDiff(orig, corr, VERB_SUBJECT_PAIRS[0], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        if (resultText.contains("있어요") && (sentence.contains("할아버지") || sentence.contains("선생님"))) {
            val orig = "있어요"
            val corr = "계세요"
            diffList.add(HonorificDiff(orig, corr, VERB_SUBJECT_PAIRS[3], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        // 4. 谦逊动词替换（줘요 -> 드려요, 만나요 -> 봬요）
        if (resultText.contains("줘요") && resultText.contains("께")) {
            val orig = "줘요"
            val corr = "드려요"
            diffList.add(HonorificDiff(orig, corr, VERB_HUMBLE_PAIRS[0], resultText.indexOf(orig)))
            resultText = resultText.replace(orig, corr)
        }

        val explanation = buildString {
            append("本句共诊断出 ${diffList.size} 处敬语/谦语转换项：\n")
            diffList.forEachIndexed { i, d ->
                append("${i + 1}. [${d.pairRule.category.displayName}] '${d.originalToken}' 应转换为 '${d.correctedToken}'。原因: ${d.pairRule.explanation}\n")
            }
        }

        return HonorificCorrectionReport(
            originalSentence = sentence,
            correctedSentence = resultText,
            diffList = diffList,
            totalReplacements = diffList.size,
            explanation = explanation.trim()
        )
    }
}
