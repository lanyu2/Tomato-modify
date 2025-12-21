package com.example.kmp

import com.example.kmp.config.PluginExtension
import com.example.kmp.tasks.KmpMigrateTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpAdaptationPlugin : Plugin<Project> {

    override fun apply(project: Project) {

        val extension = project.extensions.create(
            "kmpMigration",
            PluginExtension::class.java
        )

        project.tasks.register("kmpMigrate", KmpMigrateTask::class.java) {
            group = "kmp"
            description = "Analyze Android code and prepare KMP migration"
            this.extension = extension
        }
    }
}
