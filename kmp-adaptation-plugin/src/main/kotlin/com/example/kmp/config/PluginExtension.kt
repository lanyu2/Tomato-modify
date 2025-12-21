package com.example.kmp.config

open class PluginExtension {

    /**
     * 要扫描的源码目录（相对于 projectDir）
     */
    var sourceDir: String = "src/main/java"

    /**
     * 输出目录（相对于 buildDir）
     */
    var outputDir: String = "kmp-migration"
}
