package com.yonsei.koreanlearner.di

import android.content.Context
import androidx.room.Room
import com.yonsei.koreanlearner.data.local.database.KoreanDatabase
import com.yonsei.koreanlearner.data.local.dao.GrammarDao
import com.yonsei.koreanlearner.data.local.dao.ReviewRecordDao
import com.yonsei.koreanlearner.data.local.dao.WordDao
import com.yonsei.koreanlearner.data.repository.IoDispatcher
import com.yonsei.koreanlearner.data.repository.ReviewRepositoryImpl
import com.yonsei.koreanlearner.domain.repository.ReviewRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

// ════════════════════════════════════════════════════════════════
// Room 数据库与 DAO 注入
// ════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供单例 KoreanDatabase 实例。
     *
     * 【生产环境推荐】：
     *   将预置数据库文件（korean_learner.db）放入 assets/database/ 目录，
     *   使用 .createFromAsset("database/korean_learner.db") 代替 .build()，
     *   省去首次启动时的 JSON 解析开销。
     *
     * 【开发/调试环境】：
     *   使用 .build() + DatabaseSeeder 动态注入，便于频繁重置数据。
     */
    @Provides
    @Singleton
    fun provideKoreanDatabase(
        @ApplicationContext context: Context
    ): KoreanDatabase = Room.databaseBuilder(
        context,
        KoreanDatabase::class.java,
        KoreanDatabase.DATABASE_NAME
    )
        // ── 生产环境：支持预置数据库导入 ──
        // .createFromAsset("database/yonsei_korean.db")

        // ── 迁移策略：注入增量迁移脚本与降级保护 ──
        .addMigrations(*com.yonsei.koreanlearner.data.local.database.DatabaseMigrations.ALL_MIGRATIONS)
        .fallbackToDestructiveMigration()
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Provides
    fun provideWordDao(db: KoreanDatabase): WordDao = db.wordDao()

    @Provides
    fun provideGrammarDao(db: KoreanDatabase): GrammarDao = db.grammarDao()

    @Provides
    fun provideReviewRecordDao(db: KoreanDatabase): ReviewRecordDao = db.reviewRecordDao()

    @Provides
    fun provideIdiomDao(db: KoreanDatabase): com.yonsei.koreanlearner.data.local.dao.IdiomDao = db.idiomDao()

    @Provides
    fun provideLoanwordDao(db: KoreanDatabase): com.yonsei.koreanlearner.data.local.dao.LoanwordDao = db.loanwordDao()
}

// ════════════════════════════════════════════════════════════════
// Repository 绑定
// ════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** 将 ReviewRepositoryImpl（数据层）绑定到 ReviewRepository（领域层接口） */
    @Binds
    @Singleton
    abstract fun bindReviewRepository(
        impl: ReviewRepositoryImpl
    ): ReviewRepository
}

// ════════════════════════════════════════════════════════════════
// Coroutine Dispatcher 注入
// ════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {

    /** 提供 IO Dispatcher（用于所有数据库和文件 I/O 操作） */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
