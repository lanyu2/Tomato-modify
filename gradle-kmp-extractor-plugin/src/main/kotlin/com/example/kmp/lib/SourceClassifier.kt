//package com.example.kmp.lib
//
//import java.io.File
//
//data class ClassifyResult(
//    val commonFiles: List<File>,
//    val androidFiles: List<File>,
//    val depends: Map<String, Set<String>>
//)
//
//object SourceClassifier {
//
//    private val androidPrefixes = listOf(
//        "android.", "androidx.", "com.google.android.material.",
//        "androidx.compose.", "android.view.", "android.app.", "android.content.",
//        "androidx.activity.", "androidx.fragment."
//    )
//
//    fun classify(root: File): ClassifyResult {
//        val ktFiles = root.walkTopDown()
//            .filter { it.isFile && it.extension == "kt" }
//            .toList()
//
//        val common = mutableListOf<File>()
//        val android = mutableListOf<File>()
//        val depends = mutableMapOf<String, MutableSet<String>>()
//
//        ktFiles.forEach { file ->
//            val text = file.readText()
//            val imports = Regex("""import\s+([\w\.]+)""")
//                .findAll(text)
//                .map { it.groupValues[1] }
//                .toList()
//
//            depends[file.relativeTo(root).path] = imports.toMutableSet()
//
//            // --- 修正点：不要在判断时嵌套错误的 any，直接检查当前 import (imp)
//            val isAndroid = imports.any { imp ->
//                // 如果 import 以 android/androidx 等前缀开头，则是 Android 平台相关
//                androidPrefixes.any { pref -> imp.startsWith(pref) } ||
//                        // 或者 import 名称包含常见的 Android/Compose/Activity/Fragment/View 标识
//                        imp.contains("Activity") ||
//                        imp.contains("Fragment") ||
//                        imp.contains("View") ||
//                        imp.contains("Compose")
//            }
//
//            if (isAndroid) android.add(file) else common.add(file)
//        }
//
//        return ClassifyResult(common, android, depends)
//    }
//}

package com.example.kmp.lib

import java.io.File

data class ClassifyResult(
    val commonUi: List<File>,
    val commonLogic: List<File>,
    val androidUi: List<File>,
    val androidLogic: List<File>,
    val depends: Map<String, Set<String>>
)

object SourceClassifier {

    private val androidPrefixes = listOf(
        "android.", "androidx.", "com.google.android.material.",
        "android.view.", "android.app.", "android.content.",
        "androidx.activity.", "androidx.fragment."
    )

    private val composePrefixes = listOf(
        "androidx.compose.", "kotlinx.compose" // kotlin compose packages (if any)
    )

    private val logicHints = listOf(
        "kotlinx.coroutines", "kotlinx.serialization", "org.koin", "dagger", "retrofit"
    )

    // file path hints that indicate UI/logic folder structure
    private val uiPathHints = listOf("/ui/", "/presentation/", "/screen/", "/view/")
    private val logicPathHints = listOf("/domain/", "/usecase/", "/repository/", "/data/")

    fun classify(root: File): ClassifyResult {
        val ktFiles = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        val commonUi = mutableListOf<File>()
        val commonLogic = mutableListOf<File>()
        val androidUi = mutableListOf<File>()
        val androidLogic = mutableListOf<File>()
        val depends = mutableMapOf<String, MutableSet<String>>()

        ktFiles.forEach { file ->
            val text = file.readText()

            // gather imports
            val imports = Regex("""import\s+([\w\.]+)""")
                .findAll(text)
                .map { it.groupValues[1] }
                .toList()

            depends[file.relativeTo(root).path] = imports.toMutableSet()

            val isAndroidPlatform = imports.any { imp ->
                androidPrefixes.any { pref -> imp.startsWith(pref) }
            } || imports.any { it.contains("Activity") || it.contains("Fragment") || it.contains("View") }

            val isCompose = imports.any { imp -> composePrefixes.any { imp.startsWith(it) } } ||
                    text.contains("@Composable")

            val pathLower = file.relativeTo(root).path.replace('\\', '/').lowercase()

            val pathLooksUi = uiPathHints.any { pathLower.contains(it) } ||
                    file.name.contains("View", ignoreCase = true) ||
                    file.name.contains("Screen", ignoreCase = true)

            val pathLooksLogic = logicPathHints.any { pathLower.contains(it) } ||
                    file.name.contains("Repository", ignoreCase = true) ||
                    file.name.contains("UseCase", ignoreCase = true) ||
                    file.name.contains("Interactor", ignoreCase = true)

            val importHintsLogic = imports.any { imp ->
                logicHints.any { hint -> imp.startsWith(hint) }
            }

            // Decide UI vs Logic
            val isUi = isCompose || pathLooksUi || imports.any { it.contains("Activity") || it.contains("Fragment") || it.contains("View") }
            val isLogic = pathLooksLogic || importHintsLogic || file.name.contains("ViewModel", ignoreCase = true)

            // Platform: androidMain if Android APIs or compose detected; else common
            val targetIsAndroid = isAndroidPlatform || isCompose

            // Final assignment rules:
            // - If isUi -> UI
            // - Else if isLogic -> Logic
            // - Else fallback: if platform android -> androidLogic else commonLogic
            val assignedToUi = isUi
            val assignedToLogic = !assignedToUi && isLogic

            if (targetIsAndroid) {
                if (assignedToUi) androidUi.add(file)
                else if (assignedToLogic) androidLogic.add(file)
                else androidLogic.add(file) // fallback to androidLogic
            } else {
                if (assignedToUi) commonUi.add(file)
                else if (assignedToLogic) commonLogic.add(file)
                else commonLogic.add(file) // fallback to commonLogic
            }
        }

        return ClassifyResult(
            commonUi = commonUi,
            commonLogic = commonLogic,
            androidUi = androidUi,
            androidLogic = androidLogic,
            depends = depends
        )
    }
}

