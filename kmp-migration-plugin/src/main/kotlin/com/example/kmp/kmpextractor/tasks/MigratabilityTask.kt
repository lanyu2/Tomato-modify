package com.example.kmp.kmpextractor.tasks

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind
import com.example.kmp.kmpextractor.migratability.MigratabilityAnalyzer
import com.example.kmp.kmpextractor.migratability.MigratabilityResult
import com.example.kmp.kmpextractor.scanner.KotlinSourceScanner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File


abstract class MigratabilityTask : DefaultTask() {
    init {
        // 直接在任务类里设置，注册时不用额外 lambda
        group = "kmp-migration"
        description = "Analyze Android code migratability to KMP commonMain"
    }

    @TaskAction
    fun run() {
        val srcDir = File(project.projectDir, "src")
        if (!srcDir.exists()) return

        val scanner = KotlinSourceScanner()
        val analyzer = MigratabilityAnalyzer()

        val results = srcDir.walkTopDown()
            .filter { it.extension == "kt" }
            .map { file ->
                val usages: Set<AndroidUsageKind> = scanner.detectAndroidUsages(file)
                analyzer.analyze(file.relativeTo(project.projectDir).path, usages)
            }
            .toList()

        val outputDir = File(project.projectDir, "kmp-migration")
        outputDir.mkdirs()
        File(outputDir, "report.txt").writeText(
            results.joinToString("\n\n") { it.toReadableString() }
        )

        logger.lifecycle("KMP migratability report generated at: $outputDir")
    }
}
