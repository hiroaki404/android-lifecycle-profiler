plugins {
    kotlin("jvm") version "2.1.20"
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("lifecyclePlugin") {
            id = "com.hiroaki404.lifecycle"
            implementationClass = "com.hiroaki404.lifecycle.LifecycleGradlePlugin"
        }
    }
}
