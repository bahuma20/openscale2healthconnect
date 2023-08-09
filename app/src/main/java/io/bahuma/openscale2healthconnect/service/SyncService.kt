package io.bahuma.openscale2healthconnect.service

import android.content.SharedPreferences
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.bahuma.openscale2healthconnect.AppViewModel
import io.bahuma.openscale2healthconnect.PACKAGE_NAME_KEY
import io.bahuma.openscale2healthconnect.SyncWorker
import java.time.Instant
import java.util.concurrent.TimeUnit

class SyncService(
    private val context: ComponentActivity,
    private val viewModel: AppViewModel,
    private val openScalePackageName: String
) {
    private val tag = "SyncService"

    companion object {
        const val PREFERENCE_STORE = "OpenScaleToHealthConnectSyncService"
    }


    fun setUpSyncWorker() {
        Log.d(tag, "Setup sync worker")
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(2, TimeUnit.HOURS)
            .setInputData(getWorkerData())
            .addTag("OPENSCALE_TO_HEALTHCONNECT_SYNC")
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun watchSyncWorkerState() {
        WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData("OPENSCALE_TO_HEALTHCONNECT_SYNC")
            .observe(context) {
                if (it.any { workerInfo -> workerInfo.state == WorkInfo.State.RUNNING }) {
                    viewModel.setSyncRunning(true)
                } else {
                    viewModel.setSyncRunning(false)
                }

                if (it.any { workerInfo -> workerInfo.state == WorkInfo.State.RUNNING || workerInfo.state == WorkInfo.State.ENQUEUED }) {
                    viewModel.setSyncEnabled(true)
                } else {
                    viewModel.setSyncEnabled(false)
                }

                val sp: SharedPreferences =
                    context.getSharedPreferences(PREFERENCE_STORE, 0)

                val lastRun = sp.getLong("LAST_SYNC_RUN", 0)
                if (lastRun == 0L) {
                    viewModel.setLastRun(null)
                } else {
                    viewModel.setLastRun(Instant.ofEpochMilli(lastRun))
                }
            }
    }

    fun removeSyncWorker() {
        Log.d(tag, "Run sync worker once")
        WorkManager.getInstance(context)
            .cancelAllWorkByTag("OPENSCALE_TO_HEALTHCONNECT_SYNC")
    }

    fun runWorkOnce() {
        Log.d(tag, "Setup sync worker")

        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(getWorkerData())
            .addTag("OPENSCALE_TO_HEALTHCONNECT_SYNC")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun getWorkerData(): Data {
        val data = Data.Builder()

        data.putString(PACKAGE_NAME_KEY, openScalePackageName)

        return data.build()
    }
}