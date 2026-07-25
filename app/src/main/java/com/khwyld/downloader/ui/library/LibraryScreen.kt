package com.khwyld.downloader.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.khwyld.downloader.domain.model.DownloadTask

/**
 * شاشة المكتبة. التصنيف هنا مبني على mimeType فقط (أيقونة مناسبة لكل نوع)
 * كنسخة مبسطة مؤقتة تحل محل FileScanner الكامل (كشف فئة + توليد صورة مصغرة
 * فعلية) الموصوف في الوثيقة، والمحجوز للمرحلة الثانية.
 */
@Composable
fun LibraryScreen(viewModel: LibraryViewModel) {
    val files by viewModel.completedDownloads.collectAsState()
    var pendingDelete by remember { mutableStateOf<DownloadTask?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد ملفات في المكتبة بعد", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(files, key = { it.id }) { task ->
                    LibraryItemRow(
                        task = task,
                        onDeleteClick = { pendingDelete = task }
                    )
                }
            }
        }
    }

    pendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("حذف الملف") },
            text = { Text("هل تريد حذف \"${task.fileName}\" من الجهاز أيضًا، أم إزالته من المكتبة فقط؟") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDelete(task.id, deleteFile = true)
                    pendingDelete = null
                }) { Text("حذف من الجهاز") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onDelete(task.id, deleteFile = false)
                    pendingDelete = null
                }) { Text("إزالة من المكتبة فقط") }
            }
        )
    }
}

@Composable
private fun LibraryItemRow(task: DownloadTask, onDeleteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconForMimeType(task.mimeType),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.fileName, maxLines = 1)
                Text(
                    text = formatSize(task.totalBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "حذف")
            }
        }
    }
}

private fun iconForMimeType(mimeType: String?): ImageVector = when {
    mimeType == null -> Icons.Filled.InsertDriveFile
    mimeType.startsWith("image/") -> Icons.Filled.Image
    mimeType.startsWith("video/") -> Icons.Filled.VideoFile
    mimeType.startsWith("audio/") -> Icons.Filled.AudioFile
    mimeType == "application/pdf" || mimeType.startsWith("text/") -> Icons.Filled.Description
    else -> Icons.Filled.InsertDriveFile
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "حجم غير معروف"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1 -> "%.2f GB".format(gb)
        mb >= 1 -> "%.2f MB".format(mb)
        else -> "%.0f KB".format(kb)
    }
}
