package com.phos.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single user profile
    val weightKg: Double,
    val gender: Gender,
    val birthYear: Int? = null
)

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}
