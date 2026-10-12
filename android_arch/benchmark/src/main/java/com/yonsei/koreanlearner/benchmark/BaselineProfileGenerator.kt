package com.yonsei.koreanlearner.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ══════════════════════════════════════════════════════════════════
 * Baseline Profile 自动化规则生成器（BaselineProfileGenerator）
 * ══════════════════════════════════════════════════════════════════
 *
 * 运行命令：
 *   ./gradlew :benchmark:generateBaselineProfile
 *
 * 覆盖的关键用户旅程（CUJ - Critical User Journeys）：
 * 1. 【CUJ 1: 冷启动至主页渲染完成】(Cold Start -> Home Render)
 * 2. 【CUJ 2: 首页 Feed 与 Story 进度行顺畅滑动】(Feed Scrolling)
 * 3. 【CUJ 3: 单词卡片 3D 翻转与手势滑卡】(Card Flip & Gestures)
 * 4. 【CUJ 4: 语法树折叠展开与搜索流】(Grammar Tree Interactions)
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    private val targetPackage = "com.yonsei.koreanlearner"

    @Test
    fun generateAllCoreProfiles() {
        baselineProfileRule.collect(
            packageName = targetPackage,
            includeInStartupProfile = true // 标记关键类优先纳入 Startup 预编译
        ) {
            // ── CUJ 1: 冷启动 ─────────────────────────────────────
            pressHome()
            startActivityAndWait()

            // ── CUJ 2: 首页 Feed 与 Story 滚动 ───────────────────
            device.wait(Until.hasObject(By.pkg(targetPackage).depth(0)), 5000)

            // 滚动 Story 进度条
            val storyRow = device.findObject(By.scrollable(true))
            storyRow?.setGestureMargin(device.displayWidth / 5)
            storyRow?.fling(Direction.RIGHT)
            device.waitForIdle()

            // 垂直滚动首页词汇列表
            device.findObject(By.scrollable(true))?.fling(Direction.DOWN)
            device.waitForIdle()
            device.findObject(By.scrollable(true))?.fling(Direction.UP)
            device.waitForIdle()

            // ── CUJ 3: 导航至单词卡片与 3D 翻转交互 ──────────────
            val wordTab = device.findObject(By.text("单词流"))
            wordTab?.click()
            device.waitForIdle()

            // 点击卡片执行 3D 翻转
            val card = device.findObject(By.pkg(targetPackage))
            card?.click()
            device.waitForIdle()

            // ── CUJ 4: 导航至语法树 ──────────────────────────────
            val grammarTab = device.findObject(By.text("语法树"))
            grammarTab?.click()
            device.waitForIdle()

            // 点击展开折叠课时分组
            val groupHeader = device.findObject(By.textContains("第1册"))
            groupHeader?.click()
            device.waitForIdle()
        }
    }
}
