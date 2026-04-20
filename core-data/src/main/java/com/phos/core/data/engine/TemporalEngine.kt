package com.phos.core.data.engine

import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor

class TemporalEngine {
    /**
     * Calculates the scheduled time for a medication based on the T-Wake anchor.
     */
    fun calculateScheduledTime(medication: MedicationRecord, anchor: TemporalAnchor): Long {
        return anchor.wakeTime + medication.frequencyOffset
    }

    /**
     * Determines if a medication is within its "Fuzzy Window" (e.g., +/- 30 mins).
     */
    fun isWithinFuzzyWindow(scheduledTime: Long, currentTime: Long, windowMillis: Long = 30 * 60 * 1000): Boolean {
        return currentTime >= (scheduledTime - windowMillis) && currentTime <= (scheduledTime + windowMillis)
    }
}
