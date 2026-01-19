package com.example.fitlife.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavBottomBar(navController: NavController) {

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = backStackEntry?.destination

    data class BottomItem(
        val label: String,
        val route: String,
        val icon: @Composable () -> Unit
    )

    val items = listOf(
        BottomItem("Home", Routes.DASHBOARD) {
            Icon(Icons.Default.Home, contentDescription = "Home")
        },

        // ✅ Workout tab goes to WORKOUTS HOME (list)
        BottomItem("Workout", Routes.WORKOUTS) {
            Icon(Icons.Default.FitnessCenter, contentDescription = "Workout")
        },

        // ✅ ADD THIS: Exercise tab
        BottomItem("Exercise", Routes.EXERCISES) {
            Icon(Icons.Default.List, contentDescription = "Exercise")
        },



        BottomItem("Profile", Routes.PROFILE) {
            Icon(Icons.Default.Person, contentDescription = "Profile")
        }
    )

    NavigationBar {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}
