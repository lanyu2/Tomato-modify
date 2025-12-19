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
