package com.yonsei.koreanlearner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ══════════════════════════════════════════════════════════════════
// 字体配置
//
// 韩文首选 Pretendard（需开发者自行购买/下载授权字体文件并放入
//   app/src/main/res/font/ 目录）。
// 若无 Pretendard 授权，系统自动回退到 Noto Sans KR（Google Fonts）。
//
// 引入方式（build.gradle.kts）：
//   implementation("androidx.compose.ui:ui-text-google-fonts:1.x.x")
//   val provider = GoogleFont.Provider(
//       providerAuthority = "com.google.android.gms.fonts",
//       providerPackage   = "com.google.android.gms",
//       certificates      = R.array.com_google_android_gms_fonts_certs
//   )
//   val notoSansKR = GoogleFont("Noto Sans KR")
// ══════════════════════════════════════════════════════════════════

// 使用系统标准 Default 字体（无需额外 font 资源文件，杜绝字体加载崩溃）
private val AppFontFamily = FontFamily.Default

// ══════════════════════════════════════════════════════════════════
// Typography（Material 3 字型比例）
//
// 设计原则（Instagram 美感）：
//   - Display / Headline：偏大字号，Bold，吸引视觉注意
//   - Body：适中行高，阅读舒适
//   - Label：精简，用于徽章、标签、辅助信息
// ══════════════════════════════════════════════════════════════════

val KoreanLearnerTypography = HangulWordBreakTypography.createTypography(
    Typography(
        // ── Display（大标题，用于欢迎页/成就页） ─────────────────────
        displayLarge = TextStyle(
            fontFamily = AppFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 38.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.3).sp
    ),
    displaySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 30.sp,
        lineHeight = 38.sp
    ),

    // ── Headline（卡片标题、界面大标题） ──────────────────────────
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        lineHeight = 26.sp
    ),

    // ── Title（列表项、卡片副标题） ───────────────────────────────
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp
    ),

    // ── Body（正文、例句、语法说明） ──────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 26.sp,    // 韩文行高略宽，阅读舒适
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),

    // ── Label（徽章、标签、辅助信息、按钮文字） ───────────────────
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize   = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
    )
)

// ── 韩文单词展示专用字号（不在 Typography 规范内，直接引用） ─────

/** 单词卡片正面：韩文大字展示 */
val KoreanWordDisplaySize = 42.sp

/** 单词卡片背面：中文释义展示 */
val ChineseAnswerDisplaySize = 26.sp

/** 语法 Pattern 展示字号 */
val GrammarPatternSize = 28.sp
