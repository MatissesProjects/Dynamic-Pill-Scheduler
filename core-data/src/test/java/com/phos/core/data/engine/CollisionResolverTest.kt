package com.phos.core.data.engine

import com.phos.core.data.model.FoodLog
import com.phos.core.data.model.InteractionRule
import com.phos.core.data.model.MedicationRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollisionResolverTest {

    private val rules = listOf(
        InteractionRule(
            sourceId = "fiber_supplements",
            targetId = "bp_medication",
            gapMillis = 2 * 60 * 60 * 1000L,
            reason = "Sponge Effect"
        ),
        InteractionRule(
            sourceId = "grapefruit_juice",
            targetId = "statin_medication",
            gapMillis = 24 * 60 * 60 * 1000L,
            reason = "Grapefruit inhibits metabolism of Statins"
        )
    )
    private val resolver = CollisionResolver(rules)

    @Test
    fun testFindMedicationCollisions() {
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

        val collisions = resolver.findMedicationCollisions(listOf(fiber, bp))
        assertEquals(1, collisions.size)
        assertEquals("Sponge Effect", collisions[0].reason)
    }

    @Test
    fun `test multi-pill conflict chain`() {
        val rules = listOf(
            InteractionRule(sourceId = "med_a", targetId = "med_b", gapMillis = 2 * 3600000L, reason = "Conflict A-B"),
            InteractionRule(sourceId = "med_b", targetId = "med_c", gapMillis = 2 * 3600000L, reason = "Conflict B-C")
        )
        val resolver = CollisionResolver(rules)
        
        val medA = MedicationRecord(medicationId = "med_a", name = "A", dosage = "1", frequencyOffset = 0L, validFrom = 0L)
        val medB = MedicationRecord(medicationId = "med_b", name = "B", dosage = "1", frequencyOffset = 1 * 3600000L, validFrom = 0L) // Conflicts with A
        val medC = MedicationRecord(medicationId = "med_c", name = "C", dosage = "1", frequencyOffset = 2 * 3600000L, validFrom = 0L) // Conflicts with B
        
        val collisions = resolver.findMedicationCollisions(listOf(medA, medB, medC))
        assertEquals(2, collisions.size)
        assertTrue(collisions.any { it.reason == "Conflict A-B" })
        assertTrue(collisions.any { it.reason == "Conflict B-C" })
    }

    @Test
    fun `test partial ID matching and case insensitivity`() {
        val rules = listOf(
            InteractionRule(sourceId = "fiber", targetId = "lisinopril", gapMillis = 3600000L, reason = "Match")
        )
        val resolver = CollisionResolver(rules)
        
        val medA = MedicationRecord(medicationId = "High_Fiber_Supp", name = "Fiber", dosage = "1", frequencyOffset = 0L, validFrom = 0L)
        val medB = MedicationRecord(medicationId = "LISINOPRIL_TAB", name = "Lisinopril", dosage = "1", frequencyOffset = 300000L, validFrom = 0L)
        
        val collisions = resolver.findMedicationCollisions(listOf(medA, medB))
        assertEquals(1, collisions.size)
        assertEquals("Match", collisions[0].reason)
    }

    @Test
    fun testFindFoodCollisions() {
        val twake = 1000000000000L
        val statin = MedicationRecord(
            medicationId = "statin_medication",
            name = "Atorvastatin",
            dosage = "20mg",
            frequencyOffset = 2 * 60 * 60 * 1000L, // 2 hours after T-Wake
            validFrom = 0L
        )
        val food = FoodLog(
            foodId = "grapefruit_juice",
            name = "Grapefruit Juice",
            timestamp = twake + 1 * 60 * 60 * 1000L // 1 hour after T-Wake (Collision!)
        )

        val collisions = resolver.findFoodCollisions(listOf(statin), listOf(food), twake)
        assertEquals(1, collisions.size)
        assertEquals("statin_medication", collisions[0].medicationA.medicationId)
        assertEquals("grapefruit_juice", collisions[0].otherId)
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
            frequencyOffset = 30 * 60 * 1000L,
            validFrom = 0L
        )

        val collisions = resolver.findMedicationCollisions(listOf(medA, medB))
        assertTrue(collisions.isEmpty())
    }

    @Test
    fun `test findAbsorptionSpacingSuggestions detects conflict`() {
        val absorptionRules = listOf(
            com.phos.core.data.model.AbsorptionRule(medicationId = "sucralfate", requiredGapMinutes = 120, reason = "Spacing required")
        )
        val resolverWithAbsorption = CollisionResolver(absorptionRules = absorptionRules)
        
        val sucralfate = MedicationRecord(id = 1L, medicationId = "sucralfate", name = "Sucralfate", dosage = "1g", frequencyOffset = 0L, validFrom = 0L)
        val other = MedicationRecord(id = 2L, medicationId = "other", name = "Other", dosage = "10mg", frequencyOffset = 60 * 60 * 1000L, validFrom = 0L) // 60 mins < 120 mins
        
        val suggestions = resolverWithAbsorption.findAbsorptionSpacingSuggestions(listOf(sucralfate, other))
        assertEquals(1, suggestions.size)
        assertEquals("Spacing required", suggestions[0])
    }

    @Test
    fun `test getSideEffectAlerts returns matches`() {
        val sideEffectRules = listOf(
            com.phos.core.data.model.SideEffectRule(medicationId = "lisinopril", sideEffect = "Cough", advice = "Watch out")
        )
        val resolverWithSideEffects = CollisionResolver(sideEffectRules = sideEffectRules)
        
        val med = MedicationRecord(medicationId = "lisinopril", name = "Lisinopril", dosage = "10mg", frequencyOffset = 0L, validFrom = 0L)
        
        val alerts = resolverWithSideEffects.getSideEffectAlerts(listOf(med))
        assertEquals(1, alerts.size)
        assertEquals("Cough", alerts[0].sideEffect)
    }
}
