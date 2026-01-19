package com.example.fitlife.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebasePaths {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    fun uid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    fun userDoc() = db.collection("users").document(uid())

    fun mealsCol() = userDoc().collection("meals")
    fun workoutsCol() = userDoc().collection("workouts")
    fun progressCol() = userDoc().collection("progress")
    fun routinesCol() = userDoc().collection("routines")
}
