package com.hiroaki404.lifecycle

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class LogLifecycle(
    val tag: String = "",
    val logLevel: LogLevel = LogLevel.DEBUG
)
