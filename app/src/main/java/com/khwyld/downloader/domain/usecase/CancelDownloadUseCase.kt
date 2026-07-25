package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.repository.DownloadRepository

class CancelDownloadUseCase(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(taskId: Long) = repository.cancelDownload(taskId)
}
