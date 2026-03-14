plugins {
    id("buildsrc.convention.kotlin-jvm")
    `maven-publish`
}

dependencies {
    compileOnly(libs.kotlinCompilerEmbeddable)
    compileOnly(project(":annotations"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.hiroaki404"
            artifactId = "lifecycle-compiler-plugin"
            version = "0.1.0"
            from(components["java"])
        }
    }
}
