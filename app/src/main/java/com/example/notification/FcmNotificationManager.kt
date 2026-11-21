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
import androidx.work.Configuration
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

data class FcmNotificationItem(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val body: String,
    val type: String, // "appointment_reminder", "promotion", "general"
    val timestamp: Long = System.currentTimeMillis(),
    val data: Map<String, String> = emptyMap()
)

class BookingReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Напоминание о визите NailiX 💅"
        val body = inputData.getString(KEY_BODY) ?: "Скоро ваш визит в студию NailiX!"
        val type = inputData.getString(KEY_TYPE) ?: "appointment_reminder"
        FcmNotificationManager.showSystemNotification(
            context = applicationContext,
            title = title,
            body = body,
            type = type
        )
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "reminder_title"
        const val KEY_BODY = "reminder_body"
        const val KEY_TYPE = "reminder_type"
    }
}

object FcmNotificationManager {
    private const val TAG = "FcmNotificationManager"

    const val CHANNEL_REMINDERS_ID = "nailix_reminders_channel"
    const val CHANNEL_PROMOTIONS_ID = "nailix_promotions_channel"

    private val _fcmToken = MutableStateFlow("Ожидание токена FCM...")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _fcmError = MutableStateFlow<String?>(null)
    val fcmError: StateFlow<String?> = _fcmError.asStateFlow()

    private val _notifications = MutableStateFlow<List<FcmNotificationItem>>(emptyList())
    val notifications: StateFlow<List<FcmNotificationItem>> = _notifications.asStateFlow()

    fun init(context: Context) {
        createNotificationChannels(context)
        ensureFirebaseInitialized(context)
        fetchFcmToken(context)
    }

    private fun ensureFirebaseInitialized(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context.applicationContext)
            }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseApp initialization note: ${e.message}")
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
        _fcmError.value = null
        Log.d(TAG, "FCM Token updated: $token")
    }

    private fun fetchFcmToken(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrBlank()) {
                        _fcmToken.value = task.result
                        _fcmError.value = null
                        Log.d(TAG, "Fetched real FCM Token")
                    } else {
                        val reason = task.exception?.localizedMessage ?: "FCM сервис недоступен (возможно, нет Google Play Services)"
                        _fcmToken.value = "FCM недоступен"
                        _fcmError.value = reason
                        Log.w(TAG, "FCM token unavailable: $reason")
                    }
                }
            } else {
                _fcmToken.value = "FirebaseApp не инициализирован"
                _fcmError.value = "Отсутствует google-services.json конфигурация"
            }
        } catch (e: Throwable) {
            _fcmToken.value = "FCM недоступен"
            _fcmError.value = e.localizedMessage
            Log.w(TAG, "FCM token error: ${e.message}")
        }
    }

    fun scheduleBookingReminder(
        context: Context,
        title: String,
        body: String,
        delayMinutes: Long
    ) {
        val input = Data.Builder()
            .putString(BookingReminderWorker.KEY_TITLE, title)
            .putString(BookingReminderWorker.KEY_BODY, body)
            .putString(BookingReminderWorker.KEY_TYPE, "appointment_reminder")
            .build()

        val request = OneTimeWorkRequestBuilder<BookingReminderWorker>()
            .setInitialDelay(delayMinutes.coerceAtLeast(0L), TimeUnit.MINUTES)
            .setInputData(input)
            .build()

        runCatching {
            val appCtx = context.applicationContext
            if (!WorkManager.isInitialized()) {
                WorkManager.initialize(appCtx, Configuration.Builder().build())
            }
            WorkManager.getInstance(appCtx).enqueue(request)
        }.onFailure { e ->
            Log.w(TAG, "Unable to schedule WorkManager booking reminder: ${e.message}")
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
        createNotificationChannels(context)
        val channelId = if (type == "appointment_reminder") CHANNEL_REMINDERS_ID else CHANNEL_PROMOTIONS_ID

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() and 0xFFFFFF).toInt(),
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
            val notifId = (System.currentTimeMillis() and 0x7FFFFFFF).toInt()
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
