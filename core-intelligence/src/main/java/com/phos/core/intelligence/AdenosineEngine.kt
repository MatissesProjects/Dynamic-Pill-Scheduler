package com.phos.core.intelligence

import com.phos.core.data.model.CaffeineLog
import androidx.health.connect.client.records.SleepSessionRecord
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.math.exp
import kotlin.math.ln

data class SleepPressureState(
    val actualPressure: Double, // 0.0 to 1.0
    val feltPressure: Double,   // 0.0 to 1.0 (adjusted for caffeine)
    val caffeinePlasmaMgL: Double,
    val receptorOccupancy: Double, // 0.0 to 1.0
    val napPropensityScore: Int // 0 to 100
)

class AdenosineEngine {

    // Time constants from clinical literature
    private val TAU_WAKE = 18.2 // hours
    private val TAU_SLEEP = 4.2 // hours
    private val CAFFEINE_HALF_LIFE = 5.0 // hours
    private val EC50_CAFFEINE = 2.5 // mg/L average for A1/A2a
    private val MAX_OCCUPANCY = 0.8

    /**
     * Models the current state of homeostatic sleep pressure and caffeine interference.
     */
    fun calculateCurrentState(
        tWakeEpoch: Long,
        caffeineLogs: List<CaffeineLog>,
        now: Instant = Instant.now()
    ): SleepPressureState {
        val hoursSinceWake = Duration.between(Instant.ofEpochMilli(tWakeEpoch), now).toMillis() / 3600000.0
        
        // 1. Calculate Actual Sleep Pressure (Process S)
        // S(t) = 1.0 - (1.0 - S0) * exp(-t / TAU_WAKE)
        // Assuming S0 = 0.0 for a fully rested wake-up.
        val actualS = 1.0 - exp(-hoursSinceWake / TAU_WAKE)

        // 2. Calculate Plasma Caffeine Concentration
        // C(t) = C0 * exp(-t * ln(2) / HALF_LIFE)
        // mg/L approximation: 100mg caffeine -> ~3.5 mg/L peak
        var currentPlasmaMgL = 0.0
        caffeineLogs.forEach { log ->
            val hoursAgo = Duration.between(log.timestamp, now).toMillis() / 3600000.0
            if (hoursAgo >= 0) {
                val peakMgL = (log.mg.toDouble() / 100.0) * 3.5
                currentPlasmaMgL += peakMgL * exp(-hoursAgo * ln(2.0) / CAFFEINE_HALF_LIFE)
            }
        }

        // 3. Calculate Receptor Occupancy
        val occupancy = MAX_OCCUPANCY * (currentPlasmaMgL / (currentPlasmaMgL + EC50_CAFFEINE))

        // 4. Calculate Felt Pressure
        val feltS = actualS * (1.0 - occupancy)

        return SleepPressureState(
            actualPressure = actualS,
            feltPressure = feltS,
            caffeinePlasmaMgL = currentPlasmaMgL,
            receptorOccupancy = occupancy,
            napPropensityScore = (feltS * 100.0).toInt().coerceIn(0, 100)
        )
    }
}
