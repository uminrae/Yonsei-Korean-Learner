package com.yonsei.koreanlearner.algorithm.korean

import androidx.compose.ui.geometry.Offset

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩文字母标准笔顺骨架与矢量路径数据 (HangulStrokeData)
 * ══════════════════════════════════════════════════════════════════
 */

/** 单个笔画骨架定义 (基于 0.0 ~ 1.0 归一化坐标系) */
data class StrokeSkeleton(
    val strokeOrder : Int,                  // 笔画顺序 (1, 2, 3...)
    val startPoint  : Offset,               // 起笔点 (归一化 0.0~1.0)
    val waypoints   : List<Offset>,         // 拐点序列
    val endPoint    : Offset                // 收笔点
)

/** 字母笔顺模型 */
data class HangulLetterStroke(
    val charName      : String,             // 字母名称（如 "기역 (ㄱ)", "아 (ㅏ)"）
    val letterSymbol  : String,             // 字母字符（如 "ㄱ", "ㄴ", "ㅏ", "ㅎ"）
    val isConsonant   : Boolean,            // 是否辅音
    val totalStrokes  : Int,                // 总笔画数
    val strokeList    : List<StrokeSkeleton>, // 笔画骨架列表
    val strokeGuide   : String              // 书写要领
)

object HangulStrokeData {

