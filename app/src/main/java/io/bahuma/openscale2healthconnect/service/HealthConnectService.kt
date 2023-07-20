package io.bahuma.openscale2healthconnect.service

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.coroutineScope
import io.bahuma.openscale2healthconnect.AppViewModel
import kotlinx.coroutines.launch

class HealthConnectService(
    private val context: ComponentActivity,
    private val viewModel: AppViewModel
) {
    private lateinit var healthConnectClient: HealthConnectClient
    private val tag = "HealthConnectService"

    private val requiredPermissions = setOf(
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getWritePermission(BodyWaterMassRecord::class),
        HealthPermission.getWritePermission(BodyFatRecord::class),
    )

    private val healthConnectPermissionContract =
        PermissionController.createRequestPermissionResultContract()

    private val healthConnectRequestPermissions =
        context.registerForActivityResult(healthConnectPermissionContract) { granted ->
            context.lifecycle.coroutineScope.launch { checkAllPermissionsGranted() }

            Log.d(tag, granted.toString())
            if (granted.containsAll(requiredPermissions)) {
                Log.d(tag, "health connect permissions granted")
            } else {
                Log.d(tag, "lack of required permissions")
            }
        }

    suspend fun checkAllPermissionsGranted() {
        val granted = this.healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(requiredPermissions)) {
            viewModel.setHealthConnectAllPermissionsGranted(true)
            Log.d(tag, "health connect permissions already granted")
        } else {
            viewModel.setHealthConnectAllPermissionsGranted(false)
        }
    }

    suspend fun requestPermissions() {
        val granted = this.healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(requiredPermissions)) {
            Log.d(tag, "health connect permissions already granted")
        } else {
            healthConnectRequestPermissions.launch(requiredPermissions)
        }

    }

    suspend fun detectHealthConnect(): HealthConnectClient? {
        Log.d(tag, "Detect health connect")
        val availabilityStatus = HealthConnectClient.getSdkStatus(context)

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            Log.d(tag, "HealthConnect not available")
            viewModel.setHealthConnectAvailable(false)
            return null
        }

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            Log.d(
                tag,
                "HealthConnect not installed or has to be updated. Consider redirecting to appstore"
            )
            viewModel.setHealthConnectAvailable(false)
            return null
        }

        Log.d(tag, "Health Connect available")

        viewModel.setHealthConnectAvailable(true)

        healthConnectClient = HealthConnectClient.getOrCreate(context)

        Log.d(tag, "Health connect available")

        checkAllPermissionsGranted()

        return healthConnectClient

    }


}