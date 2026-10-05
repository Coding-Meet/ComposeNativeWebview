@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    applyDefaultHierarchyTemplate()

    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.kotlinx.serializationJson)
            implementation(project(":e2e-shared"))
            implementation(project(":webview-compose"))
        }
    }
}
