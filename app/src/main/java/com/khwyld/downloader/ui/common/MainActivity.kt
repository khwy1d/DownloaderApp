package com.khwyld.downloader.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.khwyld.downloader.DownloaderApplication
import com.khwyld.downloader.util.UrlValidator

/**
 * نقطة الدخول الوحيدة. تعالج ثلاث حالات:
 * 1) فتح عادي من الأيقونة.
 * 2) استقبال مشاركة (ACTION_SEND) من تطبيق آخر يحتوي رابطًا.
 * 3) فتح رابط مباشر (ACTION_VIEW) من تطبيق آخر.
 */
class MainActivity : ComponentActivity() {

    private val sharedUrlHolder = SharedUrlHolder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as DownloaderApplication).container
        handleIncomingIntent(intent)

        setContent {
            DownloaderApp(container = container, pendingSharedUrl = sharedUrlHolder.consume())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
        // في نسخة MVP نكتفي بإعادة إنشاء المحتوى عند استلام نية جديدة والتطبيق مفتوح فعليًا
        recreate()
    }

    private fun handleIncomingIntent(intent: Intent) {
        val url: String? = when (intent.action) {
            Intent.ACTION_SEND -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
                UrlValidator.extractFirstUrl(text)
            }
            Intent.ACTION_VIEW -> intent.data?.toString()
            else -> null
        }
        if (url != null && UrlValidator.isValid(url)) {
            sharedUrlHolder.set(url)
        }
    }
}

/** حامل بسيط لتمرير الرابط المشارك مرة واحدة فقط إلى الواجهة عند فتح التطبيق. */
class SharedUrlHolder {
    private var value: String? = null
    fun set(url: String) { value = url }
    fun consume(): String? {
        val v = value
        value = null
        return v
    }
}
