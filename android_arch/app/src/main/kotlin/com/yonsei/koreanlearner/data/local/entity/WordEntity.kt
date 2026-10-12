package com.yonsei.koreanlearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity for 延世韩国语 vocabulary (1~6册, 4,446词).
 *
 * ID strategy: book * 10_000 + cumulativeIndex
 *   → Book1: 10001..11061, Book2: 20001..20880, ..., Book6: 60001..60481
 * This guarantees global uniqueness while preserving natural ordering.
 */
@Entity(
    tableName = "words",
    indices = [
        Index(value = ["book"]),
        Index(value = ["book", "lesson"]),
        Index(value = ["cumulative_index"]),
        Index(value = ["level"])
    ]
)
data class WordEntity(

    /** Global unique ID = book * 10_000 + cumulative_index */
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int,

    /** 册数 1~6 */
    @ColumnInfo(name = "book")
    val book: Int,

    /** 课次 1~10 */
    @ColumnInfo(name = "lesson")
    val lesson: Int,

    /** 册内累积序号 (种子数据中的 cumulative_index) */
    @ColumnInfo(name = "cumulative_index")
    val cumulativeIndex: Int,

    /** 韩文原文 */
    @ColumnInfo(name = "korean")
    val korean: String,

    /** 中文释义 */
    @ColumnInfo(name = "chinese")
    val chinese: String,

    /** 词性：名词 / 动词 / 形容词 / 副词 / 助词 / 常用语 / 惯用型/短语 / 数词 / 量词 / 代词 */
    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String,

    /** TOPIK 等级：TOPIK 1 ~ TOPIK 6 */
    @ColumnInfo(name = "level")
    val level: String,

    /** 不规则变形类型；如无则为 null
     *  取值范围：ㅂ不规则 / 르不规则 / 으脱落 / ㅅ不规则 / ㅎ不规则 / ㄷ不规则 */
    @ColumnInfo(name = "irregular_type")
    val irregularType: String? = null
)
