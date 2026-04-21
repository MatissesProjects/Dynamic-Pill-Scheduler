package com.phos.core.data.engine

import com.phos.core.data.model.DoseLog
import com.phos.core.data.model.GeoBoundary
import com.phos.core.data.model.LocationAnchor
import com.phos.core.data.model.MedicationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoContextManagerTest {

    private val geoManager = GeoContextManager()

    @Test
    fun testProximityExit_triggered() {
        val home = GeoBoundary("home", 37.422, -122.084, 100f)
        val medication = MedicationRecord(
            medicationId = "t_wake_med",
            name = "Morning Pill",
            dosage = "10mg",
            frequencyOffset = 0L,
            validFrom = 0L
        )
        
        // Exiting the boundary: from center to far away
        val alerts = geoManager.checkProximityExit(
            37.5, -122.2, // current (outside)
            37.422, -122.084, // previous (inside center)
            home,
            listOf(medication),
            emptyList() // No dose logs yet
        )
        
        assertEquals(1, alerts.size)
        assertEquals(GeoAlertType.PROXIMITY_EXIT, alerts[0].type)
    }

    @Test
    fun testProximityExit_notTriggered_doseTaken() {
        val home = GeoBoundary("home", 37.422, -122.084, 100f)
        val medication = MedicationRecord(
            medicationId = "t_wake_med",
            name = "Morning Pill",
            dosage = "10mg",
            frequencyOffset = 0L,
            validFrom = 0L
        )
        val doseLog = DoseLog(
            medicationId = "t_wake_med",
            dosage = "10mg",
            timestamp = System.currentTimeMillis()
        )
        
        val alerts = geoManager.checkProximityExit(
            37.5, -122.2,
            37.422, -122.084,
            home,
            listOf(medication),
            listOf(doseLog)
        )
        
        assertTrue(alerts.isEmpty())
    }

    @Test
    fun testLocationArrival_triggered() {
        val anchor = LocationAnchor(
            medicationId = "work_med",
            targetLatitude = 37.7749,
            targetLongitude = -122.4194,
            radiusMeters = 200f
        )
        
        val alerts = geoManager.checkLocationArrival(
            37.775, -122.419, // Arriving at target
            listOf(anchor)
        )
        
        assertEquals(1, alerts.size)
        assertEquals(GeoAlertType.LOCATION_ARRIVAL, alerts[0].type)
    }
}
