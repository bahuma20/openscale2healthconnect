package io.bahuma.openscale2healthconnect

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.bahuma.openscale2healthconnect.model.OpenScaleUser
import java.time.Instant

class AppViewModel : ViewModel() {
    private val _openScaleAvailable = mutableStateOf(false)
    private val _openScalePermissionsGranted = mutableStateOf(false)
    private val _healthConnectAvailable = mutableStateOf(false)
    private val _healthConnectAllPermissionsGranted = mutableStateOf(false)
    private val _openScaleUsers: MutableState<List<OpenScaleUser>> = mutableStateOf(listOf())
    private val _openScaleSelectedUser: MutableState<OpenScaleUser?> = mutableStateOf(null)
    private val _syncEnabled = mutableStateOf(false)
    private val _syncRunning = mutableStateOf(false)
    private val _lastRun: MutableState<Instant?> = mutableStateOf(null)

    val openScaleAvailable: State<Boolean> get() = _openScaleAvailable

    fun setOpenScaleAvailable(value: Boolean) {
        _openScaleAvailable.value = value
    }

    val openScalePermissionsGranted: State<Boolean> get() = _openScalePermissionsGranted

    fun setOpenScalePermissionsGranted(value: Boolean) {
        _openScalePermissionsGranted.value = value
    }


    val healthConnectAvailable: State<Boolean> get() = _healthConnectAvailable

    fun setHealthConnectAvailable(value: Boolean) {
        _healthConnectAvailable.value = value
    }


    val healthConnectAllPermissionsGranted: State<Boolean> get() = _healthConnectAllPermissionsGranted

    fun setHealthConnectAllPermissionsGranted(value: Boolean) {
        _healthConnectAllPermissionsGranted.value = value
    }


    val openScaleUsers: State<List<OpenScaleUser>> get() = _openScaleUsers

    fun setOpenScaleUsers(value: List<OpenScaleUser>) {
        _openScaleUsers.value = value
    }


    val openScaleSelectedUser: State<OpenScaleUser?> get() = _openScaleSelectedUser

    fun selectOpenScaleUser(value: OpenScaleUser?) {
        _openScaleSelectedUser.value = value
    }

    val syncEnabled: State<Boolean> get() = _syncEnabled

    fun setSyncEnabled(value: Boolean) {
        this._syncEnabled.value = value
    }

    val lastRun: State<Instant?> get() = _lastRun

    fun setLastRun(value: Instant?) {
        _lastRun.value = value
    }

    val syncRunning: State<Boolean> get() = _syncRunning

    fun setSyncRunning(value: Boolean) {
        this._syncRunning.value = value
    }
}