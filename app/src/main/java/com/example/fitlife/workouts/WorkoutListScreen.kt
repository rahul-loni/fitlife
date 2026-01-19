package com.example.fitlife.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    navController: NavController,
    vm: WorkoutsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    DisposableEffect(Unit) {
        vm.startListening()
        onDispose { vm.stopListening() }
    }

    // Dark Neon palette
    val bg = Color(0xFF070A0F)
    val surface = Color(0xFF0B1020)
    val neon = Color(0xFF00FF7A)
    val neonSoft = neon.copy(alpha = 0.14f)
    val text = Color(0xFFEAF2FF)
    val muted = Color(0xFF9AA7C0)
    val danger = Color(0xFFFF4D4D)

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { Text("My Workouts", color = text, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = text
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
        ) {

            when {
                state.loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = neon
                    )
                }

                state.workouts.isEmpty() -> {
                    EmptyNeonState(
                        title = "No workouts yet",
                        subtitle = "Create one in Workout Plan.",
                        neon = neon,
                        bg = surface,
                        text = text,
                        muted = muted,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        state.error?.let {
                            Text(
                                text = it,
                                color = danger,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 18.dp)
                        ) {
                            items(state.workouts, key = { it.id }) { w ->
                                WorkoutListNeonCard(
                                    workout = w,
                                    neon = neon,
                                    neonSoft = neonSoft,
                                    bg = surface,
                                    text = text,
                                    muted = muted,
                                    danger = danger,
                                    onClick = { navController.navigate("workout_detail/${w.id}") },
                                    onCheckedChange = { vm.setWorkoutCompleted(w.id, it) },
                                    onDelete = { vm.deleteWorkout(w.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutListNeonCard(
    workout: Workout,
    neon: Color,
    neonSoft: Color,
    bg: Color,
    text: Color,
    muted: Color,
    danger: Color,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Card(
        onClick = onClick,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = bg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, neon.copy(alpha = 0.35f), shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = workout.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = neon,
                    uncheckedColor = neon.copy(alpha = 0.55f),
                    checkmarkColor = Color.Black
                )
            )

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = workout.name,
                        color = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Status pill
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (workout.isCompleted) neonSoft else Color.Transparent,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (workout.isCompleted) neon else neon.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (workout.isCompleted) "DONE" else "TO DO",
                            color = if (workout.isCompleted) neon else muted,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "${workout.day} • ${workout.durationMin} min",
                    color = muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Items: ${workout.items.size}",
                    color = muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = danger)
            }
        }
    }
}

@Composable
private fun EmptyNeonState(
    title: String,
    subtitle: String,
    neon: Color,
    bg: Color,
    text: Color,
    muted: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .padding(16.dp)
            .border(1.dp, neon.copy(alpha = 0.35f), shape)
            .background(bg, shape)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, color = text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, color = muted, style = MaterialTheme.typography.bodyMedium)
    }
}
