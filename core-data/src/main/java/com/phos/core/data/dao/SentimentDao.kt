package com.phos.core.data.dao

import androidx.room.*
import com.phos.core.data.model.SentimentLog
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface SentimentDao {
    @Insert
    suspend fun insertLog(log: SentimentLog)

    @Query("SELECT * FROM sentiment_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<SentimentLog>>

    @Query("SELECT * FROM sentiment_logs WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getLogsSince(since: Instant): List<SentimentLog>

    @Query("SELECT AVG(score) FROM sentiment_logs WHERE timestamp >= :since")
    suspend fun getAverageScoreSince(since: Instant): Float?
}
