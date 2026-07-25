package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.repository.DownloadRepository

/**
 * حالة استخدام بدء تنزيل جديد.
 * تُستدعى من ثلاث نقاط دخول: لصق الرابط، المشاركة، اعتراض المتصفح الداخلي.
 */
class StartDownloadUseCase(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(url: String, suggestedFileName: String? = null): Result<Long> {
        val trimmed = url.trim()
        if (trimmed.isEmpty() || !(trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            return Result.failure(IllegalArgumentException("رابط غير صالح"))
        }
        return try {
            val id = repository.startDownload(trimmed, suggestedFileName)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
