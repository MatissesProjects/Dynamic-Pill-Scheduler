package com.phos.core.intelligence

import com.phos.core.data.dao.DoseLogDao
import com.phos.core.data.dao.InteractionDao
import com.phos.core.data.dao.IntelligenceDao
import com.phos.core.data.dao.MedicationDao
import com.phos.core.data.model.MedicationRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class VoiceLogCoordinatorTest {

    private lateinit var doseLogDao: DoseLogDao
    private lateinit var interactionDao: InteractionDao
    private lateinit var medicationDao: MedicationDao
    private lateinit var intelligenceDao: IntelligenceDao
    private lateinit var dreamDao: com.phos.core.data.dao.DreamDao
    private lateinit var parser: VoiceEntityParser
    private lateinit var coordinator: VoiceLogCoordinator

    @Before
    fun setup() {
        doseLogDao = mock()
        interactionDao = mock()
        medicationDao = mock()
        intelligenceDao = mock()
        dreamDao = mock()
        parser = mock()
        coordinator = VoiceLogCoordinator(doseLogDao, interactionDao, medicationDao, intelligenceDao, dreamDao, parser)
    }

    @Test
    fun `test processVoiceCommand logs multiple entities`() = runBlocking {
        val text = "Took lisinopril and feeling a headache"
        val entities = ExtractedEntities(
            medications = listOf(VoiceMedication("lisinopril")),
            symptoms = listOf(VoiceSymptom("headache", 3))
        )
        
        whenever(parser.parse(text)).thenReturn(entities)
        
        val medRecord = MedicationRecord(medicationId = "lis_123", name = "Lisinopril", dosage = "10mg", frequencyOffset = 0L, validFrom = 0L)
        whenever(medicationDao.getAllActiveMedications()).thenReturn(listOf(medRecord))

        val result = coordinator.processVoiceCommand(text)

        assertEquals(1, result.medications.size)
        assertEquals(1, result.symptoms.size)
        
        verify(doseLogDao).insertLog(any())
        verify(intelligenceDao).insertSymptom(any())
    }
}
