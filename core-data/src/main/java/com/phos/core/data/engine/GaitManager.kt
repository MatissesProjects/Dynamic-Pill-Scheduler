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
     * Placeholder for gait sync.
     */
    suspend fun syncGaitMetrics() {
        // Temporarily disabled due to shifting Health Connect Alpha API for Wear OS 5 metrics
    }

    /**
     * Compares recent gait (last 24h) against a 7-day baseline.
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
