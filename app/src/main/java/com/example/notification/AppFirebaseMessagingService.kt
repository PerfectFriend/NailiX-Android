package com.example.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        FcmNotificationManager.updateToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notif = remoteMessage.notification

        val title = notif?.title ?: data["title"] ?: "Студия Маникюра NailiX 💅"
        val body = notif?.body ?: data["body"] ?: "У вас новое уведомление от мастера."
        val type = data["type"] ?: if (title.contains("Запис", ignoreCase = true) || title.contains("Напомина", ignoreCase = true)) {
            "appointment_reminder"
        } else {
            "promotion"
        }

        Log.d(TAG, "Notification received: title=$title, body=$body, type=$type")

        FcmNotificationManager.showSystemNotification(
            context = applicationContext,
            title = title,
            body = body,
            type = type,
            data = data
        )
    }

    companion object {
        private const val TAG = "AppFirebaseMessaging"
    }
}
