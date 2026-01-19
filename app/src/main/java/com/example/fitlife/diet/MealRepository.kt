package com.example.fitlife.diet

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MealRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun uidOrNull(): String? = auth.currentUser?.uid

    private fun mealsCollectionOrNull() =
        uidOrNull()?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("meals")
        }

    fun listenMeals(
        onUpdate: (List<Meal>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration? {

        val col = mealsCollectionOrNull()
        if (col == null) {
            onUpdate(emptyList())
            onError("Not logged in. Please login again.")
            return null
        }

        return col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e.message ?: "Firestore error")
                    return@addSnapshotListener
                }

                val meals = snapshot?.documents?.map { doc ->
                    val data = doc.data ?: emptyMap<String, Any>()
                    Meal(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        mealType = data["mealType"] as? String ?: "Breakfast",
                        calories = (data["calories"] as? Number)?.toInt() ?: 0,
                        notes = data["notes"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        isCompleted = data["isCompleted"] as? Boolean ?: false,
                        createdAt = data["createdAt"] as? Timestamp,
                        completedAt = data["completedAt"] as? Timestamp
                    )
                } ?: emptyList()

                onUpdate(meals)
            }
    }

    fun addMeal(
        title: String,
        mealType: String,
        calories: Int,
        notes: String,
        date: String,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        val col = mealsCollectionOrNull() ?: run {
            onError("Not logged in. Please login again.")
            return
        }

        val payload = hashMapOf(
            "title" to title.trim(),
            "mealType" to mealType,
            "calories" to calories,
            "notes" to notes.trim(),
            "date" to date.trim(),
            "isCompleted" to false,
            "createdAt" to Timestamp.now(),
            "completedAt" to null
        )

        col.add(payload)
            .addOnSuccessListener { onDone() }
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to add meal")
            }
    }

    fun setMealCompleted(
        mealId: String,
        done: Boolean,
        onError: (String) -> Unit
    ) {
        val col = mealsCollectionOrNull() ?: run {
            onError("Not logged in. Please login again.")
            return
        }

        val updates = hashMapOf<String, Any?>(
            "isCompleted" to done,
            "completedAt" to if (done) Timestamp.now() else null
        )

        col.document(mealId)
            .update(updates)
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to update meal")
            }
    }

    fun deleteMeal(
        mealId: String,
        onError: (String) -> Unit
    ) {
        val col = mealsCollectionOrNull() ?: run {
            onError("Not logged in. Please login again.")
            return
        }

        col.document(mealId)
            .delete()
            .addOnFailureListener { e ->
                onError(e.message ?: "Failed to delete meal")
            }
    }
}
