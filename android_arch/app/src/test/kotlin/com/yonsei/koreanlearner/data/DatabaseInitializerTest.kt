package com.yonsei.koreanlearner.data

import com.yonsei.koreanlearner.data.local.database.DatabaseInitStatus
import com.yonsei.koreanlearner.data.local.database.DatabaseMigrations
import org.junit.Assert.*
import org.junit.Test

/**
 * 数据库预置加载、版本迁移与初始化器单元测试
 */
class DatabaseInitializerTest {

    @Test
    fun testMigrationsArray() {
        val migrations = DatabaseMigrations.ALL_MIGRATIONS
        assertEquals(2, migrations.size)
        assertEquals(1, migrations[0].startVersion)
        assertEquals(2, migrations[0].endVersion)
        assertEquals(2, migrations[1].startVersion)
        assertEquals(3, migrations[1].endVersion)
    }

    @Test
    fun testDatabaseInitStatusModel() {
        val idle = DatabaseInitStatus.Idle
        val loading = DatabaseInitStatus.Loading(0.5f, "正在校验词库...")
        val ready = DatabaseInitStatus.Ready(4446, 314)

        assertEquals(0.5f, loading.progress, 0.001f)
        assertEquals(4446, ready.totalWords)
        assertEquals(314, ready.totalGrammars)
    }
}
