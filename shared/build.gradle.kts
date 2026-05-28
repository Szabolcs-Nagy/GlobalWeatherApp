import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Read API key from local.properties (never committed to VCS)
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
}
val weatherApiKey: String = localProperties.getProperty("weatherApiKey", "")

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// ── Generate ApiConfig.kt from local.properties at build time ─────────────
val generatedDir = layout.buildDirectory.dir("generated/commonMain/kotlin")

val generateApiConfig by tasks.registering {
    val key = weatherApiKey
    val outputDir = generatedDir
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get()
            .file("com/coding/global_weather_app/ApiConfig.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            """
            package com.coding.global_weather_app

            /** Build-time injected API config. Key is read from local.properties. */
            internal object ApiConfig {
                val weatherApiKey: String = "$key"
            }
            """.trimIndent()
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.coding.global_weather_app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedDir)
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
        }
    }
}

// Wire the generation task into compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    dependsOn(generateApiConfig)
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}