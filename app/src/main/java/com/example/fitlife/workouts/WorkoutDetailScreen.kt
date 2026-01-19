package com.example.fitlife.workouts

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fitlife.reminders.ReminderScheduler
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: String,
    vm: WorkoutsViewModel,
    navController: NavController
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // ✅ Aurora Glass theme (matches Home/Register/Workouts)
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

    val danger = if (isDark) Color(0xFFFF6B6B) else Color(0xFFD32F2F)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Android 13+ notification permission
    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(Unit) {
        vm.startListening()
        onDispose { vm.stopListening() }
    }

    val workout = state.workouts.firstOrNull { it.id == workoutId }

    // ✅ optimistic done state to avoid UI flicker while Firestore updates
    val doneOverrides = remember { mutableStateMapOf<String, Boolean>() }

    // ✅ cleanup overrides once Firestore matches
    LaunchedEffect(workout?.items) {
        workout?.items?.forEach { it ->
            val override = doneOverrides[it.id]
            if (override != null && override == it.isDone) {
                doneOverrides.remove(it.id)
            }
        }
    }

    // ✅ SHAKE: reset workout -> remove ticks AND remove completed
    val onShakeAction by rememberUpdatedState(
        newValue = {
            doneOverrides.clear()
            vm.resetWorkoutItems(workoutId)
            scope.launch { snackbarHostState.showSnackbar("Workout reset ✅") }
        }
    )

    // ✅ Lifecycle-aware sensor register/unregister so it DOESN'T run on Home
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accel = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // ✅ anti-spam cooldown to prevent freezes from repeated shakes
    val lastShakeMs = remember { mutableLongStateOf(0L) }
    val cooldownMs = 1200L

    val detector = remember {
        ShakeDetector(
            threshold = 2.2f,
            slopTimeMs = 700L
        ) {
            val now = System.currentTimeMillis()
            if (now - lastShakeMs.longValue >= cooldownMs) {
                lastShakeMs.longValue = now
                onShakeAction()
            }
        }
    }

    DisposableEffect(lifecycleOwner, workoutId) {
        if (accel == null) {
            scope.launch { snackbarHostState.showSnackbar("No accelerometer found") }
            onDispose { }
        } else {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        sensorManager.registerListener(detector, accel, SensorManager.SENSOR_DELAY_UI)
                    }
                    Lifecycle.Event.ON_PAUSE,
                    Lifecycle.Event.ON_STOP -> {
                        sensorManager.unregisterListener(detector)
                    }
                    else -> Unit
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                sensorManager.unregisterListener(detector)
            }
        }
    }

    // SMS body computed when workout changes
    val smsBody = remember(workout) {
        if (workout == null) "" else buildString {
            append("Workout: ${workout.name}\n")
            append("Day: ${workout.day}\n")
            append("Duration: ${workout.durationMin} min\n\n")

            if (workout.requiredEquipment.isNotEmpty()) {
                append("Required Equipment:\n")
                workout.requiredEquipment.forEach { append("- $it\n") }
                append("\n")
            }

            append("Checklist:\n")
            workout.items.sortedBy { it.order }.forEach { item ->
                val effectiveDone = doneOverrides[item.id] ?: item.isDone
                val mark = if (effectiveDone) "✅" else "⬜"
                append("$mark ${item.title} (Sets ${item.sets} x Reps ${item.reps})\n")
            }

            if (workout.instructions.isNotBlank()) {
                append("\nInstructions:\n${workout.instructions}\n")
            }
        }
    }

    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = workout?.name ?: "Workout",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Black
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onBackground
                ),
                actions = {
                    FilledTonalIconButton(
                        onClick = { confirmDelete = true },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = danger
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    Spacer(Modifier.width(10.dp))
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
        ) {

            if (confirmDelete) {
                AlertDialog(
                    onDismissRequest = { confirmDelete = false },
                    title = { Text("Delete workout?") },
                    text = { Text("This will permanently delete this workout.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                confirmDelete = false
                                vm.deleteWorkout(
                                    workoutId = workoutId,
                                    onDone = { navController.popBackStack() },
                                    onError = { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                )
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = danger)
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
                    }
                )
            }

            // Loading / Not found
            if (workout == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.loading) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = accentA)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Loading workout…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = cs.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Workout not found.", fontStyle = FontStyle.Italic, color = cs.onSurface)
                            state.error?.let {
                                Spacer(Modifier.height(8.dp))
                                Text(it, color = danger)
                            }
                        }
                    }
                }
                return@Scaffold
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 12.dp)
            ) {

                // Hero card (image + title + quick info)
                item {
                    AuroraCard(
                        accentA = accentA,
                        accentB = accentB,
                        outlineSoft = outlineSoft,
                        glass = glassStrong,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!workout.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = workout.imageUrl,
                                    contentDescription = "Workout image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(cs.surfaceVariant.copy(alpha = 0.55f))
                                        .border(1.dp, outlineSoft, RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No image added", color = cs.onSurfaceVariant)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AuroraInfoPill(
                                    icon = Icons.Default.CalendarToday,
                                    title = "Day",
                                    value = workout.day,
                                    outlineSoft = outlineSoft,
                                    modifier = Modifier.weight(1f)
                                )
                                AuroraInfoPill(
                                    icon = Icons.Default.Timer,
                                    title = "Duration",
                                    value = "${workout.durationMin} min",
                                    outlineSoft = outlineSoft,
                                    modifier = Modifier.weight(1f)
                                )
                                AuroraInfoPill(
                                    icon = Icons.Default.Work,
                                    title = "Items",
                                    value = "${workout.items.size}",
                                    outlineSoft = outlineSoft,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Summary actions card
                item {
                    AuroraCard(
                        accentA = accentA,
                        accentB = accentB,
                        outlineSoft = outlineSoft,
                        glass = glass,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = { vm.setWorkoutCompleted(workout.id, !workout.isCompleted) },
                                    label = { Text(if (workout.isCompleted) "Completed ✅" else "Mark Completed") },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                                        labelColor = if (workout.isCompleted) accentA else cs.onSurface,
                                        leadingIconContentColor = if (workout.isCompleted) accentA else cs.onSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (workout.isCompleted) accentA.copy(alpha = 0.55f) else outlineSoft
                                    )
                                )

                                Spacer(Modifier.weight(1f))

                                FilledTonalButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("smsto:")
                                            putExtra("sms_body", smsBody)
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                                        contentColor = cs.onSurface
                                    )
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("SMS")
                                }

                                Button(
                                    onClick = {
                                        ReminderScheduler.scheduleReminder(
                                            context = context,
                                            title = "Workout Reminder",
                                            message = "Time to do workout: ${workout.name}",
                                            delayMillis = 10_000L
                                        )
                                    },
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
                                            .background(Brush.linearGradient(listOf(accentA, accentB)))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Remind", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (workout.instructions.isNotBlank()) {
                                AuroraCard(
                                    accentA = accentA,
                                    accentB = accentB,
                                    outlineSoft = outlineSoft,
                                    glass = cs.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier.fillMaxWidth(),
                                    inner = true
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(
                                            "Instructions",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = accentA,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            workout.instructions,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = cs.onSurface.copy(alpha = 0.95f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { AuroraSectionHeader(title = "Required Equipment", trailing = null) }

                item {
                    AuroraCard(
                        accentA = accentA,
                        accentB = accentB,
                        outlineSoft = outlineSoft,
                        glass = glass,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (workout.requiredEquipment.isEmpty()) {
                                Text("None", fontStyle = FontStyle.Italic, color = cs.onSurfaceVariant)
                            } else {
                                workout.requiredEquipment.forEach { eq -> BulletRowAurora(text = eq) }
                            }
                        }
                    }
                }

                item {
                    AuroraSectionHeader(
                        title = "Exercises",
                        trailing = {
                            TextButton(
                                onClick = { navController.navigate("exercise_picker/${workout.id}") },
                                colors = ButtonDefaults.textButtonColors(contentColor = accentA)
                            ) { Text("+ Add") }
                        }
                    )
                }

                if (workout.items.isEmpty()) {
                    item { Text("No exercises in this workout.", fontStyle = FontStyle.Italic, color = cs.onSurfaceVariant) }
                } else {
                    items(workout.items.sortedBy { it.order }, key = { it.id }) { item ->

                        val localDone = doneOverrides[item.id] ?: item.isDone

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                when (value) {
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        val newValue = !localDone
                                        doneOverrides[item.id] = newValue
                                        vm.setItemDone(workout.id, item.id, newValue)
                                        false
                                    }
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        vm.deleteWorkoutItem(workout.id, item.id)
                                        true
                                    }
                                    else -> false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                val dir = dismissState.dismissDirection
                                val swipeShape = RoundedCornerShape(18.dp)

                                // ✅ FIX: always return a Brush (no mixed Brush/Color types)
                                val swipeBrush: Brush = when (dir) {
                                    SwipeToDismissBoxValue.StartToEnd ->
                                        Brush.linearGradient(
                                            listOf(
                                                accentA.copy(alpha = 0.14f),
                                                accentB.copy(alpha = 0.10f)
                                            )
                                        )
                                    SwipeToDismissBoxValue.EndToStart ->
                                        Brush.linearGradient(
                                            listOf(
                                                danger.copy(alpha = 0.14f),
                                                danger.copy(alpha = 0.14f)
                                            )
                                        )
                                    else ->
                                        Brush.linearGradient(
                                            listOf(Color.Transparent, Color.Transparent)
                                        )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .clip(swipeShape)
                                        .background(swipeBrush)
                                        .border(
                                            1.dp,
                                            when (dir) {
                                                SwipeToDismissBoxValue.StartToEnd -> accentA.copy(alpha = 0.45f)
                                                SwipeToDismissBoxValue.EndToStart -> danger.copy(alpha = 0.55f)
                                                else -> Color.Transparent
                                            },
                                            swipeShape
                                        ),
                                    contentAlignment = when (dir) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                ) {
                                    Text(
                                        text = when (dir) {
                                            SwipeToDismissBoxValue.StartToEnd ->
                                                if (localDone) "↩ Mark Undone" else "✔ Mark Done"
                                            SwipeToDismissBoxValue.EndToStart -> "🗑 Delete"
                                            else -> ""
                                        },
                                        color = when (dir) {
                                            SwipeToDismissBoxValue.StartToEnd -> accentA
                                            SwipeToDismissBoxValue.EndToStart -> danger
                                            else -> cs.onSurfaceVariant
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        ) {
                            ExerciseItemCardAurora(
                                title = item.title,
                                subtitle = "Sets: ${item.sets} • Reps: ${item.reps}",
                                notes = item.notes,
                                checked = localDone,
                                accentA = accentA,
                                accentB = accentB,
                                glass = glass,
                                outlineSoft = outlineSoft,
                                onCheckedChange = { done ->
                                    doneOverrides[item.id] = done
                                    vm.setItemDone(workout.id, item.id, done)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/* -------------------- Aurora UI helpers -------------------- */

@Composable
private fun AuroraCard(
    accentA: Color,
    accentB: Color,
    outlineSoft: Color,
    glass: Color,
    modifier: Modifier = Modifier,
    inner: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = if (inner) RoundedCornerShape(18.dp) else RoundedCornerShape(24.dp)
    val shadow = if (inner) 0.dp else 10.dp
    val tonal = if (inner) 0.dp else 8.dp

    Surface(
        modifier = modifier,
        shape = shape,
        color = glass,
        tonalElevation = tonal,
        shadowElevation = shadow
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, outlineSoft, shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            accentA.copy(alpha = if (inner) 0.10f else 0.16f),
                            accentB.copy(alpha = if (inner) 0.07f else 0.12f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun AuroraInfoPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    outlineSoft: Color,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Surface(
        modifier = modifier.border(1.dp, outlineSoft, shape),
        shape = shape,
        color = cs.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleSmall, color = cs.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AuroraSectionHeader(
    title: String,
    trailing: (@Composable (() -> Unit))?
) {
    val cs = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = cs.onSurface, fontWeight = FontWeight.SemiBold)
        trailing?.invoke()
    }
}

@Composable
private fun BulletRowAurora(text: String) {
    val cs = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Text("•  ", style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
    }
}

@Composable
private fun ExerciseItemCardAurora(
    title: String,
    subtitle: String,
    notes: String?,
    checked: Boolean,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)

    Card(
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = accentA,
                        uncheckedColor = cs.onSurface.copy(alpha = 0.30f),
                        checkmarkColor = Color.White
                    )
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = cs.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                    notes?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                AssistChip(
                    onClick = { onCheckedChange(!checked) },
                    label = { Text(if (checked) "Done" else "To do") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                        labelColor = if (checked) accentA else cs.onSurfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (checked) accentA.copy(alpha = 0.45f) else outlineSoft
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (checked) Brush.linearGradient(listOf(accentA.copy(alpha = 0.12f), accentB.copy(alpha = 0.08f)))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Brush.linearGradient(listOf(accentA.copy(alpha = 0.55f), accentB.copy(alpha = 0.35f))))
            )
        }
    }
}

/**
 * ✅ Shake detector using gForce
 */
private class ShakeDetector(
    private val threshold: Float = 2.2f,
    private val slopTimeMs: Long = 700L,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastShakeTime = 0L

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)

        if (gForce > threshold) {
            val now = System.currentTimeMillis()
            if (now - lastShakeTime > slopTimeMs) {
                lastShakeTime = now
                onShake()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
