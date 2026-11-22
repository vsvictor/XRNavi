rootProject.name = "XRNavi"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            content { includeGroupAndSubgroups("com.mapbox") }
            // Current releases are public. Never put downloads credentials in app resources.
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
                includeGroupAndSubgroups("org.chromium")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":shared")
include(":jsonRpcClient")
project(":jsonRpcClient").projectDir = file("external/JSON-RPC-2-Client-KMP")