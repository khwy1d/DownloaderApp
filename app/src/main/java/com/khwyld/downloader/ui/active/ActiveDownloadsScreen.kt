package com.khwyld.downloader.ui.active

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.khwyld.downloader.domain.model.DownloadStatus
import com.khwyld.downloader.domain.model.DownloadTask
import com.khwyld.downloader.domain.model.VideoStreamOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveDownloadsScreen(
    viewModel: ActiveDownloadsViewModel,
    prefillUrl: String?
) {
    val downloads by viewModel.activeDownloads.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val isResolving by viewModel.isResolving.collectAsState()
    val pendingVideoOptions by viewModel.pendingVideoOptions.collectAsState()
    var linkText by remember { mutableStateOf(prefillUrl.orEmpty()) }

    // إذا وصل رابط عبر المشاركة نبدأ التنزيل تلقائيًا مرة واحدة
    LaunchedEffect(prefillUrl) {
        if (!prefillUrl.isNullOrBlank()) {
            viewModel.onPasteLink(prefillUrl)
            linkText = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = linkText,
                onValueChange = { linkText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("الصق رابط التنزيل هنا") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                enabled = !isResolving,
                onClick = {
                    if (linkText.isNotBlank()) {
                        viewModel.onPasteLink(linkText)
                        linkText = ""
                    }
                }
            ) {
                if (isResolving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("بدء")
                }
            }
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
            LaunchedEffect(it) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (downloads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد تنزيلات جارية", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(downloads, key = { it.id }) { task ->
                    DownloadItemRow(task = task, onCancel = { viewModel.onCancel(task.id) })
                }
            }
        }
    }

    pendingVideoOptions?.let { videoPage ->
        VideoQualityPickerDialog(
            title = videoPage.title,
            streams = videoPage.streams,
            onSelect = { stream -> viewModel.onStreamSelected(videoPage.title, stream) },
            onDismiss = { viewModel.onDismissVideoOptions() }
        )
    }
}

@Composable
private fun VideoQualityPickerDialog(
    title: String,
    streams: List<VideoStreamOption>,
    onSelect: (VideoStreamOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, maxLines = 2) },
        text = {
            Column {
                Text("اختر جودة التنزيل:", color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                streams.forEach { stream ->
                    TextButton(
                        onClick = { onSelect(stream) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stream.label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun DownloadItemRow(task: DownloadTask, onCancel: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = task.fileName, maxLines = 1, modifier = Modifier.weight(1f))
                IconButton(onClick = onCancel) {
                    Icon(Icons.Filled.Close, contentDescription = "إلغاء")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = (task.progress.coerceIn(0, 100)) / 100f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusLabel(task),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun statusLabel(task: DownloadTask): String = when (task.status) {
    DownloadStatus.PENDING -> "بانتظار البدء..."
    DownloadStatus.RUNNING -> "جارٍ التنزيل: ${task.progress}%"
    DownloadStatus.PAUSED -> "متوقف مؤقتًا"
    DownloadStatus.COMPLETED -> "اكتمل"
    DownloadStatus.FAILED -> "فشل: ${task.errorMessage ?: "خطأ غير معروف"}"
}
