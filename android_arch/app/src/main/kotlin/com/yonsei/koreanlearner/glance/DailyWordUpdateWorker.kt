package com.yonsei.koreanlearner.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.*
import com.yonsei.koreanlearner.data.local.database.KoreanDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * ══════════════════════════════════════════════════════════════════
 * 每日桌面小组件定时更新与同步 Worker (DailyWordUpdateWorker)
 * ══════════════════════════════════════════════════════════════════
 */
class DailyWordUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = androidx.room.Room.databaseBuilder(
                appContext,
                KoreanDatabase::class.java,
                KoreanDatabase.DATABASE_NAME
            ).build()

            // 1. 优先拉取今日到期复习词；若无到期词则拉取第 1 册新词
            val now = System.currentTimeMillis()
            val dueWords = db.wordDao().getDueReviewWords(now, 10).firstOrNull() ?: emptyList()
            val targetWord = dueWords.firstOrNull()?.word
                ?: db.wordDao().getWordById(1)
                ?: return@withContext Result.success()

            val record = db.reviewRecordDao().getRecord(targetWord.id, "WORD")
            val repLevel = record?.repetitionLevel ?: 0

            // 2. 更新全部已添加的 Glance 小组件实例
            val manager = GlanceAppWidgetManager(appContext)
            val glanceIds = manager.getGlanceIds(DailyWordGlanceWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(appContext, glanceId) { prefs ->
                    prefs[GlanceWordKeys.WORD_ID] = targetWord.id
                    prefs[GlanceWordKeys.KOREAN] = targetWord.korean
                    prefs[GlanceWordKeys.CHINESE] = targetWord.chinese
                    prefs[GlanceWordKeys.PART_OF_SPEECH] = targetWord.partOfSpeech
                    prefs[GlanceWordKeys.BOOK] = targetWord.book
                    prefs[GlanceWordKeys.LESSON] = targetWord.lesson
                    prefs[GlanceWordKeys.REPETITION_LEVEL] = repLevel
                    prefs[GlanceWordKeys.IS_FLIPPED] = false
                }
                DailyWordGlanceWidget().update(appContext, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "daily_word_glance_update_work"

        fun schedulePeriodic(context: Context) = enqueueDailyUpdate(context)

        /**
         * 设置每日定时后台刷新调度（周期 24 小时）
         */
        fun enqueueDailyUpdate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<DailyWordUpdateWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
