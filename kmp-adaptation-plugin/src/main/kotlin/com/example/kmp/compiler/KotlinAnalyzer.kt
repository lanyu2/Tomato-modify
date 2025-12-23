package com.example.kmp.compiler

import com.example.kmp.model.NodeKind
import com.example.kmp.model.SourceNode
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType

class KotlinAnalyzer {

    // 返回 Pair: 包名 -> 节点列表
    fun analyze(ktFile: KtFile): Pair<String, List<SourceNode>> {
        val packageName = ktFile.packageFqName.asString()

        // 建立别名映射: "Entity" -> "androidx.room.Entity"
        val importMap = ktFile.importDirectives.associate {
            (it.aliasName ?: it.importedName?.identifier ?: "") to
                    (it.importedFqName?.asString() ?: "")
        }

        val nodes = mutableListOf<SourceNode>()

        // 遍历所有顶层声明 (类、函数、变量)
        ktFile.declarations.forEach { decl ->
            if (decl is KtNamedDeclaration) {
                val deps = mutableSetOf<String>()

                // --- 核心修复：添加注解扫描 ---
                fun scanAnnotations(element: KtAnnotated) {
                    element.annotationEntries.forEach { entry ->
                        // 1. 获取注解名称 (如 "Entity") 并检查 import
                        entry.shortName?.asString()?.let { name ->
                            importMap[name]?.let { deps.add(it) }
                        }

                        // 2. 扫描注解参数中的类型 (如 @OptIn(markerClass = ...))
                        entry.valueArgumentList?.arguments?.forEach { arg ->
                            arg.collectDescendantsOfType<KtNameReferenceExpression>().forEach { ref ->
                                importMap[ref.getReferencedName()]?.let { deps.add(it) }
                            }
                        }
                    }
                }

                // 辅助函数：收集代码体内的依赖
                fun scanBody(element: KtElement?) {
                    element?.collectDescendantsOfType<KtNameReferenceExpression>()?.forEach { ref ->
                        importMap[ref.getReferencedName()]?.let { deps.add(it) }
                    }
                }

                // 辅助函数：收集类型引用 (如参数类型、父类类型)
                fun scanType(typeRef: KtTypeReference?) {
                    typeRef?.collectDescendantsOfType<KtUserType>()?.forEach { userType ->
                        userType.referencedName?.let { name ->
                            importMap[name]?.let { deps.add(it) }
                        }
                    }
                }

                // 对声明本身进行注解扫描 (捕获 @Entity, @PrimaryKey 等)
                scanAnnotations(decl)

                val kind = when (decl) {
                    is KtNamedFunction -> {
                        scanBody(decl.bodyExpression)
                        decl.valueParameters.forEach {
                            scanType(it.typeReference)
                            scanAnnotations(it) // 扫描参数上的注解
                        }
                        scanType(decl.typeReference)
                        NodeKind.FUNCTION
                    }
                    is KtProperty -> {
                        scanBody(decl.initializer)
                        scanBody(decl.delegate)
                        decl.accessors.forEach {
                            scanBody(it.bodyExpression)
                            scanAnnotations(it)
                        }
                        scanType(decl.typeReference)
                        NodeKind.PROPERTY
                    }
                    is KtClassOrObject -> {
                        // 扫描父类/接口 (例如 class MyView : View)
                        decl.superTypeListEntries.forEach {
                            scanType(it.typeReference)
                        }

                        // 扫描主构造函数的参数 (例如 val context: Context)
                        decl.primaryConstructorParameters.forEach {
                            scanType(it.typeReference)
                            scanAnnotations(it)
                        }

                        // 扫描初始化块
                        decl.getAnonymousInitializers().forEach { scanBody(it.body) }

                        NodeKind.CLASS
                    }
                    else -> NodeKind.UNKNOWN
                }

                if (kind != NodeKind.UNKNOWN) {
                    nodes.add(SourceNode(
                        name = decl.name ?: "<anonymous>",
                        element = decl,
                        dependencies = deps,
                        kind = kind
                    ))
                }
            }
        }

        return packageName to nodes
    }
}