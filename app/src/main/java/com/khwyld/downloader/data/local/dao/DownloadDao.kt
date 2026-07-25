package com.khwyld.downloader.data.local.dao

import androidx.room.*
import com.khwyld.downloader.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert
    suspend fun insert(entity: DownloadEntity): Long

    @Update
    suspend fun update(entity: DownloadEntity)

    @Query("SELECT * FROM download_tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DownloadEntity?

    @Query("SELECT * FROM download_tasks WHERE systemDownloadId = :systemId LIMIT 1")
    suspend fun getBySystemId(systemId: Long): DownloadEntity?

    @Query("SELECT * FROM download_tasks WHERE status IN ('PENDING','RUNNING','PAUSED') ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY updatedAt DESC")
    fun observeCompleted(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('PENDING','RUNNING','PAUSED')")
    suspend fun getActiveOnce(): List<DownloadEntity>

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun delete(id: Long)
}
