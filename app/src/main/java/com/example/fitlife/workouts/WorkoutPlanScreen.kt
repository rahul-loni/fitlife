package com.example.fitlife.workouts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanScreen(
    navController: NavController,
    workoutVm: WorkoutsViewModel
) {
    val state by workoutVm.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Start/stop listening
    DisposableEffect(Unit) {
        workoutVm.startListening()
        onDispose { workoutVm.stopListening() }
    }

    // Create dialog state
    var showCreate by remember { mutableStateOf(false) }

    // Create form states (used by dialog + save)
    var name by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("Monday") }
    var duration by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var equipmentText by remember { mutableStateOf("") }

    // ✅ Image picker state
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    // ✅ Exercise selection for "new workout"
    // We store selected exercise IDs here; you can also store WorkoutItem if you want.
    val selectedExerciseIds = remember { mutableStateListOf<String>() }

    // Optional: show errors from VM
    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Workout Plan") },
                actions = {
                    TextButton(onClick = { showCreate = true }) { Text("New") }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp)
        ) {

            if (state.loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            if (state.workouts.isEmpty() && !state.loading) {
                Text("No workouts yet. Tap New to create one.")
                Spacer(Modifier.height(12.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.workouts, key = { it.id }) { w ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("workout_detail/${w.id}") }
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(w.name.ifBlank { "Workout" }, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                AssistChip(onClick = {}, label = { Text(w.day) })
                                AssistChip(onClick = {}, label = { Text("${w.durationMin} min") })
                                AssistChip(onClick = {}, label = { Text("${w.items.size} items") })
                            }
                            if (!w.imageUrl.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "✅ Image attached",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ✅ Create dialog (full creation flow with image + pick exercises)
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Create Workout Routine") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Workout name") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it },
                        label = { Text("Day (e.g., Monday)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (minutes)") },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions") }
                    )

                    OutlinedTextField(
                        value = equipmentText,
                        onValueChange = { equipmentText = it },
                        label = { Text("Required equipment (comma separated)") }
                    )

                    // ✅ Pick Image
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { pickImageLauncher.launch("image/*") }) {
                            Text(if (selectedImageUri == null) "Pick Image" else "Change Image")
                        }
                        Text(
                            text = if (selectedImageUri == null) "No image" else "✅ Image selected",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Divider()

                    // ✅ Pick exercises for this routine (navigate to picker)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Exercises selected: ${selectedExerciseIds.size}")
                        TextButton(
                            onClick = {
                                // We create a TEMP workout first? ❌ Not needed.
                                // Instead: open picker screen in "selection mode" is harder.
                                // So here we just let user add exercises AFTER creating workout.
                                // This keeps your architecture simple & avoids crashes.
                                scope.launch {
                                    snackbarHostState.showSnackbar("Create workout first, then Add exercises inside it.")
                                }
                            }
                        ) { Text("+ Add") }
                    }

                    Text(
                        "Tip: Save routine first, then open it and use +Add in Exercises section.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        workoutVm.createWorkoutWithExercises(
                            name = name,
                            day = day,
                            durationText = duration,
                            instructions = instructions,
                            requiredEquipmentText = equipmentText,
                            imageUri = selectedImageUri,              // ✅ PASS IMAGE
                            selectedItems = emptyList(),              // ✅ start empty, add later from picker
                            onDone = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Workout routine saved ✅")
                                }
                                // reset fields
                                name = ""
                                day = "Monday"
                                duration = ""
                                instructions = ""
                                equipmentText = ""
                                selectedImageUri = null
                                selectedExerciseIds.clear()

                                showCreate = false
                            }
                        )
                    }
                ) { Text("Save Workout Routine") }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) { Text("Cancel") }
            }
        )
    }
}
