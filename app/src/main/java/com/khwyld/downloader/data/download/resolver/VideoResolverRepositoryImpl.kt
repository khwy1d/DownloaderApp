package com.khwyld.downloader.data.download.resolver

import com.khwyld.downloader.domain.model.ResolvedLink
import com.khwyld.downloader.domain.model.VideoStreamOption
import com.khwyld.downloader.domain.repository.VideoResolverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList

private val YOUTUBE_URL_REGEX = Regex(
    """(youtube\.com/watch\?v=|youtu\.be/|m\.youtube\.com/watch\?v=|youtube\.com/shorts/)""",
    RegexOption.IGNORE_CASE
)

/**
 * VideoResolver (جزء من LinkResolver الموصوف في الوثيقة).
 * إذا كان الرابط صفحة يوتيوب: يستخدم NewPipeExtractor لاستخراج قائمة جودات
 * قابلة للتنزيل مباشرة (progressive streams: صوت+فيديو بملف واحد فقط،
 * لتفادي الحاجة لدمج ملفين — الدمج محجوز لمرحلة لاحقة إن لزم).
 * أي رابط آخر يمر كما هو (سلوك MVP الأصلي بدون تغيير).
 */
class VideoResolverRepositoryImpl : VideoResolverRepository {

    override suspend fun resolve(url: String): ResolvedLink = withContext(Dispatchers.IO) {
        if (!YOUTUBE_URL_REGEX.containsMatchIn(url)) {
            return@withContext ResolvedLink.DirectFile(url)
        }

        val extractor = ServiceList.YouTube.getStreamExtractor(url)
        extractor.fetchPage()

        val title = extractor.name?.takeIf { it.isNotBlank() } ?: "video"

        val streams = extractor.videoStreams.mapNotNull { stream ->
            val streamUrl = stream.content ?: return@mapNotNull null
            val formatSuffix = stream.format?.suffix ?: "mp4"
            VideoStreamOption(
                label = "${stream.resolution ?: "جودة غير معروفة"} · $formatSuffix",
                url = streamUrl,
                format = formatSuffix,
                resolution = stream.resolution
            )
        }

        if (streams.isEmpty()) {
            // بعض الفيديوهات ما فيها progressive streams (صوت+فيديو بملف واحد)،
            // فقط جودات مفصولة تحتاج دمج غير مدعوم حاليًا.
            throw IllegalStateException(
                "هذا الفيديو ما فيه صيغة مدمجة (صوت+فيديو) مدعومة حاليًا"
            )
        }

        ResolvedLink.VideoPage(
            title = title,
            thumbnailUrl = null, // TODO: إضافته لاحقًا حسب واجهة extractor.thumbnails بنفس نسخة المكتبة
            streams = streams
        )
    }
}
