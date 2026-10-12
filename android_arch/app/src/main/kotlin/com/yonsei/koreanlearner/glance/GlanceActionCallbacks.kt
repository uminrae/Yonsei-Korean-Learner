package com.yonsei.koreanlearner.glance

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.yonsei.koreanlearner.algorithm.MemoryScheduler
import com.yonsei.koreanlearner.data.local.database.KoreanDatabase
import com.yonsei.koreanlearner.domain.model.ReviewRating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

/**
 * ══════════════════════════════════════════════════════════════════
 * 小组件交互回调动作 (Glance Action Callbacks)
 * ══════════════════════════════════════════════════════════════════
 */

/** 1. 翻转卡片正面/背面回调 */
class FlipCardActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentFlipped = prefs[GlanceWordKeys.IS_FLIPPED] ?: false
            prefs[GlanceWordKeys.IS_FLIPPED] = !currentFlipped
        }
        DailyWordGlanceWidget().update(context, glanceId)
    }
}

/** 2. 点击音频播放回调（启动后台 Foreground TTS 服务） */
class PlayTtsActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val koreanParamKey = ActionParameters.Key<String>("korean_text")
        val koreanText = parameters[koreanParamKey] ?: return

        val intent = Intent(context, GlanceTtsService::class.java).apply {
            putExtra(GlanceTtsService.EXTRA_KOREAN_TEXT, koreanText)
        }
        context.startService(intent)
    }
}

/** 3. 快速打卡已掌握并切换下一词回调 */
class MarkLearnedActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        withContext(Dispatchers.IO) {
            val db = androidx.room.Room.databaseBuilder(
                context,
                KoreanDatabase::class.java,
                KoreanDatabase.DATABASE_NAME
            ).build()

            val wordIdParamKey = ActionParameters.Key<Int>("word_id")
            val currentWordId = parameters[wordIdParamKey] ?: 1

            // 1. 获取并更新当前词复习记录
            val existingRecord = db.reviewRecordDao().getRecord(currentWordId, "WORD")
            val currentRecord = existingRecord ?: MemoryScheduler.createInitialRecord(currentWordId, "WORD")
            val updatedRecord = MemoryScheduler.calculateNextReview(currentRecord, ReviewRating.GOOD)
            db.reviewRecordDao().upsert(updatedRecord)

            // 2. 拉取下一个待复习或新单词
            val dueWords = db.wordDao().getDueReviewWords(System.currentTimeMillis(), 10).firstOrNull() ?: emptyList()
            val nextWordWithRecord = dueWords.firstOrNull()

            val nextWord = nextWordWithRecord?.word ?: db.wordDao().getWordById(currentWordId + 1)

            if (nextWord != null) {
                val nextRecord = db.reviewRecordDao().getRecord(nextWord.id, "WORD")
                val repLevel = nextRecord?.repetitionLevel ?: 0

                // 3. 更新 Glance Preferences 并刷新
                updateAppWidgetState(context, glanceId) { prefs ->
                    prefs[GlanceWordKeys.WORD_ID] = nextWord.id
                    prefs[GlanceWordKeys.KOREAN] = nextWord.korean
                    prefs[GlanceWordKeys.CHINESE] = nextWord.chinese
                    prefs[GlanceWordKeys.PART_OF_SPEECH] = nextWord.partOfSpeech
                    prefs[GlanceWordKeys.BOOK] = nextWord.book
                    prefs[GlanceWordKeys.LESSON] = nextWord.lesson
                    prefs[GlanceWordKeys.REPETITION_LEVEL] = repLevel
                    prefs[GlanceWordKeys.IS_FLIPPED] = false
                    prefs[GlanceWordKeys.STREAK_DAYS] = (prefs[GlanceWordKeys.STREAK_DAYS] ?: 1) + 1
                }
                DailyWordGlanceWidget().update(context, glanceId)
            }
        }
    }
}
