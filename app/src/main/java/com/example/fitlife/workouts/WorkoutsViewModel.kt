package com.example.fitlife.workouts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext






data class WorkoutsState(
    val loading: Boolean = true,
    val workouts: List<Workout> = emptyList(),
    val error: String? = null
)

class WorkoutsViewModel(
    private val repo: WorkoutsRepository = WorkoutsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutsState())
    val state: StateFlow<WorkoutsState> = _state
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var sub: ListenerRegistration? = null

    fun startListening() {
        if (sub != null) return
        _state.value = _state.value.copy(loading = true, error = null)

        sub = repo.listenWorkouts { list ->
            _state.value = _state.value.copy(
                loading = false,
                workouts = list,
                error = null
            )
        }

        if (sub == null) {
            _state.value = _state.value.copy(
                loading = false,
                workouts = emptyList(),
                error = "Please log in to view workouts."
            )
        }
    }

    fun stopListening() {
        sub?.remove()
        sub = null
    }

    // ✅ IMAGE IGNORED: pass null safely
    fun createWorkoutWithExercises(
        name: String,
        day: String,
        durationText: String,
        instructions: String,
        requiredEquipmentText: String,
        imageUri: Uri?, // keep param but we ignore it
        selectedItems: List<WorkoutItem>,
        onDone: () -> Unit
    ) {
        val durationMin = durationText.toIntOrNull() ?: 0
        val equipment = requiredEquipmentText
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        viewModelScope.launch {
            runCatching {
                repo.createWorkout(
                    name = name,
                    day = day,
                    durationMin = durationMin,
                    instructions = instructions,
                    requiredEquipment = equipment,
                    imageUri = null, // ✅ FORCE NO IMAGE
                    items = selectedItems
                )
            }.onSuccess {
                onDone()
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to create workout"
                )
            }
        }
    }

    fun setWorkoutCompleted(workoutId: String, completed: Boolean) {
        viewModelScope.launch { repo.setWorkoutCompleted(workoutId, completed) }
    }



    // ✅ robust add (prevents blank screen by exposing error)
    fun addExercisesToWorkout(
        workoutId: String,
        exerciseIds: List<String>,
        onDone: (Boolean, String) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onDone(false, "Not logged in")
            return
        }
        if (workoutId.isBlank()) {
            onDone(false, "Invalid workout")
            return
        }
        if (exerciseIds.isEmpty()) {
            onDone(false, "No exercises selected")
            return
        }

        val db = FirebaseFirestore.getInstance()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Firestore whereIn limit = 10 -> chunk it
                val allDocs = mutableListOf<com.google.firebase.firestore.DocumentSnapshot>()
                exerciseIds.distinct().chunked(10).forEach { chunk ->
                    val snap = db.collection("users").document(uid)
                        .collection("exercises")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()
                    allDocs.addAll(snap.documents)
                }

                val itemsRef = db.collection("users").document(uid)
                    .collection("workouts").document(workoutId)
                    .collection("items")

                // Avoid duplicates
                val existingExerciseIds = itemsRef.get().await().documents
                    .mapNotNull { it.getString("exerciseId") }
                    .toSet()

                val batch = db.batch()
                var addedCount = 0

                allDocs.forEach { d ->
                    val exId = d.id
                    if (existingExerciseIds.contains(exId)) return@forEach

                    val name = d.getString("name") ?: d.getString("title") ?: "Exercise"

                    val newDoc = itemsRef.document()
                    batch.set(newDoc, mapOf(
                        "exerciseId" to exId,
                        "title" to name,
                        "sets" to 3,
                        "reps" to 10,
                        "notes" to "",
                        "order" to System.currentTimeMillis(),
                        "isDone" to false
                    ))
                    addedCount++
                }

                batch.commit().await()

                withContext(Dispatchers.Main) {
                    onDone(true, "Added $addedCount exercise(s)")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onDone(false, e.message ?: "Failed to add exercises")
                }
            }
        }
    }


    fun deleteWorkoutItem(
        workoutId: String,
        itemId: String,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching { repo.deleteWorkoutItem(workoutId, itemId) }
                .onSuccess { onDone() }
                .onFailure { onError(it.message ?: "Failed to delete item") }
        }
    }

    fun resetWorkoutItems(
        workoutId: String,
        onDone: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            runCatching { repo.resetWorkoutItems(workoutId) }
                .onSuccess { onDone() }
                .onFailure { onError(it.message ?: "Failed to reset items") }
        }
    }

    fun scheduleSmsReminder(
        context: android.content.Context,
        smsBody: String,
        delayMinutes: Long
    ) {
        val data = workDataOf("sms_body" to smsBody)

        val req = OneTimeWorkRequestBuilder<com.example.fitlife.reminders.WorkoutSmsReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .setInputData(data)
            .addTag("workout_sms_reminder")
            .build()

        WorkManager.getInstance(context).enqueue(req)
    }

    fun deleteWorkout(workoutId: String, onDone: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repo.deleteWorkout(workoutId) }
                .onSuccess { onDone() }
                .onFailure { onError(it.message ?: "Failed to delete workout") }
        }
    }

    fun setItemDone(workoutId: String, itemId: String, done: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val workoutRef = db.collection("users").document(uid)
            .collection("workouts").document(workoutId)

        workoutRef.collection("items")
            .document(itemId)
            .update("isDone", done)
            .addOnSuccessListener {
                // recompute completion based on ALL items
                workoutRef.collection("items")
                    .get()
                    .addOnSuccessListener { snap ->
                        val allDone =
                            snap.documents.isNotEmpty() &&
                                    snap.documents.all { it.getBoolean("isDone") == true }

                        workoutRef.update("isCompleted", allDone)
                    }
            }
    }

    fun resetWorkoutItems(workoutId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val workoutRef = db.collection("users").document(uid)
                .collection("workouts").document(workoutId)

            try {
                val itemsSnap = workoutRef.collection("items").get().await()

                // update all items to isDone=false
                val batch = db.batch()
                for (doc in itemsSnap.documents) {
                    batch.update(doc.reference, "isDone", false)
                }

                // also remove completed
                batch.update(workoutRef, "isCompleted", false)

                batch.commit().await()
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        stopListening()
        super.onCleared()
    }
}
