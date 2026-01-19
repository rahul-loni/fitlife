package com.example.fitlife.auth

import android.util.Log
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
import androidx.compose.material.icons.filled.*
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Hero header (same vibe as login) */
@Composable
private fun FitLifeHeroHeader(
    title: String,
    subtitle: String
) {
    val accent = Color(0xFF7C4DFF) // purple
    val accent2 = Color(0xFF00E5FF) // cyan

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(accent, accent2))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "FitLife",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Build your plan • track progress",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenComposable(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val pref = remember { UserPref(context) } // keeping it (even if unused for now)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // ✅ ALWAYS LIGHT BACKGROUND (same as login)
    val bgBrush = remember {
        Brush.radialGradient(
            colors = listOf(
                Color(0xFFF6F7FF),
                Color(0xFFEFF3FF),
                Color(0xFFFFFFFF)
            )
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create account") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBackToLogin,
                        enabled = !loading,
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
                // ❌ removed theme toggle actions completely
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
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.Top
            ) {

                FitLifeHeroHeader(
                    title = "Start strong",
                    subtitle = "Create your FitLife account in under a minute."
                )

                Spacer(Modifier.height(18.dp))

                val cardShape = RoundedCornerShape(24.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = cardShape,
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    // ✅ light glass surface
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {

                        // Accent strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
                                    )
                                )
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full name") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            }
                        )

                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !loading,
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null)
                            }
                        )

                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }, enabled = !loading) {
                                    Icon(
                                        imageVector = if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            }
                        )

                        Spacer(Modifier.height(14.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm password") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirm = !showConfirm }, enabled = !loading) {
                                    Icon(
                                        imageVector = if (showConfirm) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle confirm password"
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
                            if (error != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.35f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = error!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }

                        val buttonShape = RoundedCornerShape(18.dp)
                        Button(
                            onClick = {
                                val name = fullName.trim()
                                val em = email.trim()
                                val pw = password
                                val cpw = confirmPassword

                                error = when {
                                    name.isEmpty() -> "Enter full name"
                                    em.isEmpty() -> "Enter email"
                                    pw.length < 6 -> "Password must be at least 6 characters"
                                    pw != cpw -> "Passwords do not match"
                                    else -> null
                                }
                                if (error != null) return@Button

                                loading = true

                                auth.createUserWithEmailAndPassword(em, pw)
                                    .addOnSuccessListener { result ->
                                        val uid = result.user?.uid
                                        if (uid == null) {
                                            loading = false
                                            error = "Registration failed (no user id)"
                                            return@addOnSuccessListener
                                        }

                                        val userDoc = hashMapOf(
                                            "fullName" to name,
                                            "email" to em,
                                            "createdAt" to Timestamp.now()
                                        )

                                        db.collection("users").document(uid)
                                            .set(userDoc)
                                            .addOnSuccessListener {
                                                Log.d("AUTH", "REGISTER SUCCESS uid=$uid")
                                                loading = false
                                                // pref.setLoggedIn(em, true) // keep commented if you want manual login
                                                onRegisterSuccess()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("AUTH", "FIRESTORE SAVE FAIL: ${e.message}", e)
                                                loading = false
                                                error = e.message ?: "Failed to save user profile"
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("AUTH", "REGISTER FAIL: ${e.message}", e)
                                        loading = false
                                        error = e.message ?: "Registration failed"
                                    }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            enabled = !loading,
                            shape = buttonShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(buttonShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (loading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text("Creating...", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("Create Account", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account?",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            TextButton(onClick = onBackToLogin, enabled = !loading) {
                                Text("Back to login")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Tip: Use a strong password (6+ characters).",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 6.dp)
                )
            }
        }
    }
}
