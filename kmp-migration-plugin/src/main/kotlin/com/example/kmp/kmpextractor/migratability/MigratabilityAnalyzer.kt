/*
package com.example.kmp.kmpextractor.migratability

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind

class MigratabilityAnalyzer {

    private val replaceable = setOf(
        AndroidUsageKind.STORAGE,
        AndroidUsageKind.LOGGING,
        AndroidUsageKind.THREADING,
        AndroidUsageKind.TIME
    )

    private val nonReplaceable = setOf(
        AndroidUsageKind.UI,
        AndroidUsageKind.STATE,
        AndroidUsageKind.RESOURCES
    )

    fun analyze(
        filePath: String,
        usages: Set<AndroidUsageKind>
    ): MigratabilityResult {

        val reasons = mutableListOf<String>()
        val replacements = mutableListOf<String>()

        val migratability = when {
            usages.isEmpty() -> {
                reasons += "No Android-specific APIs detected"
                Migratability.COMMON_READY
            }

            usages.any { it in nonReplaceable } -> {
                reasons += "Uses non-replaceable Android APIs: ${usages.intersect(nonReplaceable)}"
                Migratability.ANDROID_ONLY
            }

            usages.all { it in replaceable } -> {
                usages.forEach { replacements += replacementFor(it) }
                reasons += "All Android usages have known KMP replacements"
                Migratability.MIGRATABLE
            }

            else -> {
                reasons += "Mixed Android API usage, requires manual split"
                Migratability.PARTIAL
            }
        }

        return MigratabilityResult(
            filePath = filePath,
            migratability = migratability,
            androidUsages = usages,
            reasons = reasons,
            suggestedReplacements = replacements.distinct()
        )
    }

    private fun replacementFor(kind: AndroidUsageKind): String =
        when (kind) {
            AndroidUsageKind.STORAGE -> "Use com.russhwolf:multiplatform-settings"
            AndroidUsageKind.LOGGING -> "Use co.touchlab:kermit"
            AndroidUsageKind.THREADING -> "Use kotlinx.coroutines"
            AndroidUsageKind.TIME -> "Use kotlin.time or expect/actual"
            else -> "No replacement"
        }
}
*/
package com.example.kmp.kmpextractor.migratability

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind

class MigratabilityAnalyzer {

    // 定义可以在 KMP commonMain 中找到成熟替代方案的种类
    private val replaceable = setOf(
        AndroidUsageKind.STORAGE,
        AndroidUsageKind.LOGGING,
        AndroidUsageKind.THREADING,
        AndroidUsageKind.TIME,
        AndroidUsageKind.NETWORK,
        AndroidUsageKind.HTTP,
        AndroidUsageKind.JSON
    )

    // 定义深度绑定 Android 系统的种类
    private val nonReplaceable = setOf(
        AndroidUsageKind.UI,
        AndroidUsageKind.RESOURCES
    )

    fun analyze(filePath: String, usages: Set<AndroidUsageKind>): MigratabilityResult {
        val reasons = mutableListOf<String>()
        val replacements = mutableListOf<String>()

        val migratability = when {
            usages.isEmpty() -> {
                reasons += "未检测到 Android 依赖，可直接迁移"
                Migratability.COMMON_READY
            }

            usages.any { it in nonReplaceable } -> {
                reasons += "包含不可平替的 UI/系统组件: ${usages.intersect(nonReplaceable)}"
                Migratability.ANDROID_ONLY
            }

            usages.all { it in replaceable || it == AndroidUsageKind.STATE } -> {
                usages.forEach {
                    val suggestion = replacementFor(it)
                    if (suggestion != "No replacement") replacements += suggestion
                }
                reasons += "所有 Android 依赖均有成熟的 KMP 平替方案"
                Migratability.MIGRATABLE
            }

            else -> {
                reasons += "混合了多种 API，建议手动拆分业务逻辑与平台代码"
                Migratability.PARTIAL
            }
        }

        return MigratabilityResult(
            filePath = filePath,
            migratability = migratability,
            androidUsages = usages,
            reasons = reasons,
            suggestedReplacements = replacements.distinct()
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