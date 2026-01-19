package com.example.fitlife.equipment

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class EquipmentViewModel : ViewModel() {

    private var nextId = 0

    val equipmentList = mutableStateListOf(
        Equipment(nextId++, "Dumbbells", "Strength Equipment"),
        Equipment(nextId++, "Yoga Mat", "Mats"),
        Equipment(nextId++, "Resistance Bands", "Accessories")
    )

    fun toggleDone(equipment: Equipment) {
        val index = equipmentList.indexOfFirst { it.id == equipment.id }
        if (index != -1) {
            equipmentList[index] =
                equipment.copy(checked = !equipment.checked)
        }
    }
}
