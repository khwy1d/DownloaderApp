package com.khwyld.downloader.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.khwyld.downloader.DownloaderApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * يلتقط ACTION_DOWNLOAD_COMPLETE من النظام ويحدّث السجل المحلي فورًا،
 * بدل الاعتماد فقط على الـ polling الدوري في شاشة التنزيلات الجارية.
 */
class DownloadCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        val systemId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (systemId == -1L) return

        val repository = (context.applicationContext as DownloaderApplication)
            .container.downloadRepository

        CoroutineScope(Dispatchers.IO).launch {
            repository.onSystemDownloadComplete(systemId)
        }
    }
}
