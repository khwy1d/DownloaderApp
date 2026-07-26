package com.khwyld.downloader.ui.active

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khwyld.downloader.domain.model.DownloadTask
import com.khwyld.downloader.domain.model.ResolvedLink
import com.khwyld.downloader.domain.model.VideoStreamOption
import com.khwyld.downloader.domain.usecase.CancelDownloadUseCase
import com.khwyld.downloader.domain.usecase.ObserveActiveDownloadsUseCase
import com.khwyld.downloader.domain.usecase.RefreshActiveDownloadsUseCase
import com.khwyld.downloader.domain.usecase.ResolveLinkUseCase
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
    private val startDownload: StartDownloadUseCase,
    private val resolveLink: ResolveLinkUseCase
) : ViewModel() {

    private val _activeDownloads = MutableStateFlow<List<DownloadTask>>(emptyList())
    val activeDownloads: StateFlow<List<DownloadTask>> = _activeDownloads.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    /** يُعبّأ عندما يكون الرابط صفحة فيديو (يوتيوب) تحتاج اختيار جودة قبل التنزيل. */
    private val _pendingVideoOptions = MutableStateFlow<ResolvedLink.VideoPage?>(null)
    val pendingVideoOptions: StateFlow<ResolvedLink.VideoPage?> = _pendingVideoOptions.asStateFlow()

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
            _isResolving.value = true
            val resolvedResult = resolveLink(url)
            _isResolving.value = false

            resolvedResult.fold(
                onSuccess = { resolved ->
                    when (resolved) {
                        is ResolvedLink.DirectFile -> startAndReport(resolved.url)
                        is ResolvedLink.VideoPage -> _pendingVideoOptions.value = resolved
                    }
                },
                onFailure = { e -> _errorMessage.value = e.message }
            )
        }
    }

    fun onStreamSelected(title: String, stream: VideoStreamOption) {
        val safeTitle = title.replace(Regex("""[\\/:*?"<>|]"""), "_")
        startAndReport(stream.url, "$safeTitle.${stream.format}")
        _pendingVideoOptions.value = null
    }

    fun onDismissVideoOptions() {
        _pendingVideoOptions.value = null
    }

    private fun startAndReport(url: String, suggestedFileName: String? = null) {
        viewModelScope.launch {
            val result = startDownload(url, suggestedFileName)
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
