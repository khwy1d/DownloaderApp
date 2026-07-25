package com.khwyld.downloader.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khwyld.downloader.domain.usecase.AddHistoryUseCase
import com.khwyld.downloader.domain.usecase.StartDownloadUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrowserViewModel(
    private val addHistory: AddHistoryUseCase,
    private val startDownload: StartDownloadUseCase
) : ViewModel() {

    private val _currentUrl = MutableStateFlow("https://www.google.com")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _downloadStartedMessage = MutableStateFlow<String?>(null)
    val downloadStartedMessage: StateFlow<String?> = _downloadStartedMessage.asStateFlow()

    fun onNavigate(url: String) {
        _currentUrl.value = url
    }

    fun onPageVisited(url: String, title: String) {
        viewModelScope.launch { addHistory(url, title) }
    }

    /** يُستدعى من DownloadInterceptor داخل WebViewController عند اعتراض رابط تنزيل. */
    fun onDownloadIntercepted(url: String, suggestedFileName: String?) {
        viewModelScope.launch {
            val result = startDownload(url, suggestedFileName)
            result.onSuccess { _downloadStartedMessage.value = "بدأ التنزيل" }
        }
    }

    fun clearDownloadMessage() {
        _downloadStartedMessage.value = null
    }
}
