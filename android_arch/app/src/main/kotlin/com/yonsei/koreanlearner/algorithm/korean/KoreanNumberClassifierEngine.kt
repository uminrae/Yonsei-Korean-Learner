package com.yonsei.koreanlearner.algorithm.korean

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语数词与量词智能匹配转换引擎（KoreanNumberClassifierEngine）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心特性：
 * 1. 汉字词系统（Sino-Korean）与固有词系统（Native-Korean）双轨转换。
 * 2. 固有数量词 1~4 及 20 音变缩略规则（하나->한, 둘->두, 셋->세, 넷->네, 스물->스무）。
 * 3. 6月 (유월)、10月 (시월) 音变特例。
 * 4. 复合时间转换（小时使用固有词 + 分钟使用汉字词）。
 * 5. 常见量词归属自动判定。
 */
object KoreanNumberClassifierEngine {

    // ── 1. 数词系统类型枚举 ────────────────────────────────────

    enum class NumberSystemType(val displayName: String, val description: String) {
        SINO("汉字词数词", "源自汉字发音，用于计算分钟、年月日、金额、电话、楼层等"),
        NATIVE("固有词数词", "韩语纯固有发音，用于计算小时、年龄、日常绝大多数基础量词"),
        HYBRID("复合混合数词", "如时间表达：小时采用固有词，分钟/秒采用汉字词")
    }

    /**
     * 常用量词定义与归属系统
     */
    enum class Classifier(
        val korean        : String,
        val chineseMeaning: String,
        val system        : NumberSystemType,
        val example       : String
    ) {
        // 固有词量词
        GAE("개", "个 (通用物品)", NumberSystemType.NATIVE, "사과 한 개 (一个苹果)"),
        MYEONG("명", "名/人 (普通)", NumberSystemType.NATIVE, "학생 두 명 (两名学生)"),
        BUN_HONORIFIC("분", "位 (尊称)", NumberSystemType.NATIVE, "선생님 세 분 (三位老师)"),
        SAL("살", "岁 (年龄)", NumberSystemType.NATIVE, "스물다섯 살 (二十五岁)"),
        SI("시", "点/时 (时间)", NumberSystemType.NATIVE, "세 시 (三点)"),
        GWON("권", "本 (书籍/杂志)", NumberSystemType.NATIVE, "책 네 권 (四本书)"),
        JANG("장", "张 (纸/票/照片)", NumberSystemType.NATIVE, "종이 다섯 장 (五张纸)"),
        BYEONG("병", "瓶 (酒/水/饮料)", NumberSystemType.NATIVE, "맥주 두 병 (两瓶啤酒)"),
        JAN("잔", "杯 (茶/咖啡)", NumberSystemType.NATIVE, "커피 한 잔 (一杯咖啡)"),
        MARI("마리", "只/头 (动物)", NumberSystemType.NATIVE, "고양이 세 마리 (三只猫)"),
        GYEOLLE("켤레", "双 (鞋/袜/手套)", NumberSystemType.NATIVE, "신발 한 켤레 (一双鞋)"),
        BEOL("벌", "套/件 (衣服)", NumberSystemType.NATIVE, "옷 두 벌 (两套衣服)"),
        GEUREUT("그릇", "碗 (饭/面)", NumberSystemType.NATIVE, "밥 한 그릇 (一碗饭)"),
        SONGI("송이", "朵/束 (花/葡萄)", NumberSystemType.NATIVE, "꽃 세 송이 (三朵花)"),
        DAE("대", "台/辆 (机器/车)", NumberSystemType.NATIVE, "차 한 대 (一辆车)"),

        // 汉字词量词 / 场景
        BUN_MINUTE("분", "分钟", NumberSystemType.SINO, "삼십 분 (三十分钟)"),
        CHO("초", "秒", NumberSystemType.SINO, "사십오 초 (四十五秒)"),
        NYEON("년", "年", NumberSystemType.SINO, "이천이십육 년 (2026年)"),
        WOL("월", "月", NumberSystemType.SINO, "오월 (5月), 유월 (6月)"),
        IL("일", "日/号", NumberSystemType.SINO, "십오 일 (15日)"),
        WON("원", "韩元 (金额)", NumberSystemType.SINO, "오만 원 (五万韩元)"),
        CHEUNG("층", "层 (楼层)", NumberSystemType.SINO, "칠 층 (七楼)"),
        BEON_NUM("번", "号 (号码/线路)", NumberSystemType.SINO, "백이 번 (102号)"),
        DO("도", "度 (温度)", NumberSystemType.SINO, "이십오 도 (25度)");

        companion object {
            fun findByName(name: String): Classifier? =
                entries.firstOrNull { it.korean == name || it.name.equals(name, ignoreCase = true) }
        }
    }

