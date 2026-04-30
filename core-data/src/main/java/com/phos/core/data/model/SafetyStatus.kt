package com.phos.core.data.model

import androidx.annotation.Keep

@Keep
enum class SafetyStatus {
    /**
     * All medication schedules are adhered to, and biometric parameters are within normal ranges.
     */
    GREEN,

    /**
     * Minor adherence issues (e.g., late dose within fuzzy window) or slight physiological deviations.
     */
    YELLOW,

    /**
     * Critical safety risk: multiple missed doses, medication collisions, or severe biometric alerts.
     */
    RED
}

data class SafetyProof(
    val status: SafetyStatus,
    val proof: String, // Base64 encoded ZKP
    val timestamp: Long = System.currentTimeMillis()
)
