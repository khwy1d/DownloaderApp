package com.khwyld.downloader.data.download.resolver

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.Request as OkRequest

/**
 * تنفيذ Downloader المطلوب من NewPipeExtractor فوق OkHttp.
 *
 * ⚠️ هذا أكثر ملف عرضة لاختلاف بسيط بتوقيعات NewPipeExtractor حسب الإصدار،
 * بما إني ما قدرت أبني المشروع فعليًا للتحقق (الشبكة معطلة عندي).
 * لو صار خطأ ترجمة هنا تحديدًا (مثل اختلاف اسم دالة بـ Request/Response)، الصق الخطأ وبصلحه فورًا.
 */
class OkHttpDownloaderImpl private constructor(
    private val client: OkHttpClient
) : Downloader() {

    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val builder = OkRequest.Builder().url(url)

        headers.forEach { (name, values) ->
            values.forEach { value -> builder.addHeader(name, value) }
        }

        if (dataToSend != null && dataToSend.isNotEmpty()) {
            builder.method(httpMethod, dataToSend.toRequestBody())
        } else {
            builder.method(httpMethod, null)
        }

        client.newCall(builder.build()).execute().use { httpResponse ->
            val bodyString = httpResponse.body?.string() ?: ""
            val responseHeaders = httpResponse.headers.toMultimap()
            return Response(
                httpResponse.code,
                httpResponse.message,
                responseHeaders,
                bodyString,
                httpResponse.request.url.toString()
            )
        }
    }

    companion object {
        @Volatile private var instance: OkHttpDownloaderImpl? = null

        fun getInstance(): OkHttpDownloaderImpl =
            instance ?: synchronized(this) {
                instance ?: OkHttpDownloaderImpl(OkHttpClient()).also { instance = it }
            }
    }
}
