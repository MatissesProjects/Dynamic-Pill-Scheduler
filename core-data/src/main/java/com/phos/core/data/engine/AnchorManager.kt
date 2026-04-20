package com.phos.core.data.engine

import com.phos.core.data.dao.TemporalAnchorDao
import com.phos.core.data.model.TemporalAnchor
import com.phos.core.data.sync.DataLayerRepository
import com.phos.core.data.sync.HealthSyncManager
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AnchorManager(
    private val healthSyncManager: HealthSyncManager,
    private val temporalAnchorDao: TemporalAnchorDao,
    private val dataLayerRepository: DataLayerRepository
) {
    private val syncMutex = Mutex()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())

    /**
     * Synchronizes the T-Wake anchor with the latest data from Health Connect.
     */
    suspend fun syncTWakeFromHealthConnect(): Boolean = syncMutex.withLock {
        val latestWakeInstant = healthSyncManager.fetchLatestTWake() ?: return false
        
        // Staleness Check: Ignore data older than 12 hours
        if (latestWakeInstant.isBefore(Instant.now().minus(12, ChronoUnit.HOURS))) {
            return false
        }

        val date = dateFormatter.format(latestWakeInstant)
        val epochMillis = latestWakeInstant.toEpochMilli()

        val anchor = TemporalAnchor(
            date = date,
            wakeTime = epochMillis,
            source = "HealthConnect"
        )
        
        // 1. Persist locally in Room
        temporalAnchorDao.insertAnchor(anchor)

        // 2. Sync to Wear OS via DataLayer
        dataLayerRepository.updateTWake(epochMillis)
        
        return true
    }
}
