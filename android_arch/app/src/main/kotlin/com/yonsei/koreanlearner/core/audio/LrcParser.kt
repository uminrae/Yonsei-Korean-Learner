package com.yonsei.koreanlearner.core.audio

import com.yonsei.koreanlearner.algorithm.korean.KoreanTokenizer

/**
 * ══════════════════════════════════════════════════════════════════
 * 标准 LRC / SRT 歌词字幕解析器与智能挖空生成引擎 (LrcParser)
 * ══════════════════════════════════════════════════════════════════
 */
object LrcParser {

    /**
     * 单行歌词/台词数据模型
     */
    data class LrcLine(
        val index          : Int,               // 行号索引
        val startTimeMs    : Long,              // 起始时间戳 (ms)
        val endTimeMs      : Long,              // 结束时间戳 (ms)
        val koreanText     : String,            // 韩文原文字幕
        val chineseText    : String? = null,    // 中文翻译对照
        val blankQuestion  : BlankQuestion? = null // 自动生成的听写挖空题
    )

    /**
     * 听写挖空题模型
     */
    data class BlankQuestion(
        val fullKorean     : String,            // 完整句
        val maskedKorean   : String,            // 挖空后的句子（带 ___ 占位）
        val targetAnswer   : String,            // 挖空的正确词汇（如 "기 때문에", "을", "도와줘"）
        val targetIndex    : Int,               // 在句子中的单词索引
        val phoneticTip    : String? = null     // 连音化/紧音化发音提示（如 "[거러요]"）
    )

    private val TIME_REGEX = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2,3}))?\\]")

    /**
     * 解析标准 LRC 歌词文本
     *
     * @param lrcContent LRC 文本内容
     */
    fun parseLrc(lrcContent: String): List<LrcLine> {
        if (lrcContent.isBlank()) return emptyList()

        val rawLines = lrcContent.lines()
        val parsedEntries = mutableListOf<Pair<Long, String>>()

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") || trimmed.startsWith("[al:")) {
                continue
            }

            val match = TIME_REGEX.find(trimmed) ?: continue
            val minutes = match.groupValues[1].toLongOrNull() ?: 0L
            val seconds = match.groupValues[2].toLongOrNull() ?: 0L
            val millisRaw = match.groupValues[3]
            val millis = when (millisRaw.length) {
                2 -> (millisRaw.toLongOrNull() ?: 0L) * 10
                3 -> millisRaw.toLongOrNull() ?: 0L
                else -> 0L
            }

            val timestampMs = minutes * 60 * 1000 + seconds * 1000 + millis
            val textContent = trimmed.substring(match.range.last + 1).trim()
            if (textContent.isNotBlank()) {
                parsedEntries.add(timestampMs to textContent)
            }
        }

        // 按时间戳升序排序
        val sorted = parsedEntries.sortedBy { it.first }
        val result = mutableListOf<LrcLine>()

        for (i in sorted.indices) {
            val (startMs, rawText) = sorted[i]
            // 下一句开始时间作为本句结束时间（最后一句默认延展 4 秒）
            val endMs = if (i + 1 < sorted.size) sorted[i + 1].first else startMs + 4000L

            // 解析双语（支持 "한글 | 中文" 或 "한글 // 中文" 格式）
            val parts = if (rawText.contains("|")) {
                rawText.split("|").map { it.trim() }
            } else if (rawText.contains("//")) {
                rawText.split("//").map { it.trim() }
            } else {
                listOf(rawText)
            }

            val korean = parts[0]
            val chinese = if (parts.size > 1) parts[1] else null
            val blank = generateSmartBlank(korean)

            result.add(
                LrcLine(
                    index = i,
                    startTimeMs = startMs,
                    endTimeMs = endMs,
                    koreanText = korean,
                    chineseText = chinese,
                    blankQuestion = blank
                )
            )
        }

        return result
    }

    /**
     * 智能识别句子中的高频助词、连词或活用动词生成挖空题
     */
    fun generateSmartBlank(koreanSentence: String): BlankQuestion? {
        val tokens = KoreanTokenizer.tokenize(koreanSentence)
        if (tokens.isEmpty()) return null

        // 优先挖掘带有助词或多音节动词的 Token
        var targetToken = tokens.firstOrNull { token ->
            val clean = token.cleanWord
            clean.endsWith("은") || clean.endsWith("는") ||
            clean.endsWith("이") || clean.endsWith("가") ||
            clean.endsWith("을") || clean.endsWith("를") ||
            clean.endsWith("에서") || clean.endsWith("에게") ||
            clean.endsWith("때문에") || clean.endsWith("는데")
        }

        if (targetToken == null) {
            // 次选：选择长度 >= 2 的实词
            targetToken = tokens.firstOrNull { it.cleanWord.length >= 2 } ?: tokens.first()
        }

        val targetWord = targetToken.cleanWord
        val masked = koreanSentence.replace(targetWord, "_____")

        val phoneticTip = generatePhoneticTip(targetWord)

        return BlankQuestion(
            fullKorean = koreanSentence,
            maskedKorean = masked,
            targetAnswer = targetWord,
            targetIndex = targetToken.index,
            phoneticTip = phoneticTip
        )
    }

    private fun generatePhoneticTip(word: String): String? {
        return when {
            word.contains("같이") -> "连音化发音: [가치]"
            word.contains("굳이") -> "连音化发音: [구지]"
            word.contains("맞아") -> "连音发音: [마자]"
            word.contains("먹어") -> "连音发音: [머거]"
            word.contains("있어") -> "连音发音: [이써]"
            word.contains("듣고") -> "紧音化发音: [듣꼬]"
            word.contains("학교") -> "紧音化发音: [학꾜]"
            word.contains("식당") -> "紧音化发音: [식땅]"
            else -> null
        }
    }
}
