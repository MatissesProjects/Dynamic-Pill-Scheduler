package com.phos.core.data.engine

import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.GeoBoundary
import com.phos.core.data.model.LocationAnchor
import com.phos.core.data.model.MedicationRecord
import kotlin.math.*

data class GeoAlert(
    val medicationId: String,
    val message: String,
    val type: GeoAlertType
)

enum class GeoAlertType {
    PROXIMITY_EXIT, // User left home without meds
    LOCATION_ARRIVAL // User arrived at target location for med
}

class GeoContextManager {

    /**
     * Checks if the user is leaving the home boundary without taking required doses.
     */
    fun checkProximityExit(
        currentLat: Double,
        currentLon: Double,
        previousLat: Double,
        previousLon: Double,
        homeBoundary: GeoBoundary,
        requiredMedications: List<MedicationRecord>,
        recentDoseLogs: List<DoseLog>
    ): List<GeoAlert> {
        val alerts = mutableListOf<GeoAlert>()
        
        val wasInside = isInside(previousLat, previousLon, homeBoundary)
        val isOutside = !isInside(currentLat, currentLon, homeBoundary)
        
        if (wasInside && isOutside) {
            // User just exited the boundary. Check for missing doses.
            for (med in requiredMedications) {
                val isTaken = recentDoseLogs.any { it.medicationId == med.medicationId }
                if (!isTaken) {
                    alerts.add(GeoAlert(
                        med.medicationId,
                        "Leaving Home: Did you forget your ${med.name}?",
                        GeoAlertType.PROXIMITY_EXIT
                    ))
                }
            }
        }
        
        return alerts
    }

    /**
     * Checks if the user has arrived at a target location for an anchored dose.
     */
    fun checkLocationArrival(
        currentLat: Double,
        currentLon: Double,
        anchors: List<LocationAnchor>
    ): List<GeoAlert> {
        val alerts = mutableListOf<GeoAlert>()
        
        for (anchor in anchors) {
            val distance = calculateDistance(
                currentLat, currentLon, 
                anchor.targetLatitude, anchor.targetLongitude
            )
            
            if (distance <= anchor.radiusMeters) {
                alerts.add(GeoAlert(
                    anchor.medicationId,
                    "Arrived at location: Dose due in ${anchor.delayMinutesAfterArrival} minutes.",
                    GeoAlertType.LOCATION_ARRIVAL
                ))
            }
        }
        
        return alerts
    }

    private fun isInside(lat: Double, lon: Double, boundary: GeoBoundary): Boolean {
        val distance = calculateDistance(lat, lon, boundary.latitude, boundary.longitude)
        return distance <= boundary.radiusMeters
    }

    /**
     * Haversine formula to calculate distance between two points in meters.
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // Earth radius in meters
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val deltaPhi = (lat2 - lat1) * PI / 180
        val deltaLambda = (lon2 - lon1) * PI / 180

        val a = sin(deltaPhi / 2).pow(2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }
}
