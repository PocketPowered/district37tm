plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.apollo3)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
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

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.firebase.messaging)
            implementation(libs.android.driver)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.koin.core)
            implementation(projects.shared)
            implementation(libs.bundles.landscapist)
            implementation(libs.accompanist.placeholder)
            implementation(libs.coroutines.extensions)
            implementation(libs.apollo.runtime)
        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)
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
        versionCode = 10
        versionName = "10.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    androidTestImplementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    debugImplementation(compose.uiTooling)
    implementation(libs.accompanist.pager)
    implementation(libs.coil.compose)
}

sqldelight {
    databases {
        create("TMDatabase") {
            packageName.set("com.district37.toastmasters.database")
        }
    }
}

apollo {
    service("supabase") {
        packageName.set("com.district37.toastmasters.graphql")
        srcDir("src/commonMain/graphql")
        schemaFile.set(file("src/commonMain/graphql/schema.graphqls"))
        mapScalar("BigInt", "kotlin.Long", "com.apollographql.apollo3.api.LongAdapter")
        mapScalar("JSON", "kotlin.Any", "com.apollographql.apollo3.api.AnyAdapter")
        mapScalar("UUID", "kotlin.String", "com.apollographql.apollo3.api.StringAdapter")
    }
}

tasks.matching { it.name == "packageDebug" }.configureEach {
    dependsOn("writeDebugAppMetadata")
}
