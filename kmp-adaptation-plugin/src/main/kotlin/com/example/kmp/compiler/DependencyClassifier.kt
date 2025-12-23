package com.example.kmp.compiler

import com.example.kmp.config.ImportRules
import com.example.kmp.model.*
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

class DependencyClassifier {

    fun classify(nodes: List<SourceNode>): List<AnalysisResult> =
        nodes.map { node ->
            val deps = node.dependencies
            val element = node.element

            // 1. 判断是否包含 UI 特征
            // 特征 A: 引用了 UI 相关的包
            val hasUiImports = deps.any { ImportRules.isUI(it) }

            // 特征 B: 包含 @Composable 注解 (针对函数)
            val isComposable = element.annotationEntries.any {
                it.shortName?.asString() == "Composable"
            }

            // 特征 C: 继承了 Activity/Fragment/View (针对类)
            val isViewSubclass = if (element is KtClassOrObject) {
                element.superTypeListEntries.any {
                    val text = it.text
                    text.contains("Activity") || text.contains("Fragment") || text.contains("View")
                }
            } else false

            val isUI = hasUiImports || isComposable || isViewSubclass

            // 2. 综合判断平台类型
            val type = when {
                // --- Android 平台 ---
                deps.any { ImportRules.isAndroid(it) } -> {
                    if (isUI) DependencyType.ANDROID_UI else DependencyType.ANDROID_LOGIC
                }

                // --- Java 平台 ---
                deps.any { ImportRules.isJavaPlatform(it) } -> DependencyType.JAVA_PLATFORM

                // --- Common / Adaptable ---
                else -> {
                    if (ImportRules.isAdaptableJava(deps.firstOrNull() ?: "")) {
                        DependencyType.ADAPTABLE_JAVA
                    } else if (isUI) {
                        // 如果是纯 Kotlin 且包含 UI 特征 (如纯 Compose Multiplatform)，归为 Common UI
                        DependencyType.COMMON_UI
                    } else {
                        DependencyType.COMMON_LOGIC
                    }
                }
            }

            AnalysisResult(node, type)
        }
}