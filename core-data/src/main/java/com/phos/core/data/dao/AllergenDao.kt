package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.AllergenProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface AllergenDao {
    @Query("SELECT * FROM allergen_profiles")
    fun getAllergensFlow(): Flow<List<AllergenProfile>>

    @Query("SELECT * FROM allergen_profiles")
    suspend fun getAllergens(): List<AllergenProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergen(allergen: AllergenProfile)

    @Query("DELETE FROM allergen_profiles WHERE allergenId = :id")
    suspend fun deleteAllergen(id: String)
}
