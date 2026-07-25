package com.khwyld.downloader.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khwyld.downloader.domain.model.DownloadTask
import com.khwyld.downloader.domain.usecase.DeleteFromLibraryUseCase
import com.khwyld.downloader.domain.usecase.ObserveLibraryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val observeLibrary: ObserveLibraryUseCase,
    private val deleteFromLibrary: DeleteFromLibraryUseCase
) : ViewModel() {

    private val _completedDownloads = MutableStateFlow<List<DownloadTask>>(emptyList())
    val completedDownloads: StateFlow<List<DownloadTask>> = _completedDownloads.asStateFlow()

    init {
        viewModelScope.launch {
            observeLibrary().collect { _completedDownloads.value = it }
        }
    }

    fun onDelete(taskId: Long, deleteFile: Boolean) {
        viewModelScope.launch { deleteFromLibrary(taskId, deleteFile) }
    }
}
