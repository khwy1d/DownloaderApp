package com.khwyld.downloader.domain.usecase

import com.khwyld.downloader.domain.model.HistoryEntry
import com.khwyld.downloader.domain.repository.BrowserRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(
    private val repository: BrowserRepository
) {
    operator fun invoke(): Flow<List<HistoryEntry>> = repository.observeHistory()
}