    /**
     * 数词转换结果模型
     */
    data class KoreanNumberResult(
        val rawInput           : String,
        val koreanFullText     : String,
        val pronunciation      : String,
        val systemType         : NumberSystemType,
        val numberPart         : String,
        val classifierPart     : String?,
        val ruleExplanation    : String
    )

    // ── 2. 基础数词表 ──────────────────────────────────────────

    private val SINO_DIGITS = arrayOf("", "일", "이", "삼", "사", "오", "육", "칠", "팔", "구")
    private val SINO_UNITS  = arrayOf("", "십", "백", "천")
    private val SINO_BIG_UNITS = arrayOf("", "만", "억", "조")

    private val NATIVE_UNITS = arrayOf(
        "", "하나", "둘", "셋", "넷", "다섯", "여섯", "일곱", "여덟", "아홉"
    )
    private val NATIVE_TENS = arrayOf(
        "", "열", "스물", "서른", "마흔", "쉰", "예순", "일흔", "여든", "아흔"
    )

    // ════════════════════════════════════════════════════════════
    // 3. 核心转换 API
    // ════════════════════════════════════════════════════════════

    /**
     * 将数字转换为汉字词数词（支持 0 ~ 999,999,999,999）
     *
     * @param number 待转换数字
     */
    fun toSinoKorean(number: Long): String {
        if (number == 0L) return "영"
        if (number < 0) return "마이너스 " + toSinoKorean(-number)

        val sb = StringBuilder()
        var temp = number
        var bigUnitIdx = 0

        while (temp > 0) {
            val chunk = (temp % 10000).toInt()
            if (chunk > 0) {
                val chunkStr = convertSino4Digits(chunk, isHighestGroup = (temp / 10000 == 0L))
                val bigUnit = SINO_BIG_UNITS[bigUnitIdx]
                if (chunkStr.isNotEmpty()) {
                    val part = if (bigUnit.isNotEmpty()) "$chunkStr $bigUnit" else chunkStr
                    if (sb.isEmpty()) sb.insert(0, part)
                    else sb.insert(0, "$part ")
                }
            }
            temp /= 10000
            bigUnitIdx++
        }

        return sb.toString().trim()
    }

    private fun convertSino4Digits(chunk: Int, isHighestGroup: Boolean): String {
        val sb = StringBuilder()
        val d1000 = chunk / 1000
        val d100 = (chunk % 1000) / 100
        val d10 = (chunk % 100) / 10
        val d1 = chunk % 10

        if (d1000 > 0) {
            if (d1000 > 1) sb.append(SINO_DIGITS[d1000])
            sb.append("천")
        }
        if (d100 > 0) {
            if (d100 > 1) sb.append(SINO_DIGITS[d100])
            sb.append("백")
        }
        if (d10 > 0) {
            if (d10 > 1) sb.append(SINO_DIGITS[d10])
            sb.append("십")
        }
        if (d1 > 0) {
            sb.append(SINO_DIGITS[d1])
        }
        return sb.toString()
    }

    /**
     * 将数字转换为固有词数词（支持 1 ~ 99）
     *
     * @param number 待转换数字 (1~99)
     * @param hasClassifier 是否后接量词（若接量词，1~4及20发生特殊变形：한, 두, 세, 네, 스무）
     */
    fun toNativeKorean(number: Int, hasClassifier: Boolean = false): String {
        require(number in 1..99) { "固有词数词通常仅支持 1~99 的范围: $number" }

        val tens = number / 10
        val units = number % 10

        if (tens == 0) {
            // 纯个位数 1~9
            return if (hasClassifier) {
                when (units) {
                    1 -> "한"
                    2 -> "두"
                    3 -> "세"
                    4 -> "네"
                    else -> NATIVE_UNITS[units]
                }
            } else {
                NATIVE_UNITS[units]
            }
        }

        // 纯整十数（如 10, 20, 30...）
        if (units == 0) {
            return if (hasClassifier && tens == 2) {
                "스무" // 20 在量词前变为 스무 (如 스무 살, 스무 명)
            } else {
                NATIVE_TENS[tens]
            }
        }

        // 复合十位数（如 11, 23, 34...）
        val tensStr = NATIVE_TENS[tens]
        val unitsStr = if (hasClassifier) {
            when (units) {
                1 -> "한"
                2 -> "두"
                3 -> "세"
                4 -> "네"
                else -> NATIVE_UNITS[units]
            }
        } else {
            NATIVE_UNITS[units]
        }

        return "$tensStr$unitsStr"
    }

