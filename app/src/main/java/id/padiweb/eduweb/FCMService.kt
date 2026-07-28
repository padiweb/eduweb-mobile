package id.padiweb.eduweb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * FCMService — Push Notification Handler
 *
 * Firebase dinonaktifkan sementara.
 * Untuk aktifkan push notification:
 * 1. Buat project Firebase di https://console.firebase.google.com
 * 2. Download google-services.json ke folder app/
 * 3. Tambah kembali dependency firebase di build.gradle.kts
 * 4. Ganti class ini dengan FirebaseMessagingService
 */
class FCMService {

    companion object {
        const val CHANNEL_ID   = "eduweb_channel"
        const val CHANNEL_NAME = "Altan EduWeb Notifikasi"

        fun showNotification(context: Context, title: String, body: String) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifikasi dari Altan EduWeb"
                    enableLights(true)
                    enableVibration(true)
                }
                nm.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            nm.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
