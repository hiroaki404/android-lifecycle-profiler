package com.hiroaki404.lifecycle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompile

class LifecycleGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.dependencies.add("implementation", "com.hiroaki404:lifecycle-annotations:0.1.0")

        val pluginConf = target.configurations.create("lifecycleCompilerPluginClasspath") {
            it.isTransitive = false
        }
        target.dependencies.add(pluginConf.name, "com.hiroaki404:lifecycle-compiler-plugin:0.1.0")

        target.tasks.withType(AbstractKotlinCompile::class.java).configureEach { task ->
            task.pluginClasspath.from(pluginConf)
        }
    }
}
