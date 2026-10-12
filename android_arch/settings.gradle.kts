// android_arch/settings.gradle.kts
// ─────────────────────────────────────────────────────────────
// 「未来的韩语」— 模块与仓库解析策略
// ─────────────────────────────────────────────────────────────

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "FutureKoreanApp"
include(":app")
