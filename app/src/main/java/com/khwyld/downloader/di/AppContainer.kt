package com.khwyld.downloader.di

import android.content.Context
import com.khwyld.downloader.data.browser.BrowserRepositoryImpl
import com.khwyld.downloader.data.download.DownloadRepositoryImpl
import com.khwyld.downloader.data.download.engine.DownloadEngine
import com.khwyld.downloader.data.download.engine.SystemDownloadEngine
import com.khwyld.downloader.data.download.notification.NotificationController
import com.khwyld.downloader.data.download.resolver.VideoResolverRepositoryImpl
import com.khwyld.downloader.data.local.AppDatabase
import com.khwyld.downloader.domain.repository.BrowserRepository
import com.khwyld.downloader.domain.repository.DownloadRepository
import com.khwyld.downloader.domain.repository.VideoResolverRepository
import com.khwyld.downloader.domain.usecase.*

/**
 * حاوية DI يدوية بسيطة (بدل Hilt/Dagger) — قرار مقصود لتبسيط MVP.
 * كل شيء singleton بسيط عبر lazy، ويمكن استبدالها بـ Hilt لاحقًا
 * دون التأثير على طبقتي domain وui لأنهما يعتمدان على الواجهات فقط.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    private val downloadEngine: DownloadEngine by lazy { SystemDownloadEngine(context) }
    private val notificationController: NotificationController by lazy { NotificationController(context) }

    val downloadRepository: DownloadRepository by lazy {
        DownloadRepositoryImpl(database.downloadDao(), downloadEngine, notificationController)
    }

    val browserRepository: BrowserRepository by lazy {
        BrowserRepositoryImpl(database.historyDao())
    }

    private val videoResolverRepository: VideoResolverRepository by lazy {
        VideoResolverRepositoryImpl()
    }

    // ---- UseCases ----
    val startDownloadUseCase by lazy { StartDownloadUseCase(downloadRepository) }
    val observeActiveDownloadsUseCase by lazy { ObserveActiveDownloadsUseCase(downloadRepository) }
    val refreshActiveDownloadsUseCase by lazy { RefreshActiveDownloadsUseCase(downloadRepository) }
    val cancelDownloadUseCase by lazy { CancelDownloadUseCase(downloadRepository) }
    val observeLibraryUseCase by lazy { ObserveLibraryUseCase(downloadRepository) }
    val deleteFromLibraryUseCase by lazy { DeleteFromLibraryUseCase(downloadRepository) }
    val addHistoryUseCase by lazy { AddHistoryUseCase(browserRepository) }
    val observeHistoryUseCase by lazy { ObserveHistoryUseCase(browserRepository) }
    val resolveLinkUseCase by lazy { ResolveLinkUseCase(videoResolverRepository) }
}
