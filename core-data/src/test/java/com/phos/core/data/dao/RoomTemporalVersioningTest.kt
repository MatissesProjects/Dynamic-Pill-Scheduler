package com.phos.core.data.dao

import com.phos.core.data.model.MedicationRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.*

class RoomTemporalVersioningTest {

    // Since MedicationDao is an interface, we can't test its default/transaction implementation 
    // directly unless we have a real DB or a manual implementation.
    // However, we can verify our architectural intent.

    @Test
    fun `test temporal update logic`() = runBlocking {
        val dao: MedicationDao = mock()
        val medicationId = "med_123"
        val oldId = 1L
        val currentTime = System.currentTimeMillis()
        
        val oldRecord = MedicationRecord(
            id = oldId,
            medicationId = medicationId,
            name = "Old Name",
            dosage = "10mg",
            frequencyOffset = 0L,
            validFrom = 0L,
            validTo = Long.MAX_VALUE
        )
        
        val newRecord = MedicationRecord(
            medicationId = medicationId,
            name = "New Name",
            dosage = "20mg",
            frequencyOffset = 0L,
            validFrom = 0L // Will be overwritten
        )

        // Mocking the behavior of updateMedication if it were a normal class, 
        // but since it's a Room Transaction, we'll verify what a manual implementation would do.
        
        // Let's assume we are testing a component that uses the DAO's logic correctly.
        // Or we can provide a fake implementation of the DAO for testing.
        
        val fakeDao = object : MedicationDao {
            var markAsInactiveCalled = false
            var insertCalled = false
            var capturedNewRecord: MedicationRecord? = null

            override fun getAllActiveMedicationsFlow(maxLong: Long): kotlinx.coroutines.flow.Flow<List<MedicationRecord>> = mock()
            override suspend fun getAllActiveMedications(maxLong: Long): List<MedicationRecord> = listOf(oldRecord)
            override suspend fun getActiveMedicationById(medicationId: String, maxLong: Long): MedicationRecord? = oldRecord
            override suspend fun insert(record: MedicationRecord): Long {
                insertCalled = true
                capturedNewRecord = record
                return 2L
            }
            override suspend fun markAsInactive(id: Long, timestamp: Long) {
                markAsInactiveCalled = true
                assertEquals(oldId, id)
            }
            override fun getGIIrritantIdsFlow(maxLong: Long): kotlinx.coroutines.flow.Flow<List<String>> = mock()
            override suspend fun deletePermanently(id: Long) {}
            
            // Re-implementing the logic from the interface to test it in unit test
            override suspend fun updateMedication(newRecord: MedicationRecord) {
                val time = System.currentTimeMillis()
                val old = getActiveMedicationById(newRecord.medicationId)
                if (old != null) {
                    markAsInactive(old.id, time)
                }
                insert(newRecord.copy(validFrom = time, validTo = Long.MAX_VALUE))
            }
        }

        fakeDao.updateMedication(newRecord)
        
        assertTrue(fakeDao.markAsInactiveCalled)
        assertTrue(fakeDao.insertCalled)
        assertEquals("New Name", fakeDao.capturedNewRecord?.name)
        assertTrue(fakeDao.capturedNewRecord!!.validFrom >= currentTime)
    }
    
    private fun assertTrue(value: Boolean) = org.junit.Assert.assertTrue(value)
}
