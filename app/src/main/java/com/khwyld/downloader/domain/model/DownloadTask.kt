package com.khwyld.downloader.domain.model

/**
 * تمثيل مهمة التنزيل عبر كامل الطبقات (Domain model).
 * هذا النموذج مستقل عن Room ومستقل عن DownloadManager.
 */
data class DownloadTask(
    val id: Long = 0,
    val sourceUrl: String,
    val resolvedUrl: String = sourceUrl,
    val fileName: String,
    val mimeType: String? = null,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0, // 0..100
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val speedBytesPerSec: Long = 0,
    val remainingTimeSec: Long = -1,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val engineType: String = "SYSTEM", // MVP: محرك واحد فقط، محجوز للتوسع لاحقًا
    val resumeSupported: Boolean = false,
    val errorMessage: String? = null,
    val systemDownloadId: Long? = null, // معرف DownloadManager الداخلي
    val localFilePath: String? = null
)
