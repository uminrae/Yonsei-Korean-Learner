package com.yonsei.koreanlearner.data.local.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yonsei.koreanlearner.data.local.entity.GrammarExample

/**
 * Room TypeConverter：处理无法直接存储为 SQLite 原始类型的字段。
 *
 * 转换范围：
 * - List<GrammarExample> ↔ JSON String（语法例句列表）
 * - List<String> ↔ JSON String（关联语法列表）
 */
class Converters {

    private val gson = Gson()

    // ── List<GrammarExample> ↔ String ──────────────────────────

    @TypeConverter
    fun fromGrammarExampleList(examples: List<GrammarExample>?): String =
        gson.toJson(examples ?: emptyList<GrammarExample>())

    @TypeConverter
    fun toGrammarExampleList(json: String?): List<GrammarExample> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<GrammarExample>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // ── List<String> ↔ String ──────────────────────────────────

    @TypeConverter
    fun fromStringList(list: List<String>?): String =
        gson.toJson(list ?: emptyList<String>())

    @TypeConverter
    fun toStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // ── List<RoundReviewLog> ↔ String ──────────────────────────

    @TypeConverter
    fun fromRoundReviewLogList(list: List<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog>?): String =
        gson.toJson(list ?: emptyList<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog>())

    @TypeConverter
    fun toRoundReviewLogList(json: String?): List<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog> {
        if (json.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<com.yonsei.koreanlearner.data.local.entity.RoundReviewLog>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
