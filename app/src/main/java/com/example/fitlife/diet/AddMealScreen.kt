package com.example.fitlife.diet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealScreen(
    navController: NavController,
    vm: MealViewModel = viewModel()
) {
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

    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Breakfast") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    var typeMenu by remember { mutableStateOf(false) }
    val types = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentA.copy(alpha = 0.65f),
        unfocusedBorderColor = outlineSoft,
        focusedLabelColor = accentA,
        cursorColor = accentA
    )

    val canSave = title.isNotBlank() && date.isNotBlank()

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
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.padding(6.dp))
                        Text("Add Meal", fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ✅ Header glass card
            AuroraCard(
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Log your meal", color = cs.onSurface, fontWeight = FontWeight.Bold)
                    Text(
                        "Keep track of calories and notes for better progress.",
                        color = cs.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Brush.linearGradient(listOf(accentA.copy(alpha = 0.14f), accentB.copy(alpha = 0.10f))))
                            .border(1.dp, accentA.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text("Nutrition", color = accentA, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Meal name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = tfColors
            )

            // ✅ Meal type selector (dropdown, aurora styled)
            Box(modifier = Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Meal type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = tfColors,
                    trailingIcon = {
                        FilledTonalIconButton(
                            onClick = { typeMenu = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                                contentColor = accentA
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Change type")
                        }
                    }
                )

                DropdownMenu(
                    expanded = typeMenu,
                    onDismissRequest = { typeMenu = false },
                    modifier = Modifier
                        .background(glass)
                        .border(1.dp, outlineSoft, RoundedCornerShape(14.dp))
                ) {
                    types.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt, color = cs.onSurface) },
                            onClick = {
                                type = opt
                                typeMenu = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = tfColors
            )

            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = tfColors
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = tfColors
            )

            Button(
                onClick = {
                    vm.addMeal(
                        title = title,
                        type = type,
                        caloriesText = calories,
                        notes = notes,
                        date = date
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
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
                            if (canSave) Brush.linearGradient(listOf(accentA, accentB))
                            else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.padding(6.dp))
                        Text("Save Meal", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
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
    content: @Composable () -> Unit
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
                .padding(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
