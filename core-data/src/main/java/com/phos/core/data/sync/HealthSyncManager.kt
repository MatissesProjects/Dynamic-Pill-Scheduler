package com.phos.core.data.sync

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthSyncManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    suspend fun hasPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            // Log error or handle specific exceptions like API unavailable
            false
        }
    }

    /**
     * Fetches the latest sleep session end time to use as T-Wake.
     * @return Instant? the end time of the latest sleep session, or null if no data or no permissions.
     */
    suspend fun fetchLatestTWake(): Instant? {
        try {
            if (!hasPermissions()) return null

            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minus(24, ChronoUnit.HOURS),
                    Instant.now()
                )
            )

            val response = healthConnectClient.readRecords(request)
            // Filtering for valid sessions (at least 3 hours) to avoid naps/noise as T-Wake
            return response.records
                .filter { ChronoUnit.MINUTES.between(it.startTime, it.endTime) > 180 }
                .maxByOrNull { it.endTime }?.endTime
        } catch (e: Exception) {
            // Handle specific Health Connect errors
            return null
        }
    }

    /**
     * Fetches the latest nap (short sleep session < 3 hours) in the last 12 hours.
     */
    suspend fun fetchLatestNap(): SleepSessionRecord? {
        try {
            if (!hasPermissions()) return null

            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minus(12, ChronoUnit.HOURS),
                    Instant.now()
                )
            )

            val response = healthConnectClient.readRecords(request)
            return response.records
                .filter { 
                    val mins = ChronoUnit.MINUTES.between(it.startTime, it.endTime)
                    mins in 15..179 
                }
                .maxByOrNull { it.endTime }
        } catch (e: Exception) {
            return null
        }
    }
}
