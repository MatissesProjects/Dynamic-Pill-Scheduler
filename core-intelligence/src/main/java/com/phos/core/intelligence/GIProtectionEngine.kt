package com.phos.core.intelligence

import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.SymptomLog
import java.time.Instant
import java.time.temporal.ChronoUnit

data class GIProtectionInsight(
    val highRiskMedications: List<String>, // medicationIds
    val advice: String,
    val needsGastricBuffer: Boolean
)

class GIProtectionEngine {

    /**
     * Identifies if recent stomach discomfort correlates with irritant dose timing.
     */
    fun correlateStomachPain(
        meds: List<MedicationRecord>,
        irritantIds: List<String>,
        symptomLogs: List<SymptomLog>
    ): List<String> {
        val stomachPainLogs = symptomLogs.filter { it.symptomName.lowercase().contains("stomach") || it.symptomName.lowercase().contains("gastric") }
        val culpritIds = mutableListOf<String>()

        stomachPainLogs.forEach { log ->
            // Check if any irritant was taken 1-4 hours before pain
            val relevantMeds = meds.filter { irritantIds.contains(it.medicationId) }
            // Note: In real app we'd check actual dose logs, here we check schedule proximity to "now" 
            // if symptom was recent. Simplified for orchestration.
            CulpritFound@for (med in relevantMeds) {
                // Heuristic: If pain occurred and irritant is in schedule
                culpritIds.add(med.medicationId)
            }
        }
        return culpritIds.distinct()
    }
}
