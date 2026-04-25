package com.phos.core.data.engine

import com.phos.core.data.dao.NocturiaDao
import com.phos.core.data.dao.TemporalAnchorDao
import com.phos.core.data.model.NocturiaLog
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
    private val dataLayerRepository: DataLayerRepository,
    private val nocturiaDao: NocturiaDao
) {
    private val syncMutex = Mutex()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())

    /**
     * Synchronizes the T-Wake anchor with the latest data from Health Connect.
     */
    suspend fun syncTWakeFromHealthConnect(): Boolean = syncMutex.withLock {
        val result = healthSyncManager.fetchLatestTWakeFull() ?: return false
        val latestWakeInstant = result.first
        val wasInterrupted = result.second
        val bridgedGaps = result.third
        
        // Staleness Check: Ignore data older than 12 hours
        if (latestWakeInstant.isBefore(Instant.now().minus(12, ChronoUnit.HOURS))) {
            return false
        }

        val date = dateFormatter.format(latestWakeInstant)
        val epochMillis = latestWakeInstant.toEpochMilli()

        val anchor = TemporalAnchor(
            date = date,
            wakeTime = epochMillis,
            source = "HealthConnect",
            wasInterrupted = wasInterrupted
        )
        
        // 1. Persist locally in Room
        temporalAnchorDao.insertAnchor(anchor)

        // 2. Log Nocturia (Bathroom Breaks)
        bridgedGaps.forEach { gap ->
            nocturiaDao.insertNocturiaLog(NocturiaLog(
                startTime = gap.first,
                endTime = gap.second,
                durationMinutes = ChronoUnit.MINUTES.between(gap.first, gap.second),
                isAutomatic = true,
                notes = "Auto-detected during sleep synchronization."
            ))
        }

        // 3. Sync to Wear OS via DataLayer
        dataLayerRepository.updateTWake(epochMillis, wasInterrupted)
        
        return true
    }
}
