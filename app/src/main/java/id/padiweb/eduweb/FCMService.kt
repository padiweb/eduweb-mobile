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
 * Untuk aktifkan Firebase push notification:
 * 1. Buat project Firebase di https://console.firebase.google.com
 * 2. Tambah aplikasi Android dengan package: id.padiweb.eduweb
 * 3. Download google-services.json ke folder app/
 * 4. Uncomment dependency firebase di build.gradle.kts
 * 5. Uncomment import dan extends FirebaseMessagingService di bawah
 *
 * Setelah Firebase aktif, ganti class ini dengan:
 * class FCMService : FirebaseMessagingService() {
 *     override fun onMessageReceived(remoteMessage: RemoteMessage) {
 *         val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "EduWeb"
 *         val body  = remoteMessage.notification?.body  ?: remoteMessage.data["body"]  ?: ""
 *         if (body.isNotEmpty()) showNotification(this, title, body)
 *     }
 *     override fun onNewToken(token: String) {
 *         // Kirim token ke server Laravel untuk disimpan
 *         // sendTokenToServer(token)
 *     }
 * }
 */
class FCMService {

    companion object {
        const val CHANNEL_ID       = "eduweb_channel"
        const val CHANNEL_NAME     = "EduWeb Notifikasi"
        const val CHANNEL_ID_TUGAS = "eduweb_tugas"
        const val CHANNEL_ID_ABSEN = "eduweb_absensi"
        const val CHANNEL_ID_PENGUMUMAN = "eduweb_pengumuman"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                listOf(
                    Triple(CHANNEL_ID, "Umum", NotificationManager.IMPORTANCE_DEFAULT),
                    Triple(CHANNEL_ID_TUGAS, "Tugas Baru", NotificationManager.IMPORTANCE_HIGH),
                    Triple(CHANNEL_ID_ABSEN, "Absensi", NotificationManager.IMPORTANCE_HIGH),
                    Triple(CHANNEL_ID_PENGUMUMAN, "Pengumuman", NotificationManager.IMPORTANCE_DEFAULT),
                ).forEach { (id, name, importance) ->
                    val channel = NotificationChannel(id, name, importance).apply {
                        enableLights(true)
                        enableVibration(true)
                    }
                    nm.createNotificationChannel(channel)
                }
            }
        }

        fun showNotification(
            context: Context,
            title: String,
            body: String,
            channelId: String = CHANNEL_ID
        ) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            createChannels(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, System.currentTimeMillis().toInt(), intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(context.getColor(R.color.primary))
                .build()

            nm.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
