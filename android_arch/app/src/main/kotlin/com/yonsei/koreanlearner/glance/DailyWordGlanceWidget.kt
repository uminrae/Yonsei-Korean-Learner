package com.yonsei.koreanlearner.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.futurekorean.app.MainActivity

/**
 * ══════════════════════════════════════════════════════════════════
 * 延世韩国语每日背诵 Glance 桌面小组件 (DailyWordGlanceWidget)
 * ══════════════════════════════════════════════════════════════════
 */
class DailyWordGlanceWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyWordGlanceWidget()
}

class DailyWordGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(140.dp, 110.dp), // 2x2 紧凑模式
            DpSize(260.dp, 120.dp)  // 4x2 展开模式
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()

            val wordId          = prefs[GlanceWordKeys.WORD_ID] ?: 1
            val korean          = prefs[GlanceWordKeys.KOREAN] ?: "공부하다"
            val chinese         = prefs[GlanceWordKeys.CHINESE] ?: "学习，用功"
            val partOfSpeech    = prefs[GlanceWordKeys.PART_OF_SPEECH] ?: "动词"
            val book            = prefs[GlanceWordKeys.BOOK] ?: 1
            val lesson          = prefs[GlanceWordKeys.LESSON] ?: 1
            val repetitionLevel = prefs[GlanceWordKeys.REPETITION_LEVEL] ?: 0
            val isFlipped       = prefs[GlanceWordKeys.IS_FLIPPED] ?: false
            val streakDays      = prefs[GlanceWordKeys.STREAK_DAYS] ?: 1

            val size = LocalSize.current

            if (size.width >= 200.dp) {
                ExpandedLayout(
                    wordId          = wordId,
                    korean          = korean,
                    chinese         = chinese,
                    partOfSpeech    = partOfSpeech,
                    book            = book,
                    lesson          = lesson,
                    repetitionLevel = repetitionLevel,
                    isFlipped       = isFlipped,
                    streakDays      = streakDays
                )
            } else {
                CompactLayout(
                    wordId          = wordId,
                    korean          = korean,
                    chinese         = chinese,
                    partOfSpeech    = partOfSpeech,
                    book            = book,
                    lesson          = lesson,
                    repetitionLevel = repetitionLevel,
                    isFlipped       = isFlipped,
                    streakDays      = streakDays
                )
            }
        }
    }

    /** 2x2 紧凑模式 */
    @Composable
    private fun CompactLayout(
        wordId          : Int,
        korean          : String,
        chinese         : String,
        partOfSpeech    : String,
        book            : Int,
        lesson          : Int,
        repetitionLevel : Int,
        isFlipped       : Boolean,
        streakDays      : Int
    ) {
        val textColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFFFFFFFF))
        val subColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF9CA3AF))
        val primaryColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF833AB4))
        val bgColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF141414))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .cornerRadius(18.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // 顶部标签栏
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "第${book}册",
                    style = TextStyle(color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.width(4.dp))
                Text(
                    text = "${repetitionLevel}轮",
                    style = TextStyle(color = subColor, fontSize = 10.sp)
                )
            }

            Spacer(GlanceModifier.defaultWeight())

            // 单词主体
            if (!isFlipped) {
                Text(
                    text = korean,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = partOfSpeech,
                    style = TextStyle(color = subColor, fontSize = 10.sp)
                )
            } else {
                Text(
                    text = chinese,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )
            }

            Spacer(GlanceModifier.defaultWeight())

            // 底部操作区 (TTS发音 + 翻卡 + 标记掌握)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 发音
                Text(
                    text = "",
                    modifier = GlanceModifier.clickable(
                        actionRunCallback<PlayTtsActionCallback>(
                            actionParametersOf(ActionParameters.Key<String>("korean_text") to korean)
                        )
                    )
                )

                Spacer(GlanceModifier.width(10.dp))

                // 翻卡
                Text(
                    text = if (isFlipped) "" else "",
                    modifier = GlanceModifier.clickable(
                        actionRunCallback<FlipCardActionCallback>()
                    )
                )

                Spacer(GlanceModifier.width(10.dp))

                // 打卡
                Text(
                    text = "✓",
                    style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF10B981)), fontWeight = FontWeight.Bold),
                    modifier = GlanceModifier.clickable(
                        actionRunCallback<MarkLearnedActionCallback>(
                            actionParametersOf(ActionParameters.Key<Int>("word_id") to wordId)
                        )
                    )
                )
            }
        }
    }

    /** 4x2 展开模式 */
    @Composable
    private fun ExpandedLayout(
        wordId          : Int,
        korean          : String,
        chinese         : String,
        partOfSpeech    : String,
        book            : Int,
        lesson          : Int,
        repetitionLevel : Int,
        isFlipped       : Boolean,
        streakDays      : Int
    ) {
        val textColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFFFFFFFF))
        val subColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF9CA3AF))
        val primaryColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF833AB4))
        val bgColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF141414))
        val greenColor = ColorProvider(androidx.compose.ui.graphics.Color(0xFF10B981))

        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(bgColor)
                .cornerRadius(18.dp)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：单词信息与释义
            Column(
                modifier = GlanceModifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "延世第${book}册 · L$lesson",
                        style = TextStyle(color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        text = " ${streakDays}天连续",
                        style = TextStyle(color = subColor, fontSize = 10.sp)
                    )
                }

                Spacer(GlanceModifier.height(4.dp))

                Text(
                    text = korean,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(GlanceModifier.height(2.dp))

                Text(
                    text = "[$partOfSpeech] $chinese",
                    style = TextStyle(color = subColor, fontSize = 12.sp),
                    maxLines = 1
                )
            }

            // 右侧：7轮复习进度与操作按钮
            Column(
                horizontalAlignment = Alignment.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 轮次徽章
                Text(
                    text = "${repetitionLevel}/7 轮",
                    style = TextStyle(color = greenColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )

                Spacer(GlanceModifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = " 发音",
                        style = TextStyle(color = textColor, fontSize = 11.sp),
                        modifier = GlanceModifier.clickable(
                            actionRunCallback<PlayTtsActionCallback>(
                                actionParametersOf(ActionParameters.Key<String>("korean_text") to korean)
                            )
                        )
                    )

                    Spacer(GlanceModifier.width(10.dp))

                    Text(
                        text = "✓ 已掌握",
                        style = TextStyle(color = greenColor, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        modifier = GlanceModifier.clickable(
                            actionRunCallback<MarkLearnedActionCallback>(
                                actionParametersOf(ActionParameters.Key<Int>("word_id") to wordId)
                            )
                        )
                    )
                }
            }
        }
    }
}
