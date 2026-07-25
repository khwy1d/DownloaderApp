package com.khwyld.downloader.util

object UrlValidator {
    fun isValid(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("http://") || trimmed.startsWith("https://")
    }

    /** يستخرج أول رابط http/https من نص مشارك (مفيد عند استقبال نص مشاركة يحتوي كلامًا زائدًا). */
    fun extractFirstUrl(text: String): String? {
        val regex = Regex("""https?://[^\s]+""")
        return regex.find(text)?.value
    }
}
