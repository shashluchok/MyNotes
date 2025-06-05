plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    // Needed for R and BuildConfig
    namespace = "com.shashluchok.medianotes"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Enables code shrinking, obfuscation, and optimization for only
            // release build type.
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.audioRecorder)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.navigation)

    // Compose
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.compose.material3.adaptive)

    api(libs.kotlinx.datetime)
    api(libs.kotlinx.collections.immutable)

    // DI
    implementation(libs.coin.compose)

    // Permissions handling
    implementation(libs.google.accompanist.permissions)

    // Image loading
    implementation(libs.coil.compose)

    // Lottie
    implementation(libs.lottie.compose)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Camera
    implementation(libs.cameraX.camera2)
    implementation(libs.cameraX.view)
    implementation(libs.cameraX.core)
    implementation(libs.cameraX.lifecycle)

    debugImplementation(libs.androidx.ui.tooling)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
