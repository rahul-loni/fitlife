package com.example.fitlife.workouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitlife.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    navController: NavController,
    viewModel: WorkoutsViewModel
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    var showCreate by remember { mutableStateOf(false) }

    // ✅ Aurora Glass theme (matches Home/Register)
    val cs = MaterialTheme.colorScheme
    val isDark = cs.background.luminance() < 0.3f

    val accentA = if (isDark) Color(0xFF7C4DFF) else Color(0xFF5B5FEF)
    val accentB = if (isDark) Color(0xFF00E5FF) else Color(0xFF00B8D4)

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
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Workouts", fontWeight = FontWeight.Black)
                            Text(
                                text = "Plan • track • complete",
                                style = MaterialTheme.typography.labelMedium,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onBackground
                ),
                actions = {
                    // purely visual, like your Home/Register: you can remove if you don’t want it
                    FilledTonalIconButton(
                        onClick = { /* no-op here */ },
                        enabled = false,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        },
        floatingActionButton = {
            // Gradient FAB (fits the aurora theme)
            FloatingActionButton(
                onClick = { showCreate = true },
                containerColor = Color.Transparent,
                contentColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(accentA, accentB))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add workout")
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                // 🔥 Aurora summary header (matches the vibe of Home cards)
                WorkoutsAuroraSummary(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    accentA = accentA,
                    accentB = accentB,
                    glass = glassStrong,
                    outlineSoft = outlineSoft,
                    total = state.workouts.size,
                    done = state.workouts.count { it.isCompleted }
                )

                when {
                    state.loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 24.dp),
                            color = accentA
                        )
                    }

                    state.workouts.isEmpty() -> {
                        EmptyAuroraState(
                            title = "No workouts yet",
                            subtitle = "Tap + to create your first workout.",
                            accentA = accentA,
                            accentB = accentB,
                            glass = glass,
                            outlineSoft = outlineSoft,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 22.dp)
                        ) {
                            item { Spacer(Modifier.height(2.dp)) }

                            items(state.workouts, key = { it.id }) { workout ->
                                WorkoutAuroraCard(
                                    workout = workout,
                                    accentA = accentA,
                                    accentB = accentB,
                                    glass = glass,
                                    outlineSoft = outlineSoft,
                                    onClick = { navController.navigate(Routes.workoutDetail(workout.id)) },
                                    onCheckedChange = { checked ->
                                        viewModel.setWorkoutCompleted(workout.id, checked)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ✅ CREATE WORKOUT POPUP (unchanged)
            if (showCreate) {
                WorkoutCreateDialog(
                    onDismiss = { showCreate = false },
                    onCreate = { name, day, durationText, instructions, requiredEquipmentText ->
                        viewModel.createWorkoutWithExercises(
                            name = name,
                            day = day,
                            durationText = durationText,
                            instructions = instructions,
                            requiredEquipmentText = requiredEquipmentText,
                            imageUri = null,
                            selectedItems = emptyList(),
                            onDone = { showCreate = false }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkoutsAuroraSummary(
    modifier: Modifier = Modifier,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    total: Int,
    done: Int
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val progress = if (total <= 0) 0f else (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Surface(
        modifier = modifier,
        shape = shape,
        color = glass,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineSoft, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accentA.copy(alpha = 0.18f),
                            accentB.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Weekly overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = cs.onSurface
                    )
                    Text(
                        text = "$done of $total completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(cs.surfaceVariant.copy(alpha = 0.55f))
                        .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentA
                    )
                }
            }

            // progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(cs.onSurface.copy(alpha = 0.06f))
                    .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Brush.linearGradient(listOf(accentA, accentB)))
                )
            }

            // accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(accentA, accentB)))
            )
        }
    }
}

@Composable
private fun WorkoutAuroraCard(
    workout: Workout,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = glass),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, outlineSoft, shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = workout.isCompleted,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = accentA,
                        uncheckedColor = cs.onSurface.copy(alpha = 0.35f),
                        checkmarkColor = Color.White
                    )
                )

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = workout.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cs.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // status pill
                        val pillBg = if (workout.isCompleted) {
                            Brush.linearGradient(listOf(accentA.copy(alpha = 0.18f), accentB.copy(alpha = 0.12f)))
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(pillBg)
                                .border(
                                    border = BorderStroke(
                                        1.dp,
                                        if (workout.isCompleted) accentA else cs.onSurface.copy(alpha = 0.18f)
                                    ),
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = if (workout.isCompleted) accentA else cs.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (workout.isCompleted) "DONE" else "TO DO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (workout.isCompleted) accentA else cs.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoPillAurora(text = workout.day, outlineSoft = outlineSoft)
                        InfoPillAurora(text = "${workout.durationMin} min", outlineSoft = outlineSoft)
                        InfoPillAurora(text = "${workout.items.size} items", outlineSoft = outlineSoft)
                    }
                }
            }

            // subtle divider + accent hint
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(cs.onSurface.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(accentA.copy(alpha = 0.5f), accentB.copy(alpha = 0.35f))))
            )
        }
    }
}

@Composable
private fun InfoPillAurora(
    text: String,
    outlineSoft: Color
) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(cs.surfaceVariant.copy(alpha = 0.55f))
            .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = cs.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
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
        modifier = modifier,
        shape = shape,
        color = glass,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineSoft, shape)
                .background(
                    Brush.linearGradient(
                        listOf(accentA.copy(alpha = 0.14f), accentB.copy(alpha = 0.10f), Color.Transparent)
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
