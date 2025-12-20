package com.example.kmp.kmpextractor.migratability

import kotlinx.serialization.Serializable

@Serializable
enum class Migratability {
    COMMON_READY,
    MIGRATABLE,
    PARTIAL,
    ANDROID_ONLY
}
