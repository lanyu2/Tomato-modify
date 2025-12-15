//package com.example.kmp.lib
//
//import java.io.File
//
//
//object FileMover {
//
//
//    fun copyToTargets(root: File, commonFiles: List<File>, androidFiles: List<File>) {
//        val commonTarget = File(root, "src/commonMain/kotlin")
//        val androidTarget = File(root, "src/androidMain/kotlin")
//
//
//        commonFiles.forEach { f ->
//            val rel = f.relativeTo(root).path
//            val dest = File(commonTarget, rel)
//            dest.parentFile.mkdirs()
//            f.copyTo(dest, overwrite = true)
//        }
//
//
//        androidFiles.forEach { f ->
//            val rel = f.relativeTo(root).path
//            val dest = File(androidTarget, rel)
//            dest.parentFile.mkdirs()
//            f.copyTo(dest, overwrite = true)
//        }
//    }
//}

package com.example.kmp.lib

import java.io.File

object FileMover {

    fun copyToTargets(root: File,
                      commonUi: List<File>,
                      commonLogic: List<File>,
                      androidUi: List<File>,
                      androidLogic: List<File>) {

        val commonUiTarget = File(root, "src/commonMain/kotlin/ui")
        val commonLogicTarget = File(root, "src/commonMain/kotlin/logic")
        val androidUiTarget = File(root, "src/androidMain/kotlin/ui")
        val androidLogicTarget = File(root, "src/androidMain/kotlin/logic")

        fun copyList(files: List<File>, targetRoot: File) {
            files.forEach { f ->
                val rel = f.relativeTo(root).path
                val dest = File(targetRoot, rel)
                dest.parentFile.mkdirs()
                f.copyTo(dest, overwrite = true)
            }
        }

        copyList(commonUi, commonUiTarget)
        copyList(commonLogic, commonLogicTarget)
        copyList(androidUi, androidUiTarget)
        copyList(androidLogic, androidLogicTarget)
    }
}
