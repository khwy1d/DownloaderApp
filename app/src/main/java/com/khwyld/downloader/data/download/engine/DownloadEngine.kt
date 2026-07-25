package com.khwyld.downloader.data.download.engine

/**
 * عقد محرك التنزيل. في نسخة MVP يوجد تنفيذ واحد فقط (SystemDownloadEngine).
 * تعدد المحركات و DownloadEngineSelector محجوزان للمرحلة الثانية/الثالثة
 * حسب "قرارات نهائية معتمدة" في الوثيقة، لكن الواجهة موجودة من الآن
 * حتى لا يكسر إضافة محرك جديد لاحقًا عقد الطبقات الأعلى.
 */
interface DownloadEngine {

    /** يبدأ تنزيلًا نظاميًا ويعيد معرف التنزيل الخاص بالمحرك (systemDownloadId). */
    fun enqueue(url: String, fileName: String, mimeType: String?): Long

    /** حالة لحظية لتنزيل معيّن حسب معرف المحرك. */
    fun queryStatus(systemDownloadId: Long): EngineDownloadStatus?

    /** إلغاء تنزيل عبر معرف المحرك. */
    fun cancel(systemDownloadId: Long)
}

data class EngineDownloadStatus(
    val systemDownloadId: Long,
    val isComplete: Boolean,
    val isFailed: Boolean,
    val isRunning: Boolean,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val localUri: String?,
    val reason: String? = null
)
