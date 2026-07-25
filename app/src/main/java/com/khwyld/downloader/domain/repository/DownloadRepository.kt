package com.khwyld.downloader.domain.repository

import com.khwyld.downloader.domain.model.DownloadTask
import kotlinx.coroutines.flow.Flow

/**
 * عقد مجرد لا يعرف شيئًا عن Room أو DownloadManager.
 * طبقة data هي من تنفذ هذه الواجهة (DownloadRepositoryImpl).
 */
interface DownloadRepository {

    /** بدء تنزيل جديد وإرجاع معرف المهمة المُنشأة محليًا. */
    suspend fun startDownload(url: String, suggestedFileName: String? = null): Long

    /** مراقبة كل التنزيلات الجارية (Pending / Running / Paused) كـ Flow حي. */
    fun observeActiveDownloads(): Flow<List<DownloadTask>>

    /** مراقبة الملفات المكتملة (تُستخدم في شاشة المكتبة). */
    fun observeCompletedDownloads(): Flow<List<DownloadTask>>

    /** مزامنة حالة التنزيلات الجارية مع النظام (تُستدعى دوريًا من الشاشة). */
    suspend fun refreshActiveDownloads()

    /** إلغاء / حذف مهمة تنزيل. */
    suspend fun cancelDownload(taskId: Long)

    /** حذف ملف مكتمل من المكتبة (سجل + ملف فعلي اختياريًا). */
    suspend fun deleteCompleted(taskId: Long, deleteFile: Boolean)

    /** يُستدعى عند استقبال Broadcast اكتمال التنزيل من النظام. */
    suspend fun onSystemDownloadComplete(systemDownloadId: Long)
}
