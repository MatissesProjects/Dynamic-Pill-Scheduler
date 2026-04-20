package com.phos.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.phos.core.data.dao.MedicationDao
import com.phos.core.data.dao.TemporalAnchorDao
import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.MedicationRecord
import com.phos.core.data.model.TemporalAnchor

@Database(
    entities = [
        MedicationRecord::class,
        TemporalAnchor::class,
        DoseLog::class
    ],
    version = 1
)
abstract class PhosDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun temporalAnchorDao(): TemporalAnchorDao
}
