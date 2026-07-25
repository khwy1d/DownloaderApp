package com.khwyld.downloader.ui.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khwyld.downloader.domain.model.DownloadTask
import com.khwyld.downloader.domain.usecase.CancelDownloadUseCase
import com.khwyld.downloader.domain.usecase.ObserveActiveDownloadsUseCase
import com.khwyld.downloader.domain.usecase.RefreshActiveDownloadsUseCase
import com.khwyld.downloader.domain.usecase.StartDownloadUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActiveDownloadsViewModel(
    private val observeActiveDownloads: ObserveActiveDownloadsUseCase,
    private val refreshActiveDownloads: RefreshActiveDownloadsUseCase,
    private val cancelDownload: CancelDownloadUseCase,
    private val startDownload: StartDownloadUseCase
) : ViewModel() {

    private val _activeDownloads = MutableStateFlow<List<DownloadTask>>(emptyList())
    val activeDownloads: StateFlow<List<DownloadTask>> = _activeDownloads.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            observeActiveDownloads().collect { _activeDownloads.value = it }
        }

        // مزامنة دورية بسيطة كل ثانيتين مع DownloadManager طالما الشاشة مفتوحة.
        // (Polling مقصود ومبسط لنسخة MVP بدل مراقبة أعمق عبر Content Observer)
        viewModelScope.launch {
            while (true) {
                refreshActiveDownloads()
                delay(2000)
            }
        }
    }

    fun onPasteLink(url: String) {
        viewModelScope.launch {
            val result = startDownload(url)
            result.onFailure { e -> _errorMessage.value = e.message }
        }
    }

    fun onCancel(taskId: Long) {
        viewModelScope.launch { cancelDownload(taskId) }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
