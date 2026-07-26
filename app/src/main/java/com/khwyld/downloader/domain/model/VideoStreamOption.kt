package com.khwyld.downloader.domain.model

/** خيار جودة واحد لفيديو (progressive stream: صوت+فيديو مدمجين بملف واحد). */
data class VideoStreamOption(
    val label: String,
    val url: String,
    val format: String,
    val resolution: String?
)
