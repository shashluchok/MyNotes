buildscript {
    dependencies {
        classpath(libs.ktlint.composeRules)
    }
}

plugins {
    alias(libs.plugins.ktlint)
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.androidLibrary) apply false
}

allprojects {
    configureKtLint()
}

fun Project.configureKtLint() {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    ktlint {
        filter {
            exclude("**/generated/**")
        }
    }
    dependencies {
        ktlintRuleset(rootProject.libs.ktlint.composeRules)
    }
}

fun Project.stringProperty(propertyName: String): String? {
    return findProperty(propertyName)?.toString()
}
