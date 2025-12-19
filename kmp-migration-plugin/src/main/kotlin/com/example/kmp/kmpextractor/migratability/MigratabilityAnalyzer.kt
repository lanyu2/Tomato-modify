package com.example.kmp.kmpextractor.migratability

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind

class MigratabilityAnalyzer {

    // 定义可以在 KMP commonMain 中找到成熟替代方案的种类
    private val replaceable = setOf(
        AndroidUsageKind.STORAGE,
        AndroidUsageKind.LOGGING,
        AndroidUsageKind.THREADING,
        AndroidUsageKind.TIME,
        AndroidUsageKind.NETWORK, // 需在 AndroidUsageKind 枚举中定义
        AndroidUsageKind.HTTP,    // 需在 AndroidUsageKind 枚举中定义
        AndroidUsageKind.JSON     // 需在 AndroidUsageKind 枚举中定义
    )

    // 定义深度绑定 Android 系统的种类
    private val nonReplaceable = setOf(
        AndroidUsageKind.UI,
        AndroidUsageKind.RESOURCES
    )

    /**
     * 修改后的 analyze 函数
     * 新增 originalCode 参数，并将其传递给 MigratabilityResult
     */
    fun analyze(
        filePath: String,
        usages: Set<AndroidUsageKind>,
        originalCode: String // 新增参数：接收源码全文
    ): MigratabilityResult {

        val reasons = mutableListOf<String>()
        val replacements = mutableListOf<String>()

        val migratability = when {
            usages.isEmpty() -> {
                reasons += "未检测到 Android 特定 API"
                Migratability.COMMON_READY
            }

            usages.any { it in nonReplaceable } -> {
                reasons += "使用了不可平替的 Android API: ${usages.intersect(nonReplaceable)}"
                Migratability.ANDROID_ONLY
            }

            usages.all { it in replaceable || it == AndroidUsageKind.STATE } -> {
                usages.forEach { replacements += replacementFor(it) }
                reasons += "所有 Android 依赖项均有已知的 KMP 平替方案"
                Migratability.MIGRATABLE
            }

            else -> {
                reasons += "混合了多种 Android API，建议手动拆分逻辑"
                Migratability.PARTIAL
            }
        }

        return MigratabilityResult(
            filePath = filePath,
            migratability = migratability,
            androidUsages = usages,
            reasons = reasons,
            suggestedReplacements = replacements.distinct(),
            originalCode = originalCode // 将源码存入结果对象
        )
    }

    private fun replacementFor(kind: AndroidUsageKind): String =
        when (kind) {
            AndroidUsageKind.STORAGE -> "使用 Multiplatform-Settings 或 SQLDelight 数据库"
            AndroidUsageKind.LOGGING -> "使用 Kermit 或 Napier 日志库"
            AndroidUsageKind.THREADING -> "使用 kotlinx.coroutines 实现跨平台并发"
            AndroidUsageKind.TIME -> "使用 kotlinx-datetime 处理日期与时间"
            AndroidUsageKind.STATE -> "使用官方 AndroidX Lifecycle KMP 版 (ViewModel)"
            AndroidUsageKind.NETWORK -> "使用 Ktor 的 Network Observer 机制"
            AndroidUsageKind.HTTP -> "使用 io.ktor:ktor-client 替代 OkHttp/Retrofit"
            AndroidUsageKind.JSON -> "使用 kotlinx.serialization 替代 Gson/JSON"
            else -> "建议使用 expect/actual 声明平台差异"
        }
}