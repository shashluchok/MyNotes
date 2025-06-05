plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.android)
}

val ndkVersion: String by project
val cmakeVersion: String by project
val abi: String by project

android {
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    namespace = "com.shashluchok.audiorecorder"
    ndkVersion = this@Build_gradle.ndkVersion

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()


        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += abi.split(',')
        }

    }
    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
            version = cmakeVersion
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.collections.immutable)
}