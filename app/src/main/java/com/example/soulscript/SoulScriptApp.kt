package com.example.soulscript

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

@HiltAndroidApp
class SoulScriptApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
}

fun createNotificationChannel(app: Application) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Journal Reminders"
        val descriptionText = "Daily reminders to write in your journal"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("journal_reminder_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}