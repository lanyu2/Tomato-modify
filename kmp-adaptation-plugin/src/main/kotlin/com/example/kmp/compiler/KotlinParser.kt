package com.example.kmp.compiler

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.io.File

/*KotlinParser 的工作流如下：

启动：初始化一个虚拟的编译器环境 (KotlinCoreEnvironment)。

输入：接收一个 java.io.File。

加工：使用 KtPsiFactory 将文件的文本内容“翻译”成 PSI 树。

输出：返回 KtFile 对象，交给后续的 KotlinAnalyzer 去进行依赖分析。*/

class KotlinParser {

    private val environment: KotlinCoreEnvironment

    init {
        // 1. 创建编译器配置容器
        val configuration = CompilerConfiguration()
        // 2. 创建 Kotlin 核心环境
        environment = KotlinCoreEnvironment.createForProduction(
            Disposer.newDisposable(), // 用于管理资源生命周期的对象
            configuration, // 编译器配置
            EnvironmentConfigFiles.JVM_CONFIG_FILES // 环境配置文件类型
        )
    }

    fun parse(file: File): KtFile {
        // 1. 创建 PSI 工厂，生产节点的工厂，需要 environment.project 作为参数，因为在 PSI（程序结构接口）体系中，任何代码元素都必须属于一个“项目（Project）”
        val factory = KtPsiFactory(environment.project)
        // 2. 读取文件内容并生成 AST 根节点，把 .kt 文件当作纯文本读入内存
        return factory.createFile(file.readText())
        //factory.createFile(...): 这是最关键的一步。工厂拿到文本后，会按照 Kotlin 的语法规则进行词法分析和语法分析，最终返回一个 KtFile 对象
        //KtFile是什么？是解析结果的根节点
    }
}
