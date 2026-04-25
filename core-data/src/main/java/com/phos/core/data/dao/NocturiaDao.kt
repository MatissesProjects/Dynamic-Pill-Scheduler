package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phos.core.data.model.NocturiaLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface NocturiaDao {
    @Insert
    suspend fun insertNocturiaLog(log: NocturiaLog)

    @Query("SELECT * FROM nocturia_logs WHERE startTime >= :since ORDER BY startTime DESC")
    fun getNocturiaLogsSince(since: Instant): Flow<List<NocturiaLog>>

    @Query("SELECT COUNT(*) FROM nocturia_logs WHERE startTime >= :since")
    suspend fun getNocturiaCountSince(since: Instant): Int
}
