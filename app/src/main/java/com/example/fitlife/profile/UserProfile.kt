package com.example.fitlife.profile

data class UserProfile(
    val fullName: String,
    val email: String,
    val age: Int,
    val heightCm: Int,
    val weightKg: Double,
    val goal: String
)
