plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register<Exec>("runAdminPortal") {
    workingDir = file("$projectDir/event-manager")
    commandLine("sh", "-c", "npm install --legacy-peer-deps && npm start")
}