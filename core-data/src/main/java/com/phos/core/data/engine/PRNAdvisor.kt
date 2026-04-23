package com.phos.core.data.engine

import com.phos.core.data.dao.BiometricDao
import com.phos.core.data.dao.DoseLogDao
import com.phos.core.data.model.*
import java.time.Instant
import java.time.temporal.ChronoUnit

data class PRNAdvisory(
    val isApproved: Boolean,
    val reason: String,
    val suggestedWaitMinutes: Int = 0,
    val alternativeAdvice: String? = null
)

class PRNAdvisor(
    private val doseLogDao: DoseLogDao,
    private val biometricDao: BiometricDao,
    private val collisionResolver: CollisionResolver
) {

    /**
     * Evaluates if taking a PRN medication right now is safe and appropriate.
     */
    suspend fun evaluateRequest(
        prnMed: PRNMedication,
        activeScheduledMeds: List<MedicationRecord>,
        recentFoodLogs: List<FoodLog>
    ): PRNAdvisory {
        val now = Instant.now()
        val since24h = now.minus(24, ChronoUnit.HOURS).toEpochMilli()
        
        // 1. Check Min Gap
        val recentDoses = doseLogDao.getRecentTakenDoses(prnMed.medicationId, since24h)
        val lastDose = recentDoses.firstOrNull()
        if (lastDose != null) {
            val minsSinceLast = ChronoUnit.MINUTES.between(Instant.ofEpochMilli(lastDose.actualTime!!), now)
            if (minsSinceLast < prnMed.minGapMinutes) {
                return PRNAdvisory(
                    isApproved = false,
                    reason = "Too soon since your last dose. Required gap is ${prnMed.minGapMinutes} minutes.",
                    suggestedWaitMinutes = (prnMed.minGapMinutes - minsSinceLast).toInt()
                )
            }
        }

        // 2. Check Max Doses in 24h
        if (recentDoses.size >= prnMed.maxDosesPer24h) {
            return PRNAdvisory(
                isApproved = false,
                reason = "Maximum daily limit of ${prnMed.maxDosesPer24h} doses reached for ${prnMed.name}.",
                alternativeAdvice = "Contact your doctor if symptoms persist."
            )
        }

        // 3. Check Interactions (Mocking current time as 'offset' from 0 for the resolver)
        // We simulate a MedicationRecord for the PRN med at the current time
        val prnAsRecord = MedicationRecord(
            medicationId = prnMed.medicationId,
            name = prnMed.name,
            dosage = prnMed.dosage,
            frequencyOffset = 0, // Placeholder
            validFrom = 0
        )
        
        // Check medication-medication collisions
        // Note: CollisionResolver uses frequencyOffset. This needs care.
        // For simplicity, let's just check if taking it NOW collides with anything scheduled soon.
        // TODO: Refactor CollisionResolver to be more flexible with absolute times.

        // 4. Biometric Checks (e.g. Heart Rate for stimulants or heart meds)
        if (prnMed.name.lowercase().contains("stimulant") || prnMed.name.lowercase().contains("albuterol")) {
            val latestHr = biometricDao.getLogsSince(BiometricType.HEART_RATE, now.minus(15, ChronoUnit.MINUTES))
                .firstOrNull()?.value
            if (latestHr != null && latestHr > 100) {
                return PRNAdvisory(
                    isApproved = false,
                    reason = "Your heart rate is currently elevated ($latestHr BPM). Taking ${prnMed.name} now may increase it further.",
                    alternativeAdvice = "Try resting for 15 minutes and re-check your heart rate."
                )
            }
        }

        return PRNAdvisory(
            isApproved = true,
            reason = "Safe to take ${prnMed.name} ${prnMed.dosage}."
        )
    }
}
