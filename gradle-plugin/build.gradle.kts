plugins {
    kotlin("jvm") version "2.3.0"
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    compileOnly(libs.kotlinGradlePlugin)
}

gradlePlugin {
    plugins {
        create("lifecyclePlugin") {
            id = "com.hiroaki404.lifecycle"
            implementationClass = "com.hiroaki404.lifecycle.LifecycleGradlePlugin"
        }
    }
}
