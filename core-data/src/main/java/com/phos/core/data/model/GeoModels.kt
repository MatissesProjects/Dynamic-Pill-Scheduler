package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines a geographic boundary (Geofence) for the user's home or other critical areas.
 */
@Entity(tableName = "geo_boundaries")
data class GeoBoundary(
    @PrimaryKey val id: String, // e.g., "home"
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val isEnabled: Boolean = true
)

/**
 * Anchors a specific medication dose to a geographic arrival event.
 */
@Entity(tableName = "location_anchored_doses")
data class LocationAnchor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: String,
    val targetLatitude: Double,
    val targetLongitude: Double,
    val radiusMeters: Float,
    val delayMinutesAfterArrival: Int = 0,
    val lastTriggeredTimestamp: Long? = null
)
