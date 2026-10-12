package com.yonsei.koreanlearner.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ══════════════════════════════════════════════════════════════════
 * Macrobenchmark 性能基准量化测试（启动耗时与帧率分析）
 * ══════════════════════════════════════════════════════════════════
 *
 * 运行命令：
 *   ./gradlew :benchmark:connectedCheck
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupAndScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val targetPackage = "com.yonsei.koreanlearner"

    /**
     * 测试 1：对比未编译 JIT vs 启用 Baseline Profile 的冷启动速度
     */
    @Test
    fun startupColdNoCompilation() = measureStartup(CompilationMode.None())

    @Test
    fun startupColdWithBaselineProfile() = measureStartup(CompilationMode.Partial())

    private fun measureStartup(compilationMode: CompilationMode) {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            pressHome()
            startActivityAndWait()
        }
    }

    /**
     * 测试 2：测试首页 Feed 与卡片滑动 120fps 帧率（丢帧与 Frame Overrun 量化）
     */
    @Test
    fun scrollFeedFrameTiming() {
        benchmarkRule.measureRepeated(
            packageName = targetPackage,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 5,
            startupMode = StartupMode.WARM
        ) {
            val list = device.findObject(By.scrollable(true))
            list?.setGestureMargin(device.displayWidth / 5)

            // 快速连续滑动，测试渲染管线承载力
            repeat(3) {
                list?.fling(Direction.DOWN)
                device.waitForIdle()
                list?.fling(Direction.UP)
                device.waitForIdle()
            }
        }
    }
}
