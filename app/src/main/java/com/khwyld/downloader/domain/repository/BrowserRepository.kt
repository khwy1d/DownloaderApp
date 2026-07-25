package com.khwyld.downloader.domain.repository

import com.khwyld.downloader.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface BrowserRepository {
    suspend fun addHistory(url: String, title: String)
    fun observeHistory(): Flow<List<HistoryEntry>>
    suspend fun clearHistory()
}
