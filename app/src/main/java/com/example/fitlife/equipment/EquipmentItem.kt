package com.example.fitlife.equipment

data class Equipment(
    val id: Int,
    val name: String,
    val category: String,
    var checked: Boolean = false
)
