package com.example.fitlife.exercises

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitlife.workouts.WorkoutsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class PickerExercise(
    val id: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    navController: NavController,
    workoutId: String,
    vm: WorkoutsViewModel
) {
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

    // ---- original logic unchanged ----
    if (workoutId.isBlank()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Pick Exercises", fontWeight = FontWeight.Black) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = cs.onBackground
                    )
                )
            }
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(bgBrush),
                contentAlignment = Alignment.Center
            ) {
                EmptyAuroraState(
                    title = "Invalid workout",
                    subtitle = "Go back and try again.",
                    accentA = danger,
                    accentB = danger,
                    glass = glassStrong,
                    outlineSoft = outlineSoft
                )
            }
        }
        return
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var list by remember { mutableStateOf<List<PickerExercise>>(emptyList()) }
    val selectedIds = remember { mutableStateListOf<String>() }

    var query by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uid) {
        loading = true
        error = null

        if (uid == null) {
            error = "Please log in"
            loading = false
            return@LaunchedEffect
        }

        runCatching {
            val snap = db.collection("users").document(uid).collection("exercises").get().await()
            list = snap.documents.map { d ->
                PickerExercise(
                    id = d.id,
                    name = d.getString("name") ?: d.getString("title") ?: "Exercise"
                )
            }.sortedBy { it.name }
        }.onFailure { e ->
            error = e.message ?: "Failed to load exercises"
        }

        loading = false
    }

    val filtered = remember(list, query) {
        val q = query.trim().lowercase()
        if (q.isBlank()) list else list.filter { it.name.lowercase().contains(q) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pick Exercises", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    FilledTonalIconButton(
                        enabled = !saving,
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = cs.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                actions = {
                    val canAdd = selectedIds.isNotEmpty() && !saving

                    Button(
                        enabled = canAdd,
                        onClick = {
                            saving = true
                            vm.addExercisesToWorkout(
                                workoutId = workoutId,
                                exerciseIds = selectedIds.toList()
                            ) { ok, msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                                saving = false
                                if (ok) navController.popBackStack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = cs.surfaceVariant.copy(alpha = 0.45f),
                            disabledContentColor = cs.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (canAdd) Brush.linearGradient(listOf(accentA, accentB))
                                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (saving) "Adding…" else "Add (${selectedIds.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onBackground
                )
            )
        }
    ) { padding ->

        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(bgBrush)
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentA
                    )
                }

                error != null -> {
                    EmptyAuroraState(
                        title = "Couldn’t load exercises",
                        subtitle = error!!,
                        accentA = danger,
                        accentB = danger,
                        glass = glassStrong,
                        outlineSoft = outlineSoft,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                list.isEmpty() -> {
                    EmptyAuroraState(
                        title = "No exercises found",
                        subtitle = "Create exercises first, then come back here.",
                        accentA = accentA,
                        accentB = accentB,
                        glass = glassStrong,
                        outlineSoft = outlineSoft,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                else -> {
                    Column(Modifier.fillMaxSize()) {

                        // ✅ Search + selected header
                        AuroraCard(
                            accentA = accentA,
                            accentB = accentB,
                            outlineSoft = outlineSoft,
                            glass = glassStrong,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Selected: ${selectedIds.size}",
                                        color = cs.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(Modifier.weight(1f))

                                    if (selectedIds.isNotEmpty()) {
                                        AssistChip(
                                            onClick = { /* visual */ },
                                            label = { Text("MULTI-SELECT") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.SelectAll,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                                                labelColor = accentA,
                                                leadingIconContentColor = accentA
                                            ),
                                            border = BorderStroke(1.dp, accentA.copy(alpha = 0.35f))
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    enabled = !saving,
                                    placeholder = { Text("Search exercises…") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = accentA.copy(alpha = 0.55f),
                                        unfocusedBorderColor = outlineSoft,
                                        focusedLeadingIconColor = accentA,
                                        unfocusedLeadingIconColor = cs.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtered, key = { it.id }) { ex ->
                                val isSelected = selectedIds.contains(ex.id)
                                ExercisePickAuroraRow(
                                    name = ex.name,
                                    selected = isSelected,
                                    accentA = accentA,
                                    accentB = accentB,
                                    glass = glass,
                                    outlineSoft = outlineSoft,
                                    onToggle = {
                                        if (isSelected) selectedIds.remove(ex.id)
                                        else selectedIds.add(ex.id)
                                    }
                                )
                            }

                            item { Spacer(Modifier.height(18.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePickAuroraRow(
    name: String,
    selected: Boolean,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    onToggle: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = glass),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (selected) accentA.copy(alpha = 0.45f) else outlineSoft,
                shape
            )
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = accentA,
                    uncheckedColor = cs.onSurface.copy(alpha = 0.30f),
                    checkmarkColor = Color.White
                )
            )

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = cs.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (selected) "Selected" else "Tap to select",
                    color = if (selected) accentA else cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // status pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected)
                            Brush.linearGradient(listOf(accentA.copy(alpha = 0.14f), accentB.copy(alpha = 0.10f)))
                        else
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(
                        BorderStroke(1.dp, if (selected) accentA else outlineSoft),
                        RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (selected) accentA else cs.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (selected) "ON" else "OFF",
                        color = if (selected) accentA else cs.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
