package com.example.kmp.lib

import java.io.File


/**
 * 简单示例：根据 import 关键词推断 libs.versions.toml 中的引用键名
 * 这个模块仅生成一个依赖片段文件供人工审阅
 */
object AutoGradleWriter {


    private val mapping = mapOf(
        "kotlinx.coroutines" to "libs.kotlinx.coroutines.core",
        "kotlinx.serialization" to "libs.kotlinx.serialization.json",
        "androidx.activity" to "libs.androidx.activity",
        "androidx.compose.ui" to "libs.androidx.compose.ui"
    )


    fun generateDependencySnippet(root: File, depends: Map<String, Set<String>>) {
        val commonDeps = mutableSetOf<String>()
        val androidDeps = mutableSetOf<String>()


        depends.forEach { (_, imports) ->
            imports.forEach { imp ->
                mapping.entries.forEach { (k, v) ->
                    if (imp.startsWith(k)) {
                        if (k.startsWith("androidx") || k.startsWith("android")) androidDeps.add(v)
                        else commonDeps.add(v)
                    }
                }
            }
        }


        val outDir = File(root, "build/kmp-extract")
        outDir.mkdirs()
        File(outDir, "common-deps-snippet.gradle.kts").writeText(
            buildString {
                appendLine("// 自动生成：供手工校验后复制进 build.gradle.kts")
                appendLine("dependencies {")
                commonDeps.forEach { appendLine(" implementation($it)") }
                appendLine("}")
            }
        )


        File(outDir, "android-deps-snippet.gradle.kts").writeText(
            buildString {
                appendLine("// 自动生成：供手工校验后复制进 build.gradle.kts")
                appendLine("dependencies {")
                androidDeps.forEach { appendLine(" implementation($it)") }
                appendLine("}")
            }
        )
    }
}