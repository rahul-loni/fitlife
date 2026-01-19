package com.example.fitlife.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val dateText = runCatching {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }.getOrDefault("Today")

    val cs = MaterialTheme.colorScheme
    val isDark = cs.background.luminance() < 0.3f

    val bgBrush = if (isDark) {
        Brush.radialGradient(
            listOf(Color(0xFF1A1633), Color(0xFF0B1020), Color(0xFF060814))
        )
    } else {
        Brush.radialGradient(
            listOf(Color(0xFFF7F7FF), Color(0xFFEFF4FF), Color.White)
        )
    }

    val accentA = if (isDark) Color(0xFF7C4DFF) else Color(0xFF5B5FEF)
    val accentB = if (isDark) Color(0xFF00E5FF) else Color(0xFF00B8D4)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("FitLife", fontWeight = FontWeight.Black)
                        Spacer(Modifier.width(10.dp))
                        FitLifePill("Dashboard", accentA, accentB)
                    }
                },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // HERO
                item {
                    AuroraHeroCardV2(
                        accentA,
                        accentB,
                        dateText,
                        title = "Welcome back",
                        highlight = "Train smart. Stay consistent.",
                        sub = "Your plan for today is ready."
                    )
                }

                item { SectionHeader("YOUR SNAPSHOT") }

                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProgressStatCardV2(
                            Modifier.weight(1f),
                            accentA,
                            accentB,
                            Icons.Default.DirectionsRun,
                            "Workouts",
                            "4",
                            "this week",
                            "on track",
                            0.6f
                        )
                        ProgressStatCardV2(
                            Modifier.weight(1f),
                            accentA,
                            accentB,
                            Icons.Default.Bolt,
                            "Streak",
                            "8",
                            "days",
                            "best",
                            0.8f
                        )
                    }
                }

                item { SectionHeader("TODAY’S PLAN") }

                item {
                    GlassListCardV2(
                        accentA,
                        accentB,
                        title = "Training Focus",
                        subtitle = "Balanced routine",
                        chips = listOf("Strength", "Cardio"),
                        items = listOf(
                            "Warm-up • 8 min",
                            "Strength • Push day",
                            "Cardio • Incline walk",
                            "Stretch • Recovery"
                        )
                    )
                }
            }
        }
    }
}

/* ---------- UI HELPERS ---------- */

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    )
}

@Composable
private fun FitLifePill(text: String, a: Color, b: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(a, b)))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

/* ---------- HERO CARD ---------- */

@Composable
private fun AuroraHeroCardV2(
    accentA: Color,
    accentB: Color,
    dateText: String,
    title: String,
    highlight: String,
    sub: String
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(accentA.copy(0.2f), accentB.copy(0.15f))
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.ExtraBold)
            Text(dateText, style = MaterialTheme.typography.bodySmall)
            Text(highlight, fontWeight = FontWeight.Bold)
            Text(sub, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/* ---------- STATS CARD ---------- */

@Composable
private fun ProgressStatCardV2(
    modifier: Modifier,
    a: Color,
    b: Color,
    icon: ImageVector,
    label: String,
    value: String,
    note: String,
    badge: String,
    progress: Float
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = a)
            Text(label, fontWeight = FontWeight.SemiBold)
            Text(value, fontWeight = FontWeight.Black)
            LinearProgressIndicator(
                progress = progress,
                color = a,
                trackColor = a.copy(alpha = 0.2f)
            )
        }
    }
}

/* ---------- PLAN CARD ---------- */

@Composable
private fun GlassListCardV2(
    a: Color,
    b: Color,
    title: String,
    subtitle: String,
    chips: List<String>,
    items: List<String>
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach {
                    AssistChip(
                        onClick = {},
                        label = { Text(it) },
                        border = BorderStroke(1.dp, a.copy(alpha = 0.4f))
                    )
                }
            }

            items.forEach {
                Text("• $it")
            }
        }
    }
}
