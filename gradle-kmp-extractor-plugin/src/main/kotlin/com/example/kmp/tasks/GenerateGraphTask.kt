package com.example.kmp.tasks

import com.example.kmp.lib.SourceClassifier
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class GenerateGraphTask : DefaultTask() {

    init {
        group = "kmp-extract"
        description = "Generate a DOT-format dependency graph for visualization"
    }

    @TaskAction
    fun run() {
        val root = project.projectDir
        val result = SourceClassifier.classify(root)

        // ✔ Gradle 8.x 推荐写法
        val out = project.layout.buildDirectory.dir("kmp-extract").get().asFile
        out.mkdirs()

        val dot = buildString {
            appendLine("digraph G {")
            result.depends.forEach { (file, imports) ->
                val from = file.substringBeforeLast('.').replace('/', '_')
                imports.forEach { imp ->
                    val to = imp.replace('.', '_')
                    appendLine("  \"$from\" -> \"$to\";")
                }
            }
            appendLine("}")
        }

        out.resolve("dependency-graph.dot").writeText(dot)

        project.logger.lifecycle("Wrote DOT graph to ${out.absolutePath}")
    }
}
