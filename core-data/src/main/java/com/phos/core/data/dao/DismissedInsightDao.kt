package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.DismissedInsight
import kotlinx.coroutines.flow.Flow

@Dao
interface DismissedInsightDao {
    @Query("SELECT insightId FROM dismissed_insights")
    fun getAllDismissedIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun dismiss(insight: DismissedInsight)

    @Query("DELETE FROM dismissed_insights WHERE insightId = :insightId")
    suspend fun restore(insightId: String)
}
