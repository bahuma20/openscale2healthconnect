package io.bahuma.openscale2healthconnect.service

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import io.bahuma.openscale2healthconnect.model.OpenScaleMeasurement
import io.bahuma.openscale2healthconnect.model.OpenScaleUser
import java.math.BigDecimal
import java.util.Date

class OpenScaleDataService(private val context: Context, openScalePackage: String) {
    private val tag = "OpenScaleDataService"
    private val authority = "$openScalePackage.provider"

    companion object {
        val PREFERENCE_STORE = "OpenScaleSyncSettings"
    }

    fun getUsers(): List<OpenScaleUser> {

        val userUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(authority)
            .path("users")
            .build()

        val records = context.contentResolver.query(
            userUri,
            null,
            null,
            null,
            null
        )

        val users = arrayListOf<OpenScaleUser>()

        records.use { record ->
            while (record?.moveToNext() == true) {
                var id: Int? = null
                var username: String? = null

                for (i in 0 until record.columnCount) {
                    if (record.getColumnName(i).equals("_ID")) {
                        id = record.getInt(i)
                    }

                    if (record.getColumnName(i).equals("username")) {
                        username = record.getString(i)
                    }
                }

                if (id != null && username != null) {
                    users.add(OpenScaleUser(id, username))
                } else {
                    Log.e(tag, "ID or username missing")
                }
            }
        }

        Log.d(tag, users.toString())

        return users
    }

    fun getMeasurements(openScaleUser: OpenScaleUser): List<OpenScaleMeasurement> {
        Log.d(tag, "Get measurements for user ${openScaleUser.id}")
        val measurementsUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(authority)
            .path("measurements/" + openScaleUser.id)
            .build()

        val records = context.contentResolver.query(
            measurementsUri,
            null,
            null,
            null,
            null
        )

        val measurements = arrayListOf<OpenScaleMeasurement>()

        records.use { record ->
            while (record?.moveToNext() == true) {
                var id: Int? = null
                var dateTime: Date? = null
                var weight: BigDecimal? = null
                var fat: BigDecimal? = null
                var water: BigDecimal? = null
                var muscle: BigDecimal? = null

                for (i in 0 until record.columnCount) {
                    if (record.getColumnName(i).equals("_ID")) {
                        id = record.getInt(i)
                    }

                    if (record.getColumnName(i).equals("datetime")) {
                        val timestamp = record.getLong(i)
                        dateTime = Date(timestamp)
                    }

                    if (record.getColumnName(i).equals("weight")) {
                        weight = BigDecimal(record.getString(i))
                    }

                    if (record.getColumnName(i).equals("fat")) {
                        fat = BigDecimal(record.getString(i))
                    }

                    if (record.getColumnName(i).equals("water")) {
                        water = BigDecimal(record.getString(i))
                    }

                    if (record.getColumnName(i).equals("muscle")) {
                        muscle = BigDecimal(record.getString(i))
                    }
                }

                if (id != null && dateTime != null && weight != null && fat != null && water != null && muscle != null) {
                    measurements.add(
                        OpenScaleMeasurement(
                            id,
                            dateTime,
                            weight,
                            fat,
                            water,
                            muscle
                        )
                    )
                } else {
                    Log.e(tag, "Not all required parameters are set")
                }
            }
        }

        Log.d(tag, "Loaded ${measurements.size} measurements for user ${openScaleUser.id}")

        return measurements
    }

    fun getSavedSelectedUserId(): Int? {
        val sp: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_STORE, 0)
        val userId = sp.getInt("SELECTED_USER_ID", OpenScaleService.EMPTY_USER_ID)

        if (userId == OpenScaleService.EMPTY_USER_ID) {
            return null
        }

        return userId
    }

    fun saveSelectedUserId(userId: Int?) {
        val sp: SharedPreferences.Editor =
            context.getSharedPreferences(PREFERENCE_STORE, 0).edit()
        if (userId != null) {
            sp.putInt("SELECTED_USER_ID", userId)
        } else {
            sp.putInt("SELECTED_USER_ID", OpenScaleService.EMPTY_USER_ID)
        }

        sp.apply()
    }
}