package com.example.fitlife.profile

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    private val _profile = mutableStateOf(
        UserProfile(
            fullName = "Your Name",
            email = "you@email.com",
            age = 20,
            heightCm = 170,
            weightKg = 70.0,
            goal = "Lose Weight"
        )
    )

    val profile = _profile

    fun updateProfile(updated: UserProfile) {
        _profile.value = updated
    }
}
