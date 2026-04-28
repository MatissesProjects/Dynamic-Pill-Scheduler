package com.phos.core.intelligence

import com.phos.core.data.dao.*
import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.FoodLog
import com.phos.core.data.model.SymptomLog
import com.phos.core.data.model.DreamLog
import java.time.Instant

class VoiceLogCoordinator(
    private val doseLogDao: DoseLogDao,
    private val interactionDao: InteractionDao,
    private val medicationDao: MedicationDao,
    private val intelligenceDao: IntelligenceDao,
    private val dreamDao: DreamDao,
    private val parser: VoiceEntityParser
) {
    /**
     * Parses the spoken text and automatically logs all detected entities.
     * @return ExtractedEntities The entities that were understood and logged.
     */
    suspend fun processVoiceCommand(text: String): ExtractedEntities {
        val entities = parser.parse(text)
        val now = System.currentTimeMillis()

        // 1. Log Medications (Doses)
        entities.medications.forEach { voiceMed ->
            val medRecord = medicationDao.getActiveMedicationById(voiceMed.name.lowercase())
                ?: medicationDao.getAllActiveMedications().find { it.name.contains(voiceMed.name, ignoreCase = true) }
            
            if (medRecord != null) {
                doseLogDao.insertLog(DoseLog(
                    medicationId = medRecord.medicationId,
                    scheduledTime = now,
                    actualTime = now,
                    status = "TAKEN",
                    notes = "Logged via Voice: '$text'"
                ))
            }
        }

        // 2. Log Symptoms
        entities.symptoms.forEach { voiceSymptom ->
            intelligenceDao.insertSymptom(SymptomLog(
                symptomName = voiceSymptom.name,
                severity = voiceSymptom.severity ?: 5,
                timestamp = Instant.ofEpochMilli(now),
                notes = "Logged via Voice: '$text'"
            ))
        }

        // 3. Log Foods
        entities.foods.forEach { voiceFood ->
            interactionDao.insertFoodLog(FoodLog(
                foodId = voiceFood.name.lowercase(),
                name = voiceFood.name,
                timestamp = now
            ))
        }

        // 4. Log Dreams (T36)
        entities.dreams.forEach { voiceDream ->
            val today = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.withZone(java.time.ZoneId.systemDefault()).format(Instant.now())
            dreamDao.insertLog(DreamLog(
                date = today,
                rawText = voiceDream.description,
                intensity = 5,
                vividness = 5
            ))
        }

        return entities
    }
}
