package br.com.fiap.wtcconnect.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Try to register token for the current authenticated user
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            registerTokenForUser(user.uid, token)
        } else {
            // Save to local storage so it can be registered after login
            applicationContext.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                .edit().putString("pending_fcm_token", token).apply()
        }
    }

    private fun registerTokenForUser(uid: String, token: String) {
        val userRef = firestore.collection("users").document(uid)
        val map = mapOf("fcmTokens.$token" to true)
        userRef.set(map, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { /* token registered */ }
            .addOnFailureListener { it.printStackTrace() }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Do not call super: FirebaseMessagingService implementation is empty

        val data = remoteMessage.data
        val chatId = data["chatId"]
        val messageId = data["messageId"]
        val title = remoteMessage.notification?.title ?: data["title"]
        val body = remoteMessage.notification?.body ?: data["body"] ?: data["text"]

        // If app is in foreground, emit an in-app event for UI to show popup
        if (isAppInForeground()) {
            InAppEventBus.emit(InAppEvent.NewMessage(chatId = chatId, messageId = messageId, title = title, body = body))
        } else {
            // Show system notification to bring user back to app
            showSystemNotification(title ?: "Nova mensagem", body ?: "Você recebeu uma mensagem", chatId)
        }
    }

    private fun showSystemNotification(title: String, body: String, chatId: String?) {
        val channelId = "wtc_messages"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Mensagens WTC", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Create pending intent to open the app (MainActivity)
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        notificationManager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notificationBuilder.build())
    }

    private fun isAppInForeground(): Boolean {
        // For now return true to always dispatch in-app events in foreground if desired.
        return true
    }
}
