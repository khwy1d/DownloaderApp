package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.repository.BrowserRepository

class AddHistoryUseCase(
    private val repository: BrowserRepository
) {
    suspend operator fun invoke(url: String, title: String) = repository.addHistory(url, title)
}
