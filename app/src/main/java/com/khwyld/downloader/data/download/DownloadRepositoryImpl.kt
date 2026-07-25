package com.khwyld.downloader.data.download

import com.khwyld.downloader.data.download.engine.DownloadEngine
import com.khwyld.downloader.data.download.notification.NotificationController
import com.khwyld.downloader.data.local.dao.DownloadDao
import com.khwyld.downloader.data.local.entity.toDomain
import com.khwyld.downloader.data.local.entity.toEntity
import com.khwyld.downloader.domain.model.DownloadStatus
import com.khwyld.downloader.domain.model.DownloadTask
import com.khwyld.downloader.domain.repository.DownloadRepository
import com.khwyld.downloader.util.FileUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DownloadRepositoryImpl(
    private val dao: DownloadDao,
    private val engine: DownloadEngine,
    private val notificationController: NotificationController
) : DownloadRepository {

    override suspend fun startDownload(url: String, suggestedFileName: String?): Long {
        val fileName = suggestedFileName ?: FileUtils.guessFileName(url)
        val mimeType = FileUtils.guessMimeType(fileName)

        // إنشاء السجل المحلي أولًا بحالة PENDING قبل تسليمه للمحرك
        val task = DownloadTask(
            sourceUrl = url,
            resolvedUrl = url, // MVP: لا يوجد LinkResolver بعد، الرابط يُستخدم كما هو
            fileName = fileName,
            mimeType = mimeType,
            status = DownloadStatus.PENDING,
            engineType = "SYSTEM"
        )
        val localId = dao.insert(task.toEntity())

        // تسليم المهمة لمحرك التنزيل النظامي والحصول على معرفه الخاص
        val systemId = engine.enqueue(url, fileName, mimeType)

        val updated = task.copy(
            id = localId,
            status = DownloadStatus.RUNNING,
            systemDownloadId = systemId
        )
        dao.update(updated.toEntity())

        return localId
    }

    override fun observeActiveDownloads(): Flow<List<DownloadTask>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeCompletedDownloads(): Flow<List<DownloadTask>> =
        dao.observeCompleted().map { list -> list.map { it.toDomain() } }

    /**
     * تُستدعى دوريًا (polling) من ViewModel شاشة التنزيلات الجارية لتحديث
     * تقدم كل مهمة نشطة من DownloadManager إلى Room.
     */
    override suspend fun refreshActiveDownloads() {
        val activeTasks = dao.getActiveOnce()
        for (entity in activeTasks) {
            val systemId = entity.systemDownloadId ?: continue
            val status = engine.queryStatus(systemId) ?: continue

            val newStatus = when {
                status.isComplete -> DownloadStatus.COMPLETED
                status.isFailed -> DownloadStatus.FAILED
                status.isRunning -> DownloadStatus.RUNNING
                else -> DownloadStatus.PENDING
            }

            val progress = if (status.totalBytes > 0) {
                ((status.downloadedBytes * 100) / status.totalBytes).toInt()
            } else 0

            val updated = entity.copy(
                status = newStatus.name,
                progress = progress,
                downloadedBytes = status.downloadedBytes,
                totalBytes = status.totalBytes,
                updatedAt = System.currentTimeMillis(),
                localFilePath = status.localUri ?: entity.localFilePath,
                errorMessage = if (newStatus == DownloadStatus.FAILED) status.reason else null
            )
            dao.update(updated)

            if (newStatus == DownloadStatus.FAILED && entity.status != DownloadStatus.FAILED.name) {
                notificationController.showFailed(entity.id, entity.fileName)
            }
        }
    }

    override suspend fun cancelDownload(taskId: Long) {
        val entity = dao.getById(taskId) ?: return
        entity.systemDownloadId?.let { engine.cancel(it) }
        dao.delete(taskId)
    }

    override suspend fun deleteCompleted(taskId: Long, deleteFile: Boolean) {
        val entity = dao.getById(taskId) ?: return
        if (deleteFile) {
            entity.systemDownloadId?.let { engine.cancel(it) } // إزالة السجل من DownloadManager يحذف الملف أيضًا
        }
        dao.delete(taskId)
    }

    /** يُستدعى من DownloadCompleteReceiver عند وصول Broadcast الاكتمال من النظام مباشرة. */
    override suspend fun onSystemDownloadComplete(systemDownloadId: Long) {
        val entity = dao.getBySystemId(systemDownloadId) ?: return
        val status = engine.queryStatus(systemDownloadId) ?: return

        val newStatus = if (status.isComplete) DownloadStatus.COMPLETED else DownloadStatus.FAILED
        val updated = entity.copy(
            status = newStatus.name,
            progress = if (newStatus == DownloadStatus.COMPLETED) 100 else entity.progress,
            downloadedBytes = status.downloadedBytes,
            totalBytes = status.totalBytes,
            localFilePath = status.localUri ?: entity.localFilePath,
            updatedAt = System.currentTimeMillis(),
            errorMessage = if (newStatus == DownloadStatus.FAILED) status.reason else null
        )
        dao.update(updated)

        if (newStatus == DownloadStatus.FAILED) {
            notificationController.showFailed(entity.id, entity.fileName)
        }
        // ملاحظة: تصنيف الملف الفعلي (FileScanner: نوع + صورة مصغرة) محجوز
        // للمرحلة الثانية؛ حاليًا يكتفى بنقل الحالة إلى COMPLETED لتظهر في المكتبة.
    }
}
