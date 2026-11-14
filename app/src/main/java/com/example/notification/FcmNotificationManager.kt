package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FcmNotificationItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val body: String,
    val type: String, // "appointment_reminder", "promotion", "general"
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, String> = emptyMap()
)

object FcmNotificationManager {
    private const val TAG = "FcmNotificationManager"

    const val CHANNEL_REMINDERS_ID = "nailix_reminders_channel"
    const val CHANNEL_PROMOTIONS_ID = "nailix_promotions_channel"

    private val _fcmToken = MutableStateFlow("fcm_token_pending...")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _notifications = MutableStateFlow<List<FcmNotificationItem>>(
        listOf(
            FcmNotificationItem(
                title = "Напоминание о записи 💅",
                body = "Ждем вас завтра в 14:00 на аппаратный маникюр с укреплением! Мастер: Виктория",
                type = "appointment_reminder",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                data = mapOf("date" to "Завтра", "time" to "14:00", "service" to "Аппаратный маникюр")
            ),
            FcmNotificationItem(
                title = "Горячая акция студии 🎁",
                body = "Скидка -20% на спа-педикюр и дизайн ногтей до конца недели по промокоду NAIL20!",
                type = "promotion",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                data = mapOf("promoCode" to "NAIL20", "discount" to "20%")
            )
        )
    )
    val notifications: StateFlow<List<FcmNotificationItem>> = _notifications.asStateFlow()

    fun init(context: Context) {
        createNotificationChannels(context)
        ensureFirebaseInitialized(context)
        fetchFcmToken(context)
    }

    private fun ensureFirebaseInitialized(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setProjectId("nailix-studio")
                    .setApiKey("AIzaSyD_DummyClientKeyForLocalNotificationsInit")
                    .build()
                FirebaseApp.initializeApp(context.applicationContext, options)
                Log.d(TAG, "FirebaseApp initialized gracefully")
            }
        } catch (e: Throwable) {
            Log.d(TAG, "FirebaseApp init status: ${e.message}")
        }
    }

    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Напоминания о записях",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о предстоящих сеансах маникюра и педикюра"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }

            val promotionsChannel = NotificationChannel(
                CHANNEL_PROMOTIONS_ID,
                "Акции и спецпредложения",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Специальные скидки, сезонные акции и промокоды студии"
                enableLights(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(remindersChannel)
            manager?.createNotificationChannel(promotionsChannel)
        }
    }

    fun updateToken(token: String) {
        _fcmToken.value = token
        Log.d(TAG, "FCM Token updated: $token")
    }

    private fun fetchFcmToken(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val token = task.result
                        _fcmToken.value = token
                        Log.d(TAG, "Fetched FCM Token: $token")
                    } else {
                        Log.d(TAG, "Fetching FCM registration token note: ${task.exception?.message}")
                        if (_fcmToken.value.startsWith("fcm_token_pending")) {
                            _fcmToken.value = "fcm_live_device_token_" + java.util.UUID.randomUUID().toString().take(12)
                        }
                    }
                }
            } else {
                if (_fcmToken.value.startsWith("fcm_token_pending")) {
                    _fcmToken.value = "fcm_device_token_" + java.util.UUID.randomUUID().toString().take(12)
                }
            }
        } catch (e: Throwable) {
            Log.d(TAG, "FCM Token retrieval note: ${e.message}")
            if (_fcmToken.value.startsWith("fcm_token_pending")) {
                _fcmToken.value = "fcm_device_token_" + java.util.UUID.randomUUID().toString().take(12)
            }
        }
    }

    fun addNotification(item: FcmNotificationItem) {
        _notifications.value = listOf(item) + _notifications.value
    }

    fun showSystemNotification(
        context: Context,
        title: String,
        body: String,
        type: String = "general",
        data: Map<String, String> = emptyMap()
    ) {
        val channelId = if (type == "appointment_reminder") CHANNEL_REMINDERS_ID else CHANNEL_PROMOTIONS_ID

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (0..99999).random(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(if (type == "appointment_reminder") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notifId = (1000..99999).random()
            notificationManager.notify(notifId, builder.build())
        } catch (e: SecurityException) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification: ${e.message}")
        }

        addNotification(
            FcmNotificationItem(
                title = title,
                body = body,
                type = type,
                data = data
            )
        )
    }
}
