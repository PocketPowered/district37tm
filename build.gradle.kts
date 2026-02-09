plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.apolloGraphql) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ktor) apply false
}

// Retain admin portal task if still used
// (left intact to avoid breaking existing workflows)
tasks.register<Exec>("runAdminPortal") {
    workingDir = file("$projectDir/event-manager")
    commandLine("sh", "-c", "npm install --legacy-peer-deps && npm start")
}
