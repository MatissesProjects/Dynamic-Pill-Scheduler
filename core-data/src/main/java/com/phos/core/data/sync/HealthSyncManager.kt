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
     * Strategy: Bridge short interruptions (bathroom breaks < 30 mins) to prevent premature T-Wake anchoring.
     * @return Pair<Instant, Boolean>? The end time and a flag indicating if interruptions were bridged.
     */
    suspend fun fetchLatestTWake(): Pair<Instant, Boolean>? {
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
            if (response.records.isEmpty()) return null

            // 1. Sort records by start time
            val sortedRecords = response.records.sortedBy { it.startTime }

            // 2. Heal/Bridge sessions with < 30 min gaps
            val consolidatedSessions = healSleepSessions(sortedRecords)
            val wereInterruptions = sortedRecords.size > consolidatedSessions.size

            // 3. Filtering for valid sessions (at least 3 hours consolidated)
            val bestSession = consolidatedSessions
                .filter { ChronoUnit.MINUTES.between(it.first, it.second) > 180 }
                .maxByOrNull { it.second } ?: return null
                
            return Pair(bestSession.second, wereInterruptions)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Consolidates multiple sleep sessions into single blocks if the gap between them is < 30 mins.
     * @return List<Pair<Instant, Instant>> A list of start/end time pairs representing bridged sleep blocks.
     */
    internal fun healSleepSessions(records: List<SleepSessionRecord>): List<Pair<Instant, Instant>> {
        if (records.isEmpty()) return emptyList()
        
        val merged = mutableListOf<Pair<Instant, Instant>>()
        var currentStart = records[0].startTime
        var currentEnd = records[0].endTime

        for (i in 1 until records.size) {
            val nextStart = records[i].startTime
            val nextEnd = records[i].endTime

            val gapMinutes = ChronoUnit.MINUTES.between(currentEnd, nextStart)
            
            if (gapMinutes < 30) {
                // Bridge the gap - extend current session
                currentEnd = nextEnd
            } else {
                // Gap too large - close current session and start new one
                merged.add(Pair(currentStart, currentEnd))
                currentStart = nextStart
                currentEnd = nextEnd
            }
        }
        merged.add(Pair(currentStart, currentEnd))
        return merged
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
