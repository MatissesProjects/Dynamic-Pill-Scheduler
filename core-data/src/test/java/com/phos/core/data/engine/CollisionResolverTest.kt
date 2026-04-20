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
    fun testSuggestResolution() {
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
            frequencyOffset = 60 * 60 * 1000L,
            validFrom = 0L
        )

        val collision = Collision(fiber, bp, "Test")
        val suggestedOffset = resolver.suggestResolution(collision)
        assertEquals(2 * 60 * 60 * 1000L, suggestedOffset) // 2 hours
    }
}
