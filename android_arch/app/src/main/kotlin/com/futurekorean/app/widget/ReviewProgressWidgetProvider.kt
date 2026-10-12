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
 * 4x2 延世 7 轮复习进度桌面小组件 (ReviewProgressWidgetProvider)
 * ══════════════════════════════════════════════════════════════════
 */
class ReviewProgressWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_review_progress)

            views.setTextViewText(R.id.widget_book_badge, "第 1~6 册全景")
            views.setTextViewText(R.id.widget_progress_count, "延世词汇库 4,446 词就绪")
            views.setProgressBar(R.id.widget_progress_bar, 100, 75, false)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_progress_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
