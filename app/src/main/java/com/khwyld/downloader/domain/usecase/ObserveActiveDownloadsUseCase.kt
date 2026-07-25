package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.model.DownloadTask
import com.khwyld.downloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveDownloadsUseCase(
    private val repository: DownloadRepository
) {
    operator fun invoke(): Flow<List<DownloadTask>> = repository.observeActiveDownloads()
}
