package com.liegestuetz.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LiegestuetzApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        NotificationChannel(
            CHANNEL_ID_COMPLETIONS,
            getString(R.string.notification_channel_completions_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.notification_channel_completions_desc)
            notificationManager.createNotificationChannel(this)
        }

        NotificationChannel(
            CHANNEL_ID_REMINDERS,
            getString(R.string.notification_channel_reminders_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_reminders_desc)
            notificationManager.createNotificationChannel(this)
        }
    }

    companion object {
        const val CHANNEL_ID_COMPLETIONS = "completions"
        const val CHANNEL_ID_REMINDERS = "reminders"
    }
}
