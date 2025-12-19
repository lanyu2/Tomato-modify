package com.example.kmp.kmpextractor.tasks

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind
import com.example.kmp.kmpextractor.migratability.MigratabilityAnalyzer
import com.example.kmp.kmpextractor.migratability.MigratabilityResult
import com.example.kmp.kmpextractor.scanner.KotlinSourceScanner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

abstract class MigratabilityTask : DefaultTask() {
    init {
        group = "kmp-migration"
        description = "Analyze Android code migratability and generate JSON report for LLM"
    }

    @TaskAction
    fun run() {
        val srcDir = File(project.projectDir, "src")
        if (!srcDir.exists()) return

        val scanner = KotlinSourceScanner()
        val analyzer = MigratabilityAnalyzer()

        // 1. 遍历并分析所有 Kotlin 文件
        val results = srcDir.walkTopDown()
            .filter { it.extension == "kt" }
            .map { file ->
                val usages: Set<AndroidUsageKind> = scanner.detectAndroidUsages(file)
                val relativePath = file.relativeTo(project.projectDir).path
                val sourceCode = file.readText() // 读取源码全文供 LLM 改写使用

                // 调用分析器，传入路径、依赖种类和源码内容
                analyzer.analyze(
                    filePath = relativePath,
                    usages = usages,
                    originalCode = sourceCode
                )
            }
            .toList()

        val outputDir = File(project.projectDir, "kmp-migration")
        outputDir.mkdirs()

        // 2. 生成面向人类阅读的文本报告 (保留原功能)
        File(outputDir, "report.txt").writeText(
            results.joinToString("\n\n") { it.toReadableString() }
        )

        // 3. 生成面向 LLM 的结构化 JSON 报告 (核心新增)
        // 使用 prettyPrint 使 JSON 易于阅读和调试
        val jsonSerializer = Json { prettyPrint = true }
        val jsonReport = jsonSerializer.encodeToString(results)
        File(outputDir, "report.json").writeText(jsonReport)

        logger.lifecycle("KMP 迁移报告已生成：")
        logger.lifecycle("- 文本版 (查看摘要): ${File(outputDir, "report.txt")}")
        logger.lifecycle("- JSON版 (供LLM使用): ${File(outputDir, "report.json")}")
    }
}