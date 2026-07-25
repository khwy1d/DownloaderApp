package com.khwyld.downloader.data.browser

import com.khwyld.downloader.data.local.dao.HistoryDao
import com.khwyld.downloader.data.local.entity.HistoryEntity
import com.khwyld.downloader.data.local.entity.toDomain
import com.khwyld.downloader.domain.model.HistoryEntry
import com.khwyld.downloader.domain.repository.BrowserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrowserRepositoryImpl(
    private val historyDao: HistoryDao
) : BrowserRepository {

    override suspend fun addHistory(url: String, title: String) {
        historyDao.insert(
            HistoryEntity(url = url, title = title, visitedAt = System.currentTimeMillis())
        )
    }

    override fun observeHistory(): Flow<List<HistoryEntry>> =
        historyDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun clearHistory() = historyDao.clear()
}
