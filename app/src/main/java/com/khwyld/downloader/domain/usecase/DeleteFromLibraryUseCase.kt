package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.repository.DownloadRepository

class DeleteFromLibraryUseCase(
    private val repository: DownloadRepository
) {
    suspend operator fun invoke(taskId: Long, deleteFile: Boolean) =
        repository.deleteCompleted(taskId, deleteFile)
}
