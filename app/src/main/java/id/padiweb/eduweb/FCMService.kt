package id.padiweb.eduweb

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Ambil data dari notifikasi
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Altan Mobile"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: return

        // Tentukan channel berdasarkan tipe
        val type      = remoteMessage.data["type"] ?: "umum"
        val channelId = when (type) {
            "tugas"       -> CHANNEL_ID_TUGAS
            "absensi"     -> CHANNEL_ID_ABSEN
            "pengumuman"  -> CHANNEL_ID_PENGUMUMAN
            "pelanggaran" -> CHANNEL_ID_PELANGGARAN
            else          -> CHANNEL_ID
        }

        showNotification(this, title, body, channelId)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token baru - simpan ke SharedPreferences untuk dikirim ke server
        getSharedPreferences("eduweb_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
    }

    companion object {
        const val CHANNEL_ID            = "eduweb_umum"
        const val CHANNEL_ID_TUGAS      = "eduweb_tugas"
        const val CHANNEL_ID_ABSEN      = "eduweb_absensi"
        const val CHANNEL_ID_PENGUMUMAN = "eduweb_pengumuman"
        const val CHANNEL_ID_PELANGGARAN = "eduweb_pelanggaran"

        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

                listOf(
                    Triple(CHANNEL_ID,             "Umum",        NotificationManager.IMPORTANCE_DEFAULT),
                    Triple(CHANNEL_ID_TUGAS,        "Tugas Baru",  NotificationManager.IMPORTANCE_HIGH),
                    Triple(CHANNEL_ID_ABSEN,        "Absensi",     NotificationManager.IMPORTANCE_HIGH),
                    Triple(CHANNEL_ID_PENGUMUMAN,   "Pengumuman",  NotificationManager.IMPORTANCE_DEFAULT),
                    Triple(CHANNEL_ID_PELANGGARAN,  "Pelanggaran", NotificationManager.IMPORTANCE_HIGH),
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
            createChannels(context)

            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
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
