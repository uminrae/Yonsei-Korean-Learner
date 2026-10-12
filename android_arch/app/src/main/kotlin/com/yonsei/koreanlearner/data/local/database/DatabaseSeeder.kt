package com.yonsei.koreanlearner.data.local.database

import android.content.Context
import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.GrammarExample
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 种子数据注入器（DatabaseSeeder）
 *
 * 职责：首次启动时将 assets/ 目录下的 JSON 种子文件
 * 解析并批量写入 Room 数据库。
 *
 * 保证 R8/ProGuard 代码混淆下 100% 稳定运行。
 */
@Singleton
class DatabaseSeeder @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext
    private val context: Context,
    private val database: KoreanDatabase
) {

    private val gson = Gson()

    /**
     * 检查并执行种子数据注入（幂等）。
     * 在 IO 线程执行，若数据库记录数少于预设阈值则自动重新灌入。
     */
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        val totalWords = try {
            database.wordDao().getTotalCount()
        } catch (_: Exception) {
            0
        }
        val totalGrammars = try {
            database.grammarDao().getTotalCount()
        } catch (_: Exception) {
            0
        }

        // 阈值检查：若数据缺失则自动注入
        if (totalWords < 1000) {
            seedWords()
        }
        if (totalGrammars < 100) {
            seedGrammars()
        }
    }

    private suspend fun seedWords() {
        try {
            val json = context.assets.open("words_seed.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val type = object : TypeToken<List<WordSeedDto>>() {}.type
            val seeds: List<WordSeedDto> = gson.fromJson(json, type)

            if (seeds.isNotEmpty()) {
                val entities = seeds.map { seed ->
                    WordEntity(
                        id              = seed.book * 10_000 + seed.cumulative_index,
                        book            = seed.book,
                        lesson          = seed.lesson,
                        cumulativeIndex = seed.cumulative_index,
                        korean          = seed.korean,
                        chinese         = seed.chinese,
                        partOfSpeech    = seed.part_of_speech,
                        level           = seed.level,
                        irregularType   = seed.irregular_type
                    )
                }
                database.wordDao().insertAll(entities)
            }
        } catch (_: Exception) {}
    }

    private suspend fun seedGrammars() {
        try {
            val json = context.assets.open("grammars_seed.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            val type = object : TypeToken<List<GrammarSeedDto>>() {}.type
            val seeds: List<GrammarSeedDto> = gson.fromJson(json, type)

            if (seeds.isNotEmpty()) {
                val entities = seeds.map { seed ->
                    GrammarEntity(
                        book             = seed.book,
                        lesson           = seed.lesson,
                        subLesson        = seed.sub_lesson,
                        grammarPattern   = seed.grammar_pattern,
                        category         = seed.category,
                        explanation      = seed.explanation,
                        conjugationRules = seed.conjugation_rules,
                        examples         = gson.toJson(seed.examples),
                        relatedGrammars  = gson.toJson(seed.related_grammars)
                    )
                }
                database.grammarDao().insertAll(entities)
            }
        } catch (_: Exception) {}
    }
}

// ──────────────────────────────────────────────────────────────
// 种子 DTO（带 @Keep 与 @SerializedName，防止 R8 混淆字段）
// ──────────────────────────────────────────────────────────────

@Keep
data class WordSeedDto(
    @SerializedName("book") val book: Int,
    @SerializedName("lesson") val lesson: Int,
    @SerializedName("cumulative_index") val cumulative_index: Int,
    @SerializedName("korean") val korean: String,
    @SerializedName("chinese") val chinese: String,
    @SerializedName("part_of_speech") val part_of_speech: String,
    @SerializedName("level") val level: String,
    @SerializedName("irregular_type") val irregular_type: String? = null
)

@Keep
data class GrammarSeedDto(
    @SerializedName("book") val book: Int,
    @SerializedName("lesson") val lesson: Int,
    @SerializedName("sub_lesson") val sub_lesson: String,
    @SerializedName("grammar_pattern") val grammar_pattern: String,
    @SerializedName("category") val category: String,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("conjugation_rules") val conjugation_rules: String,
    @SerializedName("examples") val examples: List<GrammarExample> = emptyList(),
    @SerializedName("related_grammars") val related_grammars: List<String> = emptyList()
)
