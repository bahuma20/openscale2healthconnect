package io.bahuma.openscale2healthconnect.service

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.BodyWaterMassRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import io.bahuma.openscale2healthconnect.model.OpenScaleMeasurement
import java.time.Instant

class HealthConnectDataService() {
    private val tag = "HealthConnectDataService"
    lateinit var healthConnectClient: HealthConnectClient


    suspend fun fullSync(measurements: List<OpenScaleMeasurement>) {
        Log.d(tag, "Writing ${measurements.size} measurements to HealthConnect")

        val records = mutableListOf<Record>()

        measurements.forEach {
            val weightRecord = buildWeightRecord(it)
            records.add(weightRecord)

            val waterRecord = buildWaterRecord(it)
            records.add(waterRecord)

            val fatRecord = buildFatRecord(it)
            records.add(fatRecord)
        }

        Log.d(tag, "Converted the ${measurements.size} measurements to ${records.size} records")

        try {
            healthConnectClient.insertRecords(records)
        } catch (e: Exception) {
            Log.e(tag, e.toString())
        } finally {
            Log.d(tag, "Write done")
        }
    }

    private fun buildMetadata(measurement: OpenScaleMeasurement, type: String): Metadata {
        return Metadata(
            clientRecordId = measurement.id.toString() + "_" + type,
            clientRecordVersion = Instant.now().toEpochMilli()
        )
    }

    private fun buildWeightRecord(measurement: OpenScaleMeasurement): WeightRecord {
        return WeightRecord(
            time = measurement.dateTime.toInstant(),
            zoneOffset = null,
            weight = Mass.kilograms(measurement.weight.toDouble()),
            metadata = buildMetadata(measurement, "weight")
        )
    }

    private fun buildWaterRecord(measurement: OpenScaleMeasurement): BodyWaterMassRecord {
        return BodyWaterMassRecord(
            time = measurement.dateTime.toInstant(),
            zoneOffset = null,
            mass = Mass.kilograms(measurement.weight.toDouble() * measurement.water.toDouble() / 100),
            metadata = buildMetadata(measurement, "water")
        )
    }

    private fun buildFatRecord(measurement: OpenScaleMeasurement): BodyFatRecord {
        return BodyFatRecord(
            time = measurement.dateTime.toInstant(),
            zoneOffset = null,
            percentage = Percentage(measurement.fat.toDouble()),
            metadata = buildMetadata(measurement, "fat")
        )
    }
}