package com.example.fitlife.workouts

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class WorkoutImageUploader(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun uploadWorkoutImage(
        workoutId: String,
        uri: Uri,
        onDone: (downloadUrl: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError("Not logged in.")
            return
        }

        val filename = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference
            .child("users")
            .child(uid)
            .child("workouts")
            .child(workoutId)
            .child(filename)

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { url -> onDone(url.toString()) }
                    .addOnFailureListener { e -> onError(e.message ?: "Failed to get download URL") }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Upload failed")
            }
    }
}
