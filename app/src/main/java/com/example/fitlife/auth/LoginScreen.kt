package com.example.fitlife.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitlife.MainNavActivity
import com.example.fitlife.ui.theme.FitLifeTheme
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // ✅ FORCE LIGHT MODE FOR LOGIN
            FitLifeTheme(darkTheme = false) {
                AuthNav(
                    onLoginSuccess = {
                        startActivity(Intent(this, MainNavActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}


@Composable
private fun AuthNav(
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable(route = "login") {
            LoginScreenComposable(
                onLoginSuccess = onLoginSuccess,
                onGoRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable(route = "register") {
            RegisterScreenComposable(
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

    }
}




/** Hero header */
@Composable
private fun FitLifeHeroHeader(
    title: String,
    subtitle: String
) {
    val accent = Color(0xFF7C4DFF)
    val accent2 = Color(0xFF00E5FF)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(accent, accent2))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("FitLife", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                Text(
                    "Train smarter • live stronger",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreenComposable(
    onLoginSuccess: () -> Unit,
    onGoRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val pref = remember { UserPref(context) }
    val auth = remember { FirebaseAuth.getInstance() }

    // ✅ LIGHT MODE BACKGROUND ONLY
    val bgBrush = Brush.radialGradient(
        colors = listOf(
            Color(0xFFF6F7FF),
            Color(0xFFEFF3FF),
            Color(0xFFFFFFFF)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sign in") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Top
            ) {

                FitLifeHeroHeader(
                    title = "Welcome back",
                    subtitle = "Log in to continue your fitness streak."
                )

                Spacer(Modifier.height(18.dp))

                val cardShape = RoundedCornerShape(24.dp)
                Surface(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    shape = cardShape,
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !loading,
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, null) }
                        )

                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            singleLine = true,
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }) {
                                    Icon(
                                        imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        Spacer(Modifier.height(10.dp))

                        AnimatedVisibility(
                            visible = error != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            error?.let {
                                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val em = email.trim()
                                val pw = password

                                error = when {
                                    em.isEmpty() -> "Enter email"
                                    pw.isEmpty() -> "Enter password"
                                    else -> null
                                }
                                if (error != null) return@Button

                                loading = true

                                auth.signInWithEmailAndPassword(em, pw)
                                    .addOnSuccessListener {
                                        pref.setLoggedIn(em, true)
                                        loading = false
                                        onLoginSuccess()
                                    }
                                    .addOnFailureListener { e ->
                                        loading = false
                                        error = e.message ?: "Invalid email or password"
                                    }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            enabled = !loading,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Signing in...")
                            } else {
                                Text("Login", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("New here?")
                            Spacer(Modifier.width(6.dp))
                            TextButton(onClick = onGoRegister) {
                                Text("Create account")
                            }
                        }
                    }
                }
            }
        }
    }
}
