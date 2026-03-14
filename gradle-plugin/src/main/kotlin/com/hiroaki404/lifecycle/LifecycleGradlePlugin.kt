package com.hiroaki404.lifecycle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class LifecycleGradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.dependencies.add(
            "implementation",
            "com.hiroaki404:lifecycle-annotations:0.1.0"
        )
    }

    override fun getPluginArtifact() = SubpluginArtifact(
        groupId = "com.hiroaki404",
        artifactId = "lifecycle-compiler-plugin",
        version = "0.1.0"
    )

    override fun getCompilerPluginId() = "com.hiroaki404.lifecycle"

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>) =
        kotlinCompilation.target.project.provider { emptyList<SubpluginOption>() }
}
