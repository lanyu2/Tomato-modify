package com.example.kmp.kmpextractor.scanner

import com.example.kmp.kmpextractor.classifier.AndroidUsageKind
import java.io.File

class KotlinSourceScanner {

    fun detectAndroidUsages(file: File): Set<AndroidUsageKind> {
        val text = file.readText()
        val usages = mutableSetOf<AndroidUsageKind>()

        // 存储：含原生 Prefs 和 Room
        if ("SharedPreferences" in text || "androidx.room" in text) usages += AndroidUsageKind.STORAGE

        // 日 : 增加包名检测防止误报
        if ("android.util.Log" in text || "Log.d(" in text || "Log.e(" in text) usages += AndroidUsageKind.LOGGING

        // 线程：含原生 Handler 和废弃的 AsyncTask
        if ("Handler(" in text || "Looper" in text || "AsyncTask" in text) usages += AndroidUsageKind.THREADING

        // 时间
        if ("SystemClock" in text) usages += AndroidUsageKind.TIME

        // UI：增加 Context, Intent 和 View
        if (listOf("Activity", "Fragment", "View", "Context", "Intent", "android.widget").any { it in text }) {
            usages += AndroidUsageKind.UI
        }

        // 状态管理
        if ("ViewModel" in text || "LiveData" in text || "SavedStateHandle" in text) usages += AndroidUsageKind.STATE

        // 资源引用
        if ("R." in text && "import" !in text) usages += AndroidUsageKind.RESOURCES

        // 网络状态检测
        if ("ConnectivityManager" in text || "NetworkInfo" in text) usages += AndroidUsageKind.NETWORK

        // 网络请求库 (Android 特定部分)
        if ("okhttp3" in text || "retrofit2" in text) usages += AndroidUsageKind.HTTP

        // 序列化检测
        if ("Gson" in text || "org.json" in text || "com.google.gson" in text) usages += AndroidUsageKind.JSON

        return usages
    }
}