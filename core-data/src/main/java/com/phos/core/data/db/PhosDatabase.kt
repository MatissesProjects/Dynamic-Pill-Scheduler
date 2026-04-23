package com.phos.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.phos.core.data.dao.*
import com.phos.core.data.model.*

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
        InventoryRecord::class,
        DismissedInsight::class
    ],
    version = 7
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
    abstract fun dismissedInsightDao(): DismissedInsightDao
}
