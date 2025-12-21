plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.example.kmp"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    // Kotlin PSI / Compiler
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
}

gradlePlugin {
    plugins {
        create("kmpAdaptation") {
            id = "com.example.kmp-adaptation"
            implementationClass = "com.example.kmp.KmpAdaptationPlugin"
        }
    }
}
