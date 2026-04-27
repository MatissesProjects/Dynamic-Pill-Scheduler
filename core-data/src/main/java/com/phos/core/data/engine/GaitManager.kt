package com.phos.core.data.engine

import com.phos.core.data.dao.GaitDao
import com.phos.core.data.model.GaitLog
import com.phos.core.data.sync.HealthSyncManager
import androidx.health.connect.client.records.ExerciseSessionRecord
import java.time.Duration
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
     * Estimates stride length from recent exercise sessions (Distance / Steps).
     */
    suspend fun syncGaitMetrics() {
        val sessions = healthSyncManager.fetchRecentExercises() ?: return
        
        sessions.forEach { session ->
            // Only process running or walking for gait analysis
            if (session.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_RUNNING || 
                session.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_WALKING) {
                
                val distance = healthSyncManager.fetchDistanceForSession(session.startTime, session.endTime)
                val steps = healthSyncManager.fetchStepsForSession(session.startTime, session.endTime)
                
                if (steps > 100) { // Ensure enough data for a stable average
                    val estimatedStride = distance / steps
                    
                    gaitDao.insertLog(GaitLog(
                        strideLengthMeters = estimatedStride,
                        cadenceSpm = (steps.toDouble() / Duration.between(session.startTime, session.endTime).toMinutes()),
                        timestamp = session.endTime,
                        source = "Inferred-HealthConnect"
                    ))
                }
            }
        }
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
