package com.phos.core.data.engine

import java.time.Instant

data class SuppressionState(
    val isSuppressed: Boolean,
    val reason: String? = null,
    val suggestedSnoozeMinutes: Int = 0
)

class PhysiologicalSuppressor {

    private val MAX_SAFE_HR = 100 // BPM threshold for suppression
    private val MIN_SNOOZE_MINUTES = 15

    /**
     * Determines if a medication notification should be suppressed based on physiological data.
     */
    fun shouldSuppressNotification(currentHeartRate: Int, medicationName: String): SuppressionState {
        // Rule: If HR is elevated (Exercise/Stress), suppress non-critical alerts to avoid "Notification Fatigue" 
        // or potentially interfering with the medication's absorption/effect in a high-metabolic state.
        if (currentHeartRate > MAX_SAFE_HR) {
            return SuppressionState(
                isSuppressed = true,
                reason = "High Heart Rate ($currentHeartRate BPM) detected. Meds suppressed to ensure physiological stability.",
                suggestedSnoozeMinutes = MIN_SNOOZE_MINUTES
            )
        }

        return SuppressionState(isSuppressed = false)
    }

    /**
     * More complex check considering the specific medication type.
     * Some meds (like Beta Blockers) might be even more sensitive to high HR.
     */
    fun shouldSuppressWithContext(
        currentHeartRate: Int,
        medicationId: String,
        medicationName: String
    ): SuppressionState {
        // Example: If it's a Beta Blocker and HR is high, we definitely want to wait until HR is closer to baseline.
        if (medicationId.contains("beta_blocker", ignoreCase = true) && currentHeartRate > 90) {
             return SuppressionState(
                isSuppressed = true,
                reason = "Beta Blocker ($medicationName) held due to elevated Heart Rate ($currentHeartRate BPM).",
                suggestedSnoozeMinutes = 20
            )
        }

        return shouldSuppressNotification(currentHeartRate, medicationName)
    }
}
