package com.phos.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phos.core.data.dao.IntelligenceDao
import com.phos.core.data.dao.InteractionDao
import com.phos.core.data.dao.MedicationDao
import com.phos.core.data.dao.TemporalAnchorDao
import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.EnvironmentalLog
import com.phos.core.data.model.FoodLog
import com.phos.core.data.model.InteractionRule
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.SymptomLog
import com.phos.core.data.model.TemporalAnchor

@Database(
    entities = [
        MedicationRecord::class,
        TemporalAnchor::class,
        DoseLog::class,
        SymptomLog::class,
        EnvironmentalLog::class,
        InteractionRule::class,
        FoodLog::class
    ],
    version = 2
)
abstract class PhosDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun temporalAnchorDao(): TemporalAnchorDao
    abstract fun intelligenceDao(): IntelligenceDao
    abstract fun interactionDao(): InteractionDao
}
