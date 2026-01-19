package com.example.fitlife.workouts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutItemCard(
    item: WorkoutItem,
    onToggleDone: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // ✅ Swipe right (StartToEnd) to toggle done
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    // swipe right
                    onToggleDone(!item.isDone)
                    true
                }
                else -> false
            }
        }
    )

    // ✅ Reset swipe after action so it doesn't stay dismissed
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,   // ✅ swipe right enabled
        enableDismissFromEndToStart = false,  // disable swipe left (optional)
        backgroundContent = {
            // background shown while swiping
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(if (item.isDone) "Mark Undone" else "Mark Done")
            }
        }
    ) {
        ElevatedCard(
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isDone,
                    onCheckedChange = { onToggleDone(it) }
                )

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${item.sets} sets • ${item.reps} reps",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
