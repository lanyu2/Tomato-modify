/*package com.example.kmp.tasks

import com.example.kmp.lib.FileMover
import com.example.kmp.lib.SourceClassifier
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File


open class MoveFilesTask : DefaultTask() {


    init {
        group = "kmp-extract"
        description = "Copy classified files into KMP source sets (src/commonMain/kotlin, src/androidMain/kotlin)"
    }


    @TaskAction
    fun run() {
        // 当前 Android 模块目录：例如 app/
        val moduleDir = project.projectDir

        // 只扫描 src 目录，而不是整个项目
        val root = File(moduleDir, "src")
        if (!root.exists()) {
            project.logger.lifecycle("src directory not found: $root")
            return
        }

        val result = SourceClassifier.classify(root)

        FileMover.copyToTargets(moduleDir, result.commonFiles, result.androidFiles)

        project.logger.lifecycle(
            "Copied ${result.commonFiles.size} common files and ${result.androidFiles.size} android files"
        )
    }
}*/

package com.example.kmp.tasks

import com.example.kmp.lib.FileMover
import com.example.kmp.lib.SourceClassifier
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class MoveFilesTask : DefaultTask() {

    init {
        group = "kmp-extract"
        description = "Copy classified files into KMP source sets (common/android × ui/logic)"
    }

    @TaskAction
    fun run() {
        val root = project.projectDir
        val result = SourceClassifier.classify(root)

        FileMover.copyToTargets(root,
            result.commonUi,
            result.commonLogic,
            result.androidUi,
            result.androidLogic)

        val cUi = result.commonUi.size
        val cLogic = result.commonLogic.size
        val aUi = result.androidUi.size
        val aLogic = result.androidLogic.size

        project.logger.lifecycle("Copied common ui=$cUi logic=$cLogic, android ui=$aUi logic=$aLogic")
    }
}
