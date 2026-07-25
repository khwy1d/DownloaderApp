package com.khwyld.downloader.data.download.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * DownloadManager النظامي يتكفل تلقائيًا بإشعارات التقدم والاكتمال.
 * هذه الوحدة مسؤولة فقط عن الحالات التي لا يغطيها (مثل الفشل الصريح
 * أو رسائل مخصصة)، تمهيدًا لاستبدالها بتحكم كامل عند الانتقال لمحرك مُدار لاحقًا.
 */
class NotificationController(private val context: Context) {

    private val channelId = "downloader_channel"
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "التنزيلات",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
    }

    fun showFailed(taskId: Long, fileName: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("فشل التنزيل")
            .setContentText(fileName)
            .setAutoCancel(true)
            .build()
        manager.notify(taskId.toInt(), notification)
    }
}
