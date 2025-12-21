package com.example.kmp.model

import org.jetbrains.kotlin.psi.KtNamedFunction

data class FunctionNode(
    val name: String,
    val function: KtNamedFunction,
    val dependencies: Set<String>
)
