// android_arch/build.gradle.kts
// ─────────────────────────────────────────────────────────────
// 「未来的韩语」— 根工程 Gradle 配置 (Kotlin DSL)
// ─────────────────────────────────────────────────────────────

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}
