package com.phos.core.data.sync

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthSyncManager(private val context: Context) {

    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(BodyTemperatureRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class),
        HealthPermission.getReadPermission(RunningStrideLengthRecord::class),
        HealthPermission.getReadPermission(StepsCadenceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(PowerRecord::class),
        HealthPermission.getReadPermission(CyclingPedalingCadenceRecord::class)
    )

    suspend fun hasPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Fetches the latest sleep session end time to use as T-Wake.
     */
    suspend fun fetchLatestTWakeFull(): Triple<Instant, Boolean, List<Pair<Instant, Instant>>>? {
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

            val sortedRecords = response.records.sortedBy { it.startTime }
            val result = healSleepSessionsWithGaps(sortedRecords)
            val consolidatedSessions = result.first
            val bridgedGaps = result.second
            
            val wereInterruptions = bridgedGaps.isNotEmpty()

            val bestSession = consolidatedSessions
                .filter { ChronoUnit.MINUTES.between(it.first, it.second) > 180 }
                .maxByOrNull { it.second } ?: return null
                
            return Triple(bestSession.second, wereInterruptions, bridgedGaps)
        } catch (e: Exception) {
            return null
        }
    }

    internal fun healSleepSessionsWithGaps(records: List<SleepSessionRecord>): Pair<List<Pair<Instant, Instant>>, List<Pair<Instant, Instant>>> {
        if (records.isEmpty()) return Pair(emptyList(), emptyList())
        
        val merged = mutableListOf<Pair<Instant, Instant>>()
        val gaps = mutableListOf<Pair<Instant, Instant>>()
        
        var currentStart = records[0].startTime
        var currentEnd = records[0].endTime

        for (i in 1 until records.size) {
            val nextStart = records[i].startTime
            val nextEnd = records[i].endTime
            val gapMinutes = ChronoUnit.MINUTES.between(currentEnd, nextStart)
            
            if (gapMinutes < 30) {
                gaps.add(Pair(currentEnd, nextStart))
                currentEnd = nextEnd
            } else {
                merged.add(Pair(currentStart, currentEnd))
                currentStart = nextStart
                currentEnd = nextEnd
            }
        }
        merged.add(Pair(currentStart, currentEnd))
        return Pair(merged, gaps)
    }

    suspend fun fetchLatestTWake(): Pair<Instant, Boolean>? {
        val res = fetchLatestTWakeFull() ?: return null
        return Pair(res.first, res.second)
    }

    internal fun healSleepSessions(records: List<SleepSessionRecord>): List<Pair<Instant, Instant>> {
        return healSleepSessionsWithGaps(records).first
    }

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
        } catch (e: Exception) { return null }
    }

    suspend fun fetchSleepHistory(days: Int = 14): List<SleepSessionRecord>? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minus(days.toLong(), ChronoUnit.DAYS),
                    Instant.now()
                )
            )
            return healthConnectClient.readRecords(request).records
        } catch (e: Exception) { return null }
    }

    suspend fun fetchRecentExercises(): List<ExerciseSessionRecord>? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now())
            )
            return healthConnectClient.readRecords(request).records
        } catch (e: Exception) { return null }
    }

    suspend fun fetchHeartRateForSession(startTime: Instant, endTime: Instant): List<HeartRateRecord.Sample>? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            return healthConnectClient.readRecords(request).records.flatMap { it.samples }
        } catch (e: Exception) { return null }
    }

    suspend fun fetchPowerForSession(startTime: Instant, endTime: Instant): List<PowerRecord.Sample>? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = PowerRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            return healthConnectClient.readRecords(request).records.flatMap { it.samples }
        } catch (e: Exception) { return null }
    }

    suspend fun fetchCadenceForSession(startTime: Instant, endTime: Instant): List<CyclingPedalingCadenceRecord.Sample>? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = CyclingPedalingCadenceRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            return healthConnectClient.readRecords(request).records.flatMap { it.samples }
        } catch (e: Exception) { return null }
    }

    suspend fun fetchLatestHeartRate(): List<HeartRateRecord.Sample>? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now())
            )
            return healthConnectClient.readRecords(request).records.flatMap { it.samples }
        } catch (e: Exception) { return null }
    }

    suspend fun fetchTotalStepsToday(): Long? {
        try {
            if (!hasPermissions()) return null
            val startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS)
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, Instant.now())
            )
            return healthConnectClient.readRecords(request).records.sumOf { it.count }
        } catch (e: Exception) { return null }
    }

    suspend fun fetchLatestBodyTemperature(): Double? {
        try {
            if (!hasPermissions()) return null
            val request = ReadRecordsRequest(
                recordType = BodyTemperatureRecord::class,
                timeRangeFilter = TimeRangeFilter.between(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now())
            )
            return healthConnectClient.readRecords(request).records.maxByOrNull { it.time }?.temperature?.inCelsius
        } catch (e: Exception) { return null }
    }

    suspend fun fetchGaitMetrics(): List<Pair<RunningStrideLengthRecord, StepsCadenceRecord?>>? {
        try {
            if (!hasPermissions()) return null
            val timeFilter = TimeRangeFilter.between(Instant.now().minus(24, ChronoUnit.HOURS), Instant.now())
            
            val strideRequest = ReadRecordsRequest(recordType = RunningStrideLengthRecord::class, timeRangeFilter = timeFilter)
            val cadenceRequest = ReadRecordsRequest(recordType = StepsCadenceRecord::class, timeRangeFilter = timeFilter)
            
            val strides = healthConnectClient.readRecords(strideRequest).records
            val cadences = healthConnectClient.readRecords(cadenceRequest).records
            
            return strides.map { stride ->
                val matchingCadence = cadences.minByOrNull { 
                    Math.abs(it.startTime.toEpochMilli() - stride.startTime.toEpochMilli()) 
                }
                Pair(stride, matchingCadence)
            }
        } catch (e: Exception) { return null }
    }
}
