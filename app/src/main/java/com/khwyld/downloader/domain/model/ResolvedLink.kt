package com.khwyld.downloader.domain.model

/** نتيجة تحليل الرابط قبل التنزيل: ملف مباشر جاهز، أو صفحة فيديو تحتاج اختيار جودة. */
sealed class ResolvedLink {
    data class DirectFile(val url: String) : ResolvedLink()
    data class VideoPage(
        val title: String,
        val thumbnailUrl: String?,
        val streams: List<VideoStreamOption>
    ) : ResolvedLink()
}
