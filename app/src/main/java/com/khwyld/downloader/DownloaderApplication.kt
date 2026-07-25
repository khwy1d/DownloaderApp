package com.khwyld.downloader

import android.app.Application
import com.khwyld.downloader.di.AppContainer

class DownloaderApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
