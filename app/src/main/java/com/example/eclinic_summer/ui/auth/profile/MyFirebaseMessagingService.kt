package com.example.eclinic_summer.ui.auth.profile

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.eclinic_summer.MainActivity
import com.example.eclinic_summer.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Firebase Messaging Service handling push notifications.
 *
 * Saves FCM token to Firestore and displays notifications for incoming messages.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new FCM token is generated.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("🔥 New FCM Token: $token")
        saveTokenToFirestore(token)
    }

    /**
     * Saves the FCM token to Firestore under the current user's document.
     */
    private fun saveTokenToFirestore(token: String) {
        val user = Firebase.auth.currentUser
        user?.let {
            Firebase.firestore.collection("users")
                .document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Timber.d("FCM token saved to Firestore")
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Error saving FCM token")
                }
        }
    }

    /**
     * Handles incoming FCM messages.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: "E-Clinic"
        val body = remoteMessage.notification?.body ?: "You have a new notification"

        showNotification(title, body)
    }

    /**
     * Shows a notification with given title and message.
     */
    private fun showNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Timber.e("Brak uprawnień do powiadomień, pomijam...")
                return
            }
        }

        val channelId = "eclinic_channel"
        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    /**
     * Creates a notification channel for Android O+ devices.
     */
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "E-Clinic Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Appointment reminders and updates"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
