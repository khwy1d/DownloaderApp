package com.khwyld.downloader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.khwyld.downloader.domain.model.HistoryEntry

@Entity(tableName = "browser_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val visitedAt: Long
)

fun HistoryEntity.toDomain() = HistoryEntry(id, url, title, visitedAt)
fun HistoryEntry.toEntity() = HistoryEntity(id, url, title, visitedAt)
