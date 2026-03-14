plugins {
    id("buildsrc.convention.kotlin-jvm")
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.hiroaki404"
            artifactId = "lifecycle-annotations"
            version = "0.1.0"
            from(components["java"])
        }
    }
}
