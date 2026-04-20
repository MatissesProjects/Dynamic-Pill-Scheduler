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
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    /**
     * Fetches the latest sleep session end time to use as T-Wake.
     */
    suspend fun fetchLatestTWake(): Instant? {
        if (!hasPermissions()) return null

        val request = ReadRecordsRequest(
            recordType = SleepSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(
                Instant.now().minus(24, ChronoUnit.HOURS),
                Instant.now()
            )
        )

        val response = healthConnectClient.readRecords(request)
        return response.records.maxByOrNull { it.endTime }?.endTime
    }
}
