package com.futurekorean.app

import android.app.Application
import android.content.Context
import com.yonsei.koreanlearner.data.local.database.DatabaseInitializer
import com.yonsei.koreanlearner.data.local.database.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ══════════════════════════════════════════════════════════════════
 * 「未来的韩语」— 全局 Application 入口类
 * 极速冷启动 + 自动在后台注入预置全量词库与语法库
 * ══════════════════════════════════════════════════════════════════
 */
@HiltAndroidApp
class FutureKoreanApplication : Application() {

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    override fun attachBaseContext(base: Context) {
        // 第一时间在 attachBaseContext 挂载异常捕获器
        CrashHandler.install(base)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        CrashHandler.install(this)

        // 无论何种入口启动，第一时间在后台协程检查并自动注入全量 4,446 词汇与 314 条语法
        CoroutineScope(Dispatchers.IO).launch {
            try {
                databaseInitializer.initialize()
            } catch (_: Throwable) {
                try {
                    databaseSeeder.seedIfEmpty()
                } catch (_: Throwable) {}
            }
        }
    }
}
