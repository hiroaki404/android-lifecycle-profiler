package com.hiroaki404.androidlifecycleprofiler.model

data class LifecycleEvent(
    val component: String,  // "MainActivity"
    val event: String,      // "onResume"
    val timestamp: Long,    // System.currentTimeMillis()
)
