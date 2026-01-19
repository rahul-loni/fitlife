package com.example.fitlife.progress

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.fitlife.ui.theme.FitLifeTheme
import kotlin.math.pow
import androidx.compose.material3.TextFieldColors

class ProgressScreen : ComponentActivity() {

    private val viewModel: ProgressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitLifeTheme {
                ProgressPage(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPage(
    viewModel: ProgressViewModel,
    onBack: () -> Unit
) {
    val entries = viewModel.entries

    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }

    var date by remember { mutableStateOf("") }
    var newWeight by remember { mutableStateOf("") }

    // ✅ Aurora Glass theme (matches your Home/Register/Workouts screens)
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

    val bmi = remember(heightCm, weightKg) {
        val h = heightCm.toDoubleOrNull()
        val w = weightKg.toDoubleOrNull()
        if (h != null && w != null && h > 0) {
            val hm = h / 100.0
            w / (hm.pow(2))
        } else null
    }

    val bmiLabel = remember(bmi) {
        when {
            bmi == null -> ""
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accentA.copy(alpha = 0.65f),
        unfocusedBorderColor = outlineSoft,
        focusedLabelColor = accentA,
        cursorColor = accentA
    )

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
                            Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = Color.White)
                        }
                        Spacer(Modifier.padding(6.dp))
                        Text("Progress", fontWeight = FontWeight.Black)
                    }
                },
                navigationIcon = {
                    FilledTonalIconButton(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = cs.surfaceVariant.copy(alpha = 0.55f),
                            contentColor = cs.onSurface
                        )
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ✅ BMI CARD (Aurora)
            AuroraCard(
                title = "BMI Calculator",
                icon = Icons.Default.FitnessCenter,
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong
            ) {
                AuroraTextField(
                    value = heightCm,
                    onValueChange = { heightCm = it },
                    label = "Height (cm)",
                    keyboardType = KeyboardType.Number,
                    colors = tfColors
                )

                AuroraTextField(
                    value = weightKg,
                    onValueChange = { weightKg = it },
                    label = "Weight (kg)",
                    keyboardType = KeyboardType.Number,
                    colors = tfColors
                )

                bmi?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.linearGradient(listOf(accentA.copy(alpha = 0.14f), accentB.copy(alpha = 0.10f))))
                            .border(1.dp, accentA.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "BMI: ${"%.1f".format(it)}  ($bmiLabel)",
                            color = accentA,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ✅ ADD ENTRY (Aurora)
            AuroraCard(
                title = "Add Weight Entry",
                icon = Icons.Default.CalendarToday,
                accentA = accentA,
                accentB = accentB,
                outlineSoft = outlineSoft,
                glass = glassStrong
            ) {
                AuroraTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Date (YYYY-MM-DD)",
                    keyboardType = KeyboardType.Text,
                    colors = tfColors,
                    leading = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = cs.onSurfaceVariant
                        )
                    }
                )

                AuroraTextField(
                    value = newWeight,
                    onValueChange = { newWeight = it },
                    label = "Weight (kg)",
                    keyboardType = KeyboardType.Number,
                    colors = tfColors
                )

                val canSave = date.isNotBlank() && newWeight.toDoubleOrNull() != null

                Button(
                    onClick = {
                        val w = newWeight.toDoubleOrNull()
                        if (date.isNotBlank() && w != null) {
                            viewModel.addEntry(date.trim(), w)
                            date = ""
                            newWeight = ""
                        }
                    },
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth(),
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
                            Text("Save Entry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ✅ HISTORY HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Weight History",
                    color = cs.onBackground,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.weight(1f))
                FitPill(
                    text = "${entries.size}",
                    accentA = accentA,
                    accentB = accentB,
                    outlineSoft = outlineSoft
                )
            }

            if (entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No entries yet", color = cs.onSurfaceVariant, fontStyle = FontStyle.Italic)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val shape = RoundedCornerShape(22.dp)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, outlineSoft, shape),
                            colors = CardDefaults.cardColors(containerColor = glass),
                            shape = shape
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
                                    Column(Modifier.weight(1f)) {
                                        Text(entry.date, color = cs.onSurface, fontWeight = FontWeight.SemiBold)
                                        Text("${entry.weightKg} kg", color = cs.onSurfaceVariant)
                                    }

                                    IconButton(onClick = { viewModel.deleteEntry(entry.id) }) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            tint = danger,
                                            contentDescription = "Delete"
                                        )
                                    }
                                }

                                // subtle aurora divider strip
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    accentA.copy(alpha = 0.55f),
                                                    accentB.copy(alpha = 0.35f)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Aurora helpers (keep in same file) ---------- */

@Composable
private fun AuroraCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentA: Color,
    accentB: Color,
    outlineSoft: Color,
    glass: Color,
    content: @Composable () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)

    Surface(
        shape = shape,
        color = glass,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cs.surfaceVariant.copy(alpha = 0.55f))
                        .border(1.dp, outlineSoft, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentA)
                }
                Spacer(Modifier.padding(8.dp))
                Text(title, color = cs.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            content()
        }
    }
}

@Composable
private fun AuroraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    colors: TextFieldColors,
    keyboardType: KeyboardType,
    leading: (@Composable (() -> Unit))? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = colors,
        leadingIcon = leading
    )
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
