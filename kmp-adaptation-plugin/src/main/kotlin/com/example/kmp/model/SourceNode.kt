package com.example.kmp.model

import org.jetbrains.kotlin.psi.KtNamedDeclaration

enum class NodeKind {
    FUNCTION,
    PROPERTY,   // 对应 val 和 var
    CLASS,      // 普通类
    INTERFACE,  // 新增：接口
    OBJECT,     // 新增：Object (包括 companion object)
    DATA_CLASS, // 新增：数据类
    ENUM_CLASS, // 新增：枚举类
    UNKNOWN
}

data class SourceNode(
    val name: String,
    // 将 KtNamedFunction 泛化为 KtNamedDeclaration，它是 Function, Property, Class 的父类
    val element: KtNamedDeclaration,
    val dependencies: Set<String>,
    val kind: NodeKind
)