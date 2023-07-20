package io.bahuma.openscale2healthconnect.service

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import io.bahuma.openscale2healthconnect.AppViewModel
import io.bahuma.openscale2healthconnect.model.OpenScaleUser

class OpenScaleService(
    private val context: ComponentActivity,
    private val viewModel: AppViewModel,
    openScalePackage: String
) {
    companion object {
        val EMPTY_USER_ID = 999999999
    }

    private val tag = "OpenScaleService"
    private val requiredPermissions = "$openScalePackage.READ_WRITE_DATA"
    private val openScaleDataService = OpenScaleDataService(context, openScalePackage)


    private val requestPermission =
        context.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            checkPermissionGranted()

            if (viewModel.openScalePermissionsGranted.value) {
                viewModel.setOpenScaleUsers(openScaleDataService.getUsers())
            }

            if (isGranted) {
                Log.d(tag, "is granted")
            } else {
                Log.d(tag, "is not granted")
            }

        }

    fun checkPermissionGranted() {
        if (context.checkSelfPermission(requiredPermissions) == PERMISSION_GRANTED) {
            viewModel.setOpenScalePermissionsGranted(true)
        } else {
            viewModel.setOpenScalePermissionsGranted(false)
        }
    }

    fun requestPermissions() {
        requestPermission.launch(requiredPermissions)
    }

    fun getSelectedUser(): OpenScaleUser? {
        val selectedUserId = openScaleDataService.getSavedSelectedUserId()

        if (selectedUserId != null) {
            try {
                return viewModel.openScaleUsers.value.first { user -> user.id == selectedUserId }
            } catch (e: NoSuchElementException) {
                // ignored
            }
        }

        return null
    }

    fun init() {
        checkPermissionGranted()

        if (viewModel.openScalePermissionsGranted.value) {
            viewModel.setOpenScaleUsers(openScaleDataService.getUsers())
            viewModel.selectOpenScaleUser(getSelectedUser())
        }
    }
}