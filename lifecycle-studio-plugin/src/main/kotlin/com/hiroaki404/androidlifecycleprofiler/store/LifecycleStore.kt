package com.hiroaki404.androidlifecycleprofiler.store

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.hiroaki404.androidlifecycleprofiler.io.LogcatMonitor
import com.hiroaki404.androidlifecycleprofiler.model.LifecycleEvent
import com.hiroaki404.androidlifecycleprofiler.model.LifecycleSpan
import com.hiroaki404.androidlifecycleprofiler.model.closingEvent
import com.hiroaki404.androidlifecycleprofiler.model.eventToState
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val LOG = logger<LifecycleStore>()

@Service(Service.Level.PROJECT)
class LifecycleStore(project: Project) {
    private val _spans = MutableStateFlow<List<LifecycleSpan>>(emptyList())
    val spans: StateFlow<List<LifecycleSpan>> = _spans.asStateFlow()

    private val monitoredDevices = mutableSetOf<String>()
    private val monitor = LogcatMonitor()

    private val deviceListener = object : AndroidDebugBridge.IDeviceChangeListener {
        override fun deviceConnected(device: IDevice) {
            LOG.info("Device connected: ${device.serialNumber}")
            startMonitoringDevice(device)
        }

        override fun deviceDisconnected(device: IDevice) {
            LOG.info("Device disconnected: ${device.serialNumber}")
            monitoredDevices.remove(device.serialNumber)
        }

        override fun deviceChanged(device: IDevice, changeMask: Int) {}
    }

    private val bridgeListener = AndroidDebugBridge.IDebugBridgeChangeListener { bridge ->
        LOG.info("Bridge changed: ${bridge?.isConnected}")
        if (bridge != null) {
            bridge.devices.forEach { startMonitoringDevice(it) }
        }
    }

    init {
        LOG.info("LifecycleStore init, bridge=${AndroidDebugBridge.getBridge()?.isConnected}")
        AndroidDebugBridge.addDeviceChangeListener(deviceListener)
        AndroidDebugBridge.addDebugBridgeChangeListener(bridgeListener)
        AndroidDebugBridge.getBridge()?.devices?.forEach { startMonitoringDevice(it) }
    }

    private fun startMonitoringDevice(device: IDevice) {
        LOG.info("startMonitoringDevice: ${device.serialNumber}, online=${device.isOnline}")
        if (monitoredDevices.add(device.serialNumber)) {
            monitor.start(device, this)
        }
    }

    fun addEvent(event: LifecycleEvent) {
        LOG.info("addEvent: $event")
        val current = _spans.value.toMutableList()

        val newState = eventToState(event.event)
        if (newState != null) {
            current.add(
                LifecycleSpan(
                    component = event.component,
                    state = newState,
                    startTime = event.timestamp,
                    endTime = null,
                )
            )
        }

        val closingState = closingEvent(event.event)
        if (closingState != null) {
            val idx = current.indexOfLast {
                it.component == event.component &&
                    it.state == closingState &&
                    it.endTime == null
            }
            if (idx >= 0) {
                current[idx] = current[idx].copy(endTime = event.timestamp)
            }
        }

        _spans.value = current
    }

    fun rescanDevices() {
        LOG.info("rescanDevices called")
        AndroidDebugBridge.getBridge()?.devices?.forEach { startMonitoringDevice(it) }
    }

    fun clear() {
        _spans.value = emptyList()
    }
}
