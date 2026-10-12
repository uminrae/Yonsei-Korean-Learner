package com.yonsei.koreanlearner.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * ══════════════════════════════════════════════════════════════════
 * Room 数据库版本迁移策略与脚本 (DatabaseMigrations)
 * ══════════════════════════════════════════════════════════════════
 */
object DatabaseMigrations {

    /**
     * 版本 1 -> 2 迁移脚本：
     * 添加 FTS4 全文索引虚拟表 `words_fts` 并同步已有词条
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 创建 FTS4 虚拟表
            db.execSQL("""
                CREATE VIRTUAL TABLE IF NOT EXISTS `words_fts` USING FTS4(
                    `korean` TEXT NOT NULL,
                    `chinese` TEXT NOT NULL,
                    `part_of_speech` TEXT NOT NULL,
                    content=`words`
                )
            """.trimIndent())

            // 将现有 words 表数据同步建立索引
            db.execSQL("""
                INSERT INTO `words_fts`(`docid`, `korean`, `chinese`, `part_of_speech`)
                SELECT `id`, `korean`, `chinese`, `part_of_speech` FROM `words`
            """.trimIndent())
        }
    }

    /**
     * 版本 2 -> 3 迁移脚本：
     * 添加 `idioms`（俗语/成语/拟态词）与 `loanwords`（外来语双轨对照）表
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 创建 idioms 表
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `idioms` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `korean` TEXT NOT NULL,
                    `hanja` TEXT,
                    `type` TEXT NOT NULL,
                    `literal_meaning` TEXT NOT NULL,
                    `metaphor_meaning` TEXT NOT NULL,
                    `cultural_background` TEXT NOT NULL,
                    `dialogues_json` TEXT NOT NULL,
                    `level` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `icon_symbol` TEXT NOT NULL
                )
            """.trimIndent())

            // 创建 loanwords 表
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `loanwords` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `loanword` TEXT NOT NULL,
                    `foreign_origin` TEXT NOT NULL,
                    `native_equivalent` TEXT NOT NULL,
                    `chinese_meaning` TEXT NOT NULL,
                    `category` TEXT NOT NULL,
                    `example_korean` TEXT NOT NULL,
                    `example_chinese` TEXT NOT NULL,
                    `level` TEXT NOT NULL
                )
            """.trimIndent())
        }
    }

    /** 全量迁移脚本列表 */
    val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
