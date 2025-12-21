package com.example.kmp.model

enum class DependencyType {
    COMMON,             // 纯 Kotlin/KMP 代码，可放 commonMain
    ADAPTABLE_JAVA,     // 包含 java.util 等，稍作修改可放 commonMain
    JAVA_PLATFORM,      // 包含 java.io/net，建议放 jvmMain 或寻找 KMP 替代品
    ANDROID_PLATFORM    // 包含 android.*，必须放 androidMain 或使用 expect/actual
}
