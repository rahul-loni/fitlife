package com.example.fitlife.exercises

import com.google.firebase.Timestamp

data class Exercise(
    val id: String = "",
    val name: String = "",
    val muscleGroup: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val notes: String = "",
    val image: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Timestamp? = null,
    val completedAt: Timestamp? = null
)
