package com.khwyld.downloader.util

import android.webkit.MimeTypeMap
import java.net.URLDecoder

/**
 * MVP: استنتاج بسيط لاسم الملف ونوعه من الرابط مباشرة.
 * هذا مكان مؤقت يحل محل LinkResolver الكامل (DirectFileResolver) الموصوف
 * في الوثيقة؛ عند إضافة تحليل الروابط الحقيقي في المرحلة الثانية
 * يُستبدل هذا المنطق بدون التأثير على DownloadRepository لأنه يُستدعى
 * كدالة مساعدة مستقلة.
 */
object FileUtils {

    fun guessFileName(url: String): String {
        return try {
            val decoded = URLDecoder.decode(url, "UTF-8")
            val lastSegment = decoded.substringAfterLast('/').substringBefore('?')
            if (lastSegment.isBlank()) "file_${System.currentTimeMillis()}" else lastSegment
        } catch (e: Exception) {
            "file_${System.currentTimeMillis()}"
        }
    }

    fun guessMimeType(fileName: String): String? {
        val extension = fileName.substringAfterLast('.', "")
        if (extension.isBlank()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    }
}
