package com.example.kmp.tasks

import com.example.kmp.compiler.*
import com.example.kmp.config.PluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class KmpMigrateTask : DefaultTask() {

    // 使用 Gradle 注入扩展
    @get:Input
    lateinit var extension: PluginExtension

    @TaskAction
    fun run() {
        val sourceRoot = File(project.projectDir, extension.sourceDir)
        val outputRoot = File(project.buildDir, extension.outputDir)

        // 清理旧的输出
        if (outputRoot.exists()) outputRoot.deleteRecursively()
        outputRoot.mkdirs()

        if (!sourceRoot.exists()) {
            logger.error("❌ Source dir not found: $sourceRoot")
            return
        }

        logger.lifecycle("🚀 Starting KMP Migration Analysis on: $sourceRoot")

        val parser = KotlinParser()
        val analyzer = KotlinAnalyzer()
        val classifier = DependencyClassifier()
        val generator = CodeGenerator(outputRoot)

        // 1. 初始化 AST 生成器
        val astGenerator = AstGraphGenerator(outputRoot)

        var processedFiles = 0

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                try {
                    val ktFile = parser.parse(file)

                    astGenerator.generateDotFile(ktFile)

                    val (packageName, functions) = analyzer.analyze(ktFile)
                    val results = classifier.classify(functions)

                    if (results.isNotEmpty()) {
                        generator.generate(file, results, packageName)
                    }
                    processedFiles++
                } catch (e: Exception) {
                    logger.error("Failed to process file: ${file.name}", e)
                }
            }

        logger.lifecycle("✅ Analysis finished.")
        logger.lifecycle("📂 Processed $processedFiles files.")
        logger.lifecycle("📄 Report and Stubs generated at: ${outputRoot.absolutePath}")
    }

}