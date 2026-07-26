package com.khwyld.downloader

import android.app.Application
import com.khwyld.downloader.data.download.resolver.OkHttpDownloaderImpl
import com.khwyld.downloader.di.AppContainer
import org.schabi.newpipe.extractor.NewPipe

class DownloaderApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        NewPipe.init(OkHttpDownloaderImpl.getInstance())
        container = AppContainer(this)
    }
}
