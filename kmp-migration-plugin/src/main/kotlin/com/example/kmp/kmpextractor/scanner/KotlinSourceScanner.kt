package com.example.kmp.kmpextractor.scanner

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind
import java.io.File

class KotlinSourceScanner {
    fun detectAndroidUsages(file: File): Set<AndroidUsageKind> {
        val text = file.readText()
        val usages = mutableSetOf<AndroidUsageKind>()

        if ("SharedPreferences" in text || "androidx.room" in text) usages += AndroidUsageKind.STORAGE
        if ("android.util.Log" in text) usages += AndroidUsageKind.LOGGING
        if ("Handler(" in text || "Looper" in text) usages += AndroidUsageKind.THREADING
        if ("SystemClock" in text) usages += AndroidUsageKind.TIME
        if ("Activity" in text || "Fragment" in text || "Context" in text) usages += AndroidUsageKind.UI
        if ("ViewModel" in text || "LiveData" in text) usages += AndroidUsageKind.STATE
        if ("R." in text) usages += AndroidUsageKind.RESOURCES
        if ("ConnectivityManager" in text) usages += AndroidUsageKind.NETWORK
        if ("okhttp3" in text || "retrofit2" in text) usages += AndroidUsageKind.HTTP
        if ("Gson" in text || "org.json" in text) usages += AndroidUsageKind.JSON

        return usages
    }
}