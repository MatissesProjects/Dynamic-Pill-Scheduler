package com.phos.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.phos.core.data.model.GeoBoundary
import com.phos.core.data.model.LocationAnchor
import kotlinx.coroutines.flow.Flow

@Dao
interface GeoDao {
    @Query("SELECT * FROM geo_boundaries")
    fun getAllBoundaries(): Flow<List<GeoBoundary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoundary(boundary: GeoBoundary)

    @Query("SELECT * FROM location_anchored_doses")
    fun getAllLocationAnchors(): Flow<List<LocationAnchor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationAnchor(anchor: LocationAnchor)
}
