package com.example.fitlife.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Reminder fired!", Toast.LENGTH_SHORT).show() // ✅ debug

        val title = intent.getStringExtra("title") ?: "Workout Reminder"
        val msg = intent.getStringExtra("msg") ?: "Time to workout!"

        NotificationHelper.show(context, title, msg)
    }
}
