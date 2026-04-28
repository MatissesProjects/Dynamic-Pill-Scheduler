package com.phos.core.data.engine

import com.phos.core.data.model.*
import java.time.Duration
import java.time.Instant
import kotlin.math.max

class MetabolicClearanceEngine {

    /**
     * T42: Alcohol & Metabolic Load Synthesis
     * Calculates Estimated Blood Alcohol Concentration (EBAC) using Widmark formula.
     * Formula: EBAC = [ (0.806 * SD * 1.2) / (BW * Wt) ] - (MR * DP)
     * SD: Standard Drinks (14g ethanol)
     * BW: Body Water constant (0.58 for male, 0.49 for female)
     * Wt: Body Weight (kg)
     * MR: Metabolism Rate (0.015% per hour)
     * DP: Drinking Period (hours)
     */
    fun calculateEBAC(
        logs: List<AlcoholLog>,
        profile: UserProfile,
        currentTime: Instant = Instant.now()
    ): Double {
        if (logs.isEmpty()) return 0.0

        val firstDrinkTime = logs.minOf { it.timestamp }
        val drinkingPeriodHours = Duration.between(firstDrinkTime, currentTime).toMinutes() / 60.0
        val totalStandardDrinks = logs.sumOf { it.estimatedStandardDrinks }

        val bodyWaterConstant = when (profile.gender) {
            Gender.MALE -> 0.58
            Gender.FEMALE -> 0.49
            Gender.OTHER -> 0.535 // Average
        }

        val grossEbac = (0.806 * totalStandardDrinks * 1.2) / (bodyWaterConstant * profile.weightKg)
        val metabolicReduction = 0.015 * drinkingPeriodHours

        return max(0.0, grossEbac - metabolicReduction)
    }

    /**
     * Adjusts the effective half-life of a medication based on BAC and metabolic competition.
     * If the liver is busy with alcohol (BAC > 0), CYP450 metabolism for medications can be delayed.
     */
    fun calculateEffectiveHalfLife(
        baseHalfLifeMins: Int,
        currentBAC: Double,
        medicationPathway: String?
    ): Int {
        if (currentBAC <= 0.0 || medicationPathway == null) return baseHalfLifeMins

        // Heuristic: BAC of 0.08 (limit) results in a 50% increase in half-life for CYP450 drugs
        val multiplier = 1.0 + (currentBAC / 0.08) * 0.5
        return (baseHalfLifeMins * multiplier).toInt()
    }

    /**
     * Predicts when BAC will return to zero.
     */
    fun estimateTimeToSobriety(currentBAC: Double): Duration {
        val hours = currentBAC / 0.015
        return Duration.ofMinutes((hours * 60).toLong())
    }
}
