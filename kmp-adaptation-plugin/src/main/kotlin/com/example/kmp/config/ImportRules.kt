package com.example.kmp.config

object ImportRules {
    // 强依赖 Android 平台的包
    private val androidPackages = listOf(
        // 核心 SDK
        "android.",
        "androidx.",
        "com.google.android.",
        "dalvik.",

        // 常见 Android 专用第三方库
        "org.json.",            // Android SDK 内置的 json
        "org.robolectric.",     // Android 单元测试框架
        "com.google.firebase.", // Firebase Android SDK (需迁移到 Firebase Kotlin SDK)
        "dagger.hilt.",         // Hilt 依赖注入 (Android 专用，KMP 需换 Koin 或 kotlin-inject)
        "butterknife.",         // 旧 View 注入库

        // 视图/UI 相关
        "com.google.android.material.",
        "coil.",                // 图片加载 (KMP 有 Coil 3.x，但在 import 变更前视为平台相关)
        "com.bumptech.glide."   // Glide 图片加载
    )

//     需要特殊处理或替换的 Java 包 (通常意味着需要 KMP 库替换，如 Okio, Ktor)
//     建议迁移到 KMP 替代品：
//     java.io/nio -> okio / kotlinx-io
//     java.net    -> Ktor
//     java.time   -> kotlinx-datetime
//     java.sql    -> SQLDelight / Room KMP
    private val javaPlatformPackages = listOf(
        // IO 与 网络
        "java.io.",
        "java.nio.",
        "java.net.",
        "javax.net.",

        // 数据库与 SQL
        "java.sql.",
        "javax.sql.",

        // Java 扩展与其他
        "javax.",               // javax.inject, javax.annotation 等
        "sun.",                 // JDK 内部 API
        "java.awt.",            // 桌面 UI
        "java.applet.",

        // 时间日期 (强烈建议迁移到 kotlinx-datetime)
        "java.time.",           // Java 8 Date/Time API

        // 并发 (强烈建议迁移到 Kotlin Coroutines)
        "java.util.concurrent.", // Executor, Future, CountDownLatch 等

        // 反射 (KMP 反射能力有限，通常需移除)
        "java.lang.reflect.",

        // 常见的 Java 独有第三方库
        "com.google.gson.",     // Gson (依赖反射，KMP 建议用 kotlinx.serialization)
        "retrofit2.",           // Retrofit (Java 动态代理，KMP 建议用 Ktor)
        "okhttp3.",             // OkHttp (虽然有 KMP 移植计划，但目前主要用于 JVM/Android)
        "org.apache.commons."   // Apache Commons 系列
    )

    // 可直接迁移或易于适配的 Java 包 (如集合、基础类型)
    private val adaptableJavaPackages = listOf(
        "java.util.",       // 大部分集合 (List, Map) 可直接映射，但需注意 UUID, Timer 等特例
        "java.lang.",       // String, Math, System 等
        "java.math.",       // BigInteger, BigDecimal (KMP 有对应库，但包名不同，属于可适配)
        "java.text.",       // SimpleDateFormat (建议换 kotlinx-datetime，但作为临时方案可保留)

        // Kotlin 自身
        "kotlin.",
        "kotlinx.",         // 已经是 KMP 库 (coroutines, serialization, etc.)

        // 常用注解 (通常源码级兼容)
        "org.jetbrains.annotations.",
        "org.intellij.lang.annotations."
    )

    fun isAndroid(pkg: String): Boolean =
        androidPackages.any { pkg.startsWith(it) }

    fun isJavaPlatform(pkg: String): Boolean =
        javaPlatformPackages.any { pkg.startsWith(it) }

    fun isAdaptableJava(pkg: String): Boolean =
        adaptableJavaPackages.any { pkg.startsWith(it) }


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

}
