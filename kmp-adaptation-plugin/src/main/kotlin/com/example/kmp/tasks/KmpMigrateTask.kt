/*
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

}*/


package com.example.kmp.tasks

import com.example.kmp.compiler.*
import com.example.kmp.config.PluginExtension
import com.example.kmp.model.* // [修改] 新增导入，确保可见 SourceNode/AnalysisResult
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
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

        // 初始化各个组件
        val parser = KotlinParser()
        val analyzer = KotlinAnalyzer()
        val classifier = DependencyClassifier()
        val generator = CodeGenerator(outputRoot)
        val astGenerator = AstGraphGenerator(outputRoot)

        var processedFiles = 0

        sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                try {
                    // 1. 解析为 PSI (KtFile)
                    val ktFile = parser.parse(file)

                    // 2. (可选) 生成 AST 图
                    astGenerator.generateDotFile(ktFile)

                    // 3. 分析文件内容，提取节点
                    // [修改] 变量名从 functions 改为 nodes，匹配 analyze 返回的 List<SourceNode>
                    val (packageName, nodes) = analyzer.analyze(ktFile)

                    // 4. 对节点进行分类 (Logic/UI, Android/Common)
                    // [修改] 传入 nodes
                    val results = classifier.classify(nodes)

                    // 5. 生成迁移代码和报告
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