package com.khwyld.downloader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.khwyld.downloader.domain.model.DownloadStatus
import com.khwyld.downloader.domain.model.DownloadTask

/**
 * جدول التنزيلات - يطابق حقول DownloadTask المحددة في الوثيقة.
 */
@Entity(tableName = "download_tasks")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceUrl: String,
    val resolvedUrl: String,
    val fileName: String,
    val mimeType: String?,
    val status: String,
    val progress: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val speedBytesPerSec: Long,
    val remainingTimeSec: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val engineType: String,
    val resumeSupported: Boolean,
    val errorMessage: String?,
    val systemDownloadId: Long?,
    val localFilePath: String?
)

fun DownloadEntity.toDomain(): DownloadTask = DownloadTask(
    id = id,
    sourceUrl = sourceUrl,
    resolvedUrl = resolvedUrl,
    fileName = fileName,
    mimeType = mimeType,
    status = DownloadStatus.valueOf(status),
    progress = progress,
    downloadedBytes = downloadedBytes,
    totalBytes = totalBytes,
    speedBytesPerSec = speedBytesPerSec,
    remainingTimeSec = remainingTimeSec,
    createdAt = createdAt,
    updatedAt = updatedAt,
    engineType = engineType,
    resumeSupported = resumeSupported,
    errorMessage = errorMessage,
    systemDownloadId = systemDownloadId,
    localFilePath = localFilePath
)

fun DownloadTask.toEntity(): DownloadEntity = DownloadEntity(
    id = id,
    sourceUrl = sourceUrl,
    resolvedUrl = resolvedUrl,
    fileName = fileName,
    mimeType = mimeType,
    status = status.name,
    progress = progress,
    downloadedBytes = downloadedBytes,
    totalBytes = totalBytes,
    speedBytesPerSec = speedBytesPerSec,
    remainingTimeSec = remainingTimeSec,
    createdAt = createdAt,
    updatedAt = updatedAt,
    engineType = engineType,
    resumeSupported = resumeSupported,
    errorMessage = errorMessage,
    systemDownloadId = systemDownloadId,
    localFilePath = localFilePath
)
