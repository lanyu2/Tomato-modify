package com.example.kmp.kmpextractor.classifier

import kotlinx.serialization.Serializable

@Serializable
enum class AndroidUsageKind {
    STORAGE,    // 存储 (SharedPrefs, Room)
    LOGGING,    // 日志 (android.util.Log)
    THREADING,  // 线程 (Handler, Looper, AsyncTask)
    TIME,       // 时间 (SystemClock)
    STATE,      // 状态管理 (ViewModel, LiveData)
    UI,         // UI组件 (Activity, Fragment, View, Compose Android)
    RESOURCES,  // 资源引用 (R.xxx)
    NETWORK,
    // 网络连接状态 (ConnectivityManager)
    HTTP,       // 网络请求 (Retrofit, OkHttp 且未配置 KMP)
    JSON,       // 序列化 (Gson, org.json)
    DI          // 依赖注入 (Hilt/Dagger Android 特定部分)

}