    val LETTERS = listOf(
        // ── 辅音 ────────────────────────────────────────────────
        // ㄱ (1笔：横折)
        HangulLetterStroke(
            charName = "기역 (ㄱ)",
            letterSymbol = "ㄱ",
            isConsonant = true,
            totalStrokes = 1,
            strokeList = listOf(
                StrokeSkeleton(
                    strokeOrder = 1,
                    startPoint = Offset(0.25f, 0.30f),
                    waypoints = listOf(Offset(0.75f, 0.30f)),
                    endPoint = Offset(0.75f, 0.75f)
                )
            ),
            strokeGuide = "从左到右写横，到达拐点后顺势垂直向下写折，一笔完成。"
        ),
        // ㄴ (1笔：竖折)
        HangulLetterStroke(
            charName = "니은 (ㄴ)",
            letterSymbol = "ㄴ",
            isConsonant = true,
            totalStrokes = 1,
            strokeList = listOf(
                StrokeSkeleton(
                    strokeOrder = 1,
                    startPoint = Offset(0.28f, 0.25f),
                    waypoints = listOf(Offset(0.28f, 0.72f)),
                    endPoint = Offset(0.75f, 0.72f)
                )
            ),
            strokeGuide = "从上向下写竖，到达底端拐角处顺势向右写横，一笔完成。"
        ),
        // ㄷ (2笔：横 + 竖折)
        HangulLetterStroke(
            charName = "디귿 (ㄷ)",
            letterSymbol = "ㄷ",
            isConsonant = true,
            totalStrokes = 2,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.25f, 0.28f), emptyList(), Offset(0.75f, 0.28f)),
                StrokeSkeleton(2, Offset(0.25f, 0.28f), listOf(Offset(0.25f, 0.72f)), Offset(0.75f, 0.72f))
            ),
            strokeGuide = "第①笔先写上方横线；第②笔从左上角起笔向下写竖并向右折出底横。"
        ),
        // ㄹ (3笔)
        HangulLetterStroke(
            charName = "리을 (ㄹ)",
            letterSymbol = "ㄹ",
            isConsonant = true,
            totalStrokes = 3,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.25f, 0.25f), listOf(Offset(0.75f, 0.25f)), Offset(0.75f, 0.48f)),
                StrokeSkeleton(2, Offset(0.25f, 0.48f), emptyList(), Offset(0.75f, 0.48f)),
                StrokeSkeleton(3, Offset(0.25f, 0.48f), listOf(Offset(0.25f, 0.75f)), Offset(0.75f, 0.75f))
            ),
            strokeGuide = "第①笔写横折；第②笔写中横；第③笔写竖折完成底横。"
        ),
        // ㅁ (3笔)
        HangulLetterStroke(
            charName = "미음 (ㅁ)",
            letterSymbol = "ㅁ",
            isConsonant = true,
            totalStrokes = 3,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.28f, 0.28f), emptyList(), Offset(0.28f, 0.72f)),
                StrokeSkeleton(2, Offset(0.28f, 0.28f), listOf(Offset(0.72f, 0.28f)), Offset(0.72f, 0.72f)),
                StrokeSkeleton(3, Offset(0.28f, 0.72f), emptyList(), Offset(0.72f, 0.72f))
            ),
            strokeGuide = "第①笔左竖；第②笔横折；第③笔封底横。"
        ),
        // ㅅ (2笔)
        HangulLetterStroke(
            charName = "시옷 (ㅅ)",
            letterSymbol = "ㅅ",
            isConsonant = true,
            totalStrokes = 2,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.50f, 0.25f), emptyList(), Offset(0.25f, 0.75f)),
                StrokeSkeleton(2, Offset(0.42f, 0.45f), emptyList(), Offset(0.75f, 0.75f))
            ),
            strokeGuide = "第①笔从顶部向左下撇；第②笔从中偏上向右下捺出。"
        ),
        // ㅇ (1笔：逆时针圆环)
        HangulLetterStroke(
            charName = "이응 (ㅇ)",
            letterSymbol = "ㅇ",
            isConsonant = true,
            totalStrokes = 1,
            strokeList = listOf(
                StrokeSkeleton(
                    1,
                    Offset(0.50f, 0.25f),
                    listOf(Offset(0.22f, 0.50f), Offset(0.50f, 0.75f), Offset(0.78f, 0.50f)),
                    Offset(0.50f, 0.25f)
                )
            ),
            strokeGuide = "从正上方起笔，沿逆时针方向画一个完整饱满的正圆回到起点。"
        ),
        // ㅎ (3笔)
        HangulLetterStroke(
            charName = "히읗 (ㅎ)",
            letterSymbol = "ㅎ",
            isConsonant = true,
            totalStrokes = 3,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.42f, 0.20f), emptyList(), Offset(0.58f, 0.20f)),
                StrokeSkeleton(2, Offset(0.25f, 0.35f), emptyList(), Offset(0.75f, 0.35f)),
                StrokeSkeleton(3, Offset(0.50f, 0.48f), listOf(Offset(0.25f, 0.65f), Offset(0.50f, 0.82f), Offset(0.75f, 0.65f)), Offset(0.50f, 0.48f))
            ),
            strokeGuide = "第①笔顶部短横；第②笔中部稍长横；第③笔逆时针画底圆。"
        ),

        // ── 元音 ────────────────────────────────────────────────
        // ㅏ (2笔)
        HangulLetterStroke(
            charName = "아 (ㅏ)",
            letterSymbol = "ㅏ",
            isConsonant = false,
            totalStrokes = 2,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.40f, 0.20f), emptyList(), Offset(0.40f, 0.80f)),
                StrokeSkeleton(2, Offset(0.40f, 0.50f), emptyList(), Offset(0.70f, 0.50f))
            ),
            strokeGuide = "第①笔从上到下写长竖；第②笔在竖的中点向右写短横。"
        ),
        // ㅓ (2笔)
        HangulLetterStroke(
            charName = "어 (ㅓ)",
            letterSymbol = "ㅓ",
            isConsonant = false,
            totalStrokes = 2,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.30f, 0.50f), emptyList(), Offset(0.60f, 0.50f)),
                StrokeSkeleton(2, Offset(0.60f, 0.20f), emptyList(), Offset(0.60f, 0.80f))
            ),
            strokeGuide = "第①笔先写左侧短横；第②笔由上到下写右侧长竖。"
        ),
        // ㅗ (2笔)
        HangulLetterStroke(
            charName = "오 (ㅗ)",
            letterSymbol = "ㅗ",
            isConsonant = false,
            totalStrokes = 2,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.50f, 0.28f), emptyList(), Offset(0.50f, 0.58f)),
                StrokeSkeleton(2, Offset(0.22f, 0.58f), emptyList(), Offset(0.78f, 0.58f))
            ),
            strokeGuide = "第①笔由上向下写短竖；第②笔由左到右写长横相连。"
        ),
        // ㅜ (2笔)
        HangulLetterStroke(
            charName = "우 (ㅜ)",
            letterSymbol = "ㅜ",
            isConsonant = false,
            totalStrokes = 2,
            strokeList = listOf(
                StrokeSkeleton(1, Offset(0.22f, 0.42f), emptyList(), Offset(0.78f, 0.42f)),
                StrokeSkeleton(2, Offset(0.50f, 0.42f), emptyList(), Offset(0.50f, 0.78f))
            ),
            strokeGuide = "第①笔先写上方长横；第②笔在横线正中向下写垂直短竖。"
        )
    )
}
