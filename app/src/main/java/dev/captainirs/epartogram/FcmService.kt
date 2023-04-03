package dev.captainirs.epartogram

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {
    override fun onCreate() {
        super.onCreate()
        createNotificationsChannels()
    }

    private fun initNotification(
        channel: String,
        title: String,
        body: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
    }

    private fun createNotification(remoteMessage: RemoteMessage) {
        val id = 0 + (Math.random() * 2147483647).toInt()
        val title: String = remoteMessage.data["title"]!!
        val body: String = remoteMessage.data["body"]!!
        val url: String = remoteMessage.data["url"]!!
        val actionName: String = if(remoteMessage.data["action_name"] == null) "Notification" else remoteMessage.data["action_name"]!!
        val actionUrl: String = if(remoteMessage.data["action_url"] == null) getString(R.string.host_url) else remoteMessage.data["action_name"]!!
        val notificationBuilder =
            initNotification(CUSTOM_NOTIFICATION_CHANNEL, title, body)
        val notificationIntent = Intent(this, LauncherActivity::class.java)
        notificationIntent.data = Uri.parse(url)
        val notificationPendingIntent = PendingIntent.getActivity(this, id, notificationIntent, 0)
        val actionIntent = Intent(this, LauncherActivity::class.java)
        actionIntent.data = Uri.parse(actionUrl)
        val actionPendingIntent = PendingIntent.getActivity(this, id, actionIntent, 0)
        val action: NotificationCompat.Action = NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            actionName,
            actionPendingIntent
        ).build()
        val notification: Notification = notificationBuilder
            .setContentIntent(notificationPendingIntent)
            .addAction(action)
            .build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.notify(id, notification)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            if (remoteMessage.data["payload"]!!.isNotEmpty()) {
                createNotification(remoteMessage)
            }
        }
    }

    private fun createNotificationsChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val customNotificationsChannel = NotificationChannel(
                CUSTOM_NOTIFICATION_CHANNEL,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            customNotificationsChannel.description = "${getString(R.string.app_name)} Notifications Channel"
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(customNotificationsChannel)
        }
    }

    // Almost always, this event occurs after installing the application, at the time of the first launch of the app.
   override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("DEBUG", token)
        val previousToken: String = this.applicationContext
            .getSharedPreferences("_", MODE_PRIVATE)
            .getString("fb", "empty")!!

        // We check if we have a current token in the store.
        if (previousToken !== token) {

            // Installing a new token in the storage
            getSharedPreferences("_", MODE_PRIVATE)
                .edit()
                .putString("fb", token)
                .apply()
            val intent = Intent(this, LauncherActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // This command restarts the application to update or set a new token
            this.startActivity(intent)
        }
    }

    companion object {
        const val CUSTOM_NOTIFICATION_CHANNEL = "notification channel"
    }
}