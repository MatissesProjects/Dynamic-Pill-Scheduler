package com.phos.core.data.engine

import androidx.health.connect.client.records.*
import java.time.Instant

enum class CyclingType {
    TRADITIONAL,
    EBIKE,
    UNKNOWN
}

data class NormalizedEffort(
    val type: CyclingType,
    val normalizedCardioLoad: Double,
    val wasHighIntensity: Boolean
)

class EbikeEffortNormalizer {

    /**
     * Detects if a cycling session was likely assisted (e-bike) and normalizes exertion.
     */
    fun normalizeEffort(
        session: ExerciseSessionRecord,
        powerSamples: List<PowerRecord.Sample>,
        hrSamples: List<HeartRateRecord.Sample>,
        cadenceSamples: List<CyclingPedalingCadenceRecord.Sample>
    ): NormalizedEffort {
        // Fallback to integer check if constant is unresolved in some environments
        val isCycling = session.exerciseType == 8 // standard cycling type id
                        
        if (!isCycling) {
            return NormalizedEffort(CyclingType.UNKNOWN, 0.0, false)
        }

        val avgHr = if (hrSamples.isNotEmpty()) hrSamples.map { it.beatsPerMinute }.average() else 0.0
        
        // Simplified heuristic since alpha properties are shifting
        // If we have power and HR is low, it's likely e-bike
        val hasPower = powerSamples.isNotEmpty()
        val isLikelyEbike = hasPower && avgHr < 125
        
        val type = if (isLikelyEbike) CyclingType.EBIKE else CyclingType.TRADITIONAL
        val rawLoad = if (avgHr > 0) (avgHr / 190.0) * 100.0 else 0.0
        val normalizedLoad = if (isLikelyEbike) rawLoad * 0.7 else rawLoad

        return NormalizedEffort(
            type = type,
            normalizedCardioLoad = normalizedLoad,
            wasHighIntensity = avgHr > 150
        )
    }
}
