package com.hiroaki404.androidlifecycleprofiler.io

import com.android.ddmlib.IDevice
import com.android.ddmlib.logcat.LogCatListener
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.hiroaki404.androidlifecycleprofiler.model.LifecycleEvent
import com.hiroaki404.androidlifecycleprofiler.store.LifecycleStore

class LogcatMonitor {

    private val LOG_PATTERN = Regex("""(\w+)\|(\w+)\|(\d+)""")
    private val LOG_TAG = "LC_VIZ"

    fun parse(message: String): LifecycleEvent? {
        val match = LOG_PATTERN.find(message) ?: return null
        val (component, event, timestamp) = match.destructured
        return LifecycleEvent(component, event, timestamp.toLong())
    }

    fun start(device: IDevice, store: LifecycleStore) {
        val task = LogCatReceiverTask(device)
        val listener = LogCatListener { messages ->
            messages
                .filter { it.header.tag == LOG_TAG }
                .mapNotNull { parse(it.message) }
                .forEach { store.addEvent(it) }
        }
        task.addLogCatListener(listener)
        Thread(task, "LogcatMonitor").start()
    }
}
