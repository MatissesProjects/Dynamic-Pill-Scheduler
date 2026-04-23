package com.phos.core.data.engine

import com.phos.core.data.dao.TemporalAnchorDao
import com.phos.core.data.sync.DataLayerRepository
import com.phos.core.data.sync.HealthSyncManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.time.Instant
import java.time.temporal.ChronoUnit

class AnchorManagerTest {

    private lateinit var healthSyncManager: HealthSyncManager
    private lateinit var temporalAnchorDao: TemporalAnchorDao
    private lateinit var dataLayerRepository: DataLayerRepository
    private lateinit var anchorManager: AnchorManager

    @Before
    fun setup() {
        healthSyncManager = mock()
        temporalAnchorDao = mock()
        dataLayerRepository = mock()
        anchorManager = AnchorManager(healthSyncManager, temporalAnchorDao, dataLayerRepository)
    }

    @Test
    fun `test syncTWakeFromHealthConnect success`() = runBlocking {
        val now = Instant.now()
        whenever(healthSyncManager.fetchLatestTWake()).thenReturn(now)

        val result = anchorManager.syncTWakeFromHealthConnect()

        assertTrue(result)
        verify(temporalAnchorDao).insertAnchor(any())
        verify(dataLayerRepository).updateTWake(eq(now.toEpochMilli()))
    }

    @Test
    fun `test syncTWakeFromHealthConnect ignores stale data`() = runBlocking {
        val stale = Instant.now().minus(13, ChronoUnit.HOURS)
        whenever(healthSyncManager.fetchLatestTWake()).thenReturn(stale)

        val result = anchorManager.syncTWakeFromHealthConnect()

        assertFalse(result)
        verifyNoInteractions(temporalAnchorDao)
        verifyNoInteractions(dataLayerRepository)
    }

    @Test
    fun `test syncTWakeFromHealthConnect handles null`() = runBlocking {
        whenever(healthSyncManager.fetchLatestTWake()).thenReturn(null)

        val result = anchorManager.syncTWakeFromHealthConnect()

        assertFalse(result)
    }
}
