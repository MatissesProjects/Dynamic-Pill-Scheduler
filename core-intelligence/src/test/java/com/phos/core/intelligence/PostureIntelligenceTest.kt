package com.phos.core.intelligence

import com.phos.core.data.model.FoodLog
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class PostureIntelligenceTest {

    private val postureIntelligence = PostureIntelligence()

    @Test
    fun `test checkPostPrandialPosture returns recommendation when recently eaten`() {
        val now = Instant.now().toEpochMilli()
        val recentFood = listOf(
            FoodLog(foodId = "meal_1", name = "Chicken Salad", timestamp = now - 10 * 60 * 1000) // 10 mins ago
        )

        val result = postureIntelligence.checkPostPrandialPosture(recentFood)

        assertNotNull(result)
        assertEquals("Digestion Guidance", result?.title)
        assertTrue(result?.remainingMinutes ?: 0 > 0)
        assertTrue(result?.recommendation?.contains("Chicken Salad") == true)
    }

    @Test
    fun `test checkPostPrandialPosture returns null when meal was long ago`() {
        val now = Instant.now().toEpochMilli()
        val oldFood = listOf(
            FoodLog(foodId = "meal_1", name = "Breakfast", timestamp = now - 60 * 60 * 1000) // 60 mins ago
        )

        val result = postureIntelligence.checkPostPrandialPosture(oldFood)

        assertNull(result)
    }

    @Test
    fun `test checkPostPrandialPosture returns null when no food logs`() {
        val result = postureIntelligence.checkPostPrandialPosture(emptyList())
        assertNull(result)
    }
}
