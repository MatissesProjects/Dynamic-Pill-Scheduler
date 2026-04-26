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
     * Logic: If Power/Cadence is high but HR is relatively low, it's an e-bike.
     */
    fun normalizeEffort(
        session: ExerciseSessionRecord,
        powerSamples: List<PowerRecord.Sample>,
        hrSamples: List<HeartRateRecord.Sample>,
        cadenceSamples: List<CyclingPedalingCadenceRecord.Sample>
    ): NormalizedEffort {
        if (session.exerciseType != ExerciseSessionRecord.EXERCISE_TYPE_CYCLING) {
            return NormalizedEffort(CyclingType.UNKNOWN, 0.0, false)
        }

        val avgPower = if (powerSamples.isNotEmpty()) powerSamples.map { it.power.inWatts }.average() else 0.0
        val avgHr = if (hrSamples.isNotEmpty()) hrSamples.map { it.beatsPerMinute }.average() else 0.0
        val avgCadence = if (cadenceSamples.isNotEmpty()) cadenceSamples.map { it.samplesPerMinute }.average() else 0.0

        // Heuristic: E-bike rides often have high cadence and moderate power but blunted heart rate 
        // compared to the mechanical work being done.
        // For a traditional bike, 150W at 80RPM would typically yield > 140 BPM for an average user.
        // If HR is < 120 despite high power/cadence, it's likely e-bike assisted.
        
        val isLikelyEbike = avgPower > 100 && avgHr < 130 && avgCadence > 60
        
        val type = if (isLikelyEbike) CyclingType.EBIKE else CyclingType.TRADITIONAL
        
        // Normalize effort: E-bike TRIMP is typically 70% of traditional for the same duration/cadence
        val rawLoad = if (avgHr > 0) (avgHr / 190.0) * 100.0 else 0.0
        val normalizedLoad = if (isLikelyEbike) rawLoad * 0.7 else rawLoad

        return NormalizedEffort(
            type = type,
            normalizedCardioLoad = normalizedLoad,
            wasHighIntensity = avgHr > 150
        )
    }
}
