package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.phos.core.data.model.SleepSubjectiveLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSubjectiveDao {
    @Insert
    suspend fun insertSubjectiveLog(log: SleepSubjectiveLog)

    @Query("SELECT * FROM sleep_subjective_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): SleepSubjectiveLog?

    @Query("SELECT * FROM sleep_subjective_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<SleepSubjectiveLog>>
}
