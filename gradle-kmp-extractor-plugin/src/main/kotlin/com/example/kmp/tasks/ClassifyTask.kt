//package com.example.kmp.tasks
//
//import com.example.kmp.lib.SourceClassifier
//import com.example.kmp.lib.AutoGradleWriter
//import org.gradle.api.DefaultTask
//import org.gradle.api.tasks.TaskAction
//import java.io.File
//import com.google.gson.GsonBuilder
//
//
//open class ClassifyTask : DefaultTask() {
//
//
//    init {
//        group = "kmp-extract"
//        description = "Scan project kotlin files and classify into commonMain/androidMain"
//    }
//
//    @TaskAction
//    fun run() {
//        // 当前 Android module 的目录，例如 app/
//        val moduleDir = project.projectDir
//
//        // 只扫描该 module 的 src/**/kotlin
//        val srcRoot = File(moduleDir, "src")
//        if (!srcRoot.exists()) return
//
//        project.logger.lifecycle("Scanning kotlin files under ${srcRoot}")
//
//        val result = SourceClassifier.classify(srcRoot)
//
//        val out = File(project.buildDir, "kmp-extract")
//        out.mkdirs()
//
//        File(out, "common-files.txt").writeText(
//            result.commonFiles.joinToString("\n") { it.relativeTo(moduleDir).path }
//        )
//
//        File(out, "android-files.txt").writeText(
//            result.androidFiles.joinToString("\n") { it.relativeTo(moduleDir).path }
//        )
//
//        // 写 import 依赖图
//        val gson = GsonBuilder().setPrettyPrinting().create()
//        File(out, "dependency-graph.json").writeText(gson.toJson(result.depends))
//
//        // 生成依赖片段
//        AutoGradleWriter.generateDependencySnippet(moduleDir, result.depends)
//
//        project.logger.lifecycle("Wrote classification results to ${out}")
//    }
//
//}

package com.example.kmp.tasks

import com.example.kmp.lib.SourceClassifier
import com.example.kmp.lib.AutoGradleWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import com.google.gson.GsonBuilder

open class ClassifyTask : DefaultTask() {

    init {
        group = "kmp-extract"
        description = "Scan project kotlin files and classify into common/android × ui/logic"
    }

    @TaskAction
    fun run() {
        val root = project.projectDir
        project.logger.lifecycle("Scanning kotlin files under ${root}")

        val result = SourceClassifier.classify(root)

        val out = File(project.buildDir, "kmp-extract")
        out.mkdirs()

        File(out, "common-ui.txt").writeText(result.commonUi.joinToString("\n") { it.relativeTo(root).path })
        File(out, "common-logic.txt").writeText(result.commonLogic.joinToString("\n") { it.relativeTo(root).path })
        File(out, "android-ui.txt").writeText(result.androidUi.joinToString("\n") { it.relativeTo(root).path })
        File(out, "android-logic.txt").writeText(result.androidLogic.joinToString("\n") { it.relativeTo(root).path })

        val gson = GsonBuilder().setPrettyPrinting().create()
        File(out, "dependency-graph.json").writeText(gson.toJson(result.depends))

        AutoGradleWriter.generateDependencySnippet(project.projectDir, result.depends)

        project.logger.lifecycle("Wrote classification results to ${out}")
    }
}
