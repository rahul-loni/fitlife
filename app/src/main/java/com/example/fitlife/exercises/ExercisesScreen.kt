package com.example.fitlife.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    vm: ExerciseViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // ✅ Aurora theme (keep SAME vibe)
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

    // ✅ current user check
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    if (uid == null) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Exercises", fontWeight = FontWeight.Black) },
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
                    title = "Not logged in",
                    subtitle = "Please login again.",
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

    DisposableEffect(uid) {
        vm.startListening()
        onDispose { vm.stopListening() }
    }

    // Form state (same logic)
    var name by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf("General") }
    var sets by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var muscleMenu by remember { mutableStateOf(false) }

    // NEW: collapsible form
    var showForm by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(targetValue = if (showForm) 180f else 0f, label = "chev")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Exercises", fontWeight = FontWeight.Black) },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // ✅ New Header Row (count + add toggle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Your Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = cs.onBackground
                    )
                    Text(
                        "Save exercises & track completion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }

                FitPill(
                    text = "${state.exercises.size}",
                    accentA = accentA,
                    accentB = accentB,
                    outlineSoft = outlineSoft
                )

                Spacer(Modifier.width(10.dp))

                FilledTonalIconButton(
                    onClick = { showForm = !showForm },
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Toggle form",
                        modifier = Modifier.rotate(chevronRotation)
                    )
                }
            }

            // ✅ message chip (same logic)
            state.message?.let { msg ->
                Surface(
                    color = cs.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, outlineSoft, RoundedCornerShape(16.dp))
                        .clickable { vm.clearMessage() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = accentA)
                        Spacer(Modifier.width(8.dp))
                        Text(msg, color = cs.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "tap to clear",
                            color = cs.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // ✅ Collapsible Add Exercise card (same theme, better UX)
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(accentA, accentB))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White)
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                "Add Exercise",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = cs.onSurface
                            )
                            Text(
                                "Name, muscle group, sets & reps",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }

                        FitPill(
                            text = "NEW",
                            accentA = accentA,
                            accentB = accentB,
                            outlineSoft = outlineSoft
                        )
                    }

                    AnimatedVisibility(visible = showForm) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Exercise name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = auroraTextFieldColors(accentA, outlineSoft)
                            )

                            ExposedDropdownMenuBox(
                                expanded = muscleMenu,
                                onExpandedChange = { muscleMenu = !muscleMenu }
                            ) {
                                OutlinedTextField(
                                    value = muscle,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Muscle group") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    singleLine = true,
                                    colors = auroraTextFieldColors(accentA, outlineSoft),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = muscleMenu)
                                    }
                                )
                                ExposedDropdownMenu(
                                    expanded = muscleMenu,
                                    onDismissRequest = { muscleMenu = false }
                                ) {
                                    listOf("General", "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Cardio")
                                        .forEach { opt ->
                                            DropdownMenuItem(
                                                text = { Text(opt) },
                                                onClick = {
                                                    muscle = opt
                                                    muscleMenu = false
                                                }
                                            )
                                        }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = sets,
                                    onValueChange = { sets = it.filter(Char::isDigit) },
                                    label = { Text("Sets") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = auroraTextFieldColors(accentA, outlineSoft)
                                )
                                OutlinedTextField(
                                    value = reps,
                                    onValueChange = { reps = it.filter(Char::isDigit) },
                                    label = { Text("Reps") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = auroraTextFieldColors(accentA, outlineSoft)
                                )
                            }

                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notes (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = auroraTextFieldColors(accentA, outlineSoft)
                            )

                            Button(
                                onClick = {
                                    vm.addExercise(name, muscle, sets, reps, notes)
                                    name = ""
                                    sets = ""
                                    reps = ""
                                    notes = ""
                                },
                                enabled = name.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                                ),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Brush.linearGradient(listOf(accentA, accentB)))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Save Exercise", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ✅ List header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "My Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    if (state.exercises.isEmpty()) "" else "Tap item for details",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            when {
                state.loading -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = accentA)
                    }
                }

                state.exercises.isEmpty() -> {
                    EmptyAuroraState(
                        title = "No exercises yet",
                        subtitle = "Create your first exercise above.",
                        accentA = accentA,
                        accentB = accentB,
                        glass = glassStrong,
                        outlineSoft = outlineSoft
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 26.dp)
                    ) {
                        items(state.exercises, key = { it.id }) { ex ->
                            ExerciseCardAuroraV2(
                                ex = ex,
                                accentA = accentA,
                                accentB = accentB,
                                glass = glass,
                                outlineSoft = outlineSoft,
                                danger = danger,
                                onDoneToggle = { v -> vm.toggleDone(ex.id, v) },
                                onDelete = { vm.deleteExercise(ex.id) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ExerciseCardAuroraV2(
    ex: Exercise,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    danger: Color,
    onDoneToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val checked = ex.isCompleted

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

            // top row: image + main info + status chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                val img = ex.image
                if (!img.isNullOrBlank()) {
                    AsyncImage(
                        model = img,
                        contentDescription = "Exercise image",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(cs.surfaceVariant.copy(alpha = 0.55f))
                            .border(1.dp, outlineSoft, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = cs.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        ex.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniChip(text = ex.muscleGroup, outlineSoft = outlineSoft, tint = accentA)
                        MiniChip(text = "${ex.sets} sets", outlineSoft = outlineSoft, tint = accentB)
                        MiniChip(text = "${ex.reps} reps", outlineSoft = outlineSoft, tint = accentA)
                    }
                }

                StatusChip(
                    checked = checked,
                    accentA = accentA,
                    accentB = accentB,
                    outlineSoft = outlineSoft
                )
            }

            if (ex.notes.isNotBlank()) {
                Surface(
                    color = cs.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, outlineSoft, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = ex.notes,
                        color = cs.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 3
                    )
                }
            }

            // actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier
                        .toggleable(
                            value = checked,
                            role = Role.Checkbox,
                            onValueChange = { onDoneToggle(it) }
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = accentA,
                            uncheckedColor = cs.onSurface.copy(alpha = 0.30f),
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Completed",
                        color = if (checked) accentA else cs.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = danger)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Delete")
                }
            }

            // gradient divider
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

/* -------------------- UI bits -------------------- */

@Composable
private fun StatusChip(
    checked: Boolean,
    accentA: Color,
    accentB: Color,
    outlineSoft: Color
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (checked)
                    Brush.linearGradient(listOf(accentA.copy(alpha = 0.18f), accentB.copy(alpha = 0.10f)))
                else
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .border(
                BorderStroke(1.dp, if (checked) accentA.copy(alpha = 0.55f) else outlineSoft),
                shape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (checked) accentA else cs.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (checked) "DONE" else "TO DO",
                color = if (checked) accentA else cs.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MiniChip(
    text: String,
    outlineSoft: Color,
    tint: Color
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .border(1.dp, outlineSoft, shape)
            .background(tint.copy(alpha = 0.10f), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = tint)
    }
}

/* -------------------- Aurora helpers (same as before) -------------------- */

@Composable
private fun AuroraCard(
    accentA: Color,
    accentB: Color,
    outlineSoft: Color,
    glass: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
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
            Column(content = content)
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
            Text(subtitle, color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
        }
    }
}

@Composable
private fun FitPill(
    text: String,
    accentA: Color,
    accentB: Color,
    outlineSoft: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Brush.linearGradient(listOf(accentA, accentB)))
            .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun auroraTextFieldColors(accent: Color, outlineSoft: Color) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent.copy(alpha = 0.65f),
        unfocusedBorderColor = outlineSoft,
        focusedLabelColor = accent,
        cursorColor = accent,
        focusedLeadingIconColor = accent,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
