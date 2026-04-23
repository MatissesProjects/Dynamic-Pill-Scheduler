package com.phos.core.data.engine

import java.time.Instant

data class SuppressionState(
    val isSuppressed: Boolean,
    val reason: String? = null,
    val suggestedSnoozeMinutes: Int = 0,
    val isCritical: Boolean = false
)

class PhysiologicalSuppressor {

    private val MAX_SAFE_HR = 100 // BPM threshold for suppression
    private val STRESS_HR_THRESHOLD = 90
    private val MIN_SNOOZE_MINUTES = 15

    /**
     * Determines if a medication notification should be suppressed based on physiological data.
     */
    fun shouldSuppressNotification(
        currentHeartRate: Int, 
        medicationName: String,
        isCriticalMed: Boolean = false
    ): SuppressionState {
        // If the medication is critical, we generally don't want to suppress unless HR is dangerously high.
        if (isCriticalMed && currentHeartRate < 140) {
            return SuppressionState(isSuppressed = false, isCritical = true)
        }

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
     * Advanced Fuzzy Window Optimization: 
     * Delays notifications for non-critical supplements if stress is detected.
     */
    fun getFuzzyWindowOptimization(
        currentHeartRate: Int,
        heartRateVariability: Double, // Low HRV usually indicates stress
        medicationCategory: String?
    ): SuppressionState {
        val isNonCritical = medicationCategory == "Supplements" || medicationCategory == "General"
        
        // Detection of stress state: Elevated HR and Low HRV
        val isStressed = currentHeartRate > STRESS_HR_THRESHOLD && heartRateVariability < 30.0
        
        if (isNonCritical && isStressed) {
            return SuppressionState(
                isSuppressed = true,
                reason = "Elevated physiological stress detected. Optimizing dose timing for better absorption.",
                suggestedSnoozeMinutes = 30
            )
        }
        
        return SuppressionState(isSuppressed = false)
    }

    /**
     * More complex check considering the specific medication type.
     */
    fun shouldSuppressWithContext(
        currentHeartRate: Int,
        medicationId: String,
        medicationName: String,
        category: String? = null
    ): SuppressionState {
        // Example: If it's a Beta Blocker and HR is high, we definitely want to wait until HR is closer to baseline.
        if (medicationId.contains("beta_blocker", ignoreCase = true) && currentHeartRate > 90) {
             return SuppressionState(
                isSuppressed = true,
                reason = "Beta Blocker ($medicationName) held due to elevated Heart Rate ($currentHeartRate BPM).",
                suggestedSnoozeMinutes = 20,
                isCritical = true
            )
        }

        return shouldSuppressNotification(currentHeartRate, medicationName, category == "Heart")
    }
}
