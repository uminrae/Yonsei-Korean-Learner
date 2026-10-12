package com.yonsei.koreanlearner.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 俗语 / 四字成语 / 拟声拟态词分类枚举
 */
enum class IdiomType(val displayName: String, val badgeColorHex: String) {
    /** 韩国传统俗语/俗谈（如 쇠귀에 경 읽기, 누워서 떡 먹기） */
    IDIOM("韩国俗语 (속담)", "#E1306C"),

    /** 汉字四字成语（如 고진감래, 십시일반, 칠전팔기） */
    FOUR_CHAR("四字成语 (사자성어)", "#833AB4"),

    /** 拟声词 / 拟态词（如 따르릉, 차곡차곡, 다닥다닥, 두근두근） */
    ONOMATOPOEIA("拟声拟态 (의성·의태)", "#F59E0B")
}

/**
 * 情境对话例句模型
 */
data class IdiomDialogue(
    val speakerA : String,          // 说话人 A 韩文
    val speakerB : String,          // 说话人 B 韩文（包含目标词汇）
    val chineseA : String,          // 中文翻译 A
    val chineseB : String           // 中文翻译 B
)

/**
 * ══════════════════════════════════════════════════════════════════
 * 俗语/成语/拟声拟态词 Room 实体 (IdiomEntity)
 * ══════════════════════════════════════════════════════════════════
 */
@Entity(tableName = "idioms")
data class IdiomEntity(
    @PrimaryKey(autoGenerate = true)
    val id                 : Int = 0,

    @ColumnInfo(name = "korean")
    val korean             : String,                // 韩文词条（如 "쇠귀에 경 읽기", "고진감래", "차곡차곡"）

    @ColumnInfo(name = "hanja")
    val hanja              : String? = null,        // 汉字词源（如 "苦盡甘來"，俗语/拟态词可为 null）

    @ColumnInfo(name = "type")
    val type               : IdiomType,             // 分类类型

    @ColumnInfo(name = "literal_meaning")
    val literalMeaning     : String,                // 字面直译（如 "给牛耳朵诵经"）

    @ColumnInfo(name = "metaphor_meaning")
    val metaphorMeaning    : String,                // 语境隐喻义 / 对应中文成语（如 "对牛弹琴，白费口舌"）

    @ColumnInfo(name = "cultural_background")
    val culturalBackground : String,                // 韩国文化历史背景深度解析

    @ColumnInfo(name = "dialogues_json")
    val dialoguesJson      : String = "[]",         // 对话例句 JSON

    @ColumnInfo(name = "level")
    val level              : String = "TOPIK 3",    // 等级

    @ColumnInfo(name = "category")
    val category           : String = "生活智慧",     // 主题分类（生活智慧、态度、状态、声音形态等）

    @ColumnInfo(name = "icon_symbol")
    val iconSymbol         : String = ""          // 对应微动效展示图标（如 , , , ）
)
