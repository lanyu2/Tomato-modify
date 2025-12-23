package com.example.kmp.compiler

import com.example.kmp.model.*
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import java.io.File

class CodeGenerator(private val outputRoot: File) {

    fun generate(
        sourceFile: File,
        results: List<AnalysisResult>,
        packageName: String
    ) {
        // 1. 初始化或追加迁移报告
        val reportFile = File(outputRoot, "migration_report.txt")
        if (!reportFile.exists()) {
            reportFile.parentFile.mkdirs()
            reportFile.createNewFile()
        }

        reportFile.appendText("Source: ${sourceFile.path}\n")

        results.forEach { result ->
            // 2. 写入详细的分类日志 (例如 [ANDROID_UI] FUNCTION: showDialog)
            reportFile.appendText("  [${result.type}] ${result.node.kind}: ${result.node.name}\n")

            // 3. 仅针对 Android 平台相关的代码 (Logic 或 UI) 生成 expect/actual 结构
            // Common 代码通常直接移动文件即可，不需要生成占位符
            if (result.type == DependencyType.ANDROID_LOGIC || result.type == DependencyType.ANDROID_UI) {
                generateKmpStructure(result, packageName)
            }
        }
        reportFile.appendText("\n")
    }

    private fun generateKmpStructure(result: AnalysisResult, packageName: String) {
        val node = result.node
        val element = node.element
        val deps = node.dependencies
        val type = result.type

        val expectDir = File(outputRoot, "commonMain/kotlin/${packageName.replace('.', '/')}")
        val actualDir = File(outputRoot, "androidMain/kotlin/${packageName.replace('.', '/')}")
        expectDir.mkdirs()
        actualDir.mkdirs()

        // 根据节点类型 (Function/Property) 分发处理，并传入具体的依赖类型以生成不同的 TODO
        when (node.kind) {
            NodeKind.FUNCTION -> {
                val fn = element as KtNamedFunction
                generateFunction(expectDir, actualDir, fn, packageName, deps, type)
            }
            NodeKind.PROPERTY -> {
                val prop = element as KtProperty
                generateProperty(expectDir, actualDir, prop, packageName, deps, type)
            }
            else -> {
                // 对于复杂的类 (Class)，暂时生成一个文本说明文件
                File(actualDir, "${node.name}_Migration_Pending.txt").writeText(
                    "Complex class migration pending.\nType: $type\nReason: Class migration involves inheritance and members, which requires manual refactoring."
                )
            }
        }
    }

    private fun generateFunction(
        expectDir: File,
        actualDir: File,
        fn: KtNamedFunction,
        pkg: String,
        deps: Set<String>,
        type: DependencyType
    ) {
        val fileName = "${fn.name}.kt"
        val imports = formatImports(deps)
        val signature = getSignature(fn)

        // 生成 Expect (接口定义)
        File(expectDir, fileName).writeText("""
            package $pkg
            
            $imports
            
            expect $signature
        """.trimIndent())

        // 生成 Actual (平台实现)
        val todoMsg = getTodoMessage(type)
        File(actualDir, fileName).writeText("""
            package $pkg
            
            $imports
            
            actual $signature {
                // $todoMsg
                throw NotImplementedError("Pending migration: $todoMsg")
            }
        """.trimIndent())
    }

    private fun generateProperty(
        expectDir: File,
        actualDir: File,
        prop: KtProperty,
        pkg: String,
        deps: Set<String>,
        type: DependencyType
    ) {
        val fileName = "${prop.name}.kt"
        val imports = formatImports(deps)
        val signature = getSignature(prop)

        // 生成 Expect (变量定义)
        File(expectDir, fileName).writeText("""
            package $pkg
            
            $imports
            
            expect $signature
        """.trimIndent())

        // 生成 Actual (变量实现)
        val todoMsg = getTodoMessage(type)
        File(actualDir, fileName).writeText("""
            package $pkg
            
            $imports
            
            actual $signature
                get() = TODO("$todoMsg")
        """.trimIndent())
    }

    // 根据分类结果生成更有指导意义的 TODO 提示
    private fun getTodoMessage(type: DependencyType): String {
        return when (type) {
            DependencyType.ANDROID_UI -> "TODO: Migrate Android UI (View/Fragment/XML) to Compose Multiplatform"
            DependencyType.ANDROID_LOGIC -> "TODO: Migrate Android Logic (Context/System) to KMP equivalent (e.g. use moko-resources, kermit, or expect/actual utils)"
            else -> "TODO: Pending migration"
        }
    }

    // 统一生成函数或变量的签名字符串
    private fun getSignature(element: KtNamedDeclaration): String {
        return when (element) {
            is KtNamedFunction -> {
                val params = element.valueParameters.joinToString(", ") {
                    "${it.name}: ${it.typeReference?.text ?: "Any"}"
                }
                val returnType = element.typeReference?.text ?: "Unit"
                "fun ${element.name}($params): $returnType"
            }
            is KtProperty -> {
                val type = element.typeReference?.text ?: "Any"
                val varKeyword = if (element.isVar) "var" else "val"
                "$varKeyword ${element.name}: $type"
            }
            else -> ""
        }
    }

    private fun formatImports(deps: Set<String>): String {
        return deps.filter { it.isNotEmpty() }
            .sorted()
            .joinToString("\n") { "import $it" }
    }
}