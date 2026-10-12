package com.yonsei.koreanlearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity for 延世韩国语 grammar points (1~4册, 314条).
 *
 * examples 和 relatedGrammars 字段以 JSON 字符串形式存储，
 * 通过 [com.yonsei.koreanlearner.data.local.database.Converters] 进行 TypeConversion.
 */
@Entity(
    tableName = "grammars",
    indices = [
        Index(value = ["book"]),
        Index(value = ["book", "lesson"]),
        Index(value = ["sub_lesson"]),
        Index(value = ["category"])
    ]
)
data class GrammarEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    /** 册数 1~4 */
    @ColumnInfo(name = "book")
    val book: Int,

    /** 课次 1~10 */
    @ColumnInfo(name = "lesson")
    val lesson: Int,

    /** 子课号，如 "1-1", "3-4", "10-2" */
    @ColumnInfo(name = "sub_lesson")
    val subLesson: String,

    /** 语法形态，如 "-기 때문에", "-(으)면서" */
    @ColumnInfo(name = "grammar_pattern")
    val grammarPattern: String,

    /** 功能分类，如 "原因/理由", "转折/让步", "时间/顺序" */
    @ColumnInfo(name = "category")
    val category: String,

    /**
     * 完整说明，含：
     * - 使用场合/语义
     * - 限制条件（不可用于命令句/共动句/时制限制）
     * - 与近似语法的辨析
     */
    @ColumnInfo(name = "explanation")
    val explanation: String,

    /**
     * 形态变化加法规则，如：
     * "名词+때문에 / 谓词词干+-기 때문에"
     * "词干末有收音+(으)면서 / 无收音+면서"
     */
    @ColumnInfo(name = "conjugation_rules")
    val conjugationRules: String,

    /**
     * 双语例句列表，JSON 序列化字符串。
     * 原型：List<GrammarExample>
     * 通过 Room TypeConverter 转换。
     */
    @ColumnInfo(name = "examples")
    val examples: String,

    /**
     * 关联/易混淆语法列表，JSON 序列化字符串。
     * 原型：List<String>，如 ["-아/어/여서", "-(으)니까"]
     */
    @ColumnInfo(name = "related_grammars")
    val relatedGrammars: String
)

/** 语法例句数据类（用于序列化/反序列化，防 R8 混淆） */
@androidx.annotation.Keep
data class GrammarExample(
    @com.google.gson.annotations.SerializedName("korean")
    val korean: String,
    @com.google.gson.annotations.SerializedName("chinese")
    val chinese: String
)
