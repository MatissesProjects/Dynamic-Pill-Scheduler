package com.phos.core.data.engine

import com.phos.core.data.dao.GaitDao
import com.phos.core.data.model.GaitLog
import com.phos.core.data.sync.HealthSyncManager
import java.time.Instant
import java.time.temporal.ChronoUnit

data class GaitDeviation(
    val dropPercentage: Double,
    val isSignificant: Boolean,
    val currentStrideMeters: Double,
    val baselineStrideMeters: Double
)

class GaitManager(
    private val gaitDao: GaitDao,
    private val healthSyncManager: HealthSyncManager
) {

    /**
     * Pulls latest gait metrics from Health Connect and saves them locally.
     */
    suspend fun syncGaitMetrics() {
        val pairs = healthSyncManager.fetchGaitMetrics() ?: return
        
        pairs.forEach { (strideRecord, cadenceRecord) ->
            val avgStride = if (strideRecord.samples.isNotEmpty()) {
                strideRecord.samples.map { it.strideLength.inMeters }.average()
            } else 0.0

            val avgCadence = if (cadenceRecord != null && cadenceRecord.samples.isNotEmpty()) {
                cadenceRecord.samples.map { it.rate }.average()
            } else 0.0

            if (avgStride > 0.0) {
                gaitDao.insertLog(GaitLog(
                    strideLengthMeters = avgStride,
                    cadenceSpm = avgCadence,
                    timestamp = strideRecord.startTime,
                    source = "HealthConnect"
                ))
            }
        }
    }

    /**
     * Compares recent gait (last 24h) against a 7-day baseline.
     * Clinical research suggests a >15% drop in stride length is significant for fall risk.
     */
    suspend fun detectGaitDeviation(): GaitDeviation? {
        val now = Instant.now()
        val baselineStart = now.minus(7, ChronoUnit.DAYS)
        val recentStart = now.minus(24, ChronoUnit.HOURS)
        
        val baselineStride = gaitDao.getAverageStrideLength(baselineStart) ?: return null
        val recentStride = gaitDao.getAverageStrideLength(recentStart) ?: return null
        
        if (baselineStride <= 0.0) return null
        
        val drop = (baselineStride - recentStride) / baselineStride
        val dropPercentage = drop * 100.0
        
        return GaitDeviation(
            dropPercentage = dropPercentage,
            isSignificant = dropPercentage >= 15.0,
            currentStrideMeters = recentStride,
            baselineStrideMeters = baselineStride
        )
    }
}
