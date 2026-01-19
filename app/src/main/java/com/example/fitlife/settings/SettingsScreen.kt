package com.example.fitlife.settings

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.navigation.NavController
import com.example.fitlife.auth.LoginActivity
import com.example.fitlife.auth.UserPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    val context = LocalContext.current
    val pref = UserPref(context)

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

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(accentA, accentB))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Settings", fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = { navController.popBackStack() },
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

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bgBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ✅ App description card
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(cs.surfaceVariant.copy(alpha = 0.55f))
                            .border(1.dp, outlineSoft, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = accentA)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("FitLife", style = MaterialTheme.typography.titleLarge, color = cs.onSurface, fontWeight = FontWeight.Bold)
                        Text(
                            "Fitness tracking app built with Jetpack Compose.",
                            color = cs.onSurfaceVariant
                        )
                        Text(
                            "Modules: Workouts • Exercises • Meals • Progress • Profile",
                            color = cs.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            // ✅ App info card
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glass,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("App Info", style = MaterialTheme.typography.titleMedium, color = cs.onSurface, fontWeight = FontWeight.Bold)

                    InfoRowAurora(label = "Version", value = "1.0.0", outlineSoft = outlineSoft, glass = cs.surfaceVariant.copy(alpha = 0.55f), accent = accentA)
                    InfoRowAurora(label = "Developer", value = "Your Name", outlineSoft = outlineSoft, glass = cs.surfaceVariant.copy(alpha = 0.55f), accent = accentA)
                }
            }

            Spacer(Modifier.height(6.dp))

            // ✅ Logout button
            Button(
                onClick = {
                    pref.logout()

                    val intent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(danger, danger.copy(alpha = 0.85f))))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
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
private fun InfoRowAurora(
    label: String,
    value: String,
    outlineSoft: Color,
    glass: Color,
    accent: Color
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(glass, shape)
            .border(1.dp, outlineSoft, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.weight(1f))
        Text(value, color = accent, fontWeight = FontWeight.SemiBold)
    }
}
