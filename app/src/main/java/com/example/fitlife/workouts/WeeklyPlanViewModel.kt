package com.example.fitlife.workouts

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class WeeklyPlanViewModel : ViewModel() {

    private val _plan = mutableStateListOf<Workout>()
    val plan: List<Workout> = _plan

    fun addWorkout(workout: Workout) {
        if (!_plan.contains(workout)) {
            _plan.add(workout)
        }
    }

    fun removeWorkout(workout: Workout) {
        _plan.remove(workout)
    }
}
