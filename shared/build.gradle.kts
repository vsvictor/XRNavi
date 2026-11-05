import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            // PMCS avoids the Kotlin/Native CMS safepoint stall observed on the physical iPad.
            binaryOption("gc", "pmcs")
        }
        iosTarget.binaries
            .withType<org.jetbrains.kotlin.gradle.plugin.mpp.TestExecutable>()
            .configureEach {
                val sdk = if (iosTarget.name == "iosArm64") "iphoneos" else "iphonesimulator"
                val developerDir = providers.exec { commandLine("xcode-select", "-p") }
                    .standardOutput.asText.get().trim()
                val sdkPath = providers.exec { commandLine("xcrun", "--sdk", sdk, "--show-sdk-path") }
                    .standardOutput.asText.get().trim()
                linkerOpts(
                    "-L$developerDir/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/$sdk",
                    "-L$sdkPath/usr/lib/swift",
                    "-rpath", "/usr/lib/swift",
                )
            }
    }
    
    android {
       namespace = "com.mobilespace.xrnavi.shared"
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
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(project(":jsonRpcClient"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}