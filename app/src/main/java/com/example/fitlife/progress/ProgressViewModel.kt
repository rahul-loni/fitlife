package com.example.fitlife.progress

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class ProgressViewModel : ViewModel() {

    private val _entries = mutableStateListOf(
        WeightEntry(1, "2025-12-01", 72.0),
        WeightEntry(2, "2025-12-10", 71.4),
        WeightEntry(3, "2025-12-20", 70.8)
    )

    val entries: List<WeightEntry> = _entries

    fun nextId(): Int = (_entries.maxOfOrNull { it.id } ?: 0) + 1

    fun addEntry(date: String, weightKg: Double) {
        _entries.add(WeightEntry(nextId(), date, weightKg))
    }

    fun deleteEntry(id: Int) {
        _entries.removeAll { it.id == id }
    }
}
