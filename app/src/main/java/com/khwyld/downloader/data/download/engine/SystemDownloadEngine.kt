package com.khwyld.downloader.data.download.engine

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

/**
 * تنفيذ وحيد لمحرك التنزيل في نسخة MVP، يعتمد على DownloadManager النظامي.
 * هذا القرار مقصود لتبسيط النسخة الأولى (بدل ManagedDownloadEngine + Selector)
 * بناءً على أن أندرويد يوفر أساسًا مستقرًا لإدارة الطابور والإشعارات والاستئناف.
 */
class SystemDownloadEngine(private val context: Context) : DownloadEngine {

    private val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    override fun enqueue(url: String, fileName: String, mimeType: String?): Long {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        mimeType?.let { request.setMimeType(it) }

        return manager.enqueue(request)
    }

    override fun queryStatus(systemDownloadId: Long): EngineDownloadStatus? {
        val query = DownloadManager.Query().setFilterById(systemDownloadId)
        manager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) return null

            val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val reasonIdx = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
            val bytesDownloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val bytesTotalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            val localUriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

            val status = cursor.getInt(statusIdx)
            val reason = if (reasonIdx >= 0) cursor.getInt(reasonIdx).toString() else null

            return EngineDownloadStatus(
                systemDownloadId = systemDownloadId,
                isComplete = status == DownloadManager.STATUS_SUCCESSFUL,
                isFailed = status == DownloadManager.STATUS_FAILED,
                isRunning = status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING,
                downloadedBytes = cursor.getLong(bytesDownloadedIdx),
                totalBytes = cursor.getLong(bytesTotalIdx),
                localUri = if (localUriIdx >= 0) cursor.getString(localUriIdx) else null,
                reason = reason
            )
        }
    }

    override fun cancel(systemDownloadId: Long) {
        manager.remove(systemDownloadId)
    }
}
