package com.phos.core.intelligence

import com.phos.core.data.model.BiometricLog
import com.phos.core.data.model.BiometricType
import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.MedicationRecord
import java.time.Instant

/**
 * Classifies medications based on their thermal side effects.
 */
enum class ThermalEffectType {
    SWEAT_INHIBITOR, // e.g., Anticholinergics
    FLUSHING_AGENT,  // e.g., Niacin, Calcium Channel Blockers
    NEUTRAL
}

/**
 * Represents a thermal risk insight.
 */
data class ThermalInsight(
    val currentTemp: Double,
    val baselineTemp: Double,
    val tempDelta: Double,
    val effectType: ThermalEffectType,
    val correlatedMedication: MedicationRecord?,
    val riskLevel: ThermalRiskLevel,
    val advice: String?
)

enum class ThermalRiskLevel {
    LOW, ELEVATED, CRITICAL
}

/**
 * Engine for detecting thermal dysregulation risks based on skin temperature and medication.
 */
class ThermalShieldEngine {

    private val SWEAT_INHIBITORS = listOf("oxybutynin", "benztropine", "atropine", "scopolamine", "glycopyrrolate")
    private val FLUSHING_AGENTS = listOf("niacin", "amlodipine", "nifedipine", "diltiazem")

    fun classifyMedication(name: String): ThermalEffectType {
        val lowerName = name.lowercase()
        return when {
            SWEAT_INHIBITORS.any { lowerName.contains(it) } -> ThermalEffectType.SWEAT_INHIBITOR
            FLUSHING_AGENTS.any { lowerName.contains(it) } -> ThermalEffectType.FLUSHING_AGENT
            else -> ThermalEffectType.NEUTRAL
        }
    }

    fun analyzeThermalRisk(
        latestTempLogs: List<BiometricLog>,
        medications: List<MedicationRecord>,
        recentDoses: List<DoseLog>,
        ambientTempCelsius: Double? = null
    ): ThermalInsight? {
        val currentTemp = latestTempLogs.lastOrNull { it.type == BiometricType.SKIN_TEMPERATURE }?.value ?: return null
        val historicalTemps = latestTempLogs.filter { it.type == BiometricType.SKIN_TEMPERATURE && it.timestamp < Instant.now().minusSeconds(3600) }
        val baselineTemp = if (historicalTemps.isNotEmpty()) historicalTemps.map { it.value }.average() else currentTemp
        val tempDelta = currentTemp - baselineTemp

        val now = System.currentTimeMillis()
        val activeDoses = recentDoses.filter { 
            it.status == "TAKEN" && it.actualTime != null && (now - it.actualTime!!) < 4 * 3600000L 
        }

        val correlatedMed = activeDoses.mapNotNull { dose ->
            val med = medications.find { it.medicationId == dose.medicationId }
            if (med != null && classifyMedication(med.name) != ThermalEffectType.NEUTRAL) med else null
        }.firstOrNull()

        val effectType = correlatedMed?.let { classifyMedication(it.name) } ?: ThermalEffectType.NEUTRAL
        
        var riskLevel = ThermalRiskLevel.LOW
        var advice: String? = null

        if (effectType == ThermalEffectType.SWEAT_INHIBITOR && tempDelta > 1.0) {
            riskLevel = if (tempDelta > 2.0 || (ambientTempCelsius ?: 0.0) > 30.0) ThermalRiskLevel.CRITICAL else ThermalRiskLevel.ELEVATED
            advice = "Your medication (${correlatedMed?.name}) inhibits sweating. Rising skin temperature detected. Move to a cool area and hydrate immediately."
        } else if (effectType == ThermalEffectType.FLUSHING_AGENT && tempDelta > 0.8) {
            riskLevel = ThermalRiskLevel.ELEVATED
            advice = "Normal flushing detected from ${correlatedMed?.name}. This is a common side effect and should subside shortly."
        } else if (tempDelta > 2.5) {
            riskLevel = ThermalRiskLevel.CRITICAL
            advice = "High skin temperature detected without clear medication link. Monitor for fever or heat exhaustion."
        }

        return ThermalInsight(
            currentTemp = currentTemp,
            baselineTemp = baselineTemp,
            tempDelta = tempDelta,
            effectType = effectType,
            correlatedMedication = correlatedMed,
            riskLevel = riskLevel,
            advice = advice
        )
    }
}
