package com.futurekorean.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.futurekorean.app.MainActivity
import com.futurekorean.app.R

/**
 * ══════════════════════════════════════════════════════════════════
 * 桌面每日一词小组件 (DailyWordWidgetProvider)
 * ══════════════════════════════════════════════════════════════════
 */
class DailyWordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private val SAMPLE_WORDS = listOf(
            "행복하다" to "感到幸福，愉快",
            "공부하다" to "学习，用功",
            "감사합니다" to "谢谢，感谢",
            "한국어" to "韩国语，韩文",
            "친구" to "朋友，同伴",
            "따뜻하다" to "温暖，暖和",
            "희망" to "希望，憧憬",
            "성공" to "成功，成就"
        )

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val randomWord = SAMPLE_WORDS.random()
            val views = RemoteViews(context.packageName, R.layout.widget_daily_word)

            views.setTextViewText(R.id.widget_word_kr, randomWord.first)
            views.setTextViewText(R.id.widget_word_cn, randomWord.second)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
