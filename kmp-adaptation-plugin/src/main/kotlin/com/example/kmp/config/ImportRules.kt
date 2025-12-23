package com.example.kmp.config

object ImportRules {
    // --- 1. Android 平台强依赖 ---
    private val androidPackages = listOf(
        "android.",
        "androidx.",
        "com.google.android.",
        "dalvik.",
        "org.json.",
        "org.robolectric.",
        "com.google.firebase.",
        "dagger.hilt.",
        "com.jakewharton.rxbinding", // RxBinding
        // 图片加载 (通常视为平台相关)
        "coil.",
        "com.bumptech.glide.",
        "com.squareup.picasso."
    )

    // --- 2. Java 平台特定依赖 (需替换) ---
    private val javaPlatformPackages = listOf(
        "java.io.", "java.nio.", "java.net.", "javax.net.", // IO/Net
        "java.sql.", "javax.sql.",                          // DB
        "java.awt.", "java.applet.",                        // Desktop UI
        "java.time.",                                       // Time (-> kotlinx-datetime)
        "java.util.concurrent.",                            // Concurrency (-> Coroutines)
        "java.lang.reflect.",                               // Reflection
        "com.google.gson.",                                 // Gson (-> kotlinx.serialization)
        "retrofit2.", "okhttp3."                            // Net (-> Ktor)
    )

    // --- 3. 可适配/通用的 Java 依赖 ---
    private val adaptableJavaPackages = listOf(
        "java.util.",       // List, Map, Set
        "java.lang.",       // String, Math
        "java.math.",       // BigInteger
        "java.text.",       // SimpleDateFormat
        "kotlin.",
        "kotlinx.",
        "org.jetbrains.annotations."
    )

    // --- 4. UI 包定义 (白名单) ---
    private val uiPackages = listOf(
        // Android 原生 UI
        "android.view.",
        "android.widget.",
        "android.app.",          // 注意：包含 Activity/Dialog，但也包含 Application/Service (需特殊处理)
        "androidx.appcompat.",
        "androidx.fragment.",
        "androidx.constraintlayout.",
        "androidx.recyclerview.",
        "androidx.viewpager.",
        "androidx.cardview.",
        "androidx.swiperefreshlayout.",
        "com.google.android.material.",

        // Compose UI
        "androidx.compose.ui.",
        "androidx.compose.foundation.",
        "androidx.compose.material",
        "androidx.compose.runtime.Composable" // 包含 @Composable 注解
    )

    // --- 5. 新增：非 UI 类排除列表 (黑名单) ---
    // 这些类虽然在 android.app 或其他 UI 包下，但属于系统服务或逻辑组件
    private val nonUiClasses = setOf(
        "android.app.Application",
        "android.app.Service",
        "android.app.IntentService",
        "android.app.JobSchedulerService",
        "android.app.Notification",
        "android.app.NotificationManager",
        "android.app.NotificationChannel",
        "android.app.AlarmManager",
        "android.app.DownloadManager",
        "android.app.SearchManager",
        "android.app.UiModeManager",
        "android.app.ActivityManager", // 系统服务管理
        "android.app.PendingIntent",
        "android.app.TaskStackBuilder"
    )

    fun isAndroid(pkg: String): Boolean =
        androidPackages.any { pkg.startsWith(it) }

    fun isJavaPlatform(pkg: String): Boolean =
        javaPlatformPackages.any { pkg.startsWith(it) }

    fun isAdaptableJava(pkg: String): Boolean =
        adaptableJavaPackages.any { pkg.startsWith(it) }

    /**
     * 判断是否为 UI 相关依赖。
     * 逻辑：如果它在非 UI 排除列表中 -> False (Logic)
     * 否则如果它在 UI 包列表中 -> True (UI)
     */
    fun isUI(pkg: String): Boolean {
        // 1. 优先排除明确的非 UI 组件
        // 检查 pkg 是否完全匹配，或者属于排除列表中的子类/内部类 (例如 Notification.Builder)
        if (nonUiClasses.any { pkg == it || pkg.startsWith("$it.") }) {
            return false
        }

        // 2. 检查是否在 Android UI 包列表中
        return uiPackages.any { pkg.startsWith(it) }
    }
}

/*    val androidPackages = listOf(
        "android.",
        "androidx.",
        "com.google.android."
    )

    val adaptableJavaPackages = listOf(
        "java.util.",
        "java.lang."
    )

    fun isAndroid(pkg: String): Boolean =
        androidPackages.any { pkg.startsWith(it) }

    fun isAdaptableJava(pkg: String): Boolean =
        adaptableJavaPackages.any { pkg.startsWith(it) }*/


