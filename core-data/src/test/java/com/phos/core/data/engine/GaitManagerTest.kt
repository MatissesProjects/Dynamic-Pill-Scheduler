package com.phos.core.data.engine

import com.phos.core.data.dao.GaitDao
import com.phos.core.data.sync.HealthSyncManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant

class GaitManagerTest {

    private lateinit var gaitDao: GaitDao
    private lateinit var healthSyncManager: HealthSyncManager
    private lateinit var gaitManager: GaitManager

    @Before
    fun setup() {
        gaitDao = mock()
        healthSyncManager = mock()
        gaitManager = GaitManager(gaitDao, healthSyncManager)
    }

    @Test
    fun `test detectGaitDeviation returns significant when drop exceeds 15 percent`() = runBlocking {
        whenever(gaitDao.getAverageStrideLength(any())).thenReturn(1.0, 0.8) // Baseline 1.0, Recent 0.8 (20% drop)
        
        val result = gaitManager.detectGaitDeviation()
        
        assertNotNull(result)
        assertTrue(result!!.isSignificant)
        assertEquals(20.0, result.dropPercentage, 0.01)
    }

    @Test
    fun `test detectGaitDeviation returns non-significant when drop is small`() = runBlocking {
        whenever(gaitDao.getAverageStrideLength(any())).thenReturn(1.0, 0.95) // 5% drop
        
        val result = gaitManager.detectGaitDeviation()
        
        assertNotNull(result)
        assertFalse(result!!.isSignificant)
        assertEquals(5.0, result.dropPercentage, 0.01)
    }

    @Test
    fun `test detectGaitDeviation returns null when data is missing`() = runBlocking {
        whenever(gaitDao.getAverageStrideLength(any())).thenReturn(null)
        
        val result = gaitManager.detectGaitDeviation()
        
        assertNull(result)
    }
}
