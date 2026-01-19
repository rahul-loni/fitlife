package com.example.fitlife.profile

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitlife.navigation.Routes


@Composable
fun ProfileTabScreen(navController: NavController) {
    val vm: ProfileViewModel = viewModel()
    ProfilePage(
        viewModel = vm,
        onBack = {},
        onOpenSettings = { navController.navigate(Routes.SETTINGS) }
    )
}
