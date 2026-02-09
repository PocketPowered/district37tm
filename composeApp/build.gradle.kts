import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.apolloGraphql)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
            freeCompilerArgs.add("-opt-in=kotlin.experimental.ExperimentalNativeApi")
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // Add opt-in for all targets
    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
                    freeCompilerArgs.add("-opt-in=kotlin.experimental.ExperimentalNativeApi")
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            implementation(libs.jetbrains.navigation.compose)

            // Apollo GraphQL client
            implementation(libs.apollo.runtime)
            implementation(libs.apollo.normalized.cache)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json.client)

            // Kotlinx
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.io.core)

            // Koin for dependency injection
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // SQLDelight for local storage
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Image loading - landscapist is multiplatform
            implementation(libs.landscapist.coil)
            implementation(libs.landscapist.placeholder)

            // Image picker - FileKit for camera and gallery (16KB page size compatible)
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }

        androidMain.dependencies {
            // Android-specific dependencies
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.apache)

            // Koin Android extensions
            implementation(libs.koin.android)

            // SQLDelight Android driver
            implementation(libs.sqldelight.android.driver)

            // Coil for Android image loading
            implementation(libs.coil.compose)

            // Security for encrypted token storage
            implementation(libs.androidx.security.crypto)

            // Browser for OAuth Custom Tabs
            implementation(libs.androidx.browser)

            // Firebase for push notifications
            implementation(libs.firebase.messaging)

            // Google Play Services Location
            implementation(libs.play.services.location)
        }

        iosMain.dependencies {
            // iOS-specific Ktor engine
            implementation(libs.ktor.client.darwin)

            // SQLDelight iOS driver
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.district37.toastmasters"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.district37.toastmasters"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 8
        versionName = "8.0"
    }

    buildTypes {
        debug {
            // Production server for debug builds
            buildConfigField("String", "SERVER_URL", "\"https://eventsidekick-server-095fe3b57fd4.herokuapp.com/graphql\"")
        }
        release {
            // Production server
            buildConfigField("String", "SERVER_URL", "\"https://eventsidekick-server-095fe3b57fd4.herokuapp.com/graphql\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

// Apollo GraphQL configuration
apollo {
    service("eventsidekick") {
        packageName.set("com.district37.toastmasters.graphql")

        // Schema will be downloaded from running server
        introspection {
            endpointUrl.set("https://eventsidekick-server-095fe3b57fd4.herokuapp.com/graphql")
            schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        }

        // Generate Kotlin models with proper nullability
        generateKotlinModels.set(true)

        // Generate operation output as sealed classes
        generateFragmentImplementations.set(true)

        // Map custom scalars
        mapScalar("Instant", "kotlinx.datetime.Instant")
        mapScalar("LocalDate", "kotlinx.datetime.LocalDate")
        mapScalarToKotlinLong("Long")
    }
}

// SQLDelight configuration
sqldelight {
    databases {
        create("EventSidekickDatabase") {
            packageName.set("com.district37.toastmasters.database")
        }
    }
}
