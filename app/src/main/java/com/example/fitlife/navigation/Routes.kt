package com.example.fitlife.navigation

object Routes {
    const val DASHBOARD = "dashboard"

    const val EXERCISES = "exercise"
    const val MEALS = "meals"
    const val ADD_MEAL = "add_meal"

    const val PROGRESS = "progress"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"

    const val WORKOUT = "workout"
    const val WORKOUT_PLAN = "workout_plan"

    const val ITEM_DELEGATION = "item_delegation"

    const val WORKOUTS = "workouts"

    const val PICK_EXERCISES = "pick_exercises"
    fun pickExercises(workoutId: String) = "$PICK_EXERCISES/$workoutId"


    // ✅ Base route ONLY (no { } here)
    const val WORKOUT_DETAIL = "workout_detail"

    // ✅ helper
    fun workoutDetail(id: String) = "$WORKOUT_DETAIL/$id"
}
