package com.example.core

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ControllerManager(context: Context) : InputManager.InputDeviceListener {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager

    private val _connectedControllerName = MutableStateFlow<String?>(null)
    val connectedControllerName = _connectedControllerName.asStateFlow()

    private val _isControllerConnected = MutableStateFlow(false)
    val isControllerConnected = _isControllerConnected.asStateFlow()

    fun start() {
        inputManager?.registerInputDeviceListener(this, null)
        checkConnectedControllers()
    }

    fun stop() {
        inputManager?.unregisterInputDeviceListener(this)
    }

    fun checkConnectedControllers() {
        val deviceIds = InputDevice.getDeviceIds()
        var foundControllerName: String? = null

        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue
            val sources = device.sources
            val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD)
            val isJoystick = (sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)

            // Ignore virtual system keyboards/touchscreens
            if (!device.isVirtual && (isGamepad || isJoystick)) {
                foundControllerName = device.name
                break
            }
        }

        _connectedControllerName.value = foundControllerName
        _isControllerConnected.value = foundControllerName != null
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        checkConnectedControllers()
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        checkConnectedControllers()
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        checkConnectedControllers()
    }
}
