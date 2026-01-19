package com.example.fitlife.delegation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitlife.workouts.WorkoutsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDelegationScreen(navController: NavController, workoutId: String, vm: WorkoutsViewModel)
 {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Make sure workouts are loaded
    DisposableEffect(Unit) {
        vm.startListening()
        onDispose { vm.stopListening() }
    }

    val workout = state.workouts.firstOrNull { it.id == workoutId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Delegation") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.loading) {
                CircularProgressIndicator()
                return@Column
            }

            if (workout == null) {
                Text("Workout not found.", fontStyle = FontStyle.Italic)
                return@Column
            }

            Text(workout.name, style = MaterialTheme.typography.titleLarge)

            // Build SMS text (equipment + checklist + instructions)
            val smsBody = remember(workout) {
                buildString {
                    append("Workout: ${workout.name}\n")
                    append("Day: ${workout.day}\n")
                    append("Duration: ${workout.durationMin} min\n\n")

                    if (workout.requiredEquipment.isNotEmpty()) {
                        append("Required Equipment:\n")
                        workout.requiredEquipment.forEach { append("- $it\n") }
                        append("\n")
                    }

                    append("Exercise Checklist:\n")
                    workout.items.sortedBy { it.order }.forEach { item ->
                        val mark = if (item.isDone) "✅" else "⬜"
                        append("$mark ${item.title} (Sets ${item.sets} x Reps ${item.reps})\n")
                    }

                    if (workout.instructions.isNotBlank()) {
                        append("\nInstructions:\n${workout.instructions}\n")
                    }
                }
            }

            OutlinedTextField(
                value = smsBody,
                onValueChange = { /* read-only */ },
                label = { Text("Message preview") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp)
            )

            Button(
                onClick = {
                    // Opens SMS app (no permission needed)
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:") // user picks contact
                        putExtra("sms_body", smsBody)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send via SMS")
            }

            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}
