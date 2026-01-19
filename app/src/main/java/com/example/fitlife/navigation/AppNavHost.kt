package com.example.fitlife.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.fitlife.delegation.ItemDelegationScreen
import com.example.fitlife.diet.AddMealScreen
import com.example.fitlife.diet.MealPlanScreen
import com.example.fitlife.exercises.ExercisePickerScreen
import com.example.fitlife.exercises.ExerciseScreen
import com.example.fitlife.home.HomeScreen
import com.example.fitlife.profile.ProfileTabScreen
import com.example.fitlife.progress.ProgressTabScreen
import com.example.fitlife.settings.SettingsScreen
import com.example.fitlife.workouts.WorkoutDetailScreen
import com.example.fitlife.workouts.WorkoutPlanScreen
import com.example.fitlife.workouts.WorkoutsScreen
import com.example.fitlife.workouts.WorkoutsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    workoutsViewModel: WorkoutsViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier
    ) {

        composable(Routes.DASHBOARD) { HomeScreen(navController) }

        composable(Routes.WORKOUTS) {
            WorkoutsScreen(navController = navController, viewModel = workoutsViewModel)
        }

        composable(Routes.WORKOUT_PLAN) {
            WorkoutPlanScreen(
                navController = navController,
                workoutVm = workoutsViewModel
            )
        }

        composable(
            route = "${Routes.WORKOUT_DETAIL}/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
            WorkoutDetailScreen(
                workoutId = workoutId,
                vm = workoutsViewModel,
                navController = navController
            )
        }

        // ✅ ONE Exercise Picker route (no duplicates)
        composable(
            route = "exercise_picker/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
            ExercisePickerScreen(
                navController = navController,
                workoutId = workoutId,
                vm = workoutsViewModel
            )
        }

        composable(Routes.MEALS) { MealPlanScreen(navController) }
        composable("add_meal") { AddMealScreen(navController) }

        composable(Routes.EXERCISES) { ExerciseScreen() }

        composable(
            route = "${Routes.ITEM_DELEGATION}/{workoutId}",
            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
            ItemDelegationScreen(
                navController = navController,
                workoutId = workoutId,
                vm = workoutsViewModel
            )
        }

        composable(Routes.PROGRESS) { ProgressTabScreen() }
        composable(Routes.PROFILE) { ProfileTabScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
    }
}
