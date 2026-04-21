package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.FoodLog
import com.phos.core.data.model.InteractionRule
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionDao {
    @Query("SELECT * FROM interaction_rules")
    fun getAllRules(): Flow<List<InteractionRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: InteractionRule)

    @Query("SELECT * FROM food_logs WHERE timestamp > :since")
    suspend fun getRecentFoodLogs(since: Long): List<FoodLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(log: FoodLog)
}
