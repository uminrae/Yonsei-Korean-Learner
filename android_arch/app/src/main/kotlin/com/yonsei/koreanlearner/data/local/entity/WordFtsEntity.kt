package com.yonsei.koreanlearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * ══════════════════════════════════════════════════════════════════
 * 单词全文检索 FTS 虚拟表（WordFtsEntity）
 * ══════════════════════════════════════════════════════════════════
 *
 * 核心机制：
 * 1. 使用 Room @Fts4 外部内容虚拟表（contentEntity = WordEntity::class）。
 * 2. 针对韩文原词、中文翻译、词性建立全文倒排索引。
 * 3. 严格遵循 Room 规范：作为 contentEntity 映射表，不显式声明 rowid 主键。
 */
@Entity(tableName = "words_fts")
@Fts4(contentEntity = WordEntity::class)
data class WordFtsEntity(

    @ColumnInfo(name = "korean")
    val korean: String,

    @ColumnInfo(name = "chinese")
    val chinese: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String
)

/**
 * 全文检索结果模型（包含高亮摘要与词汇基础信息）
 */
data class WordSearchResult(
    @ColumnInfo(name = "id")
    val id: Int,

    @ColumnInfo(name = "book")
    val book: Int,

    @ColumnInfo(name = "lesson")
    val lesson: Int,

    @ColumnInfo(name = "cumulative_index")
    val cumulativeIndex: Int,

    @ColumnInfo(name = "korean")
    val korean: String,

    @ColumnInfo(name = "chinese")
    val chinese: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,

    @ColumnInfo(name = "level")
    val level: String,

    @ColumnInfo(name = "irregular_type")
    val irregularType: String?,

    @ColumnInfo(name = "matchSnippet")
    val matchSnippet: String? = null
)
