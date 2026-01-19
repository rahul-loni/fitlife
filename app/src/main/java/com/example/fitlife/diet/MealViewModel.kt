package com.example.fitlife.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MealUiState(
    val meals: List<Meal> = emptyList(),
    val loading: Boolean = true,
    val message: String? = null
)

class MealViewModel(
    private val repo: MealRepository = MealRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MealUiState())
    val state: StateFlow<MealUiState> = _state.asStateFlow()

    private var listener: ListenerRegistration? = null

    fun startListening() {
        if (listener != null) return

        _state.value = _state.value.copy(loading = true, message = null)

        listener = repo.listenMeals(
            onUpdate = { list ->
                _state.value = _state.value.copy(meals = list, loading = false)
            },
            onError = { msg ->
                _state.value = _state.value.copy(loading = false, message = msg)
            }
        )
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }

    fun addMeal(
        title: String,
        type: String,
        caloriesText: String,
        notes: String,
        date: String
    ) {
        val t = title.trim()
        val d = date.trim()
        val cal = caloriesText.trim().toIntOrNull() ?: 0
        val n = notes.trim()

        if (t.isEmpty()) {
            _state.value = _state.value.copy(message = "Meal name required")
            return
        }
        if (d.isEmpty()) {
            _state.value = _state.value.copy(message = "Date required (YYYY-MM-DD)")
            return
        }

        // keep UI responsive + consistent
        viewModelScope.launch {
            repo.addMeal(
                title = t,
                mealType = type,
                calories = cal,
                notes = n,
                date = d,
                onDone = {
                    _state.value = _state.value.copy(message = "Meal saved ✅")
                },
                onError = { msg ->
                    _state.value = _state.value.copy(message = msg)
                }
            )
        }
    }

    fun toggleDone(mealId: String, done: Boolean) {
        repo.setMealCompleted(
            mealId = mealId,
            done = done,
            onError = { msg -> _state.value = _state.value.copy(message = msg) }
        )
    }

    fun deleteMeal(mealId: String) {
        repo.deleteMeal(
            mealId = mealId,
            onError = { msg -> _state.value = _state.value.copy(message = msg) }
        )
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
