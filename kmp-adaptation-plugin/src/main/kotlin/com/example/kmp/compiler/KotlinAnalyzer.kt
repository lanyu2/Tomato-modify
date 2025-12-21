package com.example.kmp.compiler

import com.example.kmp.model.FunctionNode
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class KotlinAnalyzer {

    // 返回 Pair: 包名 -> 函数列表
    fun analyze(ktFile: KtFile): Pair<String, List<FunctionNode>> {
        val packageName = ktFile.packageFqName.asString()

        val importMap = ktFile.importDirectives.associate {
            (it.aliasName ?: it.importedName?.identifier ?: "") to
                    (it.importedFqName?.asString() ?: "")
        }

        val functions = ktFile.declarations
            .filterIsInstance<KtNamedFunction>()
            .map { fn ->
                val deps = mutableSetOf<String>()

                // 简单的基于文本的引用分析
                fn.bodyExpression
                    ?.collectDescendantsOfType<KtNameReferenceExpression>()
                    ?.forEach { ref ->
                        val refName = ref.getReferencedName()
                        // 1. 检查是否在 import 列表中
                        importMap[refName]?.let { deps.add(it) }

                        // 2. (可选) 这里还可以添加逻辑处理同包名下的类引用
                    }

                // 也要检查参数和返回值类型中的依赖
                fn.valueParameters.forEach { param ->
                    param.typeReference?.text?.let { typeName ->
                        importMap[typeName]?.let { deps.add(it) }
                    }
                }

                FunctionNode(
                    name = fn.name ?: "<anonymous>",
                    function = fn,
                    dependencies = deps
                )
            }

        return packageName to functions
    }
}