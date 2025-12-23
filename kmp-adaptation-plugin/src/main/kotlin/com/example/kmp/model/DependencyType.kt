package com.example.kmp.model

enum class DependencyType {
    COMMON_LOGIC,       // 纯 Kotlin 逻辑 (Domain, Utils) -> commonMain
    COMMON_UI,          // Compose Multiplatform UI -> commonMain

    ANDROID_LOGIC,      // 依赖 Context, Sensor, WorkManager 等系统能力 -> androidMain
    ANDROID_UI,         // 依赖 View, Activity, Fragment, 或 Android专用 Compose -> androidMain

    JAVA_PLATFORM,      // 依赖 java.io/net 等 -> jvmMain/androidMain 或需替换
    ADAPTABLE_JAVA      // 可适配的 Java 代码 -> commonMain
}
