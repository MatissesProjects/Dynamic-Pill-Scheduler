package com.phos.core.data.engine

import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor
import com.phos.core.data.sync.HealthSyncManager
import java.time.Instant

data class NapOverlap(
    val medicationId: String,
    val medicationName: String,
    val overlapDurationMinutes: Long,
    val suggestedShiftMillis: Long
)

class NapManager(
    private val healthSyncManager: HealthSyncManager
) {

    /**
     * Checks if any medications scheduled relative to the [anchor] overlap with the latest nap.
     * If so, returns suggestions to shift those and subsequent meds.
     */
    suspend fun checkNapOverlaps(
        medications: List<MedicationRecord>,
        anchor: TemporalAnchor
    ): List<NapOverlap> {
        val nap = healthSyncManager.fetchLatestNap() ?: return emptyList()
        val napStart = nap.startTime.toEpochMilli()
        val napEnd = nap.endTime.toEpochMilli()
        
        val overlaps = mutableListOf<NapOverlap>()
        
        for (med in medications) {
            val scheduledTime = anchor.wakeTime + med.frequencyOffset
            
            // Check if scheduled dose falls within nap window
            if (scheduledTime in napStart..napEnd) {
                val delayNeeded = napEnd - scheduledTime
                overlaps.add(NapOverlap(
                    medicationId = med.medicationId,
                    medicationName = med.name,
                    overlapDurationMinutes = (napEnd - napStart) / 60000,
                    suggestedShiftMillis = delayNeeded + (15 * 60000) // Shift to 15 mins after nap end
                ))
            }
        }
        
        return overlaps
    }
}
