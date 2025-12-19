package com.example.kmp.kmpextractor.migratability

import kotlinx.serialization.Serializable
import com.example.kmp.kmpextractor.classifier.AndroidUsageKind

/**
 * 结构化迁移结果模型
 * 添加 @Serializable 注解以便导出为 JSON 格式交给 LLM 改写
 */
@Serializable
data class MigratabilityResult(
    val filePath: String,                // 文件的相对路径
    val migratability: Migratability,     // 迁移可行性等级 (COMMON_READY, MIGRATABLE, etc.)
    val androidUsages: Set<AndroidUsageKind>, // 检测到的 Android 依赖种类
    val reasons: List<String>,            // 判定理由列表
    val suggestedReplacements: List<String>, // 针对检测到的依赖建议的 KMP 平替方案
    val originalCode: String             // 核心补全：包含完整的源码内容，作为 LLM 改写的输入上下文
) {
    /**
     * 保留原始的可读性输出方法，方便开发者在控制台查看摘要
     */
    fun toReadableString(): String =
        buildString {
            appendLine("[${migratability}] $filePath")
            appendLine("  Android usages: $androidUsages")
            reasons.forEach { appendLine("  - $it") }
            if (suggestedReplacements.isNotEmpty()) {
                appendLine("  Suggested KMP Replacements:")
                suggestedReplacements.forEach { appendLine("  * $it") }
            }
        }
}