package com.khwyld.downloader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.khwyld.downloader.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(entity: HistoryEntity)

    @Query("SELECT * FROM browser_history ORDER BY visitedAt DESC LIMIT 200")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("DELETE FROM browser_history")
    suspend fun clear()
}
