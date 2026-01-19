package com.example.fitlife.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitlife.auth.UserPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val pref = UserPref(context)

    // which user is currently logged in
    val userEmail = pref.getLoggedInEmail()

    // ✅ Aurora Glass theme (matches Home/Register/Workouts)
    val cs = MaterialTheme.colorScheme
    val isDark = cs.background.luminance() < 0.3f

    val accentA = if (isDark) Color(0xFF7C4DFF) else Color(0xFF5B5FEF)
    val accentB = if (isDark) Color(0xFF00E5FF) else Color(0xFF00B8D4)
    val danger = if (isDark) Color(0xFFFF6B6B) else Color(0xFFD32F2F)

    val bgBrush = remember(isDark) {
        if (isDark) {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A1633),
                    Color(0xFF0B1020),
                    Color(0xFF060814)
                )
            )
        } else {
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF7F7FF),
                    Color(0xFFEFF4FF),
                    Color(0xFFFFFFFF)
                )
            )
        }
    }

    val glass = cs.surface.copy(alpha = if (isDark) 0.72f else 0.92f)
    val glassStrong = cs.surface.copy(alpha = if (isDark) 0.84f else 0.96f)
    val outlineSoft = cs.onSurface.copy(alpha = if (isDark) 0.10f else 0.08f)

    // If somehow no one is logged in, show a message instead of crashing
    if (userEmail.isBlank()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Profile", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        FilledTonalIconButton(
                            onClick = onBack,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                                contentColor = cs.onSurface
                            )
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = cs.onBackground
                    )
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(bgBrush),
                contentAlignment = Alignment.Center
            ) {
                EmptyAuroraState(
                    title = "No user logged in",
                    subtitle = "Please logout and login again.",
                    accentA = danger,
                    accentB = danger,
                    glass = glassStrong,
                    outlineSoft = outlineSoft,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        return
    }

    var editing by remember { mutableStateOf(false) }

    // Load per-user saved values once
    var fullName by remember {
        mutableStateOf(
            pref.getProfileFullName(userEmail).ifBlank { pref.getFullNameFor(userEmail) }
        )
    }
    var email by remember { mutableStateOf(pref.getProfileEmail(userEmail).ifBlank { userEmail }) }
    var ageText by remember { mutableStateOf(pref.getProfileAge(userEmail).takeIf { it != 0 }?.toString() ?: "") }
    var heightText by remember { mutableStateOf(pref.getProfileHeightCm(userEmail).takeIf { it != 0 }?.toString() ?: "") }
    var weightText by remember { mutableStateOf(pref.getProfileWeightKg(userEmail).takeIf { it != 0f }?.toString() ?: "") }
    var goal by remember { mutableStateOf(pref.getProfileGoal(userEmail)) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentA.copy(alpha = 0.65f),
        unfocusedBorderColor = outlineSoft,
        focusedLabelColor = accentA,
        cursorColor = accentA
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = cs.onSurface
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onBackground
                ),
                actions = {

                    // ⚙️ Settings (TOP RIGHT)
                    FilledTonalIconButton(
                        onClick = onOpenSettings,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = cs.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }

                    Spacer(Modifier.width(8.dp))

                    // ✏️ Edit / Save toggle
                    FilledTonalIconButton(
                        onClick = {
                            if (editing) {
                                val age = ageText.toIntOrNull() ?: 0
                                val height = heightText.toIntOrNull() ?: 0
                                val weight = weightText.toFloatOrNull() ?: 0f

                                pref.saveProfile(
                                    email = userEmail,
                                    fullName = fullName.trim(),
                                    age = age,
                                    heightCm = height,
                                    weightKg = weight,
                                    goal = goal.trim()
                                )
                            }
                            editing = !editing
                        },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = if (editing) accentA else cs.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = if (editing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (editing) "Save" else "Edit"
                        )
                    }

                    Spacer(Modifier.width(10.dp))
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bgBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ✅ Header / identity card
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(accentA, accentB))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = fullName.ifBlank { "Your Name" },
                            color = cs.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = userEmail,
                            color = cs.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(cs.surfaceVariant.copy(alpha = 0.55f))
                            .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (editing) "EDITING" else "VIEW",
                            color = if (editing) accentA else cs.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ✅ Main form card
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glass,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Your details",
                        color = cs.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editing,
                        colors = tfColors,
                        shape = RoundedCornerShape(14.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = tfColors,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it.filter(Char::isDigit) },
                            label = { Text("Age") },
                            modifier = Modifier.weight(1f),
                            enabled = editing,
                            colors = tfColors,
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it.filter(Char::isDigit) },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            enabled = editing,
                            colors = tfColors,
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editing,
                        colors = tfColors,
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Goal") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editing,
                        colors = tfColors,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            // ✅ Tip card
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Tip", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Text(
                        "Connect this profile data to BMI in Progress Tracker and show a personalized dashboard summary.",
                        color = cs.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

/* -------------------- Aurora helpers -------------------- */

@Composable
private fun AuroraCard(
    accentA: Color,
    accentB: Color,
    outlineSoft: Color,
    glass: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier,
        shape = shape,
        color = glass,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineSoft, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accentA.copy(alpha = 0.16f),
                            accentB.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun EmptyAuroraState(
    title: String,
    subtitle: String,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = glass,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, outlineSoft, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accentA.copy(alpha = 0.14f),
                            accentB.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = cs.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
