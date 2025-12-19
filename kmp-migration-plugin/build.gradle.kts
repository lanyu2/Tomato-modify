import org.gradle.api.plugins.PluginContainer

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.example.kmp"
version = "0.1.0"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(kotlin("stdlib"))
    // 模板里使用了 Gson，如果你的插件需要，可保留
    implementation("com.google.code.gson:gson:2.10.1")
}

gradlePlugin {
    plugins {
        create("kmpMigration") {
            // 原插件 ID 保留
            id = "com.example.kmp.kmp-migration"
            // 原插件入口类
            implementationClass = "com.example.kmp.kmpextractor.KmpMigrationPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            // 可选：保留 groupId/artifactId/version
            groupId = "com.example.kmp"
            artifactId = "kmp-migration-plugin"
            version = project.version.toString()
        }
    }
}
