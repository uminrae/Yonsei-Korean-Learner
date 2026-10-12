package com.yonsei.koreanlearner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yonsei.koreanlearner.data.local.dao.GrammarDao
import com.yonsei.koreanlearner.data.local.dao.IdiomDao
import com.yonsei.koreanlearner.data.local.dao.LoanwordDao
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.local.entity.GrammarEntity
import com.yonsei.koreanlearner.data.local.entity.IdiomEntity
import com.yonsei.koreanlearner.data.local.entity.LoanwordEntity
import com.yonsei.koreanlearner.data.local.entity.ReviewRecordEntity
import com.yonsei.koreanlearner.data.local.entity.WordEntity
import com.yonsei.koreanlearner.data.local.entity.WordFtsEntity

/**
 * ══════════════════════════════════════════════════════════════════
 * Room 数据库核心配置类 (KoreanDatabase)
 * ══════════════════════════════════════════════════════════════════
 */
@Database(
    entities = [
        WordEntity::class,
        WordFtsEntity::class,
        GrammarEntity::class,
        ReviewRecordEntity::class,
        IdiomEntity::class,
        LoanwordEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KoreanDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun grammarDao(): GrammarDao
    abstract fun reviewRecordDao(): ReviewRecordDao
    abstract fun idiomDao(): IdiomDao
    abstract fun loanwordDao(): LoanwordDao

    companion object {
        const val DATABASE_NAME = "korean_learner.db"
    }
}
