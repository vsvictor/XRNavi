plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

val buildJsonRpcIosNativeArtifacts = tasks.register<org.gradle.api.tasks.Exec>(
    "buildJsonRpcIosNativeArtifacts",
) {
    val clientProject = layout.projectDirectory.dir("external/JSON-RPC-2-Client-KMP")
    workingDir(clientProject.asFile)
    commandLine("./scripts/build-ios.sh")
    inputs.files(
        clientProject.file("scripts/build-ios.sh"),
        clientProject.file("scripts/build-mobile.sh"),
        fileTree(clientProject.dir("src/iosMain")) {
            include("cinterop/**", "swift/**")
        },
    )
    inputs.property(
        "upstreamRef",
        providers.environmentVariable("JSON_RPC_2_REF")
            .orElse("803075d1db081fb670edea450b178927aecd4c47"),
    )
    outputs.dir(clientProject.dir("build/native/ios"))
}

val jsonRpcClient = project(":jsonRpcClient")
jsonRpcClient.afterEvaluate {
    listOf(
        "cinteropJsonrpcBridgeIosArm64",
        "cinteropJsonrpcBridgeIosSimulatorArm64",
    ).forEach { taskName ->
        tasks.named(taskName) {
            dependsOn(buildJsonRpcIosNativeArtifacts)
        }
    }
}