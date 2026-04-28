package com.phos.core.data.model

import java.time.Instant

data class DailyReadiness(
    val date: String,
    val score: Int, // 0-100
    val hrvFactor: Double, // Normalized 0-1
    val rhrFactor: Double, // Normalized 0-1
    val sleepFactor: Double, // Normalized 0-1
    val recommendation: String
)

data class CardioMismatchInsight(
    val timestamp: Long,
    val stepRate: Double, // Steps per minute
    val heartRate: Double, // BPM
    val rhr: Double,
    val mismatchIntensity: Double, // 0-1
    val isSignificant: Boolean
)

enum class ActivityScaling {
    RECOVERY, // 0-40% effort
    MODERATE, // 40-70% effort
    OPTIMAL   // 70-100% effort
}