    // ════════════════════════════════════════════════════════════
    // 4. 场景化高级匹配与转换
    // ════════════════════════════════════════════════════════════

    /**
     * 时间表达转换（例："14:35" -> "오후 두 시 삼십오 분"）
     */
    fun convertTime(hour24: Int, minute: Int): KoreanNumberResult {
        require(hour24 in 0..23 && minute in 0..59) { "无效的时间范围: $hour24:$minute" }

        val isPm = hour24 >= 12
        val amPmPrefix = if (isPm) "오후 " else "오전 "
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val hourStr = toNativeKorean(hour12, hasClassifier = true) + " 시"
        val minStr = if (minute == 0) {
            ""
        } else if (minute == 30) {
            " 삼십 분 (반)"
        } else {
            " " + toSinoKorean(minute.toLong()) + " 분"
        }

        val fullText = "$amPmPrefix$hourStr$minStr".trim()

        return KoreanNumberResult(
            rawInput = String.format("%02d:%02d", hour24, minute),
            koreanFullText = fullText,
            pronunciation = "[$fullText]",
            systemType = NumberSystemType.HYBRID,
            numberPart = "$hour12 / $minute",
            classifierPart = "시 / 분",
            ruleExplanation = "韩语时间读法规则：小时使用【固有词】(1~4变形为 한/두/세/네)，分钟使用【汉字词】。"
        )
    }

    /**
     * 年月日日期转换（例：2026年 6月 15日 -> 2026년 6월 15일 -> 이천이십육 년 유월 십오 일）
     * 自动处理 6月 (유월) 与 10月 (시월) 音变特例。
     */
    fun convertDate(year: Int, month: Int, day: Int): KoreanNumberResult {
        require(month in 1..12 && day in 1..31) { "无效的日期: $year-$month-$day" }

        val yearStr = toSinoKorean(year.toLong()) + " 년"

        // 6月与10月音变处理
        val monthStr = when (month) {
            6 -> "유월"
            10 -> "시월"
            else -> toSinoKorean(month.toLong()) + "월"
        }

        val dayStr = toSinoKorean(day.toLong()) + " 일"
        val fullText = "$yearStr $monthStr $dayStr"

        val hasSpecialMonth = month == 6 || month == 10
        val note = if (hasSpecialMonth) {
            "【音变特例】${month}月份在韩语中收音脱落读作 '${monthStr}'（非 '${toSinoKorean(month.toLong())}월'）。"
        } else {
            "年月日全部使用【汉字词】数词系统。"
        }

        return KoreanNumberResult(
            rawInput = "$year-$month-$day",
            koreanFullText = fullText,
            pronunciation = "[$fullText]",
            systemType = NumberSystemType.SINO,
            numberPart = "$year / $month / $day",
            classifierPart = "년 / 월 / 일",
            ruleExplanation = note
        )
    }

    /**
     * 数量 + 量词智能组合转换（例：数量 3 + 量词 "권" -> "세 권"；数量 25000 + 量词 "원" -> "이만 오천 원"）
     */
    fun convertQuantity(amount: Int, classifier: Classifier): KoreanNumberResult {
        return when (classifier.system) {
            NumberSystemType.NATIVE -> {
                val numStr = if (amount in 1..99) {
                    toNativeKorean(amount, hasClassifier = true)
                } else {
                    // 超出 99 固有词极限时降级使用汉字词
                    toSinoKorean(amount.toLong())
                }
                val fullText = "$numStr ${classifier.korean}"
                val deformNote = if (amount in 1..4 || amount == 20) {
                    "（触发 1~4/20 缩略变形：$amount -> $numStr）"
                } else ""

                KoreanNumberResult(
                    rawInput = "$amount ${classifier.korean}",
                    koreanFullText = fullText,
                    pronunciation = "[$fullText]",
                    systemType = NumberSystemType.NATIVE,
                    numberPart = numStr,
                    classifierPart = classifier.korean,
                    ruleExplanation = "量词 '${classifier.korean}' (${classifier.chineseMeaning}) 搭配【固有词数词】$deformNote。"
                )
            }

            NumberSystemType.SINO, NumberSystemType.HYBRID -> {
                val numStr = toSinoKorean(amount.toLong())
                val fullText = "$numStr ${classifier.korean}"
                KoreanNumberResult(
                    rawInput = "$amount ${classifier.korean}",
                    koreanFullText = fullText,
                    pronunciation = "[$fullText]",
                    systemType = NumberSystemType.SINO,
                    numberPart = numStr,
                    classifierPart = classifier.korean,
                    ruleExplanation = "量词/单位 '${classifier.korean}' (${classifier.chineseMeaning}) 搭配【汉字词数词】。"
                )
            }
        }
    }
}
