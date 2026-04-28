package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "alcohol_logs")
data class AlcoholLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Instant,
    val beverageType: BeverageType,
    val abv: Double,
    val volumeMl: Double,
    val estimatedStandardDrinks: Double = (volumeMl * abv) / 100.0 / 14.0 // Rough US standard drink: 14g ethanol
)

enum class BeverageType {
    BEER,
    WINE,
    SPIRITS,
    FERMENTED_HISTAMINE_HIGH // Sour beers, some wines
}
