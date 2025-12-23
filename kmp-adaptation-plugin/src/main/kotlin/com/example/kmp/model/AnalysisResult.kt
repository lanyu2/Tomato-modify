package com.example.kmp.model

data class AnalysisResult(
    // 修改点：将 node 类型泛化为 SourceNode
    val node: SourceNode,
    val type: DependencyType
)