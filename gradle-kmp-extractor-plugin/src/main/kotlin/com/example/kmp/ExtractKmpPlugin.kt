package com.example.kmp

import com.example.kmp.tasks.ClassifyTask
import com.example.kmp.tasks.MoveFilesTask
import com.example.kmp.tasks.GenerateGraphTask
import org.gradle.api.Plugin
import org.gradle.api.Project


class ExtractKmpPlugin : Plugin<Project> {
    override fun apply(project: Project) {
// 注册 tasks
        project.tasks.register("classifyKmpSources", ClassifyTask::class.java) { }
        project.tasks.register("moveKmpFiles", MoveFilesTask::class.java) { }
        project.tasks.register("generateKmpGraph", GenerateGraphTask::class.java) { }


        project.afterEvaluate {
            project.logger.lifecycle("com.example.kmp.extractor applied to ${project.name}")
        }
    }
}