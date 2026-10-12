package com.yonsei.koreanlearner.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.yonsei.koreanlearner.domain.model.WordHeatItem
import com.yonsei.koreanlearner.ui.screen.wordcard.WordCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ══════════════════════════════════════════════════════════════════
 * Instagram Story 9:16 高清（1080x1920）快照生成与分享引擎
 * ══════════════════════════════════════════════════════════════════
 */
object StorySnapshotGenerator {

    const val STORY_WIDTH  = 1080
    const val STORY_HEIGHT = 1920

    /**
     * 快照风格主题模式
     */
    enum class StoryTheme {
        /** 极简暗黑模式（Instagram AMOLED Deep Dark） */
        DARK,
        /** 高级冷灰浅色模式（Minimalist Clean Light） */
        LIGHT
    }

    /**
     * 快照数据包装体
     */
    data class StorySnapshotData(
        val korean          : String,
        val chinese         : String,
        val partOfSpeech    : String,
        val level           : String,
        val irregularType   : String? = null,
        val romanization    : String? = null,
        val exampleKorean   : String? = null,
        val exampleChinese  : String? = null,
        val book            : Int = 1,
        val lesson          : Int = 1,
        val repetitionLevel : Int = 1,
        val streakDays      : Int = 1,
        val easeFactor      : Double = 2.5
    ) {
        companion object {
            fun fromWordCard(card: WordCard, streakDays: Int = 7): StorySnapshotData =
                StorySnapshotData(
                    korean          = card.korean,
                    chinese         = card.chinese,
                    partOfSpeech    = card.partOfSpeech,
                    level           = card.level,
                    irregularType   = card.irregularType,
                    romanization    = card.romanization,
                    exampleKorean   = card.exampleSentences.firstOrNull()?.korean,
                    exampleChinese  = card.exampleSentences.firstOrNull()?.chinese,
                    book            = card.book,
                    lesson          = card.lesson,
                    repetitionLevel = card.repetitionLevel,
                    streakDays      = streakDays,
                    easeFactor      = card.easeFactor
                )

            fun fromWordHeatItem(item: WordHeatItem, streakDays: Int = 7): StorySnapshotData =
                StorySnapshotData(
                    korean          = item.korean,
                    chinese         = item.chinese,
                    partOfSpeech    = item.partOfSpeech,
                    level           = "TOPIK ${item.book}",
                    irregularType   = item.irregularType,
                    romanization    = null,
                    exampleKorean   = null,
                    exampleChinese  = null,
                    book            = item.book,
                    lesson          = item.lesson,
                    repetitionLevel = item.repetitionLevel,
                    streakDays      = streakDays,
                    easeFactor      = item.easeFactor
                )
        }
    }

    // ════════════════════════════════════════════════════════════
    // 1. 离线 1080x1920 高清 Bitmap 渲染函数
    // ════════════════════════════════════════════════════════════

