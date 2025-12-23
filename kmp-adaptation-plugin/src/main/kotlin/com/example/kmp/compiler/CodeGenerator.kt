package com.example.kmp.compiler

import com.example.kmp.model.*
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
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

        // 根据细化后的 kind 分发处理
        when (node.kind) {
            NodeKind.FUNCTION -> generateFunction(expectDir, actualDir, element as KtNamedFunction, packageName, deps, type)
            NodeKind.PROPERTY -> generateProperty(expectDir, actualDir, element as KtProperty, packageName, deps, type)

            // 新增类型的处理
            NodeKind.INTERFACE -> generateInterface(expectDir, actualDir, element as KtClass, packageName, deps)
            NodeKind.DATA_CLASS -> generateDataClass(expectDir, actualDir, element as KtClass, packageName, deps)
            NodeKind.ENUM_CLASS -> generateEnumClass(expectDir, actualDir, element as KtClass, packageName, deps)
            NodeKind.OBJECT -> generateObject(expectDir, actualDir, element as KtObjectDeclaration, packageName, deps)

            NodeKind.CLASS -> generateClass(expectDir, actualDir, element as KtClass, packageName, deps)

            else -> {}
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
    // --- 新增：Interface 处理 ---
    private fun generateInterface(expectDir: File, actualDir: File, cls: KtClass, pkg: String, deps: Set<String>) {
        val fileName = "${cls.name}.kt"
        val imports = formatImports(deps)
        val bodyContent = cls.getBody()?.text?.trim()?.removePrefix("{")?.removeSuffix("}") ?: ""

        // Interface 通常不需要 expect/actual，除非它包含平台相关方法
        // 这里作为示例，生成 expect interface
        File(expectDir, fileName).writeText("""
            package $pkg
            $imports
            expect interface ${cls.name} {
                $bodyContent
            }
        """.trimIndent())

        File(actualDir, fileName).writeText("""
            package $pkg
            $imports
            actual interface ${cls.name} {
                $bodyContent
            }
        """.trimIndent())
    }

    // --- 新增：Data Class 处理 ---
    private fun generateDataClass(expectDir: File, actualDir: File, cls: KtClass, pkg: String, deps: Set<String>) {
        val fileName = "${cls.name}.kt"
        val imports = formatImports(deps)

        // 获取主构造函数参数
        val params = cls.primaryConstructor?.valueParameters?.joinToString(", ") {
            "${it.valOrVarKeyword?.text ?: "val"} ${it.name}: ${it.typeReference?.text ?: "Any"}"
        } ?: ""

        // Expect: 通常不能带 data 关键字 (除非开启特定编译器选项)，这里生成普通 class
        File(expectDir, fileName).writeText("""
            package $pkg
            $imports
            expect class ${cls.name}($params)
        """.trimIndent())

        // Actual: 这里加上 data
        File(actualDir, fileName).writeText("""
            package $pkg
            $imports
            actual data class ${cls.name}(actual $params)
        """.trimIndent())
    }

    // --- 新增：Enum Class 处理 ---
    private fun generateEnumClass(expectDir: File, actualDir: File, cls: KtClass, pkg: String, deps: Set<String>) {
        val fileName = "${cls.name}.kt"
        val imports = formatImports(deps)

        // 提取枚举项 (简单提取文本)
        val entries = cls.declarations.filterIsInstance<KtEnumEntry>().joinToString(", ") { it.name ?: "" }

        File(expectDir, fileName).writeText("""
            package $pkg
            $imports
            expect enum class ${cls.name} {
                $entries
            }
        """.trimIndent())

        File(actualDir, fileName).writeText("""
            package $pkg
            $imports
            actual enum class ${cls.name} {
                $entries
            }
        """.trimIndent())
    }

    // --- 新增：Object (单例) 处理 ---
    private fun generateObject(expectDir: File, actualDir: File, obj: KtObjectDeclaration, pkg: String, deps: Set<String>) {
        val fileName = "${obj.name}.kt"
        val imports = formatImports(deps)

        File(expectDir, fileName).writeText("""
            package $pkg
            $imports
            expect object ${obj.name}
        """.trimIndent())

        File(actualDir, fileName).writeText("""
            package $pkg
            $imports
            actual object ${obj.name} {
                // TODO: Fill object members
            }
        """.trimIndent())
    }

    // --- 普通 Class 处理 ---
    private fun generateClass(expectDir: File, actualDir: File, cls: KtClass, pkg: String, deps: Set<String>) {
        val fileName = "${cls.name}.kt"
        val imports = formatImports(deps)
        val params = cls.primaryConstructor?.valueParameters?.joinToString(", ") {
            "${it.name}: ${it.typeReference?.text ?: "Any"}"
        } ?: ""

        File(expectDir, fileName).writeText("""
            package $pkg
            $imports
            expect class ${cls.name}($params) {
                // TODO: Add methods
            }
        """.trimIndent())

        File(actualDir, fileName).writeText("""
            package $pkg
            $imports
            actual class ${cls.name} actual constructor($params) {
                // TODO: Implement Logic
            }
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