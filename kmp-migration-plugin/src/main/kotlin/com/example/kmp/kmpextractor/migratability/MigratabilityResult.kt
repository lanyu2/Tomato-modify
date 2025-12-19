package com.example.kmp.kmpextractor.migratability

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind

data class MigratabilityResult(
    val filePath: String,
    val migratability: Migratability,
    val androidUsages: Set<AndroidUsageKind>,
    val reasons: List<String>,
    val suggestedReplacements: List<String>
) {
    fun toReadableString(): String =
        buildString {
            appendLine("[${migratability}] $filePath")
            appendLine("  Android usages: $androidUsages")
            reasons.forEach { appendLine("  - $it") }
            suggestedReplacements.forEach { appendLine("  * $it") }
        }
}
