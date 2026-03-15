pluginManagement {
    includeBuild("gradle-plugin")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    plugins {
        kotlin("jvm") version "2.3.0"
        kotlin("android") version "2.3.0"
        kotlin("plugin.compose") version "2.3.0"
        id("org.jetbrains.intellij.platform") version "2.10.2"
        id("com.android.application") version "8.3.2"
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":annotations")
include(":compiler-plugin")
include(":sample-android")
include(":lifecycle-studio-plugin")
// gradle-plugin は includeBuild のためここには含めない

rootProject.name = "android-lifecycle-profiler"
