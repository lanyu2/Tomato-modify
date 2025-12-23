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

                        // 扫描类体内的属性和方法（如果需要深度扫描可以加，目前逻辑似乎只看顶层依赖）
                        // 注意：目前的架构是扁平的，这里可能不需要递归扫描内部类，
                        // 但如果类内部有成员属性初始化引用了其他类，最好也扫描一下 body
                        // (原代码似乎只扫描了 init 块)
                        // -------------------------------------------------

                        // 2. 细化具体的类型判断
                        when {
                            // 判断是否为 Object (包括 companion object)
                            decl is KtObjectDeclaration -> NodeKind.OBJECT

                            // 判断是否为 KtClass (包含 class, interface, enum, annotation class)
                            decl is KtClass -> when {
                                decl.isInterface() -> NodeKind.INTERFACE
                                decl.isData() -> NodeKind.DATA_CLASS
                                decl.isEnum() -> NodeKind.ENUM_CLASS
                                else -> NodeKind.CLASS // 普通 class
                            }

                            // 兜底
                            else -> NodeKind.CLASS
                        }
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