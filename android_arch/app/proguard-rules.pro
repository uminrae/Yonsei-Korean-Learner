# ══════════════════════════════════════════════════════════════════
# Proguard / R8 优化与保活规则（「未来的韩语」生产环境）
# ══════════════════════════════════════════════════════════════════

# 1. 保留 Kotlin 标准库与协程
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,SourceFile,LineNumberTable
-keepclassmembers class * {
    @kotlin.jvm.JvmField *;
}

# 2. 保留 Room 数据库与 SQLite
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.yonsei.koreanlearner.data.local.dao.** { *; }

# 3. 保留 Hilt / Dagger 依赖注入
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }
-keep class * extends dagger.hilt.internal.ComponentManager { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# 4. 保留 Gson / 序列化模型与种子注入 DTO
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.yonsei.koreanlearner.domain.model.** { *; }
-keep class com.yonsei.koreanlearner.data.local.entity.** { *; }
-keep class com.yonsei.koreanlearner.data.local.database.** { *; }
-keepclassmembers class com.yonsei.koreanlearner.data.local.database.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 5. 保留 Compose 运行时优化
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.compose.ui.** { *; }
