// ExerciseViewModel.kt
package com.example.fitlife.exercises

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExerciseUiState(
    val exercises: List<Exercise> = emptyList(),
    val loading: Boolean = true,
    val message: String? = null
)

class ExerciseViewModel(
    private val repo: ExerciseRepository = ExerciseRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ExerciseUiState())
    val state: StateFlow<ExerciseUiState> = _state.asStateFlow()

    private var listener: ListenerRegistration? = null

    fun startListening() {
        if (listener != null) return

        _state.value = _state.value.copy(loading = true, message = null)

        listener = repo.listenExercises(
            onUpdate = { list ->
                _state.value = _state.value.copy(exercises = list, loading = false)
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

    fun addExercise(name: String, muscleGroup: String, setsText: String, repsText: String, notes: String) {
        val n = name.trim()
        if (n.isEmpty()) {
            _state.value = _state.value.copy(message = "Exercise name required")
            return
        }
        val sets = setsText.toIntOrNull() ?: 0
        val reps = repsText.toIntOrNull() ?: 0

        repo.addExercise(
            name = n,
            muscleGroup = muscleGroup.trim(),
            sets = sets,
            reps = reps,
            notes = notes.trim(),
            onDone = { _state.value = _state.value.copy(message = "Exercise saved ✅") },
            onError = { msg -> _state.value = _state.value.copy(message = msg) }
        )
    }

    fun toggleDone(exerciseId: String, done: Boolean) {
        // ✅ Optimistic UI update: tick immediately
        val oldList = _state.value.exercises
        val newList = oldList.map { ex ->
            if (ex.id == exerciseId) ex.copy(isCompleted = done) else ex
        }
        _state.value = _state.value.copy(exercises = newList)

        // ✅ Write to Firestore
        repo.setCompleted(
            exerciseId = exerciseId,
            done = done,
            onError = { msg ->
                // ❌ Revert UI if Firestore fails
                _state.value = _state.value.copy(
                    exercises = oldList,
                    message = msg
                )
            }
        )
    }


    fun deleteExercise(exerciseId: String) {
        repo.deleteExercise(
            exerciseId = exerciseId,
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
