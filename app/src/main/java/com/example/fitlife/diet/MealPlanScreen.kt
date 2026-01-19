package com.example.fitlife.diet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

object MealRoutes {
    const val MEALS = "meals"
    const val ADD_MEAL = "add_meal"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    navController: NavController,
    vm: MealViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Firestore listener life-cycle (same)
    LaunchedEffect(Unit) { vm.startListening() }
    DisposableEffect(Unit) { onDispose { vm.stopListening() } }

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
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.padding(6.dp))
                        Text("Meal Plan", fontWeight = FontWeight.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = cs.onBackground
                ),
                actions = {
                    // Count pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(cs.surfaceVariant.copy(alpha = 0.55f))
                            .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.meals.size}",
                            color = accentA,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.padding(10.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(MealRoutes.ADD_MEAL) },
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
                    Icon(Icons.Default.Add, contentDescription = "Add Meal")
                }
            }
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

            // ✅ Message chip (tap to clear) — same logic
            state.message?.let { msg ->
                Surface(
                    color = glassStrong,
                    shape = RoundedCornerShape(18.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, outlineSoft, RoundedCornerShape(18.dp))
                        .clickable { vm.clearMessage() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        accentA.copy(alpha = 0.14f),
                                        accentB.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = cs.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "My Meals",
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(cs.surfaceVariant.copy(alpha = 0.55f))
                        .border(1.dp, outlineSoft, RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${state.meals.size} total",
                        color = accentA,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            when {
                state.loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = accentA)
                    }
                }

                state.meals.isEmpty() -> {
                    EmptyAuroraState(
                        title = "No meals yet",
                        subtitle = "Tap + to add your first meal.",
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
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.meals, key = { it.id }) { meal ->
                            MealAuroraCard(
                                meal = meal,
                                accentA = accentA,
                                accentB = accentB,
                                glass = glass,
                                outlineSoft = outlineSoft,
                                danger = danger,
                                onDoneChange = { done -> vm.toggleDone(meal.id, done) },
                                onDelete = { vm.deleteMeal(meal.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealAuroraCard(
    meal: Meal,
    accentA: Color,
    accentB: Color,
    glass: Color,
    outlineSoft: Color,
    danger: Color,
    onDoneChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(22.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, outlineSoft, shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = glass),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    meal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = cs.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                // Status pill
                val pillBg =
                    if (meal.isCompleted) Brush.linearGradient(listOf(accentA.copy(alpha = 0.14f), accentB.copy(alpha = 0.10f)))
                    else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(pillBg)
                        .border(
                            BorderStroke(
                                1.dp,
                                if (meal.isCompleted) accentA.copy(alpha = 0.55f) else outlineSoft
                            ),
                            RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (meal.isCompleted) "DONE" else "TO DO",
                        color = if (meal.isCompleted) accentA else cs.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                "${meal.mealType} • ${meal.date}",
                color = cs.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            if (meal.calories > 0) {
                Text(
                    "Calories: ${meal.calories}",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (meal.notes.isNotBlank()) {
                Text(
                    "Notes: ${meal.notes}",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = meal.isCompleted,
                        onCheckedChange = { onDoneChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = accentA,
                            uncheckedColor = cs.onSurface.copy(alpha = 0.30f),
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        "Done",
                        color = if (meal.isCompleted) accentA else cs.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.weight(1f))

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = danger)
                ) { Text("Delete") }
            }

            // subtle aurora divider strip
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
