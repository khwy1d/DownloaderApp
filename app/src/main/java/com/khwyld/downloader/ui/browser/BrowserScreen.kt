package com.khwyld.downloader.ui.browser

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(viewModel: BrowserViewModel) {
    val currentUrl by viewModel.currentUrl.collectAsState()
    val downloadMessage by viewModel.downloadStartedMessage.collectAsState()
    var addressBarText by remember { mutableStateOf(currentUrl) }

    Column(modifier = Modifier.fillMaxSize()) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        ) {
            OutlinedTextField(
                value = addressBarText,
                onValueChange = { addressBarText = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("ابحث أو أدخل رابطًا") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val target = normalizeToUrl(addressBarText)
                viewModel.onNavigate(target)
            }) {
                Text("فتح")
            }
        }

        downloadMessage?.let {
            Text(text = it, modifier = Modifier.padding(horizontal = 8.dp))
            LaunchedEffect(it) {
                kotlinx.coroutines.delay(2000)
                viewModel.clearDownloadMessage()
            }
        }

        WebViewContainer(
            url = currentUrl,
            onUrlChanged = { addressBarText = it },
            onPageFinished = { url, title -> viewModel.onPageVisited(url, title) },
            onDownloadRequested = { url, fileName -> viewModel.onDownloadIntercepted(url, fileName) }
        )
    }
}

private fun normalizeToUrl(input: String): String {
    val trimmed = input.trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
        else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
    }
}
