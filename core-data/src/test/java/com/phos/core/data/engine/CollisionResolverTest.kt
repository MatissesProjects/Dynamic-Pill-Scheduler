package com.phos.core.data.engine

import com.phos.core.data.model.MedicationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollisionResolverTest {

    private val resolver = CollisionResolver()

    @Test
    fun testFindCollisions() {
        val fiber = MedicationRecord(
            medicationId = "fiber_supplements",
            name = "Fiber",
            dosage = "1 scoop",
            frequencyOffset = 0L,
            validFrom = 0L
        )
        val bp = MedicationRecord(
            medicationId = "bp_medication",
            name = "BP Med",
            dosage = "5mg",
            frequencyOffset = 60 * 60 * 1000L, // 1 hour (Collision!)
            validFrom = 0L
        )

        val collisions = resolver.findCollisions(listOf(fiber, bp))
        assertEquals(1, collisions.size)
        assertTrue(collisions[0].reason.contains("Sponge Effect"))
    }

    @Test
    fun testFindCollisions_noConflicts() {
        val medA = MedicationRecord(
            medicationId = "med_a",
            name = "Med A",
            dosage = "10mg",
            frequencyOffset = 0L,
            validFrom = 0L
        )
        val medB = MedicationRecord(
            medicationId = "med_b",
            name = "Med B",
            dosage = "5mg",
            frequencyOffset = 30 * 60 * 1000L, // 30 mins later but no rule
            validFrom = 0L
        )

        val collisions = resolver.findCollisions(listOf(medA, medB))
        assertTrue(collisions.isEmpty())
    }

    @Test
    fun testFindCollisions_withGap() {
        val fiber = MedicationRecord(
            medicationId = "fiber_supplements",
            name = "Fiber",
            dosage = "1 scoop",
            frequencyOffset = 0L,
            validFrom = 0L
        )
        val bp = MedicationRecord(
            medicationId = "bp_medication",
            name = "BP Med",
            dosage = "5mg",
            frequencyOffset = 3 * 60 * 60 * 1000L, // 3 hours (Safe gap > 2h)
            validFrom = 0L
        )

        val collisions = resolver.findCollisions(listOf(fiber, bp))
        assertTrue(collisions.isEmpty())
    }
}
