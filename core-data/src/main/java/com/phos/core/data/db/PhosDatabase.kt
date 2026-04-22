package com.phos.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phos.core.data.dao.*
import com.phos.core.data.model.*
import androidx.room.TypeConverters

@Database(
    entities = [
        MedicationRecord::class,
        TemporalAnchor::class,
        DoseLog::class,
        SymptomLog::class,
        EnvironmentalLog::class,
        InteractionRule::class,
        FoodLog::class,
        GeoBoundary::class,
        LocationAnchor::class,
        BiometricLog::class,
        BiometricBaseline::class,
        InventoryRecord::class
    ],
    version = 6
)
@TypeConverters(Converters::class)
abstract class PhosDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun temporalAnchorDao(): TemporalAnchorDao
    abstract fun intelligenceDao(): IntelligenceDao
    abstract fun interactionDao(): InteractionDao
    abstract fun geoDao(): GeoDao
    abstract fun reportingDao(): ReportingDao
    abstract fun biometricDao(): BiometricDao
    abstract fun inventoryDao(): InventoryDao
}
