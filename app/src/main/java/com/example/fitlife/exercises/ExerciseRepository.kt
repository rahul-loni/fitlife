package com.example.fitlife.exercises

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ExerciseRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun uidOrNull(): String? = auth.currentUser?.uid

    private fun exercisesColOrNull() =
        uidOrNull()?.let { uid ->
            db.collection("users").document(uid).collection("exercises")
        }

    fun listenExercises(
        onUpdate: (List<Exercise>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {
        val col = exercisesColOrNull()
        if (col == null) {
            onUpdate(emptyList())
            onError("Not logged in. Please login again.")
            return null
        }

        return col.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    onError(e.message ?: "Firestore error")
                    return@addSnapshotListener
                }

                val list = snap?.documents?.map { doc ->
                    // ✅ Manual mapping so isCompleted ALWAYS works
                    Exercise(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        muscleGroup = doc.getString("muscleGroup") ?: "",
                        sets = (doc.getLong("sets") ?: 0L).toInt(),
                        reps = (doc.getLong("reps") ?: 0L).toInt(),
                        notes = doc.getString("notes") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false,
                        createdAt = doc.getTimestamp("createdAt"),
                        completedAt = doc.getTimestamp("completedAt")
                    )
                } ?: emptyList()

                onUpdate(list)
            }
    }

    fun addExercise(
        name: String,
        muscleGroup: String,
        sets: Int,
        reps: Int,
        notes: String,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val col = exercisesColOrNull() ?: run {
            onError("Not logged in. Please login again.")
            return
        }

        val payload = hashMapOf(
            "name" to name.trim(),
            "muscleGroup" to muscleGroup.trim(),
            "sets" to sets,
            "reps" to reps,
            "notes" to notes.trim(),
            "isCompleted" to false,
            "createdAt" to Timestamp.now(),
            "completedAt" to null
        )

        col.add(payload)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to add exercise") }
    }

    fun setCompleted(
        exerciseId: String,
        done: Boolean,
        onError: (String) -> Unit
    ) {
        val col = exercisesColOrNull() ?: run {
            onError("Not logged in. Please login again.")
            return
        }

        val updates = hashMapOf<String, Any?>(
            "isCompleted" to done,
            "completedAt" to (if (done) Timestamp.now() else null)
        )

        col.document(exerciseId)
            .update(updates)
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update exercise") }
    }

    fun deleteExercise(
        exerciseId: String,
        onError: (String) -> Unit
    ) {
        val col = exercisesColOrNull() ?: run {
            onError("Not logged in. Please login again.")
            return
        }

        col.document(exerciseId)
            .delete()
            .addOnFailureListener { e -> onError(e.message ?: "Failed to delete exercise") }
    }


}
