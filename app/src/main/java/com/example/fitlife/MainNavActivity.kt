package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fitlife.auth.LoginActivity
import com.example.fitlife.navigation.AppNavHost
import com.example.fitlife.navigation.NavBottomBar
import com.example.fitlife.ui.theme.FitLifeTheme
import com.example.fitlife.workouts.WorkoutsViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding

class MainNavActivity : ComponentActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(listener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ do NOT redirect here anymore (race condition after login)
        setContent {
            FitLifeTheme {
                Surface {
                    FitLifeNavApp()
                }
            }
        }
    }
}

@Composable
private fun FitLifeNavApp() {
    val navController = rememberNavController()

    // ✅ Safe ViewModel
    val workoutsViewModel: WorkoutsViewModel = viewModel()

    Scaffold(
        bottomBar = { NavBottomBar(navController) }
    ) { padding ->
        AppNavHost(
            navController = navController,
            workoutsViewModel = workoutsViewModel,
            modifier = Modifier.padding(padding)
        )
    }
}
