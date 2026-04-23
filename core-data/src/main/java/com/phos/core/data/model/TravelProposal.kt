package com.phos.core.data.model

import java.time.Instant
import java.time.ZoneId

data class TravelProposal(
    val destination: String,
    val targetZoneId: String,
    val travelDate: Instant,
    val titrationSteps: List<TitrationStep>,
    val explanation: String? = null
)

data class TitrationStep(
    val dayNumber: Int,
    val date: String, // YYYY-MM-DD
    val targetWakeTime: Long // Epoch millis
)
