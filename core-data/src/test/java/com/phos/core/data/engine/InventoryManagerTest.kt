package com.phos.core.data.engine

import com.phos.core.data.dao.InventoryDao
import com.phos.core.data.model.InventoryRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class InventoryManagerTest {

    private lateinit var inventoryDao: InventoryDao
    private lateinit var manager: InventoryManager

    @Before
    fun setup() {
        inventoryDao = mock()
        manager = InventoryManager(inventoryDao)
    }

    @Test
    fun `test processDoseTaken decrements count`() = runBlocking {
        val medId = "med_001"
        val record = InventoryRecord(medId, currentPillCount = 30, refillThreshold = 5, pillsPerDose = 1)
        
        whenever(inventoryDao.getInventoryForMedication(medId)).thenReturn(record)
        
        manager.processDoseTaken(medId)
        
        verify(inventoryDao).decrementPillCount(medId, 1)
    }

    @Test
    fun `test predictDepletionDate calculates correctly`() = runBlocking {
        val medId = "med_001"
        val record = InventoryRecord(medId, currentPillCount = 20, refillThreshold = 5, pillsPerDose = 2)
        
        whenever(inventoryDao.getInventoryForMedication(medId)).thenReturn(record)
        
        // 20 pills / (2 pills/dose * 2 doses/day) = 5 days
        val depletionDate = manager.predictDepletionDate(medId, dosesPerDay = 2)
        
        assertNotNull(depletionDate)
        val daysDiff = ChronoUnit.DAYS.between(Instant.now(), depletionDate)
        assertEquals(5, daysDiff)
    }

    @Test
    fun `test isRefillNeeded triggers at threshold`() = runBlocking {
        val medId = "med_001"
        
        whenever(inventoryDao.getInventoryForMedication(medId)).thenReturn(
            InventoryRecord(medId, currentPillCount = 5, refillThreshold = 5)
        )
        assertTrue(manager.isRefillNeeded(medId))
        
        whenever(inventoryDao.getInventoryForMedication(medId)).thenReturn(
            InventoryRecord(medId, currentPillCount = 6, refillThreshold = 5)
        )
        assertFalse(manager.isRefillNeeded(medId))
    }
}
