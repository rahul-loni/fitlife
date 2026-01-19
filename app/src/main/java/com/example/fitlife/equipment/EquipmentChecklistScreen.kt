package com.example.fitlife.equipment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitlife.ui.theme.FitLifeTheme

class EquipmentChecklistScreen : ComponentActivity() {

    private val viewModel: EquipmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitLifeTheme {
                EquipmentChecklistPage(viewModel)
            }
        }
    }
}
const val EQUIPMENT = "equipment"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentChecklistPage(viewModel: EquipmentViewModel) {

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Equipment Checklist") })
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.equipmentList) { equipment ->
                EquipmentItem(equipment) {
                    viewModel.toggleDone(equipment)
                }
            }
        }
    }
}

@Composable
fun EquipmentItem(
    equipment: Equipment,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(equipment.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    equipment.category,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (equipment.checked)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle"
                )
            }
        }
    }
}
