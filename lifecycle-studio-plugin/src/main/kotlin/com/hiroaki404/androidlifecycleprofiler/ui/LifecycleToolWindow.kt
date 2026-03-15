package com.hiroaki404.androidlifecycleprofiler.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hiroaki404.androidlifecycleprofiler.store.LifecycleStore
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

class LifecycleToolWindowFactory : ToolWindowFactory {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val store = project.getService(LifecycleStore::class.java)

        toolWindow.addComposeTab("Timeline") {
            LifecycleContent(store = store)
        }
    }
}

@Composable
private fun LifecycleContent(store: LifecycleStore) {
    val spans by store.spans.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            OutlinedButton(onClick = { store.clear() }) {
                Text("Clear")
            }
            OutlinedButton(
                onClick = { store.rescanDevices() },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Rescan Devices")
            }
        }
        TimelineChart(
            spans = spans,
            modifier = Modifier.weight(1f),
        )
    }
}
