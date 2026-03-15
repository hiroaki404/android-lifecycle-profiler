package com.hiroaki404.androidlifecycleprofiler.model

import androidx.compose.ui.graphics.Color

data class LifecycleSpan(
    val component: String,
    val state: LifecycleState,
    val startTime: Long,
    val endTime: Long?,  // null = 現在継続中
)

enum class LifecycleState(val color: Color, val label: String) {
    CREATED(Color(0xFF4CAF50), "Created"),
    STARTED(Color(0xFF2196F3), "Started"),
    RESUMED(Color(0xFFFF9800), "Resumed"),
}

fun eventToState(event: String): LifecycleState? = when (event) {
    "onCreate" -> LifecycleState.CREATED
    "onStart"  -> LifecycleState.STARTED
    "onResume" -> LifecycleState.RESUMED
    else       -> null
}

fun closingEvent(event: String): LifecycleState? = when (event) {
    "onPause"   -> LifecycleState.RESUMED
    "onStop"    -> LifecycleState.STARTED
    "onDestroy" -> LifecycleState.CREATED
    else        -> null
}
