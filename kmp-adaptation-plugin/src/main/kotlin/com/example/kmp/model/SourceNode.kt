package com.example.kmp.model

import org.jetbrains.kotlin.psi.KtNamedDeclaration

enum class NodeKind {
    FUNCTION, PROPERTY, CLASS, UNKNOWN
}

data class SourceNode(
    val name: String,
    // 将 KtNamedFunction 泛化为 KtNamedDeclaration，它是 Function, Property, Class 的父类
    val element: KtNamedDeclaration,
    val dependencies: Set<String>,
    val kind: NodeKind
)