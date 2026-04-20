package com.phos.core.data.engine

import com.phos.core.data.dao.TemporalAnchorDao
import com.phos.core.data.model.TemporalAnchor
import com.phos.core.data.sync.HealthSyncManager
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AnchorManager(
    private val healthSyncManager: HealthSyncManager,
    private val temporalAnchorDao: TemporalAnchorDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())

    /**
     * Synchronizes the T-Wake anchor with the latest data from Health Connect.
     */
    suspend fun syncTWakeFromHealthConnect(): Boolean {
        val latestWakeInstant = healthSyncManager.fetchLatestTWake() ?: return false
        
        val date = dateFormatter.format(latestWakeInstant)
        val anchor = TemporalAnchor(
            date = date,
            wakeTime = latestWakeInstant.toEpochMilli(),
            source = "HealthConnect"
        )
        
        temporalAnchorDao.insertAnchor(anchor)
        return true
    }
}
