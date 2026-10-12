package com.yonsei.koreanlearner.algorithm.typing

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语 2-Set 标准双拼键盘按键映射引擎 (Hangul2SetJamoMapper)
 * ══════════════════════════════════════════════════════════════════
 *
 * 布局规律：
 * • 左手区：辅音（자음）— ㅂ, ㅈ, ㄷ, ㄱ, ㅅ, ㅁ, ㄴ, ㅇ, ㄹ, ㅎ, ㅋ, ㅌ, ㅊ, ㅍ
 * • 右手区：元音（모음）— ㅛ, ㅕ, ㅑ, ㅐ, ㅔ, ㅗ, ㅓ, ㅏ, ㅣ, ㅠ, ㅜ, ㅡ
 * • Shift 紧音与双元音：ㅃ, ㅉ, ㄸ, ㄲ, ㅆ, ㅒ, ㅖ
 */

data class KeyboardKeyInfo(
    val qwertyChar: Char,
    val hangulChar: String,
    val shiftHangulChar: String? = null,
    val isConsonant: Boolean // true=辅音(左手), false=元音(右手)
)

data class TypingTestSample(
    val id: String,
    val levelName: String,
    val targetKorean: String,
    val chineseMeaning: String
)

object Hangul2SetJamoMapper {

    // 2-Set 标准键盘三排键位映射
    val ROW_1 = listOf(
        KeyboardKeyInfo('q', "ㅂ", "ㅃ", true),
        KeyboardKeyInfo('w', "ㅈ", "ㅉ", true),
        KeyboardKeyInfo('e', "ㄷ", "ㄸ", true),
        KeyboardKeyInfo('r', "ㄱ", "ㄲ", true),
        KeyboardKeyInfo('t', "ㅅ", "ㅆ", true),
        KeyboardKeyInfo('y', "ㅛ", null, false),
        KeyboardKeyInfo('u', "ㅕ", null, false),
        KeyboardKeyInfo('i', "ㅑ", null, false),
        KeyboardKeyInfo('o', "ㅐ", "ㅒ", false),
        KeyboardKeyInfo('p', "ㅔ", "ㅖ", false)
    )

    val ROW_2 = listOf(
        KeyboardKeyInfo('a', "ㅁ", null, true),
        KeyboardKeyInfo('s', "ㄴ", null, true),
        KeyboardKeyInfo('d', "ㅇ", null, true),
        KeyboardKeyInfo('f', "ㄹ", null, true),
        KeyboardKeyInfo('g', "ㅎ", null, true),
        KeyboardKeyInfo('h', "ㅗ", null, false),
        KeyboardKeyInfo('j', "ㅓ", null, false),
        KeyboardKeyInfo('k', "ㅏ", null, false),
        KeyboardKeyInfo('l', "ㅣ", null, false)
    )

    val ROW_3 = listOf(
        KeyboardKeyInfo('z', "ㅋ", null, true),
        KeyboardKeyInfo('x', "ㅌ", null, true),
        KeyboardKeyInfo('c', "ㅊ", null, true),
        KeyboardKeyInfo('v', "ㅍ", null, true),
        KeyboardKeyInfo('b', "ㅠ", null, false),
        KeyboardKeyInfo('n', "ㅜ", null, false),
        KeyboardKeyInfo('m', "ㅡ", null, false)
    )

    val PRESET_TYPING_SAMPLES = listOf(
        TypingTestSample("jamo_1", "初级字母", "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ", "韩语 14 个基础初声辅音"),
        TypingTestSample("jamo_2", "元音排列", "ㅏㅑㅓㅕㅗㅛㅜㅠㅡㅣㅐㅔ", "韩语 12 个核心基础元音"),
        TypingTestSample("word_1", "日常问候", "안녕하세요", "你好（日常最常用问候）"),
        TypingTestSample("word_2", "延世基础", "한국어 공부가 재미있습니다", "韩语学习很有趣"),
        TypingTestSample("word_3", "真挚感谢", "진심으로 감사드립니다", "由衷向您表示感谢"),
        TypingTestSample("sent_1", "经典名言", "중요한 것은 꺾이지 않는 마음", "重要的不是结果，而是百折不挠的心"),
        TypingTestSample("sent_2", "自律名句", "오늘도 내 삶을 주체적으로 살아간다", "今天也主导着自己的人生向前迈进")
    )
}
