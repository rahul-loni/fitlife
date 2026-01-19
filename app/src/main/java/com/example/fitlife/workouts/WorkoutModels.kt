package com.example.fitlife.workouts

data class WorkoutItem(
    val id: String = "",
    val title: String = "",
    val order: Int = 0,
    val sets: Int = 0,
    val reps: Int = 0,
    val durationSec: Int? = null,
    val notes: String? = null,
    val isDone: Boolean = false,   // ✅ per-exercise done
)

data class Workout(
    val id: String = "",
    val name: String = "",
    val day: String = "Monday",
    val durationMin: Int = 0,
    val instructions: String = "",
    val requiredEquipment: List<String> = emptyList(),
    val imageUrl: String? = null,           // ✅ saved image URL
    val isCompleted: Boolean = false,       // ✅ workout done
    val items: List<WorkoutItem> = emptyList()
)
