plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
    application
}

repositories {
    mavenCentral()
    google()
}

configurations.all {
    resolutionStrategy {
        // Force using Guava
        force("com.google.guava:guava:32.1.1-jre")

        eachDependency {
            if (requested.group == "com.google.guava" && requested.name == "listenablefuture") {
                useTarget("com.google.guava:guava:32.1.1-jre")
                because("Resolve conflict between guava and listenablefuture dummy module")
            }
        }
    }
}

group = "com.district37.toastmasters"
version = "1.0.0"
application {
    mainClass.set("com.district37.toastmasters.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    testImplementation(libs.kotlin.test.junit)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.ktor)
    implementation(libs.kermit)
    implementation(libs.google.cloud.firestore)
}