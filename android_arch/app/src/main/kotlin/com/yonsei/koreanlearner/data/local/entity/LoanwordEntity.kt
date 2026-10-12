package com.yonsei.koreanlearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ══════════════════════════════════════════════════════════════════
 * 韩语外来语与传统/汉字词双轨对照 Room 实体 (LoanwordEntity)
 * ══════════════════════════════════════════════════════════════════
 */
@Entity(tableName = "loanwords")
data class LoanwordEntity(
    @PrimaryKey(autoGenerate = true)
    val id                     : Int = 0,

    @ColumnInfo(name = "loanword")
    val loanword               : String,            // 外来语词汇（如 "아르바이트", "스마트폰", "다이어트"）

    @ColumnInfo(name = "foreign_origin")
    val foreignOrigin          : String,            // 英文/外语原型（如 "Arbeit (德语)", "Smartphone", "Diet"）

    @ColumnInfo(name = "native_equivalent")
    val nativeKoreanEquivalent : String,            // 对应的纯正韩语/汉字词（如 "시간제 일 / 부업", "휴대전화", "체중 감량"）

    @ColumnInfo(name = "chinese_meaning")
    val chineseMeaning         : String,            // 中文释义（如 "兼职 / 打工", "智能手机", "减肥 / 节食"）

    @ColumnInfo(name = "category")
    val category               : String,            // 分类（生活日常、韩式英语Konglish、科技数码、饮食健康等）

    @ColumnInfo(name = "example_korean")
    val exampleKorean          : String,            // 常见语境例句韩文

    @ColumnInfo(name = "example_chinese")
    val exampleChinese         : String,            // 常见语境例句中文

    @ColumnInfo(name = "level")
    val level                  : String = "TOPIK 2" // 等级
)
