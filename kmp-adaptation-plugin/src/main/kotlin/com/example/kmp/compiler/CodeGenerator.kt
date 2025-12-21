package com.example.kmp.compiler

import com.example.kmp.model.*
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.File

class CodeGenerator(private val outputRoot: File) {

    fun generate(
        sourceFile: File,
        results: List<AnalysisResult>,
        packageName: String
    ) {
        val reportFile = File(outputRoot, "migration_report.txt")
        if (!reportFile.exists()) {
            reportFile.parentFile.mkdirs()
            reportFile.createNewFile()
        }

        reportFile.appendText("Source: ${sourceFile.path}\n")

        results.forEach { result ->
            reportFile.appendText("  [${result.type}] Fun: ${result.node.name}\n")

            // 针对 Android 平台强依赖的方法，生成 expect/actual 结构
            if (result.type == DependencyType.ANDROID_PLATFORM) {
                generateKmpStructure(result, packageName)
            }
        }
        reportFile.appendText("\n")
    }

    private fun generateKmpStructure(result: AnalysisResult, packageName: String) {
        val fn = result.node.function

        // 1. 生成 commonMain 中的 expect
        val expectDir = File(outputRoot, "commonMain/kotlin/${packageName.replace('.', '/')}")
        generateExpectFile(expectDir, fn, packageName, result.node.dependencies)

        // 2. 生成 androidMain 中的 actual
        val actualDir = File(outputRoot, "androidMain/kotlin/${packageName.replace('.', '/')}")
        generateActualFile(actualDir, fn, packageName, result.node.dependencies)
    }

    private fun generateExpectFile(dir: File, fn: KtNamedFunction, pkg: String, deps: Set<String>) {
        dir.mkdirs()
        val fileName = "${fn.name}.kt" // 实际项目中通常合并到一个文件，这里简化为每方法一文件
        val file = File(dir, fileName)

        val imports = formatImports(deps)
        val signature = getFunctionSignature(fn)

        val content = """
            package $pkg
            
            $imports
            
            expect $signature
        """.trimIndent()

        file.writeText(content)
    }

    private fun generateActualFile(dir: File, fn: KtNamedFunction, pkg: String, deps: Set<String>) {
        dir.mkdirs()
        val file = File(dir, "${fn.name}.kt")

        val imports = formatImports(deps)
        val signature = getFunctionSignature(fn)

        // 简单的 actual 实现：TODO 提示
        val content = """
            package $pkg
            
            $imports
            
            actual $signature {
                // TODO: Migrate original Android implementation here
                // Original body was dependent on Android Context or Views
                throw NotImplementedError("Migrated logic pending")
            }
        """.trimIndent()

        file.writeText(content)
    }

    private fun formatImports(deps: Set<String>): String {
        return deps.filter { it.isNotEmpty() }
            .joinToString("\n") { "import $it" }
    }

    private fun getFunctionSignature(fn: KtNamedFunction): String {
        val params = fn.valueParameters.joinToString(", ") {
            "${it.name}: ${it.typeReference?.text ?: "Any"}"
        }
        val returnType = fn.typeReference?.text ?: "Unit"
        return "fun ${fn.name}($params): $returnType"
    }
}