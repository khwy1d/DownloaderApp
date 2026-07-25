package com.khwyld.downloader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.khwyld.downloader.data.local.dao.DownloadDao
import com.khwyld.downloader.data.local.dao.HistoryDao
import com.khwyld.downloader.data.local.entity.DownloadEntity
import com.khwyld.downloader.data.local.entity.HistoryEntity

@Database(
    entities = [DownloadEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "downloader.db"
                ).build().also { INSTANCE = it }
            }
    }
}
