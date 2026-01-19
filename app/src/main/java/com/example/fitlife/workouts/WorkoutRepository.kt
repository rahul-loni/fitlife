package com.example.fitlife.workouts

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WorkoutsRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ✅ LISTEN workouts AND also load /items for each workout so UI shows exercises
    fun listenWorkouts(onChange: (List<Workout>) -> Unit): ListenerRegistration? {
        val uid = auth.currentUser?.uid ?: return null

        val workoutsRef = db.collection("users").document(uid).collection("workouts")

        return workoutsRef.addSnapshotListener { snap, err ->
            if (err != null || snap == null) {
                onChange(emptyList())
                return@addSnapshotListener
            }

            // Build base workouts quickly
            val baseWorkouts = snap.documents.map { d ->
                Workout(
                    id = d.id,
                    name = d.getString("name") ?: "",
                    day = d.getString("day") ?: "Monday",
                    durationMin = (d.getLong("durationMin") ?: 0L).toInt(),
                    instructions = d.getString("instructions") ?: "",
                    requiredEquipment = (d.get("requiredEquipment") as? List<*>)?.mapNotNull { it as? String }
                        ?: emptyList(),
                    imageUrl = d.getString("imageUrl"),
                    isCompleted = d.getBoolean("isCompleted") ?: false,
                    items = emptyList() // filled below
                )
            }.sortedBy { it.day }

            // Emit fast list (no items yet)
            onChange(baseWorkouts)

            // Now load items for each workout and emit updated list
            CoroutineScope(Dispatchers.IO).launch {
                val full = baseWorkouts.map { w ->
                    val itemsSnap = workoutsRef.document(w.id).collection("items").get().await()

                    val items = itemsSnap.documents.map { itDoc ->
                        WorkoutItem(
                            id = itDoc.id,
                            title = itDoc.getString("title") ?: "Exercise",
                            sets = (itDoc.getLong("sets") ?: 0L).toInt(),
                            reps = (itDoc.getLong("reps") ?: 0L).toInt(),
                            notes = itDoc.getString("notes"),
                            order = (itDoc.getLong("order") ?: 0L).toInt(),
                            isDone = itDoc.getBoolean("isDone") ?: false
                        )
                    }.sortedBy { it.order }

                    w.copy(items = items)
                }.sortedBy { it.day }

                withContext(Dispatchers.Main) {
                    onChange(full)
                }
            }
        }
    }

    // ✅ Create workout + add items (optional). Image ignored.
    suspend fun createWorkout(
        name: String,
        day: String,
        durationMin: Int,
        instructions: String,
        requiredEquipment: List<String>,
        imageUri: Uri?, // ignored
        items: List<WorkoutItem>
    ) {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")

        val workoutDoc = db.collection("users").document(uid)
            .collection("workouts")
            .document()

        val workoutData = hashMapOf(
            "name" to name,
            "day" to day,
            "durationMin" to durationMin,
            "instructions" to instructions,
            "requiredEquipment" to requiredEquipment,
            "imageUrl" to null,
            "isCompleted" to false
        )

        workoutDoc.set(workoutData).await()

        // Optional initial items
        if (items.isNotEmpty()) {
            val itemsRef = workoutDoc.collection("items")
            val batch = db.batch()

            items.sortedBy { it.order }.forEach { item ->
                val doc = itemsRef.document(if (item.id.isBlank()) itemsRef.document().id else item.id)
                batch.set(doc, mapOf(
                    "title" to item.title,
                    "sets" to item.sets,
                    "reps" to item.reps,
                    "notes" to (item.notes ?: ""),
                    "order" to item.order,
                    "isDone" to item.isDone
                ))
            }

            batch.commit().await()
        }
    }

    suspend fun setWorkoutCompleted(workoutId: String, completed: Boolean) {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        db.collection("users").document(uid)
            .collection("workouts").document(workoutId)
            .update("isCompleted", completed)
            .await()
    }

    suspend fun setItemDone(workoutId: String, itemId: String, done: Boolean) {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        db.collection("users").document(uid)
            .collection("workouts").document(workoutId)
            .collection("items").document(itemId)
            .update("isDone", done)
            .await()
    }

    suspend fun deleteWorkoutItem(workoutId: String, itemId: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        db.collection("users").document(uid)
            .collection("workouts").document(workoutId)
            .collection("items").document(itemId)
            .delete()
            .await()
    }

    suspend fun resetWorkoutItems(workoutId: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")
        val itemsRef = db.collection("users").document(uid)
            .collection("workouts").document(workoutId)
            .collection("items")

        val snap = itemsRef.get().await()
        val batch = db.batch()

        snap.documents.forEach { doc ->
            batch.update(doc.reference, "isDone", false)
        }

        batch.commit().await()
    }

    suspend fun deleteWorkout(workoutId: String) {
        val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")

        val workoutRef = db.collection("users").document(uid)
            .collection("workouts").document(workoutId)

        // delete items subcollection first
        val itemsSnap = workoutRef.collection("items").get().await()
        val batch = db.batch()

        itemsSnap.documents.forEach { d -> batch.delete(d.reference) }
        batch.delete(workoutRef)

        batch.commit().await()
    }
}
