package com.liegestuetz.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.liegestuetz.app.LiegestuetzApp
import com.liegestuetz.app.MainActivity
import com.liegestuetz.domain.repository.AuthRepository
import com.liegestuetz.domain.usecase.UpdateFcmTokenUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LiegestuetzMessagingService : FirebaseMessagingService() {

    @Inject lateinit var updateFcmTokenUseCase: UpdateFcmTokenUseCase
    @Inject lateinit var authRepository: AuthRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called when the FCM registration token is refreshed.
     * Saves the new token to Firestore so the Cloud Function can fan-out notifications.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            authRepository.currentUser().firstOrNull()?.uid?.let { uid ->
                updateFcmTokenUseCase(uid, token)
            }
        }
    }

    /**
     * Called when a notification arrives while the app is in the foreground.
     * When the app is in the background, the system displays the notification
     * automatically from the `notification` payload sent by the Cloud Function.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body ?: return
        val challengeId = message.data["challengeId"]
        showNotification(title, body, challengeId, message.data["type"])
    }

    private fun showNotification(
        title: String,
        body: String,
        challengeId: String?,
        type: String?,
    ) {
        val channelId = when (type) {
            "reminder" -> LiegestuetzApp.CHANNEL_ID_REMINDERS
            else -> LiegestuetzApp.CHANNEL_ID_COMPLETIONS
        }

        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            challengeId?.let { putExtra("challengeId", it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
