package com.futurekorean.app

import android.content.Context
import android.graphics.Color as AndroidColor
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.Surface
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.yonsei.koreanlearner.data.local.database.DatabaseInitializer
import com.yonsei.koreanlearner.data.local.database.DatabaseSeeder
import com.yonsei.koreanlearner.glance.DailyWordUpdateWorker
import com.yonsei.koreanlearner.ui.navigation.KoreanLearnerNavGraph
import com.yonsei.koreanlearner.ui.theme.KoreanLearnerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ══════════════════════════════════════════════════════════════════
 * 「未来的韩语」— 单 Activity Compose 主入口
 * 针对荣耀 MagicOS 9.0 强锁 120Hz 满血高刷 + 自动全量数据就绪保障
 * ══════════════════════════════════════════════════════════════════
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var databaseInitializer: DatabaseInitializer

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    @Inject
    lateinit var appStateManager: com.yonsei.koreanlearner.core.state.AppStateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 全屏沉浸式纯白状态栏与导航栏（状态栏文字为高对比深黑色）
        try {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    AndroidColor.TRANSPARENT,
                    AndroidColor.TRANSPARENT
                ),
                navigationBarStyle = SystemBarStyle.light(
                    AndroidColor.TRANSPARENT,
                    AndroidColor.TRANSPARENT
                )
            )
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        } catch (_: Throwable) {}

        // 2. 强行向 SurfaceFlinger 渲染器申请屏幕支持的最高 120Hz 刷新率
        enableHighRefreshRateSafely()
        window.decorView.post {
            enableHighRefreshRateSafely()
        }

        // 3. 异步确保全量 4446 词汇与 314 条语法即时就绪
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                databaseInitializer.initialize()
            } catch (_: Throwable) {
                try {
                    databaseSeeder.seedIfEmpty()
                } catch (_: Throwable) {}
            }
        }

        // 4. 后台惰性调度桌面小组件定时任务（不占用主线程冷启动时间）
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                DailyWordUpdateWorker.schedulePeriodic(applicationContext)
            } catch (_: Throwable) {}
        }

        // 5. Compose 界面渲染（语法第一 · 单词第二 · 全局状态记忆）
        setContent {
            KoreanLearnerTheme {
                KoreanLearnerNavGraph(appStateManager = appStateManager)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Window 正式挂载到 SurfaceFlinger 时二次确保 120Hz 锁定与状态栏高光
        enableHighRefreshRateSafely()
        try {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        } catch (_: Throwable) {}
    }

    override fun onResume() {
        super.onResume()
        // 恢复前台时再次强锁 120Hz 与纯白天顶
        enableHighRefreshRateSafely()
        try {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        } catch (_: Throwable) {}
    }

    /**
     * 针对国产系统（荣耀 MagicOS / 华为等）省电锁频策略的强行突破
     * 直接向系统的 SurfaceFlinger 显示渲染器申请屏幕支持的最高刷新率 (120Hz)
     */
    private fun enableHighRefreshRateSafely() {
        try {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            window.setFormat(android.graphics.PixelFormat.RGBA_8888)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val displayManager = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
                val currentDisplay = display ?: displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
                val modes = currentDisplay?.supportedModes
                val max120Mode = modes?.maxByOrNull { it.refreshRate } // 寻找 120Hz / 144Hz 模式

                if (max120Mode != null && max120Mode.refreshRate >= 90f) {
                    val lp = window.attributes
                    lp.preferredDisplayModeId = max120Mode.modeId // 强行指定 120Hz 模式 ID
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        lp.preferredRefreshRate = max120Mode.refreshRate
                    }
                    window.attributes = lp
                }
            }

            // 针对 Android 12+ / 荣耀 MagicOS 9.0 深度 SurfaceControl 120Hz 锁频
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                try {
                    val rootControl = window.decorView.rootSurfaceControl
                    val setFrameRateMethod = rootControl?.javaClass?.methods?.find {
                        it.name == "setFrameRate" && it.parameterTypes.size == 3
                    }
                    setFrameRateMethod?.invoke(rootControl, 120f, 0, 1)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }
}
