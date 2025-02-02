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

        measurements.forEach { measurement ->
            try {
                buildWeightRecord(measurement).let { records.add(it) }
            } catch (e: Exception) {
                Log.w(tag, "Skipping weight record for measurement ${measurement.id}: ${e.message}")
            }

            try {
                buildWaterRecord(measurement).let { records.add(it) }
            } catch (e: Exception) {
                Log.w(tag, "Skipping water record for measurement ${measurement.id}: ${e.message}")
            }

            try {
                buildFatRecord(measurement).let { records.add(it) }
            } catch (e: Exception) {
                Log.w(tag, "Skipping fat record for measurement ${measurement.id}: ${e.message}")
            }
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
        val weight = measurement.weight.toDouble()
        require(weight > 0) { "Weight must be positive, got: $weight" }

        return WeightRecord(
            time = measurement.dateTime.toInstant(),
            zoneOffset = null,
            weight = Mass.kilograms(weight),
            metadata = buildMetadata(measurement, "weight")
        )
    }

    private fun buildWaterRecord(measurement: OpenScaleMeasurement): BodyWaterMassRecord {
        val waterPercentage = measurement.water.toDouble()
        val weight = measurement.weight.toDouble()

        require(waterPercentage in 0.0..100.0) { "Water percentage must be between 0 and 100, got: $waterPercentage" }
        require(weight > 0) { "Weight must be positive, got: $weight" }

        return BodyWaterMassRecord(
            time = measurement.dateTime.toInstant(),
            zoneOffset = null,
            mass = Mass.kilograms(weight * waterPercentage / 100),
            metadata = buildMetadata(measurement, "water")
        )
    }

    private fun buildFatRecord(measurement: OpenScaleMeasurement): BodyFatRecord {
        val fatPercentage = measurement.fat.toDouble()
        require(fatPercentage in 0.0..100.0) { "Fat percentage must be between 0 and 100, got: $fatPercentage" }

        return BodyFatRecord(
            time = measurement.dateTime.toInstant(),
            zoneOffset = null,
            percentage = Percentage(fatPercentage),
            metadata = buildMetadata(measurement, "fat")
        )
    }
}