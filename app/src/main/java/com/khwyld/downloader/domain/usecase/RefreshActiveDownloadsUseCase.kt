package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.repository.DownloadRepository

class RefreshActiveDownloadsUseCase(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke() = repository.refreshActiveDownloads()
}