    /**
     * 在后台线程直接绘制 9:16（1080x1920）标准的 Instagram 快照 Bitmap。
     * 无主线程 UI 依赖，零丢帧、零测量延迟。
     */
    suspend fun renderStoryBitmap(
        data: StorySnapshotData,
        theme: StoryTheme = StoryTheme.DARK
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitmap = Bitmap.createBitmap(STORY_WIDTH, STORY_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isDark = theme == StoryTheme.DARK
        val bgColor = if (isDark) Color.parseColor("#121212") else Color.parseColor("#FAFAFC")
        val cardBgColor = if (isDark) Color.parseColor("#1E1E24") else Color.parseColor("#FFFFFF")
        val primaryTextColor = if (isDark) Color.parseColor("#FFFFFF") else Color.parseColor("#111827")
        val secondaryTextColor = if (isDark) Color.parseColor("#9CA3AF") else Color.parseColor("#6B7280")
        val borderColor = if (isDark) Color.parseColor("#2E2E38") else Color.parseColor("#E5E7EB")

        // ── 1. 背景绘制 ──────────────────────────────────────────
        canvas.drawColor(bgColor)

        // 绘制顶部与底部微光渐变圆晕（Instagram 氛围光）
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                STORY_WIDTH * 0.85f, STORY_HEIGHT * 0.12f, 480f,
                intArrayOf(Color.parseColor(if (isDark) "#33833AB4" else "#1A833AB4"), Color.TRANSPARENT),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(STORY_WIDTH * 0.85f, STORY_HEIGHT * 0.12f, 480f, glowPaint)

        // ── 2. 顶栏（Logo + 打卡胶囊徽章 + 日期）────────────────
        val dateStr = SimpleDateFormat("yyyy.MM.dd · E", Locale.ENGLISH).format(Date()).uppercase()

        // 品牌 Title
        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryTextColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        canvas.drawText("YONSEI KOREAN · DAILY", 80f, 130f, brandPaint)

        // 日期
        val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryTextColor
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            letterSpacing = 0.05f
        }
        canvas.drawText(dateStr, 80f, 170f, datePaint)

        // 右上角打卡天数渐变胶囊徽章
        val streakBadgeWidth = 260f
        val streakBadgeHeight = 70f
        val streakBadgeLeft = STORY_WIDTH - 80f - streakBadgeWidth
        val streakBadgeTop = 110f

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                streakBadgeLeft, streakBadgeTop, streakBadgeLeft + streakBadgeWidth, streakBadgeTop + streakBadgeHeight,
                intArrayOf(Color.parseColor("#FCCC12"), Color.parseColor("#E1306C"), Color.parseColor("#833AB4")),
                null, Shader.TileMode.CLAMP
            )
        }
        val badgeRect = RectF(streakBadgeLeft, streakBadgeTop, streakBadgeLeft + streakBadgeWidth, streakBadgeTop + streakBadgeHeight)
        canvas.drawRoundRect(badgeRect, 35f, 35f, badgePaint)

        val streakTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(" DAY ${data.streakDays} 打卡", badgeRect.centerX(), badgeRect.centerY() + 11f, streakTextPaint)

        // ── 3. 中央主体卡片 (Rounded Card 16dp / 48px) ─────────
        val cardLeft = 64f
        val cardRight = STORY_WIDTH - 64f
        val cardTop = 230f
        val cardBottom = 1540f
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

        // 卡片底色与边框
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cardBgColor }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        // 册数/课时标头（“BOOK 1 · LESSON 3”）
        val lessonHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#833AB4")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.15f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("BOOK ${data.book} · LESSON ${data.lesson}", STORY_WIDTH / 2f, cardTop + 100f, lessonHeaderPaint)

        // 韩文原词大字（特大号排版 108px）
        val koreanPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryTextColor
            textSize = 100f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.korean, STORY_WIDTH / 2f, cardTop + 240f, koreanPaint)

        // 罗马拼音 / 发音
        var currentY = cardTop + 305f
        if (!data.romanization.isNullOrBlank()) {
            val romanPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secondaryTextColor
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("[${data.romanization}]", STORY_WIDTH / 2f, currentY, romanPaint)
            currentY += 60f
        }

        // 词性与不规则标签组
        val tagY = currentY + 20f
        val posTag = "${data.partOfSpeech} · ${data.level}"
        val tagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#D8B4FE") else Color.parseColor("#6B21A8")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tagBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDark) Color.parseColor("#3B1E54") else Color.parseColor("#F3E8FF")
        }
        val tagTextWidth = tagPaint.measureText(posTag)
        val tagRect = RectF(
            STORY_WIDTH / 2f - tagTextWidth / 2f - 24f,
            tagY - 34f,
            STORY_WIDTH / 2f + tagTextWidth / 2f + 24f,
            tagY + 18f
        )
        canvas.drawRoundRect(tagRect, 16f, 16f, tagBgPaint)
        canvas.drawText(posTag, tagRect.left + 24f, tagY, tagPaint)

        // 中文释义（特大醒目 52px）
        val chineseY = tagY + 110f
        val chinesePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = primaryTextColor
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.chinese, STORY_WIDTH / 2f, chineseY, chinesePaint)

        // 分割线
        val dividerY = chineseY + 60f
        val divPaint = Paint().apply {
            color = borderColor
            strokeWidth = 2f
        }
        canvas.drawLine(cardLeft + 60f, dividerY, cardRight - 60f, dividerY, divPaint)

        // 双语例句展示区
        if (!data.exampleKorean.isNullOrBlank()) {
            val exampleStartY = dividerY + 70f

            // 例句小标题
            val exTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = secondaryTextColor
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.08f
            }
            canvas.drawText("EXAMPLE SENTENCE", cardLeft + 60f, exampleStartY, exTitlePaint)

            // 韩文例句 (StaticLayout 自动换行)
            val exKoreanPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = primaryTextColor
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val koreanLayout = StaticLayout.Builder.obtain(
                data.exampleKorean, 0, data.exampleKorean.length, exKoreanPaint, (cardRight - cardLeft - 120f).toInt()
            ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

            canvas.save()
            canvas.translate(cardLeft + 60f, exampleStartY + 25f)
            koreanLayout.draw(canvas)
            canvas.restore()

            // 中文例句
            if (!data.exampleChinese.isNullOrBlank()) {
                val exChinesePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = secondaryTextColor
                    textSize = 30f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                }
                val chineseLayout = StaticLayout.Builder.obtain(
                    data.exampleChinese, 0, data.exampleChinese.length, exChinesePaint, (cardRight - cardLeft - 120f).toInt()
                ).setAlignment(Layout.Alignment.ALIGN_NORMAL).build()

                canvas.save()
                canvas.translate(cardLeft + 60f, exampleStartY + 35f + koreanLayout.height)
                chineseLayout.draw(canvas)
                canvas.restore()
            }
        }

        // ── 4. 底部 7 轮打卡点亮进度矩阵 ────────────────────────
        val progressY = 1600f

        val progressTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryTextColor
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.1f
            textAlign = Paint.Align.CENTER
        }
        val statusDesc = if (data.repetitionLevel >= 7) " 7 轮复习毕业 · 长期记忆达成"
                         else "延世背诵本 7 轮打卡进度 · 第 ${data.repetitionLevel} 轮已完成"
        canvas.drawText(statusDesc, STORY_WIDTH / 2f, progressY, progressTitlePaint)

        // 7 个复习圆点 / 方块
        val dotRadius = 22f
        val dotSpacing = 72f
        val startDotX = STORY_WIDTH / 2f - (3 * dotSpacing)
        val dotY = progressY + 60f

        (1..7).forEach { round ->
            val dotX = startDotX + (round - 1) * dotSpacing
            val isPassed = data.repetitionLevel >= round

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPassed) {
                    if (round == 7) Color.parseColor("#833AB4") else Color.parseColor("#E1306C")
                } else {
                    if (isDark) Color.parseColor("#2E2E38") else Color.parseColor("#E5E7EB")
                }
            }
            canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)

            // 数字 R1~R7
            val dotTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isPassed) Color.WHITE else secondaryTextColor
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("R$round", dotX, dotY + 7f, dotTextPaint)
        }

        // ── 5. 底部品牌水印与标语 ────────────────────────────────
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = secondaryTextColor.copyAlpha(0.6f)
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("Instagram: @yonsei.korean.app · 科学艾宾浩斯记忆引擎", STORY_WIDTH / 2f, 1850f, footerPaint)

        bitmap
    }

    private fun Int.copyAlpha(alphaFraction: Float): Int {
        val alpha = (Color.alpha(this) * alphaFraction).toInt()
        return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
    }

    // ════════════════════════════════════════════════════════════
    // 2. 文件缓存与 FileProvider Content URI 生成
    // ════════════════════════════════════════════════════════════

    /**
     * 将 Bitmap 无损异步写入应用缓存目录并获取安全 FileProvider URI
     */
    suspend fun saveBitmapToCache(context: Context, bitmap: Bitmap): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val cacheFolder = File(context.cacheDir, "story_snapshots").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "story_snap_${System.currentTimeMillis()}.png"
            val file = File(cacheFolder, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ════════════════════════════════════════════════════════════
    // 3. 保存至系统相册（MediaStore 兼容 Android 10+ 与旧版）
    // ════════════════════════════════════════════════════════════

    /**
     * 将快照保存到系统相册 Pictures/YonseiKorean 目录
     */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "YONSEI_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png"
            val resolver = context.contentResolver

            val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/YonseiKorean")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.also { uri ->
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val targetDir = File(picturesDir, "YonseiKorean").apply { if (!exists()) mkdirs() }
                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, targetFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }

            if (imageUri != null) Result.success(imageUri)
            else Result.failure(IllegalStateException("创建相册媒体记录失败"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ════════════════════════════════════════════════════════════
    // 4. 原生分享（Instagram Stories 专属或通用系统分享）
    // ════════════════════════════════════════════════════════════

    /**
     * 调起 Instagram Stories 分享或系统原生分享选择器
     *
     * @param preferInstagram 若为 true 则优先尝试唤起 Instagram Stories 官方直达 Intent
     */
    fun shareStory(context: Context, imageUri: Uri, preferInstagram: Boolean = true) {
        val instaIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(imageUri, "image/png")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val isInstaInstalled = context.packageManager.queryIntentActivities(instaIntent, 0).isNotEmpty()

        if (preferInstagram && isInstaInstalled) {
            context.startActivity(instaIntent)
        } else {
            // 系统原生通用分享面板
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, "我正在使用《延世韩国语》打卡背诵！")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "分享今日学习快照至").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}
