package com.example.kmp.kmpextractor.scanner

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind
import java.io.File

class KotlinSourceScanner {

    fun detectAndroidUsages(file: File): Set<AndroidUsageKind> {
        val text = file.readText()
        val usages = mutableSetOf<AndroidUsageKind>()

        if ("SharedPreferences" in text) usages += AndroidUsageKind.STORAGE
        if ("android.util.Log" in text) usages += AndroidUsageKind.LOGGING
        if ("Handler(" in text || "Looper" in text)
            usages += AndroidUsageKind.THREADING
        if ("SystemClock" in text) usages += AndroidUsageKind.TIME
        if ("Activity" in text || "Fragment" in text)
            usages += AndroidUsageKind.UI
        if ("ViewModel" in text || "LiveData" in text)
            usages += AndroidUsageKind.STATE
        if ("R." in text) usages += AndroidUsageKind.RESOURCES

        return usages
    }
}
