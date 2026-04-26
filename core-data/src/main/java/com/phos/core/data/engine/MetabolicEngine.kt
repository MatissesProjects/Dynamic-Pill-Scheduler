package com.phos.core.data.engine

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import com.phos.core.data.model.MetabolicLoadLog
import java.time.Duration
import java.time.Instant

class MetabolicEngine {

    /**
     * Calculates Training Impulse (TRIMP) using Edwards' method.
     * Weights: 50-60% (1x), 60-70% (2x), 70-80% (3x), 80-90% (4x), 90-100% (5x)
     */
    fun calculateMetabolicLoad(session: ExerciseSessionRecord, hrSamples: List<HeartRateRecord.Sample>): MetabolicLoadLog {
        if (hrSamples.isEmpty()) {
            return MetabolicLoadLog(
                exerciseSessionId = session.metadata.id,
                trimpScore = 0.0,
                avgHeartRate = 0.0,
                durationMinutes = Duration.between(session.startTime, session.endTime).toMinutes()
            )
        }

        val avgHr = hrSamples.map { it.beatsPerMinute }.average()
        val durationMins = Duration.between(session.startTime, session.endTime).toMinutes()
        
        // Simplified Edwards TRIMP approximation
        // Max HR assumed 190 if not known. 
        val intensity = avgHr / 190.0
        val weight = when {
            intensity >= 0.9 -> 5.0
            intensity >= 0.8 -> 4.0
            intensity >= 0.7 -> 3.0
            intensity >= 0.6 -> 2.0
            else -> 1.0
        }
        
        val trimp = durationMins * weight
        
        return MetabolicLoadLog(
            exerciseSessionId = session.metadata.id,
            trimpScore = trimp,
            avgHeartRate = avgHr,
            durationMinutes = durationMins,
            timestamp = session.endTime,
            isHyperMetabolic = trimp > 100.0 // Threshold for significant metabolic strain
        )
    }
}
