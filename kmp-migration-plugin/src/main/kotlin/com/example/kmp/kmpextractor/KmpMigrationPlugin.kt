package com.example.kmp.kmpextractor

import com.example.kmp.kmpextractor.tasks.MigratabilityTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpMigrationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("kmpMigratabilityReport", MigratabilityTask::class.java)
    }
}
