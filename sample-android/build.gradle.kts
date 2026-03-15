plugins {
    id("com.android.application")
    kotlin("android")
    id("com.hiroaki404.lifecycle")
}

android {
    namespace = "com.hiroaki404.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hiroaki404.sample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
}
