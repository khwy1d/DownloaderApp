package com.khwyld.downloader.ui.browser

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Browser Module (مبسط لـ MVP):
 * - WebViewController: هذا الملف (تحميل، تنقل، تحديث العنوان).
 * - DownloadInterceptor: مضمّن هنا عبر setDownloadListener بدل ملف منفصل،
 *   لبساطة الحجم في MVP. الفصل الكامل (CookieManagerBridge وملف Interceptor
 *   مستقل) محجوز للمرحلة الثانية عندما تُضاف الكوكيز ودعم صفحات الفيديو.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContainer(
    url: String,
    onUrlChanged: (String) -> Unit,
    onPageFinished: (url: String, title: String) -> Unit,
    onDownloadRequested: (url: String, fileName: String?) -> Unit
) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, finishedUrl: String) {
                    super.onPageFinished(view, finishedUrl)
                    onUrlChanged(finishedUrl)
                    onPageFinished(finishedUrl, view.title ?: finishedUrl)
                }
            }

            // DownloadInterceptor: أي رابط تنزيل يطلقه المتصفح (ملفات، صور، مرفقات)
            // يُعترض هنا بدل تحميله داخل WebView، ويُمرر لمحرك التنزيل الخاص بالتطبيق.
            setDownloadListener { downloadUrl, _, contentDisposition, _, _ ->
                val fileName = contentDisposition
                    ?.substringAfter("filename=", "")
                    ?.trim('"')
                    ?.ifBlank { null }
                onDownloadRequested(downloadUrl, fileName)
            }

            loadUrl(url)
        }
    })
}
