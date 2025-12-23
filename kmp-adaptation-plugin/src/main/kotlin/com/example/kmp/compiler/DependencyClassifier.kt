package com.example.kmp.compiler

import com.example.kmp.config.ImportRules
import com.example.kmp.model.*

class DependencyClassifier {

    fun classify(nodes: List<FunctionNode>): List<AnalysisResult> =
        nodes.map { node ->
            val deps = node.dependencies

            val type = when {
                deps.any { ImportRules.isAndroid(it) } -> DependencyType.ANDROID_PLATFORM
                deps.any { ImportRules.isJavaPlatform(it) } -> DependencyType.JAVA_PLATFORM
                deps.any { ImportRules.isAdaptableJava(it) } -> DependencyType.ADAPTABLE_JAVA
                else -> DependencyType.COMMON
            }

            AnalysisResult(node, type)

        }
}