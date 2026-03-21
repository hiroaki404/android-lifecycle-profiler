plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform") version "2.10.2"
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose") version "1.9.0"
}

group = "com.hiroaki404"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        local("${System.getProperty("user.home")}/Applications/Android Studio.app")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        composeUI()

        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("org.jetbrains.android")
    }

    // kotlinx-coroutines is provided by the IntelliJ Platform; do NOT bundle it
    compileOnly(libs.kotlinxCoroutines)
    // @Preview annotation (compile-only, runtime is provided by the IDE)
    compileOnly(libs.composeUiToolingPreview) { isTransitive = false }

    // Compose runtime for preview host process (not bundled in plugin, see buildPlugin exclusions)
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.foundation)
    // Jewel standalone for preview host process (IDE provides jewel-ide-laf-bridge at runtime)
    runtimeOnly("org.jetbrains.jewel:jewel-int-ui-standalone:0.32.1-253.28294.285")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    // Compose/Jewel runtime is provided by IntelliJ Platform at runtime;
    // exclude from sandbox (runIde) and plugin zip to avoid ClassLoader conflicts
    val composeExcludes: org.gradle.api.specs.Spec<org.gradle.api.file.FileTreeElement> =
        org.gradle.api.specs.Spec { entry ->
            val name = entry.name
            name.startsWith("skiko") ||
            name.startsWith("compose-") ||
            name.startsWith("jewel-") ||
            name.startsWith("kotlinx-coroutines") ||
            name.endsWith("-desktop.jar") ||
            name.contains("-desktop-") ||
            name.startsWith("animation-") ||
            name.startsWith("runtime-") ||
            name.startsWith("ui-") ||
            name.startsWith("foundation-") ||
            name.startsWith("savedstate-") ||
            name.startsWith("lifecycle-runtime-") ||
            name.startsWith("lifecycle-viewmodel-") ||
            name.startsWith("lifecycle-common-") ||
            name.startsWith("lifecycle-runtime-compose-") ||
            name.startsWith("collection-jvm") ||
            name.startsWith("annotation-jvm")
        }
    prepareSandbox { exclude(composeExcludes) }
    buildPlugin { exclude(composeExcludes) }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
