package com.example.kmp.compiler

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtFile
import java.io.File

class AstGraphGenerator(private val outputDir: File) {

    fun generateDotFile(ktFile: KtFile) {
        val fileName = "${ktFile.name}.dot"
        val outputFile = File(outputDir, "ast/$fileName")

        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        val sb = StringBuilder()
        sb.append("digraph AST {\n")
        sb.append("  rankdir=TB;\n") // 从上到下布局
        sb.append("  node [shape=box, style=filled, color=\"#dddddd\", fontname=\"Verdana\"];\n")

        // 递归构建节点和边
        visitNode(ktFile, sb)

        sb.append("}\n")

        outputFile.writeText(sb.toString())
        println("Generatd AST Graph: ${outputFile.absolutePath}")
    }

    private fun visitNode(element: PsiElement, sb: StringBuilder) {
        val id = getId(element)
        val label = getLabel(element)

        // 定义当前节点
        // 针对不同类型的节点可以用不同颜色 (可选优化)
        val color = if (element is KtFile) "lightblue" else "white"
        sb.append("  $id [label=\"$label\", fillcolor=$color];\n")

        // 遍历子节点并建立连接
        element.children.forEach { child ->
            val childId = getId(child)
            sb.append("  $id -> $childId;\n")
            visitNode(child, sb)
        }
    }

    // 生成唯一的节点 ID (使用 hashcode 即可)
    private fun getId(element: PsiElement): String {
        return "node_${System.identityHashCode(element)}"
    }

    // 生成节点显示的文本 (类型 + 简略内容)
    private fun getLabel(element: PsiElement): String {
        val type = element::class.java.simpleName.removeSuffix("Impl")

        // 获取代码文本，截取前20个字符，并转义双引号和换行
        var text = element.text
            .replace("\"", "\\\"") // 转义双引号
            .replace("\n", "\\n") // 转义换行
            .replace("\r", "")

        if (text.length > 25) {
            text = text.substring(0, 25) + "..."
        }

        return "$type\\n($text)"
    }
}