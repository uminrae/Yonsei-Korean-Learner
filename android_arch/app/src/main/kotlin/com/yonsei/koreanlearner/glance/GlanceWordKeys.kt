package com.yonsei.koreanlearner.glance

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object GlanceWordKeys {
    val WORD_ID          = intPreferencesKey("word_id")
    val KOREAN           = stringPreferencesKey("korean")
    val CHINESE          = stringPreferencesKey("chinese")
    val PART_OF_SPEECH   = stringPreferencesKey("part_of_speech")
    val BOOK             = intPreferencesKey("book")
    val LESSON           = intPreferencesKey("lesson")
    val REPETITION_LEVEL = intPreferencesKey("repetition_level")
    val IS_FLIPPED       = booleanPreferencesKey("is_flipped")
    val STREAK_DAYS      = intPreferencesKey("streak_days")
}
