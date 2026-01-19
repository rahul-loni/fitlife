package com.example.fitlife.diet

import com.google.firebase.Timestamp

data class Meal(
    val id: String = "",
    val title: String = "",
    val mealType: String = "Breakfast", // Breakfast/Lunch/Dinner/Snack
    val calories: Int = 0,
    val notes: String = "",
    val date: String = "",              // "YYYY-MM-DD"
    val isCompleted: Boolean = false,
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null
)
