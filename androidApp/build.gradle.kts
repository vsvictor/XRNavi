import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

android {
    namespace = "com.mobilespace.xrnavi"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mobilespace.xrnavi"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val tokenFile = rootProject.file("tokens.dat")
        val publicToken = if (tokenFile.isFile) {
            Regex("(?m)^\\s*(pk\\.[A-Za-z0-9._-]+)\\s*$")
                .find(tokenFile.readText())?.groupValues?.get(1)
        } else null
        require(!publicToken.isNullOrBlank()) {
            "Mapbox public token missing. Configure local tokens.dat; see tools/mapbox-setup.en.md."
        }
        resValue("string", "mapbox_access_token", publicToken)
    }
    packaging {
        jniLibs.pickFirsts += "**/libc++_shared.so"
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        resValues = true
    }
    buildToolsVersion = "37.0.0"
}