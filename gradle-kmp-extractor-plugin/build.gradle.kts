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
}


dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}


gradlePlugin {
    plugins {
        create("kmpExtractor") {
            id = "com.example.kmp.extractor"
            implementationClass = "com.example.kmp.ExtractKmpPlugin"
        }
    }
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}