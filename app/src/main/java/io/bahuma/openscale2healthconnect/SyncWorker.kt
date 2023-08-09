package io.bahuma.openscale2healthconnect

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.bahuma.openscale2healthconnect.service.HealthConnectDataService
import io.bahuma.openscale2healthconnect.service.OpenScaleDataService
import io.bahuma.openscale2healthconnect.service.SyncService
import java.time.Instant

val PACKAGE_NAME_KEY = "packageName"

class SyncWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    private val tag: String = "SyncWorker"
    private val packageName = workerParams.inputData.getString(PACKAGE_NAME_KEY).orEmpty()
    private val openScaleDataService = OpenScaleDataService(appContext, packageName)
    private val healthConnectDataService = HealthConnectDataService()

    override suspend fun doWork(): Result {
        Log.d(tag, "Sync starting")
        try {
            val healthConnectClient = HealthConnectClient.getOrCreate(appContext)
            healthConnectDataService.healthConnectClient = healthConnectClient
        } catch (e: Exception) {
            Log.e(tag, "HealthConnect is not available")
            return Result.retry()
        }


        val users = openScaleDataService.getUsers()
        val selectedUserId = openScaleDataService.getSavedSelectedUserId()
        try {
            val user = users.first { user -> user.id == selectedUserId }
            val measurements = openScaleDataService.getMeasurements(user)

            healthConnectDataService.fullSync(measurements)
        } catch (e: NoSuchElementException) {
            Log.e(tag, "User not selected")
            return Result.retry()
        }

        saveDate()
        Log.d(tag, "Snyc done successfully")
        return Result.success()
    }

    private fun saveDate() {
        val sp: SharedPreferences.Editor =
            appContext.getSharedPreferences(SyncService.PREFERENCE_STORE, 0).edit()

        sp.putLong("LAST_SYNC_RUN", Instant.now().toEpochMilli())
        sp.apply()
    }

}